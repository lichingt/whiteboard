package message;

import org.json.simple.JSONObject;

/**
 *
 * Child class of Message to handle chat and generic messages.
 *
 * @author liching
 *
 */
public class Chat extends Message {

    public JSONObject chat;
    public String username;
    public String userAccess;
    public String category;
    public String chatMessage;
    public String recipient;

    public Chat(){
    }

    // Create a chat with given string attributes
    public Chat(String username, String userAccess, String category,
                String chatMessage, String recipient){

        chat = new JSONObject();
        chat.put("username", username);
        chat.put("userAccess",userAccess);
        chat.put("category",category);
        chat.put("chatMessage", chatMessage);
        chat.put("recipient", recipient);

        this.username = username;
        this.userAccess = userAccess;
        this.category = category;
        this.chatMessage = chatMessage;
        this.recipient = recipient;
    }

    // Create a chat with given JSONObject
    public Chat(JSONObject chat){

        this.chat = chat;
        this.username = (String) chat.get("username");
        this.userAccess = (String) chat.get("userAccess");
        this.category = (String) chat.get("category");
        this.chatMessage = (String) chat.get("chatMessage");
        this.recipient = (String) chat.get("recipient");

    }

    @Override
    public String toString() {
        return chat.toString();
    }

}
