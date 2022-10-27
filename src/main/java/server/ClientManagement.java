package server;

import message.Chat;
import message.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Client Management to maintain list of active clients
 * and assist in directing messages to the right clients.
 *
 * @author liching
 *
 */
public class ClientManagement {

    private static int counter = 0;

    private Socket managerSocket;

    private String managerUsername;

    private HashMap<String, Socket> clientList;

    private ServerGUI serverGUI;

    public ClientManagement(ServerGUI serverGUI){
        clientList = new HashMap<String, Socket>();
        this.serverGUI = serverGUI;
    }

    public void setManager(String managerUsername, Socket managerSocket){
        this.managerUsername = managerUsername;
        this.managerSocket = managerSocket;
        this.clientList.put(managerUsername, managerSocket);
    }

    public void resetClientManagement() throws IOException {
        for (Map.Entry<String, Socket> client : clientList.entrySet()) {
            Socket socket = client.getValue();
            socket.close();
        }
        clientList.clear();
        this.managerSocket = null;
        this.managerUsername = null;
    }

    public HashMap<String, Socket> getClientList(){
        return clientList;
    }

    public Chat getEmptyUserListDisplay(){
        Chat chat = new Chat("Server","bot","userlist","","all");
        return chat;
    }

    public Chat getUserList(){
        String userList = managerUsername + " (manager) \n";
        for (Map.Entry<String, Socket> client : clientList.entrySet()) {

            String username = client.getKey();
            if (!username.equals(managerUsername)){
                userList += username + "\n";
            }

        }
        Chat chat = new Chat("Server","bot","userlist",userList,"all");
        return chat;
    }

    public String getManagerUsername(){
        return managerUsername;
    }

    public void addClientList(String username, Socket socket){
        clientList.put(username, socket);
    }

    public void removeClientFromList(String username) throws IOException {
        Socket socket = clientList.get(username);
        if (socket != null){
            socket.close();
        }
        clientList.remove(username);
    }

    public boolean checkIfManager(String username){
        return managerUsername.equals(username);
    }

    public boolean checkUsername(String username){
        // Username reserved for server
        if (username.equals("Server")){
            return true;
        }
        return clientList.containsKey(username);
    }

    public void contactManager(Message message) throws IOException {
        sendMessage(managerSocket, message);
    }

    public void contactUser(String username, Message message) throws IOException {
        Socket socket = clientList.get(username);
        sendMessage(socket, message);
    }

    public void broadcastMessage(String sender, Message message) throws IOException {
        // Send to everyone except original sender
        for (Map.Entry<String, Socket> client : clientList.entrySet()) {
            if (!client.getKey().equals(sender)){
                Socket socket = client.getValue();
                sendMessage(socket, message);
            }
        }
    }

    private void sendMessage(Socket socket, Message message) throws IOException {
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeUTF(message.toString());
        output.flush();
        serverGUI.logMessage("(Sent) " + message);
    }


}
