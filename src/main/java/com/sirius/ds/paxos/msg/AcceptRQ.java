package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;
import com.sirius.ds.paxos.PeerID;

public class AcceptRQ implements PaxosMessage {

    public AcceptRQ() {

    }

    public AcceptRQ(PeerID peerID, long instanceId, int ballot, VersionedData value) {
        this.peerID = peerID;
        this.instanceId = instanceId;
        this.ballot = ballot;
        this.value = value;
    }

    private PeerID peerID;
    private long instanceId;
    private int ballot;
    private VersionedData value;

    public PeerID getPeerID() {
        return peerID;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public int getBallot() {
        return ballot;
    }

    public VersionedData getValue() {
        return value;
    }

    public void setPeerID(PeerID peerID) {
        this.peerID = peerID;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public void setBallot(int ballot) {
        this.ballot = ballot;
    }

    public void setData(VersionedData value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("peerID", peerID)
                .add("instanceId", instanceId)
                .add("ballot", ballot)
                .add("value", value)
                .toString();
    }
}
