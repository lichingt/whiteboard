package server;

import message.Chat;
import message.Draw;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * MessageWarehouse to hold messages received by server and process each incoming
 * message at one time using methods in Client Management.
 *
 * @author liching
 *
 */
public class MessageWarehouse extends Thread {

    private LinkedBlockingDeque<JSONObject> receivedByServer;

    private ClientManagement clientManagement;

    private ArrayList<Draw> currentState;

    public MessageWarehouse(ClientManagement clientManagement){
        this.receivedByServer = new LinkedBlockingDeque<>();
        this.clientManagement = clientManagement;
        this.currentState = new ArrayList<>();
    }

    @Override
    public void run(){
        try{
            while (!interrupted()){

                JSONObject msgReceived = receivedByServer.take();
                String category = (String) msgReceived.get("category");

                switch (category) {

                    // Normal chat message
                    case "chat": {
                        String msgSender = (String) msgReceived.get("username");
                        clientManagement.broadcastMessage(msgSender, new Chat(msgReceived));
                        break;
                    }
                    // Normal draw object
                    case "draw": {

                        String msg = msgReceived.toString();

                        JSONParser parser = new JSONParser();
                        JSONObject forward = (JSONObject) parser.parse(msg);
                        JSONObject state = (JSONObject) parser.parse(msg);

                        Draw drawReceived = new Draw(forward);
                        Draw drawReceivedState = new Draw(state);
                        drawReceivedState.setUsername(clientManagement.getManagerUsername());
                        clientManagement.broadcastMessage((String) msgReceived.get("username"), drawReceived);
                        currentState.add(drawReceivedState);
                        break;
                    }
                    // Message from manager to server to user
                    case "approve": {
                        String joiningUser = (String) msgReceived.get("recipient");
                        clientManagement.contactUser(joiningUser, new Chat(msgReceived));
                        // send current state to user
                        for (Draw draw : currentState){
                            clientManagement.contactUser(joiningUser, draw);
                        }
                        clientManagement.broadcastMessage("Server", clientManagement.getUserList());
                        break;
                    }

                    case "deny": {
                        String deniedUser = (String) msgReceived.get("recipient");
                        clientManagement.contactUser(deniedUser, new Chat(msgReceived));
                        clientManagement.removeClientFromList(deniedUser);
                        break;
                    }


                    // Message from user to server
                    case "quit": {
                        // process differently for manager and user
                        String userAccess = (String) msgReceived.get("userAccess");
                        String username = (String) msgReceived.get("username");

                        // leave request from manager. shut this party down
                        if (userAccess.equals("manager")){
                            clientManagement.removeClientFromList(username);
                            clientManagement.broadcastMessage("Server", clientManagement.getEmptyUserListDisplay());
                            clientManagement.broadcastMessage("Server", new Chat(msgReceived));
                            clientManagement.resetClientManagement();
                            currentState.clear();

                            // leave request from user
                        } else {
                            clientManagement.removeClientFromList(username);
                            clientManagement.broadcastMessage("Server", clientManagement.getUserList());
                            clientManagement.broadcastMessage("Server", new Chat(msgReceived));
                        }

                        break;
                    }


                    // Message from manager to server and to specific user
                    case "kick": {
                        String userKicked = (String) msgReceived.get("recipient");

                        if (clientManagement.checkUsername(userKicked) &
                                !clientManagement.checkIfManager(userKicked) &
                                !userKicked.equals("Server")){
                            clientManagement.broadcastMessage("Server", new Chat(msgReceived));
                            clientManagement.removeClientFromList(userKicked);
                            clientManagement.broadcastMessage("Server", clientManagement.getUserList());
                        } else {
                            Chat kickDenied = new Chat("Server", "bot",
                                    "chat", "Unable to kick @" + userKicked, "manager");
                            clientManagement.contactManager(kickDenied);
                        }

                        break;
                    }


                    case "new": {
                        String userAccess = (String) msgReceived.get("userAccess");
                        String username = (String) msgReceived.get("username");

                        if (userAccess.equals("manager")){
                            currentState.clear();
                            clientManagement.broadcastMessage(username, new Chat(msgReceived));
                        }

                        break;

                    }

                    default:
                        break;

                }

            }
        } catch (InterruptedException | IOException | ParseException e){
            e.printStackTrace();
        }

    }

    public void addMsgReceived(JSONObject message){
        receivedByServer.add(message);
    }

}
