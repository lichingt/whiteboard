package client;

import message.Chat;
import message.Connection;
import message.Draw;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * WhiteboardGUI class as a GUI for WhiteboardApplication
 * Structure overview of WhiteboardGUI
 *
 * Section 1: Initialization of WhiteboardGUI
 * Note: Canvas class is created as part of the initialization
 * Subsection 1: Action listeners for buttons and menu items
 * Subsection 2: Initialization of left panel
 * Subsection 3: Initialization of right panel
 * Subsection 4: Initialization of menu bar
 *
 * Section 2: Management of Interactivity with GUI
 * Subsection 1: Methods for updates relating to connection status
 * Subsection 2: Methods for updates relating to message movements
 * Subsection 3: Button handlers for left panel
 * Subsection 4: Menu item handlers
 *
 * @author liching
 *
 */
public class WhiteboardGUI extends JFrame {

    // Queues
    private LinkedBlockingDeque<Connection> connectionInfo;
    private LinkedBlockingDeque<Chat> chatMessages;

    // GUI main components
    private Canvas canvas;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JMenuBar menuBar;

    // Buttons with listeners
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton lineButton;
    private JButton circleButton;
    private JButton triangleButton;
    private JButton rectangleButton;
    private JButton penButton;
    private JButton textButton;
    private JButton colorButton;
    private JTextField chatBox;
    private JButton sendMessageButton;

    // Menu bar items
    private JMenu fileMenu;
    private JMenuItem newMenuItem;
    private JMenuItem openMenuItem;
    private JMenuItem saveMenuItem;
    private JMenuItem saveAsMenuItem;
    private JMenuItem closeMenuItem;

    // File management
    private File currentFile;
    private String basedir;

    // Settings for canvas tools
    private String toolSelected;
    private Color colorSelected;

    // Settings for connection
    private String userAccess;
    private JTextField usernameTextField;
    private JTextField ipAddressTextField;
    private JTextField portTextField;
    private JLabel connectionStatusLabel;
    private String username;

    // Right panel display
    private JTextArea userList;
    private JTextArea chatArea;

    /*
     * Section 1: Initialization of WhiteboardGUI
     */
    public WhiteboardGUI(String userAccess, String serverIPAddress,
                         String serverPort, String username){

        this.userAccess = userAccess;

        // Basedir needs to be set as null if program is launched via double-clicking jar.
        try {
            this.basedir = new File(System.getProperty("user.dir")).getCanonicalPath();
        } catch (IOException e) {
            this.basedir = null;
        }
        this.usernameTextField = new JTextField(username);
        this.ipAddressTextField = new JTextField(serverIPAddress);
        this.portTextField = new JTextField(serverPort);

        connectionInfo = new LinkedBlockingDeque<>();
        chatMessages = new LinkedBlockingDeque<>();

        String displayAccess = userAccess.substring(0,1).toUpperCase()
                + userAccess.substring(1);

        this.setTitle("Collaborative Whiteboard - " + displayAccess);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000,600);
        this.setLocationRelativeTo(null);

        // Canvas
        canvas = new Canvas();
        this.add(canvas, BorderLayout.CENTER);

        // Panel on the left
        leftPanel = new JPanel();
        initializeLeftPanel();
        this.add(leftPanel, BorderLayout.WEST);

        // Panel on the right
        rightPanel = new JPanel();
        initializeRightPanel();
        this.add(rightPanel, BorderLayout.EAST);

        // Configure menu for manager
        if (userAccess.equals("manager")){
            menuBar = new JMenuBar();
            initializeMenuBar();
            this.add(menuBar, BorderLayout.NORTH);
        }

        this.setVisible(true);
        updateButtonStatus(false);

    }

    /*
     * Section 1: Initialization of WhiteboardGUI
     * Subsection 1: Action listeners for buttons and menu items
     */
    ActionListener buttonListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            String buttonClicked = e.getActionCommand().toLowerCase();

            switch (buttonClicked) {
                case "connect":
                    handleConnectButton();
                    break;
                case "disconnect":
                    handleDisconnectButton();
                    break;
                case "insert text":
                    handleInsertTextButton();
                    break;
                case "select color":
                    handleSelectColorButton();
                    break;
                case "send":
                    handleSendButton();
                    break;
                case "new":
                    handleNewMenuItem();
                    break;
                case "open":
                    handleOpenMenuItem();
                    break;
                case "save":
                    handleSaveMenuItem();
                    break;
                case "save as..":
                    handleSaveAsMenuItem();
                    break;
                case "close":
                    handleCloseMenuItem();
                    break;
                default:
                    handleOtherTools(buttonClicked);
                    break;
            }

        }
    };
    ActionListener textFieldListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            handleSendButton();
        }
    };

    /*
     * Section 1: Initialization of WhiteboardGUI
     * Subsection 2: Initialization of left panel
     */
    private void initializeLeftPanel(){

        leftPanel.setBackground(Color.decode("#094183"));
        leftPanel.setLayout(new GridLayout(20, 1,2,1));
        leftPanel.setPreferredSize(new Dimension(150,600));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // 1. Connection status
        connectionStatusLabel = new JLabel("<html>Connection status: <br>Unavailable</html>");
        connectionStatusLabel.setForeground(Color.WHITE);
        connectionStatusLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        connectionStatusLabel.setVerticalTextPosition(SwingConstants.CENTER);
        leftPanel.add(connectionStatusLabel);

        leftPanel.add(new JLabel()); // Just for spacing

        // 2. Username
        JLabel usernameLabel = new JLabel("  Username: ");
        usernameLabel.setForeground(Color.decode("#b3cde0"));
        leftPanel.add(usernameLabel);

        usernameTextField.setToolTipText("Input username");
        leftPanel.add(usernameTextField);

        // 3. IP Address
        JLabel ipAddressLabel = new JLabel("  IP Address: ");
        ipAddressLabel.setForeground(Color.decode("#b3cde0"));
        leftPanel.add(ipAddressLabel);
        ipAddressTextField.setToolTipText("Input server's IP address");
        leftPanel.add(ipAddressTextField);

        // 4. Port
        JLabel portLabel = new JLabel("  Port: ");
        portLabel.setForeground(Color.decode("#b3cde0"));
        leftPanel.add(portLabel);
        portTextField.setToolTipText("Input server's port number");
        leftPanel.add(portTextField);

        leftPanel.add(new JLabel()); // Just for spacing

        // 5. Connect
        connectButton = new JButton("Connect");
        connectButton.setToolTipText("Connect to server");
        connectButton.setForeground(Color.decode("#8ecf7f"));
        connectButton.addActionListener(buttonListener);
        leftPanel.add(connectButton);

        // 6. Disconnect
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setToolTipText("Disconnect from server");
        disconnectButton.setForeground(Color.decode("#cf7f8e"));
        disconnectButton.addActionListener(buttonListener);
        leftPanel.add(disconnectButton);

        leftPanel.add(new JLabel()); // Just for spacing

        // 7. Line tool
        lineButton = new JButton("Line");
        lineButton.setToolTipText("Draw straight line");
        lineButton.addActionListener(buttonListener);
        leftPanel.add(lineButton);

        // 8. Circle tool
        circleButton = new JButton("Circle");
        circleButton.setToolTipText("Draw circle");
        circleButton.addActionListener(buttonListener);
        leftPanel.add(circleButton);

        // 9. Triangle tool
        triangleButton = new JButton("Triangle");
        triangleButton.setToolTipText("Draw triangle");
        triangleButton.addActionListener(buttonListener);
        leftPanel.add(triangleButton);

        // 10. Rectangle tool
        rectangleButton = new JButton("Rectangle");
        rectangleButton.setToolTipText("Draw rectangle");
        rectangleButton.addActionListener(buttonListener);
        leftPanel.add(rectangleButton);

        // 11. Pen tool
        penButton = new JButton("Pen");
        penButton.setToolTipText("Draw free hand");
        penButton.addActionListener(buttonListener);
        leftPanel.add(penButton);

        // 12. Insert text tool
        textButton = new JButton("Insert text");
        textButton.setToolTipText("Click to insert text then position on whiteboard");
        textButton.addActionListener(buttonListener);
        leftPanel.add(textButton);

        // 13. Select color tool
        colorButton = new JButton("Select color");
        colorButton.setToolTipText("Choose color for tool");
        colorButton.addActionListener(buttonListener);
        leftPanel.add(colorButton);

    }

    /*
     * Section 1: Initialization of WhiteboardGUI
     * Subsection 3: Initialization of right panel
     */
    private void initializeRightPanel(){
        rightPanel.setBackground(Color.decode("#094183"));
        rightPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.ipadx = 80;
        c.insets = new Insets(10,0,0,0);

        rightPanel.setPreferredSize(new Dimension(200,600));
        //rightPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JLabel userListLabel = new JLabel("Active users:");
        userListLabel.setForeground(Color.decode("#b3cde0"));
        c.gridy = 0;
        c.ipady = 10;
        c.insets = new Insets(5,0,0,0);
        rightPanel.add(userListLabel, c);

        // 1. User list display area
        userList = new JTextArea();
        userList.setLineWrap(true);
        userList.setEditable(false);
        userList.setBackground(Color.decode("#c5d9e7"));
        JScrollPane scrollUserList = new JScrollPane(userList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollUserList.setPreferredSize(new Dimension(200,130));
        c.gridy = 1;
        c.ipady = 200;
        c.weighty = 1.0;
        c.insets = new Insets(0,0,0,0);
        rightPanel.add(scrollUserList, c);

        JLabel chatDisplayLabel = new JLabel("Chat");
        chatDisplayLabel.setForeground(Color.decode("#b3cde0"));
        c.gridy = 2;
        c.ipady = 10;
        c.weighty = 0;
        c.insets = new Insets(5,0,0,0);
        rightPanel.add(chatDisplayLabel, c);

        // 2. Chat display area
        chatArea = new JTextArea();
        chatArea.setLineWrap(true);
        chatArea.setEditable(false);
        chatArea.setBackground(Color.decode("#c5d9e7"));
        DefaultCaret caretChatArea = (DefaultCaret) chatArea.getCaret();
        caretChatArea.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollChatBox = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollChatBox.setPreferredSize(new Dimension(200,250));
        c.gridy = 3;
        c.ipady = 400;
        c.weighty = 1.0;
        c.insets = new Insets(0,0,0,0);
        rightPanel.add(scrollChatBox, c);
        if (userAccess.equals("manager")){
            chatArea.append("@Bot: Type @kick @username to kick a user"+ "\n");
        }

        JLabel chatInputLabel = new JLabel("Type here");
        chatInputLabel.setForeground(Color.decode("#b3cde0"));
        c.gridy = 4;
        c.ipady = 10;
        c.weighty = 0;
        c.insets = new Insets(5,0,0,0);
        rightPanel.add(chatInputLabel, c);

        // 3. Input text for chat field
        chatBox = new JTextField();
        chatBox.setPreferredSize(new Dimension(200,30));
        chatBox.setToolTipText("Type your message here");
        chatBox.addActionListener(textFieldListener);
        c.gridy = 5;
        c.ipady = 8;
        c.insets = new Insets(0,0,0,0);
        rightPanel.add(chatBox, c);

        // 4. Send message button
        sendMessageButton = new JButton("Send");
        sendMessageButton.setToolTipText("Send message");
        sendMessageButton.addActionListener(buttonListener);
        c.gridy = 6;
        c.ipady = 8;
        c.insets = new Insets(0,0,10,0);
        rightPanel.add(sendMessageButton, c);

    }

    /*
     * Section 1: Initialization of WhiteboardGUI
     * Subsection 4: Initialization of menu bar
     */
    private void initializeMenuBar(){
        fileMenu = new JMenu("File");

        // 1. New
        newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(buttonListener);
        fileMenu.add(newMenuItem);

        // 2 Open
        openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(buttonListener);
        fileMenu.add(openMenuItem);

        // 3. Save
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(buttonListener);
        fileMenu.add(saveMenuItem);

        // 4. Save As
        saveAsMenuItem = new JMenuItem("Save as..");
        saveAsMenuItem.addActionListener(buttonListener);
        fileMenu.add(saveAsMenuItem);

        // 5. Close
        closeMenuItem = new JMenuItem("Close");
        closeMenuItem.addActionListener(buttonListener);
        fileMenu.add(closeMenuItem);

        menuBar.add(fileMenu);

    }

    /*
     * Section 2: Management of Interactivity with GUI
     * Subsection 1: Methods for updates relating to connection status
     */
    public LinkedBlockingDeque<Connection> getConnectionInfo(){
        return connectionInfo;
    }

    public void setConnectionStatus(boolean connected){
        if (connected){
            this.username = usernameTextField.getText();
            String displayAccess = userAccess.substring(0,1).toUpperCase()
                    + userAccess.substring(1);
            this.setTitle("Collaborative Whiteboard - " + displayAccess + " @" + username);
            connectionStatusLabel.setText("<html>Connection status: <br>Connected</html>");
            canvas.setUsername(username);
            updateButtonStatus(true);
        }
        else {
            connectionStatusLabel.setText("<html>Connection status: <br>Disconnected</html>");
            canvas.setToolSelected(null);
            canvas.clearCanvas();
            updateButtonStatus(false);
        }
    }

    private void updateButtonStatus(boolean status){
        if (menuBar != null){
            newMenuItem.setEnabled(status);
            openMenuItem.setEnabled(status);
            saveMenuItem.setEnabled(status);
            saveAsMenuItem.setEnabled(status);
            closeMenuItem.setEnabled(status);
        }
        connectButton.setEnabled(!status);
        disconnectButton.setEnabled(status);
        lineButton.setEnabled(status);
        circleButton.setEnabled(status);
        triangleButton.setEnabled(status);
        rectangleButton.setEnabled(status);
        penButton.setEnabled(status);
        textButton.setEnabled(status);
        colorButton.setEnabled(status);
        chatBox.setEnabled(status);
        sendMessageButton.setEnabled(status);
        usernameTextField.setEnabled(!status);
        ipAddressTextField.setEnabled(!status);
        portTextField.setEnabled(!status);
    }

    /*
     * Section 2: Management of Interactivity with GUI
     * Subsection 2: Methods for updates relating to message movements
     */
    // Update userList in GUI
    public void setUserList(String message){
        userList.setText(message);
    }

    // Update chatArea in GUI
    public void addToChatArea(Chat message){
        String displayMessage;
        if (message.category.equals("quit") & message.userAccess.equals("user")) {
            displayMessage = message.chatMessage;

        } else if (message.category.equals("quit") & message.userAccess.equals("manager")) {
            displayMessage = "@Bot: Manager has closed the whiteboard";
        } else {
            displayMessage = "@" + message.username + ": " + message.chatMessage;
        }
        chatArea.append(displayMessage + "\n");
    }

    // Methods to receive/send draw objects or messages
    public LinkedBlockingDeque<Draw> getSendDrawObjects(){
        return canvas.getSendDrawObjects();
    }
    public LinkedBlockingDeque<Chat> getChatMessages(){
        return chatMessages;
    }
    public void addDrawObject(Draw draw){
        canvas.addDrawObject(draw);
    }

    public String getUsername() {
        return username;
    }
    public void clearCanvas(){
        canvas.clearCanvas();
    }

    // Prompt for manager to approve join request
    public void promptJoinRequest(String message){
        int response = JOptionPane.showConfirmDialog(this, message + "\n" + "Approve?",
                "Join Request", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        String userRequesting = message.substring(0,(message.length()-" is requesting to join!".length()));
        Chat joinReply;

        if (response == JOptionPane.NO_OPTION) {
            joinReply = new Chat(username, userAccess, "deny",
                    "Your request to connect has been denied.",userRequesting);
            chatMessages.add(joinReply);
        } else if (response == JOptionPane.YES_OPTION) {
            joinReply = new Chat(username, userAccess, "approve",
                    "Connected! Welcome!",userRequesting);
            chatMessages.add(joinReply);
        } else if (response == JOptionPane.CLOSED_OPTION) {
            joinReply = new Chat(username, userAccess, "deny",
                    "Your request to connect has been denied.",userRequesting);
            chatMessages.add(joinReply);
        }

    }

    /*
     * Section 2: Management of Interactivity with GUI
     * Subsection 3: Button handlers for left panel
     */
    public void handleConnectButton(){
        Connection connection = new Connection(
                usernameTextField.getText(),
                "connect",
                ipAddressTextField.getText(),
                portTextField.getText(),
                userAccess);
        connectionInfo.add(connection);
        currentFile = null;
    }

    private void handleDisconnectButton(){

        Connection connection = new Connection(
                usernameTextField.getText(),
                "disconnect",
                ipAddressTextField.getText(),
                portTextField.getText(),
                userAccess);

        if (userAccess.equals("manager")){
            if (promptToSave()){
                connectionInfo.add(connection);
            }
        } else {
            connectionInfo.add(connection);
        }

    }

    private void handleInsertTextButton(){
        toolSelected = "text";
        String input = JOptionPane.showInputDialog(this, "Input text: ");
        canvas.setTextInput(input);
        canvas.setToolSelected(toolSelected);
    }

    private void handleSelectColorButton(){
        Color chosenColor = JColorChooser.showDialog(this, "Select color", colorSelected);
        if (chosenColor != null){
            colorSelected = chosenColor;
            canvas.setColorSelected(colorSelected);
        }
    }

    private void handleOtherTools(String buttonClicked){
        toolSelected = buttonClicked;
        canvas.setToolSelected(toolSelected);
    }

    private void handleSendButton(){
        String inputText = chatBox.getText();

        if (!inputText.equals("")){
            Chat chatMessage = new Chat(username, userAccess, "chat",inputText,"all");
            addToChatArea(chatMessage);
            chatMessages.add(chatMessage);
            chatBox.setText("");
        }
        if (inputText.startsWith("@kick @") & userAccess.equals("manager")){

            String userKicked = inputText.substring(7);

            Chat kickCommand = new Chat(username, userAccess, "kick",
                    userKicked + " has been kicked!", userKicked);
            chatMessages.add(kickCommand);

        }

    }

    /*
     * Section 2: Management of Interactivity with GUI
     * Subsection 4: Menu item handlers
     */
    private void handleNewMenuItem(){

        if (promptToSave()){
            canvas.clearCanvas();
            currentFile = null;
            Chat newCanvas = new Chat(username, userAccess, "new",
                    "A new canvas has been created.", "all");
            chatMessages.add(newCanvas);
            addToChatArea(newCanvas);

            JOptionPane.showMessageDialog(this, "New canvas created!");
        }

    }

    private void handleOpenMenuItem(){

        File fileSelected = null;
        try {
            if (promptToSave()) {
                JFileChooser fileChooser = new JFileChooser(basedir);
                fileChooser.setDialogTitle("Open file..");

                FileNameExtensionFilter filterJSON =
                        new FileNameExtensionFilter("JSON Files (*.json)", "json");
                fileChooser.addChoosableFileFilter(filterJSON);
                fileChooser.setFileFilter(filterJSON);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                int userSelection = fileChooser.showOpenDialog(this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {

                    // Start opening file
                    fileSelected = fileChooser.getSelectedFile();
                    FileReader readerToRender = new FileReader(fileSelected.getAbsolutePath());
                    FileReader readerToSend = new FileReader(fileSelected.getAbsolutePath());

                    JSONParser parser = new JSONParser();
                    JSONArray fileDataToRender = (JSONArray) parser.parse(readerToRender);
                    JSONArray fileDataSend = (JSONArray) parser.parse(readerToSend);
                    readerToRender.close();
                    readerToSend.close();

                    // Check if file contents are okay and put into different arrays
                    ArrayList<Draw> toRender = new ArrayList<>();
                    for (Object drawObjData : fileDataToRender) {
                        toRender.add(new Draw((JSONObject) drawObjData));
                    }
                    ArrayList<Draw> toSend = new ArrayList<>();
                    for (Object drawObjData : fileDataSend) {
                        toSend.add(new Draw((JSONObject) drawObjData));
                    }

                    // Wipe out everything before loading existing file
                    canvas.clearCanvas();
                    Chat newCanvas = new Chat(username, userAccess, "new",
                            "A new canvas has been opened.", "all");
                    addToChatArea(newCanvas);
                    chatMessages.add(newCanvas);

                    for (Draw drawObjData : toRender) {
                        canvas.addDrawObject(drawObjData);
                    }

                    for (Draw drawObjData : toSend) {
                        drawObjData.setUsername(username);
                        canvas.addToSendDrawObjects(drawObjData);
                    }

                    // File processed successfully: update current file.
                    currentFile = fileChooser.getSelectedFile();

                    String fullFilename = fileSelected.getName();
                    JOptionPane.showMessageDialog(this, fullFilename.substring(0, fullFilename.length() - 5) + " has been loaded.");

                }

            }

        } catch (IOException | ParseException e) {
            String fullFilename = fileSelected.getName();
            JOptionPane.showMessageDialog(this, "Error: File corrupted. Unable to load " + fullFilename.substring(0,fullFilename.length()-5) + ".");
        }

    }

    private void handleSaveMenuItem(){

        // If no existing file, prompt user to provide file details.
        if (currentFile == null) {
            handleSaveAsMenuItem();

        // Otherwise, write to the current file.
        } else {

            try {
                writeToFile(currentFile);
                String fullFilename = currentFile.getName();
                JOptionPane.showMessageDialog(this, fullFilename.substring(0,fullFilename.length()-5) + " has been saved.");

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error: Unable to save file.");
                currentFile = null;
            }
        }
    }

    private boolean handleSaveAsMenuItem(){

        // Default pre-filled name in JFileChooser
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String filename = "Whiteboard_" + username + "_" + sdf.format(timestamp);
        File fileSelected = new File(filename);

        boolean saved = false;
        JFileChooser fileChooser = new JFileChooser(basedir);
        fileChooser.setDialogTitle("Save file..");
        fileChooser.setSelectedFile(fileSelected);

        FileNameExtensionFilter filterJSON =
                new FileNameExtensionFilter("JSON Files (*.json)", "json");
        fileChooser.addChoosableFileFilter(filterJSON);
        fileChooser.setFileFilter(filterJSON);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int userSelection = fileChooser.showSaveDialog(this);

        try {
            if (userSelection == JFileChooser.APPROVE_OPTION) {

                // Check and enforce .json file name
                String filenameChosen = fileChooser.getSelectedFile().toString();
                if (!filenameChosen.toLowerCase().endsWith(".json")) {
                    filenameChosen += ".json";

                }

                fileSelected = new File(filenameChosen);
                writeToFile(fileSelected);

                // File write complete
                currentFile = fileSelected;
                String fullFilename = fileSelected.getName();
                JOptionPane.showMessageDialog(this, fullFilename.substring(0,fullFilename.length()-5) + " has been saved.");
                saved = true;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error: Unable to save file.");
        }
        return saved;
    }

    private void handleCloseMenuItem(){

        if (promptToSave()) {
            int response = JOptionPane.showConfirmDialog(this, "Exit and shutdown Whiteboard?",
                    "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);;
            }
        }

    }


    // Helper method to write file in Save and SaveAs methods
    private void writeToFile(File file) throws IOException {

        FileWriter writer = new FileWriter(file.getAbsolutePath());

        ArrayList<Draw> currentLocalState = canvas.getDrawObjects();
        JSONArray JSONLocalState = new JSONArray();
        for (Draw drawObject : currentLocalState) {
            JSONLocalState.add(drawObject.draw);

        }

        writer.write(JSONLocalState.toString());
        writer.flush();
        writer.close();

    }

    // Helper method to prompt manager to save file before creating a new file, opening
    // another file, exit the program via the close button or disconnect.
    private boolean promptToSave() {
        boolean actionDone = true;

        // If there is drawings on the canvas, check if current state has the same data as current file selected
        if (canvas.getDrawObjects().size() != 0) {

            // If current file is null, definitely not the same
            boolean dataChanged = true;

            // If there is a current file, do comparison
            if (currentFile != null) {

                // Current state
                ArrayList<Draw> currentLocalState = canvas.getDrawObjects();
                JSONArray JSONLocalState = new JSONArray();
                for (Draw drawObject : currentLocalState) {
                    JSONLocalState.add(drawObject.draw);

                }

                try {
                    // File data
                    FileReader reader = new FileReader(currentFile.getAbsolutePath());
                    JSONParser parser = new JSONParser();
                    JSONArray fileData = (JSONArray) parser.parse(reader);
                    reader.close();

                    dataChanged = !JSONLocalState.toJSONString().equals(fileData.toJSONString());

                } catch (ParseException | IOException e) {
                    currentFile = null;
                }

            }

            if (dataChanged) {
                int response = JOptionPane.showConfirmDialog(this, "Save current masterpiece before creating new canvas?",
                        "Save prompt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    actionDone = handleSaveAsMenuItem();
                } else if (response == JOptionPane.CLOSED_OPTION) {
                    actionDone = false;
                } else if (response == JOptionPane.NO_OPTION) {
                    actionDone = true;
                }

            }


        }

        return actionDone;
    }


}
