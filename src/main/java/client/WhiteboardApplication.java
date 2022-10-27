package client;

import message.Chat;
import message.Connection;
import message.Draw;
import message.Message;

import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * WhiteboardApplication to manage the interaction between
 * WhiteboardGUI (incl. Canvas) and ConnectionEngine.
 *
 * @author liching
 *
 */
public class WhiteboardApplication extends Thread {

    private WhiteboardGUI whiteboardGUI;
    private ConnectionEngine connectionEngine;
    private String userAccess;
    private boolean connectResponse;
    private boolean triggerConnectButton;

    public WhiteboardApplication(String userAccess, WhiteboardGUI whiteboardGUI, boolean triggerConnectButton){
        this.userAccess = userAccess;
        this.whiteboardGUI = whiteboardGUI;
        this.triggerConnectButton = triggerConnectButton;
        this.connectResponse = true;
    }

    @Override
    public void run(){
        while(!isInterrupted()){

            try {

                // If arguments were given upon launch
                if (triggerConnectButton) {
                    whiteboardGUI.handleConnectButton();
                    triggerConnectButton = false;
                }

                /*
                 * 1. To receive connection info from WhiteboardGUI when Connect/Disconnect button is clicked
                 */
                LinkedBlockingDeque<Connection> connectionInfo = whiteboardGUI.getConnectionInfo();

                if (!connectionInfo.isEmpty()){

                    Connection connection = connectionInfo.take();
                    String category = connection.category;

                    // Connect button clicked
                    if (category.equals("connect")){

                        if (connectResponse) {
                            connectResponse = false;
                            handleConnection(connection);
                        } else {
                            Chat wait = new Chat("Bot", "bot",
                                    "chat", "Waiting for approval from manager.", connection.username);
                            whiteboardGUI.addToChatArea(wait);
                        }

                    }

                    // Disconnect button clicked
                    else {
                        handleDisconnection(connection);
                    }

                }

                /*
                 * 2. To receive incoming messages from server through connection engine
                 */
                if (connectionEngine != null) {

                    LinkedBlockingDeque<Message> incomingActions = connectionEngine.getIncomingActions();

                    if (!incomingActions.isEmpty()){
                        Message receiveMessage = incomingActions.take();

                        if (receiveMessage.getClass().getName() == Chat.class.getName()){
                            Chat receivedChat = (Chat) receiveMessage;

                            if (receivedChat.category.equals("chat")){
                                whiteboardGUI.addToChatArea(receivedChat);

                            } else if (receivedChat.category.equals("userlist")) {
                                whiteboardGUI.setUserList(receivedChat.chatMessage);

                            } else if (receivedChat.category.equals("join") & this.userAccess.equals("manager")) {
                                whiteboardGUI.promptJoinRequest(receivedChat.chatMessage);

                            } else if (receivedChat.category.equals("approve") & this.userAccess.equals("user")) {
                                whiteboardGUI.addToChatArea(receivedChat);
                                whiteboardGUI.setConnectionStatus(true);
                                connectResponse = true;

                            } else if (receivedChat.category.equals("deny") & this.userAccess.equals("user")) {
                                whiteboardGUI.addToChatArea(receivedChat);
                                connectionEngine = null;
                                connectResponse = true;

                            } else if (receivedChat.category.equals("deny") & this.userAccess.equals("manager")) {
                                whiteboardGUI.addToChatArea(receivedChat);
                                whiteboardGUI.setConnectionStatus(false);
                                connectionEngine = null;
                                connectResponse = true;

                            // Receive message that manager has quit
                            } else if (receivedChat.category.equals("quit") & receivedChat.userAccess.equals("manager")) {
                                whiteboardGUI.addToChatArea(receivedChat);

                                // Just shutdown connection engine and update GUI
                                // No further message will be sent to server
                                whiteboardGUI.setConnectionStatus(false);
                                connectionEngine.addOutgoingAction(receivedChat);

                            } else if (receivedChat.category.equals("quit") & receivedChat.userAccess.equals("user")) {
                                whiteboardGUI.addToChatArea(receivedChat);

                            } else if (receivedChat.category.equals("kick") & receivedChat.recipient.equals(whiteboardGUI.getUsername())) {
                                whiteboardGUI.addToChatArea(receivedChat);
                                whiteboardGUI.setUserList("");
                                whiteboardGUI.setConnectionStatus(false);
                                connectionEngine = null;

                            } else if (receivedChat.category.equals("kick")) {
                                whiteboardGUI.addToChatArea(receivedChat);

                            } else if (receivedChat.category.equals("new")) {
                                whiteboardGUI.addToChatArea(receivedChat);
                                whiteboardGUI.clearCanvas();
                            } else if (receivedChat.category.equals("serverDisconnected")){
                                whiteboardGUI.setConnectionStatus(false);
                                whiteboardGUI.setUserList("");
                                whiteboardGUI.addToChatArea(receivedChat);
                                connectResponse = true;
                            }

                        } else if (receiveMessage.getClass().getName() == Draw.class.getName()) {
                            whiteboardGUI.addDrawObject((Draw) receiveMessage);
                        }

                    }

                    /*
                     * 3. To send outgoing messages from Canvas when draw object is created
                     */
                    LinkedBlockingDeque<Draw> outgoingActions = whiteboardGUI.getSendDrawObjects();
                    if (!outgoingActions.isEmpty()){
                        connectionEngine.addOutgoingAction(outgoingActions.take());
                    }

                    /*
                     * 4. To send outgoing chat messages from WhiteboardGUI to server
                     */
                    LinkedBlockingDeque<Chat> outgoingMessages =  whiteboardGUI.getChatMessages();
                    if (!outgoingMessages.isEmpty()){
                        connectionEngine.addOutgoingAction(outgoingMessages.take());
                    }
                }

            } catch (InterruptedException e){
                System.out.println("Error occurred with Whiteboard Application");
            }

        }

    }

    // Helper method to process connection request
    private void handleConnection(Connection connection){

        connectionEngine = new ConnectionEngine();
        connectionEngine.start();

        // Establish connection
        boolean connected = connectionEngine.establishConnection(connection);

        if (connected){
            if (this.userAccess.equals("manager")){
                // Update GUI directly once connected for manager. No approval required.
                whiteboardGUI.setConnectionStatus(true);
                connectResponse = true;
            }
        } else {
            Chat connectionFailed = new Chat("Bot", "", "chat", "Failed to establish connection with server using IP address and port provided.","");
            whiteboardGUI.addToChatArea(connectionFailed);
            connectionEngine.interrupt();
            connectResponse = true;
        }
    }

    // Helper method to process disconnection request
    private void handleDisconnection(Connection connection){
        // Update GUI
        whiteboardGUI.setConnectionStatus(false);
        whiteboardGUI.setUserList("");
        if (userAccess.equals("manager")){
            Chat managerClosed = new Chat("Bot", "",
                    "chat", "You have closed the whiteboard.","");
            whiteboardGUI.addToChatArea(managerClosed);
        }

        String username = connection.username;

        // Send message saying leave
        Chat quit = new Chat(username, userAccess, "quit",
                "*" + username
                        + " left*","Server");
        connectionEngine.addOutgoingAction(quit);

    }

}