package server;

import javax.swing.SwingUtilities;

/**
 *
 * Main class for server.
 * Starts the following:
 * 1. Server -> ClientManagement & ClientWarehouse
 * 2. ServerGUI
 *
 * @author liching
 *
 */
public class StartServer {

    public static void main(String[] args) {

        // Default port number of server
        Integer port = 4321;

        if (args.length == 1){
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("\"" + args[0] + "\"" + " is not a valid a number.");
                return;
            }

            if (port > 0 & port <= 65535){
                System.out.println("Server is launched with port number: " + port);
            } else {
                System.out.println("Port number provided must be between 1 and 65,535.");
                return;
            }
        } else if (args.length > 0) {
            System.out.println("Only one or no arguments can be provided.");
            return;
        }

        Integer finalPort = port;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                ServerGUI serverGUI = new ServerGUI();
                Server server = new Server(serverGUI, finalPort);
                server.start();
            }
        });

    }
}
