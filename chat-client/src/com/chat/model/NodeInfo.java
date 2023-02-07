package com.chat.model;

import java.io.Serializable;

/**
 * Class [NodeInfo] to hold the details of Address, Port, [Name] of a host
 *
 * @author manoj
 */
public class NodeInfo  implements Serializable {

    String address;
    int port;
    String name = null;

    /**
     * Constructor with all the details
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

    // getter methods
    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    // overriding default equals and hashcode methods to override the default NodeInfo object comparison
    @Override
    public int hashCode() {
        return this.name.hashCode() + this.port;
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
