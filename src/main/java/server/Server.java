package server;

import message.Chat;
import message.Connection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * Server to handle and maintain connections with multiple clients.
 * Uses ClientManagement and MessageWarehouse.
 *
 * @author liching
 *
 */
public class Server extends Thread {

    // Identifies the user number connected
    private static int counter = 0;

    private int port;

    private ClientManagement clientManagement;

    private MessageWarehouse messageWarehouse;

    private ServerGUI serverGUI;

    public Server(ServerGUI serverGUI, Integer port) {

        this.serverGUI = serverGUI;
        this.port = port;
        clientManagement = new ClientManagement(serverGUI);
        messageWarehouse = new MessageWarehouse(clientManagement);
        messageWarehouse.start();

    }

    @Override
    public void run(){

        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try(ServerSocket server = factory.createServerSocket(port)){
            this.serverGUI.logMessage("(Information) Server thread running (port: " + port + "). Waiting for client connection..");

            // Wait for connections.
            while(true){
                Socket client = server.accept();
                counter++;
                System.out.println("Connection request "+counter+".");

                // Start a new thread for a connection
                Thread t = new Thread(() -> serveClient(client));
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serveClient(Socket client) {

        try(Socket clientSocket = client)
        {

            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            String inputData = input.readUTF();
            serverGUI.logMessage("(Received) " + inputData);

            JSONParser parser = new JSONParser();
            JSONObject msgReceived = (JSONObject) parser.parse(inputData);
            String category = (String) msgReceived.get("category");
            Connection connection = null;

            // First message from client must be a connection request
            if (category.equals("connect")){

                connection = new Connection(msgReceived);
                String username = connection.username;
                String userAccess = connection.userAccess;

                // Process connection request for manager
                if (userAccess.equals("manager")){

                    // Only one manager at one time
                    if (clientManagement.getClientList().size() == 0) {
                        clientManagement.setManager(username, clientSocket);
                        Chat createdWhiteboard = new Chat("Server", "bot",
                                "chat", "Whiteboard created!", "all");
                        clientManagement.contactManager(createdWhiteboard);
                        clientManagement.broadcastMessage("Server", clientManagement.getUserList());
                    } else {
                        Chat deny = new Chat("Server", "bot",
                                "deny", "Only one manager allowed.", username);
                        output.writeUTF(deny.toString());
                        output.flush();
                        serverGUI.logMessage("(Sent) " + deny);
                        clientSocket.close();

                    }

                // Process connection request for users
                } else {

                    // Deny if no active manager
                    if (clientManagement.getClientList().size() == 0) {
                        Chat deny = new Chat("Server", "bot",
                                "deny", "No active manager.", username);
                        output.writeUTF(deny.toString());
                        output.flush();
                        serverGUI.logMessage("(Sent) " + deny);
                        clientSocket.close();

                    // Deny if invalid username
                    } else if (clientManagement.checkUsername(username)){
                        Chat deny = new Chat("Server", "bot",
                                "deny", "Invalid username", username);
                        output.writeUTF(deny.toString());
                        output.flush();
                        serverGUI.logMessage("(Sent) " + deny);
                        clientSocket.close();

                    // Send join request to manager
                    } else {

                        Chat connectedToServer = new Chat("Server", "bot",
                                "chat", "Join request sent to manager!", username);
                        output.writeUTF(connectedToServer.toString());
                        output.flush();
                        serverGUI.logMessage("(Sent) " + connectedToServer);

                        Chat join = new Chat("Server", "bot",
                                "join", username + " is requesting to join!", "manager");
                        clientManagement.contactManager(join);
                        clientManagement.addClientList(username, clientSocket);
                    }

                }

            }

            // Process subsequent messages from this client after connection request
            while(!clientSocket.isClosed()){

                try {

                    inputData = input.readUTF();
                    serverGUI.logMessage("(Received) " + inputData);

                    if (inputData != null) {
                        JSONObject subsMsgReceived = (JSONObject) parser.parse(inputData);
                        // Send to message warehouse to process

                        messageWarehouse.addMsgReceived(subsMsgReceived);
                    }

                } catch (IOException e){

                    String username = connection.username;
                    String userAccess = connection.userAccess;

                    if (clientManagement.checkUsername(username)){
                        Chat quit = new Chat(username, userAccess, "quit",
                                "*" + username
                                        + " left*","Server");

                        messageWarehouse.addMsgReceived(quit.chat);
                    }
                    break;
                }

            }

            input.close();
            output.close();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (ParseException e) {
            e.printStackTrace();

        }

    }

}
