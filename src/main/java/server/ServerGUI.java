package server;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.Color;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 *
 * ServerGUI class as a GUI for Server to display incoming and outgoing messages.
 *
 * @author liching
 *
 */
public class ServerGUI extends JFrame {

    private JTextArea infoDisplay;

    public ServerGUI(){
        initialize();
    }

    private void initialize(){
        this.setTitle("Server for Collaborative Whiteboard");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000,600);
        this.setLocationRelativeTo(null);

        infoDisplay = new JTextArea();
        infoDisplay.setLineWrap(true);
        infoDisplay.setEditable(false);
        infoDisplay.setBackground(Color.decode("#EDF2F5"));
        infoDisplay.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        DefaultCaret caretInfoDisplay = (DefaultCaret) infoDisplay.getCaret();
        caretInfoDisplay.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollInfoDisplay = new JScrollPane(infoDisplay, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollInfoDisplay);

        this.setVisible(true);

    }


    public void logMessage(String message){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String displayMessage = sdf.format(timestamp) + ": " + message + "\n\n";
        infoDisplay.append(displayMessage);
    }

}
