package ca.mcgill.ecse211.project;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

/**
 * Class that utilizes the UltraSonic Sensor to point the robot in the right
 * direction when it is in a corner position next to two walls. Utilizes falling
 * edge and rising edge for proper localization
 *
 * @author Volen Mihaylov
 * @author Patrick Ghazal
 * @author Bryan Jay
 */
public class USLocalizer {

	// vehicle constants
	private double deltaTheta;

	private Odometer odometer;
	private float[] usData;
	private SampleProvider usDistance;
	private double[] startingCoordinates = new double[2];
	int corner = 0;

	// Create a navigation
	private Navigation navigation;

	/**
	 * Constructor to initialize attributes and objects.
	 * 
	 * @param usDistance
	 *            SampleProvider instance
	 * @param corner
	 *            starting corner
	 */
	public USLocalizer(SampleProvider usDistance) {
		this.odometer = Controller.getOdometerInstance();
		this.usDistance = usDistance;
		this.usData = new float[this.usDistance.sampleSize()];
		navigation = Controller.getNavigationInstance();

	}

	/**
	 * Initializes starting coordinates according to inputed corner. corner = 0 is
	 * bottom left corner = 1 is bottom right corner = 2 is top right corner = 3 is
	 * top left
	 * 
	 * @param corner
	 *            starting corner
	 */
	public void setStartingCoordinates(int corner) {
		this.corner = corner;
		switch (corner) {
		case 0:
			this.startingCoordinates[0] = 0.25 * Robot.TILESIZE;
			this.startingCoordinates[1] = 0.25 * Robot.TILESIZE;
			break;
		case 1:
			this.startingCoordinates[0] = 11.75 * Robot.TILESIZE;
			this.startingCoordinates[1] = 0.25 * Robot.TILESIZE;
			break;
		case 2:
			this.startingCoordinates[0] = 11.75 * Robot.TILESIZE;
			this.startingCoordinates[1] = 11.75 * Robot.TILESIZE;
			break;
		case 3:
			this.startingCoordinates[0] = 0.25 * Robot.TILESIZE;
			this.startingCoordinates[1] = 11.75 * Robot.TILESIZE;
			break;
		}
	}

	/**
	 * A method to determine which localization method to use
	 * 
	 */
	public void localize(int corner) {
		// if we are facing a wall we use our rising edge localization
		setStartingCoordinates(corner);
		if (fetchUS() < 40) {
			localizeRisingEdge();
		} else {
			localizeFallingEdge();
		}
	}

	/**
	 * A method to localize position using the rising edge routine.
	 */
	public void localizeRisingEdge() {

		double angleA, angleB, turningAngle;

		// Rotate to the wall
		while (fetchUS() > Robot.D) {
			navigation.rotateCounterClockWise();
		}
		// Rotate until it sees the open space
		while (fetchUS() < Robot.D + Robot.K) {
			navigation.rotateCounterClockWise();
		}
		navigation.stopRobot();
		;
		Sound.buzz();
		// record angle
		angleA = odometer.getXYT()[2];

		// rotate the other way all the way until it sees the wall
		while (fetchUS() > Robot.D) {
			navigation.rotateClockWise();
		}

		// rotate until it sees open space
		while (fetchUS() < Robot.D + Robot.K) {
			navigation.rotateClockWise();
		}
		navigation.stopRobot();
		;
		Sound.buzz();
		angleB = odometer.getXYT()[2];

		// calculate angle of rotation
		if (angleA < angleB) {
			deltaTheta = 45 - (angleA + angleB) / 2 + 180;
		} else if (angleA > angleB) {
			deltaTheta = 225 - (angleA + angleB) / 2 + 180;
		}

		turningAngle = deltaTheta + odometer.getXYT()[2];

		// rotate robot to the theta = 0.0 using turning angle and we account for small
		// error
		navigation.rotateByAngle(turningAngle - 45, -1, 1);

		// set theta to coordinate starting corner
		cornerSet();

	}

	/**
	 * A method to localize position using the falling edge routine.
	 */
	public void localizeFallingEdge() {

		double angleA, angleB, turningAngle;

		// Rotate to open space
		while (fetchUS() < Robot.D + Robot.K) {
			navigation.rotateCounterClockWise();
		}
		// Rotate to the first wall
		while (fetchUS() > Robot.D) {
			navigation.rotateCounterClockWise();
		}
		navigation.stopRobot();
		;

		Sound.buzz();
		// record angle
		angleA = odometer.getXYT()[2];

		// rotate out of the wall range
		while (fetchUS() < Robot.D + Robot.K) {
			navigation.rotateClockWise();
		}

		// rotate to the second wall
		while (fetchUS() > Robot.D) {
			navigation.rotateClockWise();
		}
		navigation.stopRobot();
		;
		Sound.buzz();

		angleB = odometer.getXYT()[2];

		// calculate angle of rotation
		if (angleA < angleB) {
			deltaTheta = 45 - (angleA + angleB) / 2;

		} else if (angleA > angleB) {
			deltaTheta = 225 - (angleA + angleB) / 2;
		}

		turningAngle = deltaTheta + odometer.getXYT()[2];

		// rotate robot to the theta = 0.0 and we account for small error
		navigation.rotateByAngle(turningAngle + 2 - 45, -1, 1);

		// set odometer to theta to starting corner
		cornerSet();

	}

	/**
	 * Sets the starting coordinates after the localization routine.
	 */
	public void cornerSet() {
		switch (corner) {
		case 0:
			odometer.setXYT(startingCoordinates[0], startingCoordinates[1], 45.0);
			break;
		case 1:
			odometer.setXYT(startingCoordinates[0], startingCoordinates[1], 315.0);
			break;
		case 2:
			odometer.setXYT(startingCoordinates[0], startingCoordinates[1], 235.0);
			break;
		case 3:
			odometer.setXYT(startingCoordinates[0], startingCoordinates[1], 135.0);
			break;
		}
	}

	/**
	 * A method to get the distance from our sensor
	 * 
	 * @return distance distance measured by the sensor
	 */
	public int fetchUS() {
		usDistance.fetchSample(usData, 0);
		return (int) (usData[0] * 100);
	}

}
