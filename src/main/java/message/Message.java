package message;

import org.json.simple.JSONObject;

/**
 *
 * Base class for messages.
 *
 * @author liching
 *
 */
public class Message {

    public JSONObject message;

    public Message(){
    }

    @Override
    public String toString() {
        return message.toString();
    }

}
