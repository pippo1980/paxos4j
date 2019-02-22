//package com.sirius.ds.paxos.msg;
//
//import com.sirius.ds.paxos.PeerID;
//
//public class ImmutablePropose extends Proposal {
//
//    public ImmutablePropose(Proposal proposal) {
//        this.instanceId = proposal.instanceId;
//        this.ballot = proposal.ballot;
//        this.peerID = proposal.peerID;
//        this.key = proposal.key;
//        this.value = proposal.value;
//    }
//
//    @Override
//    public void setPeerID(PeerID peerID) {
//        throw new RuntimeException("not writable method");
//    }
//
//    @Override
//    public void setInstanceId(long instanceId) {
//        throw new RuntimeException("not writable method");
//    }
//
//    @Override
//    public void setBallot(int ballot) {
//        throw new RuntimeException("not writable method");
//    }
//
//    @Override
//    public void setKey(String key) {
//        throw new RuntimeException("not writable method");
//    }
//
//    @Override
//    public void setData(byte[] value) {
//        throw new RuntimeException("not writable method");
//    }
//}
