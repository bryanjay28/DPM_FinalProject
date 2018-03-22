package ca.mcgill.ecse211.project;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

/**
 * Class that allows odometer Correction by using tile lines to calculate offset from 
 * theoretical position.
 * @author volen
 *
 */
public class LightLocalizer {

	// vehicle constants
	public static int ROTATION_SPEED = 100;
	private double SENSOR_LENGTH = 11.9;

	private Odometer odometer;
	public Navigation navigation;
	// Instantiate the EV3 Color Sensor
	private static final EV3ColorSensor lightSensor = new EV3ColorSensor(LocalEV3.get().getPort("S1"));
	private float sample;
	private float prevSample;
	private float deltaSample;

	private SensorMode idColour;

	double[] lineData;
/**
 * LightLocalizet constructor
 * @param odometer Odometer object to keep track of position for coordinate related movements
 * @param nav Navigation object to be able to induce movement into the robot
 */
	public LightLocalizer(Odometer odometer, Navigation nav) {

		this.odometer = odometer;
		prevSample = 0;

		idColour = lightSensor.getRedMode(); // set the sensor light to red
		lineData = new double[4];
		this.navigation = nav;
	}

	/**
	 * 
	 * This method localizes the robot using the light sensor to precisely move to
	 * the right location
	 * 
	 * @param finalX Final X coordinate that the robot will set
	 * @param finalY Final X coordinate that the robot will set
	 * @param finalTheta Final theta coordinate that the robot will set
	 */
	public void localize(double finalX, double finalY, double finalTheta) {

		int index = 0;
		navigation.setSpeed(ROTATION_SPEED);

		// ensure that we are close to origin before rotating
		moveToIntersection();

		// Scan all four lines and record our angle
		while (index < 4) {

			navigation.rotateClockWise();

			sample = fetchSample();

			if (sample < 0.38) {
				lineData[index] = odometer.getXYT()[2];
				Sound.beepSequenceUp();
				index++;
			}
		}

		navigation.stop();

		double deltax, deltay, thetax, thetay;

		// calculate our location from 0 using the calculated angles
		thetay = lineData[3] - lineData[1];
		thetax = lineData[2] - lineData[0];

		deltax = -1 * SENSOR_LENGTH * Math.cos(Math.toRadians(thetay / 2));
		deltay = -1 * SENSOR_LENGTH * Math.cos(Math.toRadians(thetax / 2));

		// travel to one-one to correct position
		odometer.setXYT(deltax, deltay, odometer.getXYT()[2]);
		if (Robot.calculateDistance(odometer.getXYT()[2], odometer.getXYT()[1], 0, 0) > 1.5) {
			this.navigation.travelTo(0, 0, false, null);
		}

		this.navigation.turnTo(0.0);

		navigation.setSpeed(ROTATION_SPEED / 2);

		// if we are not facing 0.0 then turn ourselves so that we are
		if (odometer.getXYT()[2] <= 357 && odometer.getXYT()[2] >= 3) {
			Sound.beep();
			navigation.rotateByAngle(-odometer.getXYT()[2] + 9, 1, -1);
		}

		navigation.stop();
		odometer.setXYT(finalX * Robot.TILESIZE, finalY * Robot.TILESIZE, finalTheta - 5);
		lightSensor.close();

	}

	/**
	 * This method gets the color value of the light sensor
	 * 
	 */
	private float fetchSample() {
		float[] colorValue = new float[idColour.sampleSize()];
		idColour.fetchSample(colorValue, 0);
		return colorValue[0];
	}

	/**
	 * Move to the first intersection on the North East of the initial square
	 */
	public void moveToIntersection() {

		navigation.turnTo(-1 * Math.PI / 4);

		navigation.setSpeed(ROTATION_SPEED);

		// get sample
		sample = fetchSample();
		
		navigation.forward();

		// move forward past the origin until light sensor sees the line
		while ((sample = fetchSample()) > 0.38);
		navigation.stop();
		Sound.beep();

		
		int distance = 10; //generic value
		// TODO: change distance to distance between light sensor and wheelbase center
		
		// Move backwards so our origin is close to origin
		navigation.rotateByDistance(-1 * distance, 1, 1);

	}
}
