package ca.mcgill.ecse211.project;

/**
 * Class that tracks position of the robot
 * 
 * @author Patrick Ghazal
 *
 */
public class Odometer extends OdometerData implements Runnable {

	private static Odometer odo = null; // Returned as singleton

	// Motors and related variables
	private int leftMotorTachoCount;
	private int rightMotorTachoCount;
	private int prevlMTC = 0, prevrMTC = 0; // prevlMTC = previousLeftMotorTachoCount, same for right

	private static final long ODOMETER_PERIOD = 25; // odometer update period in ms

	private static int checks = 0;

	/**
	 * This is the default constructor of this class. It cannot be accessed
	 * externally.
	 * 
	 * @throws OdometerExceptions
	 */
	private Odometer() throws OdometerExceptions {

		// Reset the values of x, y and z to 0
		this.setXYT(0, 0, 0);

		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;

	}

	/**
	 * This method is meant to ensure only one instance of the odometer is used
	 * throughout the code.
	 * 
	 * @return new or existing Odometer Object
	 * @throws OdometerExceptions
	 */
	public synchronized static Odometer getOrCreateOdometer() throws OdometerExceptions {
		if (odo != null) { // Return existing object
			return odo;
		} else { // create object and return it
			odo = new Odometer();
			return odo;
		}
	}

	/**
	 * This class is meant to return the existing Odometer Object. It is meant to be
	 * used only if an odometer object has been created
	 * 
	 * @return found odometer instance
	 * @throws OdometerExceptions
	 */
	public synchronized static Odometer getOdometer() throws OdometerExceptions {

		if (odo == null) {
			throw new OdometerExceptions("No previous Odometer exits.");

		}
		return odo;
	}

	/**
	 * This method is where the logic for the odometer will run. Use the methods
	 * provided from the OdometerData class to implement the odometer.
	 */
	public void run() {
		long updateStart, updateEnd;
		while (true) {

			updateStart = System.currentTimeMillis();

			leftMotorTachoCount = Navigation.leftMotor.getTachoCount();
			rightMotorTachoCount = Navigation.rightMotor.getTachoCount();

			/*
			 * if the tachometer values don't change, add 1 to checks, for a max of 100 (to
			 * save space). checks > 20 means the tachometer values aren't changing for 20
			 * iterations or more, and therefore that the robot is stopped. if a different
			 * value is found, clear checks
			 */
			if (leftMotorTachoCount == prevlMTC && rightMotorTachoCount == prevrMTC) {
				if (checks < 100) {
					checks++;
				}
			} else {
				checks = 0;
			}

			double deltaX = 0, deltaY = 0, deltaTheta = 0;
			if (!hasTooManyTrues()) {
				/*
				 * All the following calculations based on the slides.
				 */
				double d1 = (Robot.WHEEL_RAD * Math.PI * (leftMotorTachoCount - prevlMTC)) / 180,
						d2 = (Robot.WHEEL_RAD * Math.PI * (rightMotorTachoCount - prevrMTC)) / 180;
				deltaTheta = (d1 - d2) / Robot.TRACK;
				double newHeading = getXYT()[2] + (deltaTheta * (180.0 / Math.PI));
				double displacement = (d1 + d2) / 2.00;
				deltaX = displacement * Math.sin(newHeading * (Math.PI / 180.0));
				deltaY = displacement * Math.cos(newHeading * (Math.PI / 180.0));
			}
			odo.update(deltaX, deltaY, deltaTheta * (180 / Math.PI));

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done
				}
			}
			prevlMTC = leftMotorTachoCount;
			prevrMTC = rightMotorTachoCount;

		}
	}

	/**
	 * Determines whether the tacho counts are changing.
	 * 
	 * @return true if a single value has been received more than 20 times in a row
	 */
	private boolean hasTooManyTrues() {
		return checks > 20;
	}

}
