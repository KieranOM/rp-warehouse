package rp.warehouse.pc.localisation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import lejos.geom.Point;
import rp.warehouse.pc.data.robot.utils.RobotLocation;
import rp.warehouse.pc.localisation.implementation.Localiser;

/**
 * Class to handle the different direction assumptions so that the robot can
 * localise from any given position and direction.
 * 
 * @author Kieran
 *
 */
public class LocaliserAssumption {

	private final static Point[] directionPoint = new Point[] { new Point(0, 1), new Point(1, 0), new Point(0, -1),
			new Point(-1, 0) };
	private final WarehouseMap map;
	private final List<Point> blockedPoints = WarehouseMap.getBlockedPoints();
	private static final Logger logger = Logger.getLogger(LocaliserAssumption.class);

	private final static String[] headings = new String[] { "North", "East", "South", "West" };

	private final byte startingDirection;
	private List<Point> possibleLocations = new ArrayList<>();
	private byte heading;

	/**
	 * Create an assumption handler for the given direction.
	 * 
	 * @param direction
	 *            the starting direction of the robot.
	 */
	public LocaliserAssumption(final byte direction, final WarehouseMap map) {
		this.startingDirection = direction;
		this.map = map;
		this.heading = direction;
	}

	/**
	 * Method to add additional blocked locations to the assumption - used to add
	 * existing robot locations to prevent them from being assumed again.
	 * 
	 * @param toBlock
	 *            the list of locations to block.
	 */
	public void addBlockedLocations(List<RobotLocation> toBlock) {
		for (RobotLocation loc : toBlock)
			blockedPoints.add(loc.toPoint());
	}

	/**
	 * Method to initialise the available positions given a set of ranges.
	 * 
	 * @param ranges
	 *            the ranges currently read from the robot to start with.
	 */
	public void start(final Ranges ranges) {
		// Populate the initial possible locations given a set of ranges by rotating the
		// ranges so that they are north-based using the heading assumption and the
		// warehouse map.
		try {
			possibleLocations = map.getPoints(Ranges.rotate(ranges, heading));
		} catch (NoIdeaException e) {
			logger.info("(" + startingDirection + "): No more directions");
		}
	}

	/**
	 * Update the assumption handler with the direction of which the robot just
	 * moved in (relative to the robot), as well as the ranges discovered after
	 * moving in that direction.
	 * 
	 * @param direction
	 *            the direction just moved in, relative to the robot.
	 * @param ranges
	 *            the ranges discovered after moving.
	 */
	public void update(final byte direction, final Ranges ranges) {
		// Only update if there are locations to process
		if (possibleLocations.size() > 0) {
			// Update the current heading using modulo.
			heading = (byte) ((heading + direction) % 4);
			logger.info("(" + startingDirection + ") Facing: " + headings[heading]);
			// Get the respective change in direction, relative to the initial assumption
			// and the heading.
			final Point move = directionPoint[heading];
			try {
				// Get the possible points of which the robot could be in given the current
				// ranges, rotated by the current heading to use north-based ranges.
				List<Point> possiblePoints = map.getPoints(Ranges.rotate(ranges, heading));
				// Then filter these positions.
				possibleLocations = filterPositions(possibleLocations, possiblePoints, move);
			} catch (NoIdeaException e) {
				logger.info("(" + startingDirection + "): No more directions");
			}
		}
	}

	/**
	 * Method to determine whether this assumption is complete or not.
	 * 
	 * @return whether this assumption has completed its localisation or not.
	 */
	public boolean isComplete() {
		return possibleLocations.size() == 1;
	}

	/**
	 * Method to get the number of points of which the robot could possibly be in.
	 * 
	 * @return the number of points that the robot could be in.
	 */
	public int getNumberOfPoints() {
		return possibleLocations.size();
	}

	/**
	 * The point in the first position of the possible locations array. Will give
	 * the location of the robot after localisation has completed.
	 * 
	 * @return the point of the robot.
	 */
	public Point getPoint() {
		return possibleLocations.get(0);
	}

	/**
	 * The heading following this assumption.
	 * 
	 * @return the robot's heading.
	 */
	public byte getHeading() {
		return heading;
	}

	/**
	 * Method to get a stream of the RobotLocations that are currently possible
	 * locations for this assumption handler.
	 * 
	 * @return the stream of currently possible locations for this assumption
	 *         handler.
	 */
	public Stream<RobotLocation> stream() {
		return possibleLocations.stream().map(l -> new RobotLocation(l, Localiser.directionProtocol[heading]));
	}

	/**
	 * Method to filter initial positions given new positions and a movement. Used
	 * to narrow down the possibility of location.
	 *
	 * @param initial
	 *            The initial possible positions recorded.
	 * @param next
	 *            The new possible positions recorded.
	 * @param change
	 *            The change in position from <b>initial</b> to <b>next</b>.
	 * @return The new list of possible positions of the robot.
	 */
	private List<Point> filterPositions(final List<Point> initial, final List<Point> next, final Point change) {
		logger.info("-- (" + startingDirection + ") Filtering");
		logger.info("(" + startingDirection + ") Initial ranges: " + initial);
		logger.info("(" + startingDirection + ") Next ranges: " + next);
		// Filter the next list by removing all points that couldn't exist given the
		// previous points and the change in position.
		next.removeIf(p -> !initial.contains(p.subtract(change)) || blockedPoints.contains(p));
		logger.info("(" + startingDirection + ") Filtered ranges: " + next);
		return next;
	}

}
