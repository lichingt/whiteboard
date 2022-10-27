package manager;

import client.WhiteboardApplication;
import client.WhiteboardGUI;

import javax.swing.SwingUtilities;
import java.util.Random;

/**
 *
 * Main class for client who is a manager.
 * Starts the following:
 * 1. WhiteboardApplication -> ConnectionEngine
 * 2. WhiteboardGUI -> Canvas
 *
 * @author liching
 *
 */
public class CreateWhiteBoard {

    public static void main(String[] args){

        String userAccess = "manager";
        String serverIPAddress;
        String serverPort;
        String username;
        boolean triggerConnectButton;

        /*
         * Default values for serverIPAddress, serverPort and username, if no arguments were provided upon launch
         * Values can still be updated prior to establishing connection via Connect button on GUI
         */
        if (args.length == 0) {

            serverIPAddress = "localhost";
            serverPort = "4321";

            // Default random generated username of 8 characters
            Random r = new Random();
            username = "";
            String alphabet = "qwertyuiopasdfghjklzxcvbnm123456789";
            for (int i = 0; i < 8; i++){
                username += alphabet.charAt(r.nextInt(alphabet.length()));
            }

            triggerConnectButton = false;

        /*
         * Values for serverIPAddress, serverPort and username set via arguments upon launch
         * Connection will be initiated upon launch
         */
        } else if (args.length == 3) {
            serverIPAddress = args[0];
            serverPort = args[1];
            username = args[2];
            triggerConnectButton = true;

        } else {
            System.out.println("Error: Invalid number of arguments provided. " +
                    "Launch with \"java CreateWhiteBoard\" or " +
                    "\"java CreateWhiteBoard <serverIPAddress> <serverPort> <username>\"");
            return;
        }

        String finalUsername = username;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                WhiteboardGUI whiteboardGUI = new WhiteboardGUI(userAccess, serverIPAddress, serverPort, finalUsername);
                WhiteboardApplication whiteboardApplication = new WhiteboardApplication(userAccess, whiteboardGUI, triggerConnectButton);
                whiteboardApplication.start();
            }
        });

    }
}
