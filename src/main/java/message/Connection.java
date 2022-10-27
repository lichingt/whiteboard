package message;

import org.json.simple.JSONObject;

/**
 *
 * Child class of Message to hold connection and disconnection requests information.
 *
 * @author liching
 *
 */
public class Connection extends Message {

    public JSONObject connection;
    public String username;
    public String category;
    public String ipAddress;
    public String port;
    public String userAccess;

    public Connection(){
    }

    // Create a connect request with given string attributes
    public Connection(String username, String category,
                      String ipAddress,
                      String port, String userAccess){

        connection = new JSONObject();
        connection.put("username", username);
        connection.put("category",category);
        connection.put("ipAddress", ipAddress);
        connection.put("port", port);
        connection.put("userAccess", userAccess);

        this.username = username;
        this.category = category;
        this.ipAddress = ipAddress;
        this.port = port;
        this.userAccess = userAccess;
    }

    // Create a connect request with given JSONObject
    public Connection(JSONObject connectRequest){

        this.connection = connectRequest;

        this.username = (String) connectRequest.get("username");
        this.category = (String) connectRequest.get("category");
        this.ipAddress = (String) connectRequest.get("ipAddress");
        this.port = (String) connectRequest.get("port");
        this.userAccess = (String) connectRequest.get("userAccess");

    }

    @Override
    public String toString() {
        return connection.toString();
    }

}
