package message;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.Color;

/**
 *
 * Child class of Message to represent draw objects on Canvas.
 *
 * @author liching
 *
 */
public class Draw extends Message {

    public JSONObject draw;
    public String username;
    public String category;
    public String drawType;
    public Integer x1;
    public Integer y1;
    public Integer x2;
    public Integer y2;
    public String color;
    public JSONArray lineDataX;
    public JSONArray lineDataY;
    public String textData;

    public Draw(){
    }

    // Create a draw object with given string attributes
    public Draw(String username, String category, String drawType,
                Integer x1, Integer y1,
                Integer x2, Integer y2, Color color){

        draw = new JSONObject();
        draw.put("username", username);
        draw.put("category",category);
        draw.put("drawType", drawType);
        draw.put("x1", x1);
        draw.put("y1", y1);
        draw.put("x2", x2);
        draw.put("y2", y2);
        draw.put("color", "#000000");
        draw.put("lineDataX",new JSONArray());
        draw.put("lineDataY",new JSONArray());
        draw.put("textData", "na");

        this.username = username;
        this.category = category;
        this.drawType = drawType;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        if (color != null){
            draw.remove("color");
            this.color = convertColorToHex(color);
            draw.put("color",this.color);
        } else {
            this.color = "#000000";
        }

    }

    // Create a draw object with given JSONObject
    public Draw(JSONObject draw){

        this.draw = draw;

        this.username = (String) draw.get("username");
        this.category = (String) draw.get("category");
        this.drawType = (String) draw.get("drawType");
        this.x1 = (int) (long) draw.get("x1");
        this.y1 = (int) (long) draw.get("y1");
        this.x2 = (int) (long) draw.get("x2");
        this.y2 = (int) (long) draw.get("y2");
        this.color = (String) draw.get("color");
        this.lineDataX = (JSONArray) draw.get("lineDataX");
        this.lineDataY = (JSONArray) draw.get("lineDataY");
        this.textData = (String) draw.get("textData");

    }

    public void setUsername(String username){
        draw.remove("username");
        draw.put("username", username);
        this.username = username;
    }
    public void setLineDataX(JSONArray lineDataX){
        draw.remove("lineDataX");
        draw.put("lineDataX", lineDataX);
        this.lineDataX = lineDataX;
    }

    public void setLineDataY(JSONArray lineDataY){
        draw.remove("lineDataY");
        draw.put("lineDataY", lineDataY);
        this.lineDataY = lineDataY;
    }

    public void setTextData(String textData){
        draw.remove("textData");
        draw.put("textData", textData);
        this.textData = textData;
    }

    public Color getColor(){
        return convertHexToColor(color);
    }

    private String convertColorToHex(Color color){
        String hex = "#" + Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
        return hex;
    }

    private Color convertHexToColor(String hex){
        Color color = Color.decode(hex);
        return color;
    }

    @Override
    public String toString() {
        return draw.toString();
    }

}




