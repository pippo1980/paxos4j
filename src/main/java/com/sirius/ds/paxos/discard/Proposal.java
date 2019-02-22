//package com.sirius.ds.paxos.msg;
//
//import com.google.common.base.MoreObjects;
//import com.sirius.ds.paxos.PeerID;
//
//public class Proposal {
//
//    public Proposal() {
//
//    }
//
//    public Proposal(PeerID peerID, String key, byte[] value) {
//        this.peerID = peerID;
//        this.key = key;
//        this.value = value;
//    }
//
//    public Proposal(PeerID peerID, long instanceId, int ballot, String key, byte[] value) {
//        this.peerID = peerID;
//        this.instanceId = instanceId;
//        this.ballot = ballot;
//        this.key = key;
//        this.value = value;
//    }
//
//    public void increaseBallot() {
//        this.ballot = this.ballot + 1;
//    }
//
//    protected PeerID peerID;
//    protected long instanceId;
//    protected int ballot = 0;
//    protected String key;
//    protected byte[] value;
//
//    public PeerID getPeerID() {
//        return peerID;
//    }
//
//    public void setPeerID(PeerID peerID) {
//        this.peerID = peerID;
//    }
//
//    public long getInstanceId() {
//        return instanceId;
//    }
//
//    public void setInstanceId(long instanceId) {
//        this.instanceId = instanceId;
//    }
//
//    public int getBallot() {
//        return ballot;
//    }
//
//    public void setBallot(int ballot) {
//        this.ballot = ballot;
//    }
//
//    public String getKey() {
//        return key;
//    }
//
//    public void setKey(String key) {
//        this.key = key;
//    }
//
//    public byte[] getValue() {
//        return value;
//    }
//
//    public void setData(byte[] value) {
//        this.value = value;
//    }
//
//    @Override
//    public String toString() {
//        return MoreObjects.toStringHelper(this)
//                .add("peerID", peerID)
//                .add("instanceId", instanceId)
//                .add("ballot", ballot)
//                .add("key", key)
//                .add("value", value)
//                .toString();
//    }
//}
