package com.chat.server;

import com.chat.model.NodeInfo;
import com.chat.server.service.ChatServerWorker;
import com.chat.server.util.NetworkUtilities;
import com.chat.server.util.PropertyHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Class [ChatServer] acts as a central server for all the clients and
 * listens to the incoming traffic by creating a socket
 *
 * @author manoj
 */
public class ChatServer implements Runnable {

    // logger for [ChatServer] class
    public static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());

    // list to store the chat client's details
    public static List<NodeInfo> participants = new ArrayList<>();

    // get server IP
    String serverIP = NetworkUtilities.getMyIP();

    // server default port
    int serverPort = 5000;

    // constructor
    public ChatServer(String propertiesFile) {
        Properties properties = null;

        try {
            // get properties from the properties file
            properties = new PropertyHandler(propertiesFile);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not open default properties file", ex.getMessage());
            // unsuccessful termination of the program
            System.exit(1);
        }

        // get server default port
        try {
            serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));
        } catch(NumberFormatException ex) {
            LOGGER.log(Level.SEVERE, "Could not read server port", ex.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            // creates the server socket
            ServerSocket serverSocket = new ServerSocket(serverPort, 50, InetAddress.getByName(serverIP));
            System.out.println("Server started, listening on port " + serverIP + ":" + serverPort);
            while (true) {
                new ChatServerWorker(serverSocket.accept()).start();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Creating server socket failed", ex);
        }
    }

    // main()
    public static void main(String[] args) {
        String propertiesFile;
        try{
            propertiesFile = args[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            propertiesFile = "resources/DefaultChatApplicationConfig.properties";
        }

        // entry point for Chat Server
        new Thread(new ChatServer(propertiesFile)).start();
    }
}