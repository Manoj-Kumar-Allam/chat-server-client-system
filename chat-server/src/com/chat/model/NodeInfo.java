package com.chat.model;

import org.w3c.dom.Node;

import java.io.Serializable;

/*
 * Class to represent the Address, Port, [Name] of a host
 */
public class NodeInfo  implements Serializable {

    String address;
    int port;
    String name = null;

    /**
     * Default Constructor with all the details
     *
     * @param address
     * @param port
     * @param name
     */
    public NodeInfo(String address, int port, String name) {
        this.address = address;
        this.port = port;
        this.name = name;
    }

    /**
     * Constructor when name is null
     *
     * @param address
     * @param port
     */
    public NodeInfo(String address, int port) {
        this.address = address;
        this.port = port;
    }

    // Getter methods
    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.port + this.address.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        else {
            NodeInfo other = (NodeInfo) obj;
            if(this.name.equals(other.name))
                return true;
            else
                return false;
        }
    }
}
