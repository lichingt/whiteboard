# Whiteboard application

## 1. Code structure

This repository contains a distributed whiteboard application using a client server architecture. There are four packages:

- `server`: Implementation of a server that manages the clients' connection and redirection of messages.
- `client`: This package includes all functionalities for a user and manager. In addition, it includes a main method containing the default initialization values for a program launch, to invoke a client (ie. user) that can join an existing whiteboard to participate in drawing and chatting. 
- `manager`: This package includes a main method containing the default initialization values for program launch, to invoke a client (from the `client` package) with additional privileges (ie. manager) including approving other clients to join the whiteboard, removing other clients from the whiteboard and controlling the state of the whiteboard via "new/open/save/save as/close" functionalities from the menu bar.
- `message`: Structure of messages sent between clients and server.

## 2. Program overview
1. The server needs to be running before any client can establish a connection with the server.
2. For clients, click the "Connect" button to initiate a connection with the Server.<br>
a) The first client must be a manager and the server will only accept and maintain the connection with one manager at any given point in time. The manager controls the whiteboard's status.<br>
b) Subsequent clients must be users and multiple users can be connected to the existing whiteboard created by the manager.<br>
c) Any client can choose to leave or the manager can kick any active users. If the manager leaves, the whiteboard closes for all users.

## 3. Running the program
1. Ensure that [Java](https://www.java.com/en/), [JDK](https://www.oracle.com/java/technologies/downloads/) and [Maven](https://maven.apache.org/download.cgi) has been installed in the local machine.
2. Pull this repository.
3. Run the program (select one of two methods below):
<hr>

### Method 1: Use the following commands in your terminal.

#### Compiling and running the Server

    cd whiteboard
    mvn clean package
    java -cp target/server-jar-with-dependencies.jar server.StartServer <port>

<em>One optional argument can be included upon launch to set the server's port number.</em>

#### Compiling and running the Manager

    cd whiteboard
    mvn clean package
    java -cp target/client-manager-jar-with-dependencies.jar manager.CreateWhiteBoard <serverIPAddress> <serverPort> <username>

<em>Optional arguments can be included upon launch to instantly initiate connection with Server. Otherwise, the values can be set via the GUI before connection.</em>

#### Compiling and running a user
    cd whiteboard
    mvn clean package
    java -cp target/client-user-jar-with-dependencies.jar client.JoinWhiteBoard <serverIPAddress> <serverPort> <username>

<em>Optional arguments can be included upon launch to instantly initiate connection with Server. Otherwise, the values can be set via the GUI before connection.</em>
<hr>

### Method 2: Generate jar files and run by double clicking on jar files.

1. Generate jar files using `mvn clean package` in the source code directory as per method 1, or use IntelliJ to do so.
2. Double click on the jar files generated from step 1.
Note: this method is not recommended when utilising functionalities from the menu bar for best user experience.

## 4. Screenshots of program

<table>

<tr>
<td><img src="/screenshots/Server.png?" width="300" title="Server screenshot"></td>
<td><img src="/screenshots/Client - Manager.png?" width="300" title="Manager screenshot"></td>
<td><img src="/screenshots/Client - User.png?" width="300" title="User screenshot"></td>
</tr>

<tr>
<td align="center"><em>Screenshot 1: Server</em></td>
<td align="center"><em>Screenshot 2: Manager</em></td>
<td align="center"><em>Screenshot 3: User</em></td>
</tr>

</table>

<hr>
<sub>This program has been built as part of an assignment in 2022 Semester 2 Distributed Systems subject at the University of Melbourne, Australia.</sub>
