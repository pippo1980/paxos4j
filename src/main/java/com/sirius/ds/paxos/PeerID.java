package com.sirius.ds.paxos;

import java.util.Objects;

public class PeerID {

    public PeerID(int nodeId, String host, int port) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
    }

    public final int nodeId;
    public final String host;
    public final int port;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PeerID peerID = (PeerID) o;
        return nodeId == peerID.nodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PeerID{");
        sb.append("nodeId=").append(nodeId);
        sb.append('}');
        return sb.toString();
    }
}
