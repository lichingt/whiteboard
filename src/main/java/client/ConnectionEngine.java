package client;

import message.Chat;
import message.Connection;
import message.Draw;
import message.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * ConnectionEngine class to manage incoming/outgoing messages
 * to/from the server
 *
 * @author liching
 *
 */
public class ConnectionEngine extends Thread {

    private Connection identity;
    private LinkedBlockingDeque<Message> incomingActions;
    private LinkedBlockingDeque<Message> outgoingActions;
    private Socket socket;

    public ConnectionEngine(){
        incomingActions = new LinkedBlockingDeque<>();
        outgoingActions = new LinkedBlockingDeque<>();
    }

    @Override
    public void run(){

        while (!isInterrupted()) {
            try {
                if (socket != null) {
                    System.out.println("Connected to server");

                    JSONParser parser = new JSONParser();
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream((socket.getOutputStream()));

                    // First message to server for introduction
                    output.writeUTF(identity.toString());
                    output.flush();
                    System.out.println("Outgoing: " + identity.toString());

                    while (!socket.isClosed()) {
                        if (input.available() > 0) {
                            takeIncomingMessages(input, parser);
                        }
                        if (!outgoingActions.isEmpty()) {
                            processOutgoingMessages(output, outgoingActions.take());
                        }
                    }
                }
            } catch (IOException | ParseException | InterruptedException e){
                System.out.println("Error has occurred with connection to server");
                Chat server = new Chat("Bot", "","serverDisconnected",
                        "Connection error with server","");
                incomingActions.add(server);
                interrupt();
            }
        }
    }

    private void processOutgoingMessages(DataOutputStream output, Message sendMessage) throws IOException {
        System.out.println("Outgoing: " + sendMessage);
        if (sendMessage.getClass().getName() == Draw.class.getName()){
            Draw sendDraw = (Draw) sendMessage;
            output.writeUTF(sendDraw.toString());
            output.flush();
        } else if (sendMessage.getClass().getName() == Chat.class.getName()){
            Chat sendChat = (Chat) sendMessage;

            // If quit message is from whiteboard gui or received from manager and forwarded here
            if (sendChat.category.equals("quit")){
                interrupt();
                socket.close();

            // All other messages, send through.
            } else {
                output.writeUTF(sendChat.toString());
                output.flush();
            }

        }
    }

    private void takeIncomingMessages(DataInputStream input, JSONParser parser) throws IOException, ParseException {
        JSONObject receivedMsg = (JSONObject) parser.parse(input.readUTF());
        System.out.println("Incoming: " + receivedMsg);
        if (receivedMsg.get("category").equals("draw")) {
            incomingActions.add(new Draw(receivedMsg));
        } else if (receivedMsg.get("category").equals("chat")){
            incomingActions.add(new Chat(receivedMsg));
        } else if (receivedMsg.get("category").equals("userlist")){
            incomingActions.add(new Chat(receivedMsg));
        } else if (receivedMsg.get("category").equals("join")){
            incomingActions.add(new Chat(receivedMsg));
        } else if (receivedMsg.get("category").equals("approve")){
            incomingActions.add(new Chat(receivedMsg));
        } else if (receivedMsg.get("category").equals("deny")){
            incomingActions.add(new Chat(receivedMsg));
        } else if (receivedMsg.get("category").equals("quit")){
            incomingActions.add(new Chat(receivedMsg));
        } else if (receivedMsg.get("category").equals("kick")){
            incomingActions.add(new Chat(receivedMsg));
        } else if (receivedMsg.get("category").equals("new")){
            incomingActions.add(new Chat(receivedMsg));
        }
    }

    public boolean establishConnection(Connection connection){

        this.identity = connection;

        String ipAddress = connection.ipAddress;
        String port = connection.port;

        try{
            socket = new Socket(InetAddress.getByName(ipAddress),
                    Integer.parseInt(port));
        } catch (IllegalArgumentException | NullPointerException | IOException e){
            System.out.println("Unable to connect to server with IP address and port provided.");
            return false;
        }
        return true;
    }

    public void addOutgoingAction(Message action){
        this.outgoingActions.add(action);
    }

    public LinkedBlockingDeque<Message> getIncomingActions(){
        return this.incomingActions;
    }

}
