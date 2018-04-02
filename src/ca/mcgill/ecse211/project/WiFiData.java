package ca.mcgill.ecse211.project;

import java.util.Map;

import ca.mcgill.ecse211.WiFiClient.WifiConnection;
import lejos.hardware.Button;

/**
 * Example class using WifiConnection to communicate with a server and receive
 * data concerning the competition such as the starting corner the robot is
 * placed in.
 * 
 * Keep in mind that this class is an **example** of how to use the WiFi code;
 * you must use the WifiConnection class yourself in your own code as
 * appropriate. In this example, we simply show how to get and process different
 * types of data.
 * 
 * There are two variables you **MUST** set manually before trying to use this
 * code.
 * 
 * 1. SERVER_IP: The IP address of the computer running the server application.
 * This will be your own laptop, until the beta beta demo or competition where
 * this is the TA or professor's laptop. In that case, set the IP to
 * 192.168.2.3.
 * 
 * 2. TEAM_NUMBER: your project team number
 * 
 * Note: We System.out.println() instead of LCD printing so that full debug
 * output (e.g. the very long string containing the transmission) can be read on
 * the screen OR a remote console such as the EV3Control program via Bluetooth
 * or WiFi. You can disable printing from the WiFi code via
 * ENABLE_DEBUG_WIFI_PRINT (below).
 * 
 * @author Michael Smith, Tharsan Ponnampalam
 *
 */
public class WiFiData {

	// ** Set these as appropriate for your team and current situation **
	private static final String SERVER_IP = "192.168.2.3";
	private static final int TEAM_NUMBER = 11;

	// Enable/disable printing of debug info from the WiFi class
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;

	// Received data
	// For the beta demo
	public static int greenTeam, greenCorner, redLLX, redLLY, redURX, redURY, greenLLX, greenLLY, greenURX, greenURY;
	public static int tnLLX, tnLLY, tnURX, tnURY, brLLX, brLLY, brURX, brURY;

	// Other data for the final project
	public static int og, or, redTeam, redCorner, srLLX, srLLY, srURX, srURY, sgLLX, sgLLY, sgURX, sgURY;

	// End of received data

	@SuppressWarnings("rawtypes")
	public static void processData() {

		System.out.println("Running..");

		// Initialize WifiConnection class
		WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);

		// Connect to server and get the data, catching any errors that might occur
		try {
			Map data = conn.getData();

			greenTeam = ((Long) data.get("GreenTeam")).intValue();
			greenCorner = ((Long) data.get("GreenCorner")).intValue();
			redTeam = ((Long) data.get("RedTeam")).intValue();
			redCorner = ((Long) data.get("RedCorner")).intValue();
			redLLX = ((Long) data.get("Red_LL_x")).intValue();
			redLLY = ((Long) data.get("Red_LL_y")).intValue();
			redURX = ((Long) data.get("Red_UR_x")).intValue();
			redURY = ((Long) data.get("Red_UR_y")).intValue();
			greenLLX = ((Long) data.get("Green_LL_x")).intValue();
			greenLLY = ((Long) data.get("Green_LL_y")).intValue();
			greenURX = ((Long) data.get("Green_UR_x")).intValue();
			greenURY = ((Long) data.get("Green_UR_y")).intValue();
			tnLLX = ((Long) data.get("TN_LL_x")).intValue();
			tnLLY = ((Long) data.get("TN_LL_y")).intValue();
			tnURX = ((Long) data.get("TN_UR_x")).intValue();
			tnURY = ((Long) data.get("TN_UR_y")).intValue();
			brLLX = ((Long) data.get("BR_LL_x")).intValue();
			brLLY = ((Long) data.get("BR_LL_y")).intValue();
			brURX = ((Long) data.get("BR_UR_x")).intValue();
			brURY = ((Long) data.get("BR_UR_y")).intValue();
			

			if (!Controller.betaDemo) {
				// ----------------------------------------------
				// Values to be used after the beta demo
				og = ((Long) data.get("OG")).intValue();
				or = ((Long) data.get("OR")).intValue();
				srLLX = ((Long) data.get("SR_LL_x")).intValue();
				srLLY = ((Long) data.get("SR_LL_y")).intValue();
				srURX = ((Long) data.get("SR_UR_x")).intValue();
				srURY = ((Long) data.get("SR_UR_y")).intValue();
				sgLLX = ((Long) data.get("SG_LL_x")).intValue();
				sgLLY = ((Long) data.get("SG_LL_y")).intValue();
				sgURX = ((Long) data.get("SG_UR_x")).intValue();
				sgURY = ((Long) data.get("SG_UR_y")).intValue();
				
				if (TEAM_NUMBER == redTeam) {
					Controller.setCurrentTeam("red");
//					Navigation.setCurrentZone("red");
//					Navigation.setStartingCorner(redCorner);
				} else if (TEAM_NUMBER == greenTeam) {
					Controller.setCurrentTeam("green");
//					Navigation.setCurrentZone("green");
//					Navigation.setStartingCorner(greenCorner);
				}
				
				// ----------------------------------------------
			} else {
				Controller.setCurrentTeam("green");
//				Navigation.setCurrentZone("green");
//				Navigation.setStartingCorner(greenCorner);
			}

			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		// Wait until user decides to end program
		Button.waitForAnyPress();
	}
}
