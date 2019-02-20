package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;
import com.sirius.ds.paxos.PeerID;

public class LearnRQ {

    public LearnRQ() {

    }

    public LearnRQ(PeerID peerID, String uuid, VersionedData data) {
        this.peerID = peerID;
        this.uuid = uuid;
        this.data = data;
    }

    private PeerID peerID;
    private String uuid;
    private VersionedData data;

    public PeerID getPeerID() {
        return peerID;
    }

    public void setPeerID(PeerID peerID) {
        this.peerID = peerID;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public VersionedData getData() {
        return data;
    }

    public void setData(VersionedData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("peerID", peerID)
                .add("data", data)
                .toString();
    }
}
