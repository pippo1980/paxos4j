package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;
import com.sirius.ds.paxos.PeerID;

public class LearnRQ implements PaxosMessage {

    public LearnRQ() {

    }

    public LearnRQ(PeerID peerID, VersionedData data) {
        this.peerID = peerID;
        this.data = data;
    }

    private PeerID peerID;
    private VersionedData data;

    public PeerID getPeerID() {
        return peerID;
    }

    public void setPeerID(PeerID peerID) {
        this.peerID = peerID;
    }

    public VersionedData getData() {
        return data;
    }

    public void setData(VersionedData data) {
        this.data = data;
    }

    @Override
    public long getInstanceId() {
        return data != null ? data.getInstanceId() : -1L;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("peerID", peerID)
                .add("data", data)
                .toString();
    }

}
