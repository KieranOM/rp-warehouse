package rp.warehouse.nxt;

import lejos.nxt.SensorPort;
import rp.warehouse.nxt.communication.Communication;
import rp.warehouse.nxt.interaction.RobotInterfaceController;
import rp.warehouse.nxt.motion.MotionController;

public class Main {
    public static void main(String[] args) {
        MotionController motion = new MotionController(RobotParameters.TEAM_24_BOT, SensorPort.S1, SensorPort.S4);
        Communication communication = new Communication(motion);
        communication.start();

    }
}
