package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;
import com.sirius.ds.paxos.PeerID;

public class LearnRS implements PaxosMessage {

    public LearnRS() {

    }

    public LearnRS(PeerID peerID, VersionedData learned, boolean learnOK) {
        this.peerID = peerID;
        this.learned = learned;
        this.learnOK = learnOK;
    }

    private PeerID peerID;
    private VersionedData learned;
    private boolean learnOK;

    public PeerID getPeerID() {
        return peerID;
    }

    public void setPeerID(PeerID peerID) {
        this.peerID = peerID;
    }

    public VersionedData getLearned() {
        return learned;
    }

    public void setLearned(VersionedData learned) {
        this.learned = learned;
    }

    public boolean isLearnOK() {
        return learnOK;
    }

    public void setLearnOK(boolean learnOK) {
        this.learnOK = learnOK;
    }

    @Override
    public long getInstanceId() {
        return learned != null ? learned.getInstanceId() : -1;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("peerID", peerID)
                .add("learned", learned)
                .add("learnOK", learnOK)
                .toString();
    }

}
