package com.chat.client.service;

import com.chat.model.Message;
import com.chat.model.MessageTypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CLass [ReceiverWorker] responsible to handles the requests coming from Server
 *
 * @author srinivas
 */
public class ReceiverWorker extends Thread implements MessageTypes {

    // logger for [ReceiverWorker] class
    public static final Logger LOGGER = Logger.getLogger(ReceiverWorker.class.getName());

    // Server Connection
    private Socket serverConnection;

    // object streams
    private ObjectInputStream readFromNet;
    private ObjectOutputStream writeToNet;

    // reference to Message object
    private Message message;

    // constructor
    public ReceiverWorker(Socket serverConnection) {
        this.serverConnection = serverConnection;
        try {
            readFromNet = new ObjectInputStream(this.serverConnection.getInputStream());
            writeToNet = new ObjectOutputStream(this.serverConnection.getOutputStream());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "could not open object streams", ex.getMessage());
        }
    }

    // thread code entry point
    @Override
    public void run() {
        try {
            // read message
            message = (Message) readFromNet.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Message could not be read", ex.getMessage());
            // no use of get going
            System.exit(1);
        }

        // decide what to do depending on the type message received
        switch (message.getType())
        {
            case SHUTDOWN:
                System.out.println("Received shutdown message from Server, exiting...");
                try {
                    serverConnection.close();
                } catch (IOException e) {
                    // we don't care, going to exit anyway
                }
                System.exit(0);
                break;
            case NOTE:
                // print the note
                System.out.println((String) message.getContent());

                try {
                    serverConnection.close();
                } catch (IOException e) {
                    System.err.println("[ReceiverWorker.run] Warning: Failed to close the server connection");
                }
                break;
            default:
                LOGGER.log(Level.SEVERE, "Invalid Message Type");
        }
    }
}
