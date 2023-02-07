package com.chat.client.service;

import com.chat.client.ChatClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class [Receiver] represents the receiver side of a chat client that receives
 * messages from the chat server
 *
 * @author srinivas
 */
public class Receiver extends Thread {

    // logger for [Receiver] class
    public static final Logger LOGGER = Logger.getLogger(Receiver.class.getName());

    // receiver socket
    private ServerSocket receiverSocket;

    // constructor
    public Receiver() {
        try {
            receiverSocket = new ServerSocket(ChatClient.clientNodeInfo.getPort());
            System.out.println("[Receiver.Receiver] receiver socket created, listening on port " + ChatClient.clientNodeInfo.getPort());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "creating client receiver socket failed", ex.getMessage());
        }

        System.out.println(ChatClient.clientNodeInfo.getName() + " listening on " + ChatClient.clientNodeInfo.getAddress() +
                ":" + ChatClient.clientNodeInfo.getPort());
    }

    // thread entry point
    @Override
    public void run() {
        while (true) {
            try {
                new ReceiverWorker(receiverSocket.accept()).start();
            } catch (IOException e) {
                System.err.println("[Receiver.run] warning: Error accepting the client");
            }
        }
    }
}
