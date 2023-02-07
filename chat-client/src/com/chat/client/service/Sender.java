package com.chat.client.service;

import com.chat.client.ChatClient;
import com.chat.model.Message;
import com.chat.model.MessageTypes;
import com.chat.model.NodeInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class [Sender] process the user input, translates the user input into [Message]
 * and sends them to the Chat Server
 *
 * @author sampath
 */
public class Sender extends Thread implements MessageTypes {

    public static final Logger LOGGER = Logger.getLogger(Sender.class.getName());

    Socket serverConnection = null;
    Scanner userInput = new Scanner(System.in);
    String inputData = null;

    // flag indicating whether we have joined chat
    boolean hasJoined;

    // object streams
    private ObjectOutputStream writeToNet;
    private ObjectInputStream readFromNet;

    // constructor
    public Sender() {
        userInput = new Scanner(System.in);
        hasJoined = false;
    }

    // thread entry point
    @Override
    public void run() {

        // until forever, unless te user enters SHUTDOWN or SHUTDOWN ALL
        while (true) {
            // get user input
            inputData = userInput.nextLine();

            if(inputData.startsWith("JOIN")) {
                // ignore if already joined
                if(hasJoined == true) {
                    System.err.println("You have already joined the chat");
                    continue;
                }

                // read server information use provided with JOIN command
                String[] connectionInfo = inputData.split("[ ]+");

                // if there is information, that may override the connectivity information
                // that was provided through properties
                try {
                    ChatClient.serverNodeInfo = new NodeInfo(connectionInfo[1], Integer.parseInt(connectionInfo[2]));
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {

                }

                // check if we have valid server information
                if(ChatClient.serverNodeInfo == null) {
                    System.err.println("[Sender].handleJoinCommand No server connectivity information found");
                    continue;
                }

                // send join request to the server
                boolean response = postDataToServer(JOIN, ChatClient.clientNodeInfo);

                // we are in
                if(response) {
                    hasJoined = true;
                    System.out.println("Joined chat...");
                }
            } else if(inputData.startsWith("LEAVE")) {
                // check if we are in the chat
                if(hasJoined == false) {
                    System.err.println("You have not joined a chat yet...");
                    continue;
                }

                // send leave request to the server
                boolean response = postDataToServer(LEAVE, ChatClient.clientNodeInfo);

                // we are out
                if(response) {
                    hasJoined = false;
                    System.out.println("Left chat..");
                }
            } else if(inputData.startsWith("SHUTDOWN ALL")) {
                // check if we are in the chat
                if(hasJoined == false) {
                    System.err.println("To shutdown the whole chat, you need to first join the chat");
                    continue;
                }

                // send shutdown all request to the server
                boolean response = postDataToServer(SHUTDOWN_ALL, ChatClient.clientNodeInfo);

                if(response) {
                    System.out.println("Sent shutdown all request...\n");
                    System.exit(0);
                }
            } else if(inputData.startsWith("SHUTDOWN")) {
                // check if we are not in chat
                if(hasJoined == true) {
                    System.err.println("To shutdown the chat, you need to first join the chat");
                    continue;
                }

                // send shutdown request to the server
                boolean response = postDataToServer(SHUTDOWN, ChatClient.clientNodeInfo);

                if(response) {
                    System.out.println("Left the chat...");
                    System.out.println("Exciting...\n");
                    System.exit(0);
                }
            } else {
                // check if we are in the chat
                if(hasJoined == false) {
                    System.err.println("To send a note, you need to first join the chat");
                    continue;
                }

                // send note request to the server
                boolean response = postDataToServer(NOTE, inputData);

                if(response) {
                    System.out.println("Message sent...\n");
                }
            }
        }
    }


    /**
     *
     * @param command
     * @param content
     *
     * @return response to indicate whether the request is success or not
     */
    private boolean postDataToServer(int command, Object content) {
        try {
            // open connection to server
            serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());

            // open object streams
            writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());
            readFromNet = new ObjectInputStream(serverConnection.getInputStream());

            // send request to the server
            if(command != NOTE) {
                writeToNet.writeObject(new Message(command, content));
            } else {
                writeToNet.writeObject(new Message(command, "Message from " + ChatClient.clientNodeInfo.getName() + "\n" + content ));
            }
            // close connection
            serverConnection.close();

            // flag to indicate that request is successful
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error Connecting to server or writing/reading object streams or closing connection",
                    ex.getMessage());
        }
        // flag to indicate that request is failed
        return false;
    }


}
