package ca.mcgill.ecse211.project;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

/**
 * Class that allows odometer Correction by using tile lines to calculate offset
 * from theoretical position.
 * 
 * @author Volen Mihaylov
 *
 */
public class LightLocalizer {

	// vehicle constants

	private Odometer odometer;
	public Navigation navigation;
	// Instantiate the EV3 Color Sensor
	private static final EV3ColorSensor lightSensorLeft = new EV3ColorSensor(LocalEV3.get().getPort("S1"));
	private static final EV3ColorSensor lightSensorRight = new EV3ColorSensor(LocalEV3.get().getPort("S4"));
	private float sampleLeft;
	private float sampleRight;

	private SensorMode idColourLeft;
	private SensorMode idColourRight;

	double[] lineData;

	/**
	 * LightLocalizet constructor
	 * 
	 * @param odometer
	 *            Odometer object to keep track of position for coordinate related
	 *            movements
	 * @param nav
	 *            Navigation object to be able to induce movement into the robot
	 */
	public LightLocalizer(Odometer odometer, Navigation nav) {

		this.odometer = odometer;
		idColourLeft = lightSensorLeft.getRedMode(); // set the sensor light to red
		this.navigation = nav;
		idColourRight = lightSensorRight.getRedMode(); // set the sensor light to red
		lineData = new double[4];
	}

	public void fullLocalize(int type) {
		switch (type) {
		case 1: { // right then up
			Navigation.rotateByAngle(90, 1, -1);
			localizeX();
			Navigation.rotateByAngle(90, -1, 1);
			localizeY();
			break;
		}
		case 2: { // left then up
			Navigation.rotateByAngle(90, -1, 1);
			localizeX(); // behavior after this assumes that the robot is still facing "left" (270)
			Navigation.rotateByAngle(90, 1, -1);
			localizeY();
			break;
		}
		default: {
			break;
		}
		}

	}

	public void localizeX() {
		Navigation.setSpeed(Robot.LOCALIZATION_SPEED);
		boolean leftCheck = true;
		boolean rightCheck = true;
		double oriCoord = odometer.getXYT()[0];
		navigation.forward();
		leftCheck = true;
		rightCheck = true;
		while (leftCheck || rightCheck) {
			sampleLeft = fetchSampleLeft();
			sampleRight = fetchSampleRight();
			if (leftCheck && sampleLeft < 0.28) {
				lineData[0] = odometer.getXYT()[0];
				Sound.beepSequenceUp();
				leftCheck = false;
			}
			if (rightCheck && sampleRight < 0.28) {
				lineData[1] = odometer.getXYT()[0];
				Sound.beepSequenceUp();
				rightCheck = false;
			}
			if (Math.abs(oriCoord - odometer.getXYT()[0]) > Robot.LSTOWHEEL * 2.5) {
				Sound.beepSequence();
				return;
			}
		}
		navigation.stopRobot();
		double deltaOdo = lineData[0] - lineData[1];
		navigation.turnTo(odometer.getXYT()[2] + Math.asin(deltaOdo / Robot.LSTOLS) * 180 / Math.PI);
		odometer.setTheta(90); // needs to be changed to accept -90 (i.e. 270) when type 2 fullLocalize
		// is that what you said you hardcoded?
		odometer.setX(expectedTile(odometer.getXYT()[0]) + deltaOdo / 2 + Robot.LSTOWHEEL);

	}

	public void localizeY() {
		navigation.setSpeed(Robot.LOCALIZATION_SPEED);
		navigation.forward();
		boolean leftCheck = true;
		boolean rightCheck = true;
		double oriCoord = odometer.getXYT()[1];
		while (leftCheck || rightCheck) {
			sampleLeft = fetchSampleLeft();
			sampleRight = fetchSampleRight();
			if (leftCheck && sampleLeft < 0.28) {
				lineData[0] = odometer.getXYT()[1];
				Sound.beepSequenceUp();
				leftCheck = false;
			}
			if (rightCheck && sampleRight < 0.28) {
				lineData[1] = odometer.getXYT()[1];
				Sound.beepSequenceUp();
				rightCheck = false;
			}
			if (Math.abs(oriCoord - odometer.getXYT()[1]) > Robot.LSTOWHEEL * 2.5) {
				navigation.stopRobot();
				Sound.beepSequence();
				return;
			}
		}
		navigation.stopRobot();
		double deltaOdo = lineData[0] - lineData[1];
		navigation.turnTo(odometer.getXYT()[2] + Math.asin(deltaOdo / Robot.LSTOLS) * 180 / Math.PI);
		odometer.setTheta(0);
		odometer.setY(expectedTile(odometer.getXYT()[1]) + deltaOdo / 2 + Robot.LSTOWHEEL);

	}

	private int expectedTile(double coordinate) {
		return (int) Math.round(coordinate);
	}

	/**
	 * This method gets the color value of the left light sensor
	 * 
	 */
	private float fetchSampleLeft() {
		float[] colorValue = new float[idColourLeft.sampleSize()];
		idColourLeft.fetchSample(colorValue, 0);
		return colorValue[0];
	}

	/**
	 * This method gets the color value of the right light sensor
	 * 
	 */
	private float fetchSampleRight() {
		float[] colorValue = new float[idColourRight.sampleSize()];
		idColourRight.fetchSample(colorValue, 0);
		return colorValue[0];
	}
}
