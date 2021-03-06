package rp.warehouse.nxt.localisation;

import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.addon.OpticalDistanceSensor;
import lejos.util.Delay;
import rp.warehouse.nxt.motion.MotionController;

/**
 * Class to get ranges for localisation from the robot
 * @author dxj786
 */
public class Ranges {
	private final MotionController motion;
	private final OpticalDistanceSensor sensor;

	public Ranges(MotionController motion) {
		this.motion = motion;
		this.sensor = new OpticalDistanceSensor(SensorPort.S2);
	}

	public float[] getRanges() {
		float[] ranges = new float[4];
		for (int i = 0; i < 4; i++) {
			float totalRanges = 0;
			for (int j = 0; j < 5; j++) {
				Delay.msDelay(20);
				totalRanges += sensor.getRange();
			}
			ranges[i] = totalRanges / 5;
			motion.rotate();
		}
		return ranges;
	}

}
