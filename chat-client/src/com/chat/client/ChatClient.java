package com.chat.client;

import com.chat.model.NodeInfo;
import com.chat.client.service.Receiver;
import com.chat.client.service.Sender;
import com.chat.client.util.NetworkUtilities;
import com.chat.client.util.PropertyHandler;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chat Client class
 * Reads configuration information and starts up the ChatClient
 *
 * @author sampath
 */
public class ChatClient implements Runnable {

    // logger for [ChatServer] class
    public static final Logger LOGGER = Logger.getLogger(ChatClient.class.getName());

    // static references to receiver/sender
    static Receiver receiver;
    static Sender sender;

    // reference for Properties object
    private Properties properties = null;

    // client connectivity information
    public static NodeInfo clientNodeInfo;
    public static NodeInfo serverNodeInfo;

    // constructor
    public ChatClient(String propertiesFile) {
        try {
            // get properties from the properties file
            this.properties = new PropertyHandler(propertiesFile);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not open default properties file", ex.getMessage());
            System.exit(1);
        }

        // get client configuration
        clientNodeInfo = fetchClientInfo();
        // get server configuration
        serverNodeInfo = fetchServerInfo();
    }

    // Method to fetch Client Node Info
    private NodeInfo fetchClientInfo() {
        // get my port
        int myPort = 0;
        try {
            myPort = Integer.parseInt(properties.getProperty("MY_PORT"));
        } catch(NumberFormatException ex) {
            LOGGER.log(Level.SEVERE, "Could not read client port", ex.getMessage());
            System.exit(1);
        }

        // get my name
        String myName = properties.getProperty("MY_NAME");
        if(myName == null) {
            LOGGER.log(Level.SEVERE, "Could not read MY_NAME property");
            System.exit(1);
        }
        // create chat client default Node Info
        return new NodeInfo(NetworkUtilities.getMyIP(), myPort, myName);
    }

    // Method to fetch server Node Info
    private NodeInfo fetchServerInfo() {
        // get server default port
        int serverPort = 0;
        try {
            serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));
        } catch(NumberFormatException ex) {
            LOGGER.log(Level.SEVERE, "Could not read server port", ex.getMessage());
        }
        // get server default IP
        String serverIP = properties.getProperty("SERVER_IP");
        if(serverIP == null) {
            LOGGER.log(Level.SEVERE, "Could not read server IP");
        }

        if(serverPort != 0 && serverIP != null) {
            // create chat server default Node Info
            return new NodeInfo(serverIP, serverPort);
        } else {
            return null;
        }
    }

    // code entry point, not used for threading
    @Override
    public void run() {
        // start the Receiver
        (receiver = new Receiver()).start();
        // now start the Sender
        (sender = new Sender()).start();
    }

    // main()
    public static void main(String[] args) {
        String propertiesFile = null;

        try{
            propertiesFile = args[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            propertiesFile = "resources/DefaultChatApplicationConfig.properties";
        }

        // start the chat node
        new Thread(new ChatClient(propertiesFile)).start();
    }
}