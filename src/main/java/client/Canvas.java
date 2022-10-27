package client;

import message.Draw;
import org.json.simple.JSONArray;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * Canvas class to manage displays and actions on the whiteboard's canvas
 *
 * Section 1: Initialization of Canvas
 * Section 2: Method to render all draw objects from ArrayList<Draw> to canvas
 * Section 3: Methods to update certain attributes from WhiteboardGUI input
 * Section 4: Other helper methods
 *
 * @author liching
 *
 */
public class Canvas extends JPanel implements MouseListener, MouseMotionListener {

    // All drawings created/received and shown on canvas
    private ArrayList<Draw> drawObjects;
    // New drawings created and to be sent to server
    private LinkedBlockingDeque<Draw> sendDrawObjects;

    // Attributes that can be updated from GUI
    private String toolSelected;
    private String textInput;
    private Color colorSelected;
    private String username;

    // Attributes that is determined from mouse movement
    private int xStart;
    private int yStart;
    private int xEnd;
    private int yEnd;
    private JSONArray lineDataX;
    private JSONArray lineDataY;

    /*
     * Section 1: Initialization of Canvas
     */
    public Canvas(){
        lineDataX = new JSONArray();
        lineDataY = new JSONArray();
        drawObjects = new ArrayList<Draw>();
        sendDrawObjects = new LinkedBlockingDeque<Draw>();
        this.setBackground(Color.decode("#EDF2F5"));
        this.setPreferredSize(new Dimension(400,400));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    /*
     * Section 2: Method to render all draw objects from ArrayList<Draw> to canvas
     */
    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        for (Draw drawObject : drawObjects) {

            String drawType = drawObject.drawType;
            Integer x1 = drawObject.x1;
            Integer y1 = drawObject.y1;
            Integer x2 = drawObject.x2;
            Integer y2 = drawObject.y2;

            Integer xMin = Math.min(x1,x2);
            Integer yMin = Math.min(y1,y2);
            Integer xDiff = Math.abs(x1-x2);
            Integer yDiff = Math.abs(y1-y2);

            Color savedColor = drawObject.getColor();

            g2.setColor(savedColor);
            g2.setStroke(new BasicStroke(3)); // default line thickness

            switch (drawType) {
                case "line":
                    g2.drawLine(x1, y1, x2, y2);
                    break;
                case "circle":
                    int r = (int) (Math.sqrt(xDiff*xDiff+yDiff*yDiff));
                    x1 = (int) (x1-(r/2));
                    y1 = (int) (y1-(r/2));
                    g2.drawOval(x1, y1, r, r);
                    break;
                case "triangle":
                    g2.drawPolygon(new int[] {xMin-xDiff,xMin,xMin+xDiff}, new int[] {yMin+yDiff,yMin,yMin+yDiff}, 3);
                    break;
                case "rectangle":
                    g2.drawRect(xMin, yMin, xDiff, yDiff);
                    break;
                case "pen":
                    JSONArray savedLineDataX = drawObject.lineDataX;
                    JSONArray savedLineDataY = drawObject.lineDataY;
                    for (int i = 0; i < savedLineDataX.size()-1; i++){
                        g2.drawLine(Integer.valueOf(savedLineDataX.get(i).toString()),
                                Integer.valueOf(savedLineDataY.get(i).toString()),
                                Integer.valueOf(savedLineDataX.get(i+1).toString()),
                                Integer.valueOf(savedLineDataY.get(i+1).toString()));
                    }
                    break;
                case "text":
                    String savedTextData = drawObject.textData;
                    if (savedTextData != null){
                        Font currentFont = g2.getFont();
                        Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.25F);
                        g2.setFont(newFont);
                        g2.drawString(savedTextData, x1, y1);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /*
     * Section 3: Methods to update certain attributes from WhiteboardGUI input
     */
    public void setToolSelected(String toolSelected){
        this.toolSelected = toolSelected;
    }
    public void setTextInput(String textInput){
        this.textInput = textInput;
    }
    public void setColorSelected(Color colorSelected){
        this.colorSelected = colorSelected;
    }
    public void setUsername(String username){
        this.username = username;
    }

    /*
     * Section 4: Other helper methods
     */
    // Add new draw object received from server
    public void addDrawObject(Draw drawObject){
        drawObjects.add(drawObject);
        this.repaint();
    }

    // Send newly drawn object to the server
    public LinkedBlockingDeque<Draw> getSendDrawObjects(){
        return sendDrawObjects;
    }

    // Clear canvas
    public void clearCanvas(){
        drawObjects.clear();
        this.repaint();
    }

    // Return current state of canvas
    public ArrayList<Draw> getDrawObjects(){
        return drawObjects;
    }

    public void addToSendDrawObjects(Draw draw){
        sendDrawObjects.add(draw);
    }

    // Mouse information collection
    @Override
    public void mousePressed(MouseEvent e) {
        xStart = e.getX();
        yStart = e.getY();
        lineDataX.add(xStart);
        lineDataY.add(yStart);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        xEnd = e.getX();
        yEnd = e.getY();

        if (toolSelected != null){
            Draw draw = new Draw(username, "draw", toolSelected,
                    xStart, yStart, xEnd, yEnd, colorSelected);
            if (toolSelected.equals("pen")){

                JSONArray savedLineDataX = new JSONArray();
                JSONArray savedLineDataY = new JSONArray();
                savedLineDataX.addAll(lineDataX);
                savedLineDataY.addAll(lineDataY);
                draw.setLineDataX(savedLineDataX);
                draw.setLineDataY(savedLineDataY);
            } else if (toolSelected.equals("text")){
                draw.setTextData(textInput);
                toolSelected = null;
            }
            drawObjects.add(draw);
            sendDrawObjects.add(draw);
            lineDataX.clear();
            lineDataY.clear();
            textInput = "";
            this.repaint();
        } else {
            lineDataX.clear();
            lineDataY.clear();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        lineDataX.add(e.getX());
        lineDataY.add(e.getY());
    }

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}
}
