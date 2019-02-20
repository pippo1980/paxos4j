package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;
import com.sirius.ds.paxos.PeerID;

public class PrepareRQ implements PaxosMessage {

    public PrepareRQ() {

    }

    public PrepareRQ(PeerID peerID, long instanceId, int ballot) {
        this.peerID = peerID;
        this.instanceId = instanceId;
        this.ballot = ballot;
    }

    private PeerID peerID;
    private long instanceId;
    private int ballot;

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

    public int getBallot() {
        return ballot;
    }

    public void setBallot(int ballot) {
        this.ballot = ballot;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("peerID", peerID)
                .add("instanceId", instanceId)
                .add("ballot", ballot)
                .toString();
    }
}
