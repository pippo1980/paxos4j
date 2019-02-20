package com.sirius.ds.paxos;

import java.net.InetAddress;
import java.util.Objects;

public class PeerID {

    public PeerID(int nodeId, InetAddress address) {
        this.nodeId = nodeId;
        this.address = address;
    }

    public final int nodeId;
    public final InetAddress address;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PeerID peerID = (PeerID) o;
        return nodeId == peerID.nodeId &&
               Objects.equals(address, peerID.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, address);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("nodeId=").append(nodeId);
        sb.append(", address=").append(address);
        sb.append('}');
        return sb.toString();
    }
}
