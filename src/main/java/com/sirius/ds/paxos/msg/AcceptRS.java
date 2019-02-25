package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;
import com.sirius.ds.paxos.PeerID;

public class AcceptRS implements PaxosMessage {

    public AcceptRS() {
    }

    public AcceptRS(PeerID peerID, long instanceId, boolean acceptOK) {
        this.peerID = peerID;
        this.instanceId = instanceId;
        this.acceptOK = acceptOK;
    }

    private PeerID peerID;
    private long instanceId;
    private boolean acceptOK;

    public PeerID getPeerID() {
        return peerID;
    }

    public void setPeerID(PeerID peerID) {
        this.peerID = peerID;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public boolean isAcceptOK() {
        return acceptOK;
    }

    public void setAcceptOK(boolean acceptOK) {
        this.acceptOK = acceptOK;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("peerID", peerID)
                .add("instanceId", instanceId)
                .add("acceptOK", acceptOK)
                .toString();
    }
}
