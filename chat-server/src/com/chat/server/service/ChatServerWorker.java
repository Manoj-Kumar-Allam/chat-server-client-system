package com.chat.server.service;

import com.chat.server.ChatServer;
import com.chat.model.Message;
import com.chat.model.MessageTypes;
import com.chat.model.NodeInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Class [ChatServerWorker] deals with incoming messages and out going messages by
 * creating sockets and object streams to handle them
 *
 * @author manoj
 */
public class ChatServerWorker extends Thread implements MessageTypes {

    // logger for [ChatServerWorker] class
    public static final Logger LOGGER = Logger.getLogger(ChatServerWorker.class.getName());

    // chat client connection
    private Socket chatConnection;

    // object streams
    private ObjectOutputStream writeToNet;
    private ObjectInputStream readFromNet;

    // Message object to capture the client communication
    private Message message;

   // constructor
    public ChatServerWorker(Socket chatClientConnection) {
        chatConnection = chatClientConnection;
    }

    // entry point to handle both incoming and outgoing traffic
    @Override
    public void run() {
        NodeInfo participantsInfo = null;
        Iterator<NodeInfo> participantIterator;

        try {
            // open object streams
            writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
            readFromNet = new ObjectInputStream(chatConnection.getInputStream());

            // read message sent by the chat client
            message = (Message) readFromNet.readObject();

            // close the chat connection
            chatConnection.close();
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("[ChatServerWorker.run] Failed to open object streams");
            System.exit(1);
        }

        switch(message.getType()) {
            case JOIN:
                // read participant's NodeInfo
                NodeInfo joiningParticipantInfo = (NodeInfo) message.getContent();
                // add participant info to the participants' list
                ChatServer.participants.add(joiningParticipantInfo);
                // show who joined
                System.out.println(joiningParticipantInfo.getName() + " joined. All current participants: ");

                participantIterator = ChatServer.participants.iterator();
                // print out all the participants
                while (participantIterator.hasNext()) {
                    participantsInfo = participantIterator.next();
                    System.out.print(participantsInfo.getName() + " ");
                }
                System.out.println();
                break;
            case LEAVE:
            case SHUTDOWN:
                // remove this participant's info
                NodeInfo leavingParticipantInfo = (NodeInfo) message.getContent();

                if(ChatServer.participants.remove(leavingParticipantInfo)) {
                    System.err.println(leavingParticipantInfo.getName() + " removed");

                    // show who left
                    System.out.println(leavingParticipantInfo.getName() + " left. Remaining participants: ");
                } else {
                    System.err.println(leavingParticipantInfo.getName() + " not found");
                }

                // print out all the remaining participants
                participantIterator = ChatServer.participants.iterator();
                while (participantIterator.hasNext()) {
                    participantsInfo = participantIterator.next();
                    System.out.print(participantsInfo.getName() + " ");
                }
                System.out.println();
                break;
            case SHUTDOWN_ALL:
                // run through all the participants and send the note to every single participant
                participantIterator = ChatServer.participants.iterator();
                while (participantIterator.hasNext()) {
                    // get next participant
                    participantsInfo = participantIterator.next();

                    try {
                        // open socket to one chat client at a time
                        chatConnection = new Socket(participantsInfo.getAddress(), participantsInfo.getPort());

                        // open object streams
                        writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
                        readFromNet = new ObjectInputStream(chatConnection.getInputStream());

                        // send shutdown message to the client
                        writeToNet.writeObject(new Message(SHUTDOWN, null));

                        // close the connection to this client
                        chatConnection.close();

                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Error in sending SHUTDOWN request to the chat clients", ex.getMessage());
                    }
                }
                System.out.println("Shutdown all the clients, exiting..");
                // now exit myself
                System.exit(0);
            case NOTE:
                // display the note
                System.out.println((String) message.getContent());

                // run through all the participants and send the note to every single participant
                participantIterator = ChatServer.participants.iterator();

                while (participantIterator.hasNext()) {
                    // get next participant
                    participantsInfo = participantIterator.next();

                    try {
                        // open socket to one chat client at a time
                        chatConnection = new Socket(participantsInfo.getAddress(), participantsInfo.getPort());

                        // open object streams
                        writeToNet = new ObjectOutputStream(chatConnection.getOutputStream());
                        readFromNet = new ObjectInputStream(chatConnection.getInputStream());

                        // write message to the net
                        writeToNet.writeObject(message);

                        // close the connection to this client
                        chatConnection.close();

                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Error in delivering a note to clients", ex.getMessage());
                    }
                }
                break;
            default:
                System.err.println("Invalid request");

        }
    }
}
