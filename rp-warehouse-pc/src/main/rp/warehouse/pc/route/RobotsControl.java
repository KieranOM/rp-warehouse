package rp.warehouse.pc.route;

import org.apache.log4j.Logger;
import rp.warehouse.pc.assignment.Auctioner;
import rp.warehouse.pc.communication.Communication;
import rp.warehouse.pc.data.Task;
import rp.warehouse.pc.data.robot.Robot;
import rp.warehouse.pc.data.robot.utils.RobotLocation;
import rp.warehouse.pc.input.Job;
import rp.warehouse.pc.localisation.NoIdeaException;
import rp.warehouse.pc.localisation.implementation.Localiser;
import rp.warehouse.pc.management.LoadingView;
import rp.warehouse.pc.management.LocalisationView;
import rp.warehouse.pc.management.MainView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Used to link different part of the system together
 * <p>
 * Job Assignment   - Can use this class to initially create Array of Robots
 * <p>
 * Route planning   - Can use to access robot classes to plan
 * <p>
 * Warehouse MI     - Can use this class to get Robot classes to get all the
 * required data
 *
 * @author roman
 */
public class RobotsControl {
    private static final ArrayList<Robot> robots = new ArrayList<>();

    //Will crash as only has one element 
    private static final String[] robotNames = new String[]{"ExpressBoi", "Meme Machine", "Orphan"};
    private static final String[] robotIDs = new String[]{"0016531AFBE1", "0016531501CA", "0016531303E0"};
    private static final RobotLocation[] robotLocations = new RobotLocation[]{new RobotLocation(0, 0, 3),
            new RobotLocation(11, 7, 3), new RobotLocation(0, 7, 3)};
    private static final Logger logger = Logger.getLogger(RobotsControl.class);
    private static List<Queue<Task>> listOfItems;

    /**
     * <p>
     * If there are 3 robots, there should be 3 queues in the ArrayList and the size
     * of the Array should be 3
     * <p>
     * If there is only 1, then there should be just one queue in the ArrayList and
     * the size of the array should be 1
     */
    public static void run(List<Job> jobs) {


        logger.debug("Starting Robot Creation");

        ExecutorService pool = Executors.newFixedThreadPool(robotNames.length * 2);

        List<RobotLocation> locations = new ArrayList<>();

        List<Communication> communications = new ArrayList<>();
        for (int i = 0; i < robotNames.length; i++) {
            try {
                Communication communication = new Communication(robotIDs[i], robotNames[i]);
                communications.add(communication);
                pool.execute(communication);

                LoadingView.finishedLoading();

                Localiser localiser = new Localiser(communication, locations);
                LocalisationView localisationView = new LocalisationView(localiser, robotNames[i]);

                RobotLocation location = localiser.getPosition();

                locations.add(location);

                localisationView.finishedLocalising();

                synchronized (localiser) {
                    localiser.wait();
                }

                localisationView.setVisible(false);

            } catch (NoIdeaException e) {
                logger.error("Could not localise " + robotNames[i]);
            } catch (InterruptedException e) {
                logger.fatal("Interrupted somehow while waiting for gui");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Auctioner auctioner = new Auctioner(jobs, locations);

        listOfItems = auctioner.assign();

        RoutePlan.setRobots(robots);


        int i = 0;
        // Create robots
        for (Queue<Task> items : listOfItems) {
            logger.trace("Robot " + i + " is being created");

            try {

                Robot newRobot = new Robot(robotIDs[i], robotNames[i], items, communications.get(i), locations.get(i));
                robots.add(newRobot);

                communications.get(i).setRobot(newRobot);

                logger.debug("Robot " + robotNames[i] + " created");

            } catch (IOException e) {
                logger.error("Could not connect to " + robotNames[i]);
            }
            i++;
        }


        // Runs Robot threads
        for (Robot robot : robots) {
            //robot.localiseRobot();
            pool.execute(robot);
        }
        logger.debug("Array of Robots has been created with " + robots.size() + " robots");

        LoadingView.finishedLoading();
        new MainView(robots);

        // Shut down the pool to prevent new threads being created, and allow the program to end
        pool.shutdown();
    }
}
//
//  `\_('_')_/`
//