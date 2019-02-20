package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;
import com.sirius.ds.paxos.PeerID;

public class PrepareRS {

    public PrepareRS() {

    }

    public PrepareRS(PeerID peerID, long instanceId, int preparedBallot, Proposal preparedProposal, boolean prepareOK) {
        this.peerID = peerID;
        this.instanceId = instanceId;
        this.preparedBallot = preparedBallot;
        this.preparedProposal = preparedProposal;
        this.prepareOK = prepareOK;
    }

    private PeerID peerID;
    private long instanceId;
    private int preparedBallot;
    private Proposal preparedProposal;
    private boolean prepareOK;

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

    public int getPreparedBallot() {
        return preparedBallot;
    }

    public void setPreparedBallot(int preparedBallot) {
        this.preparedBallot = preparedBallot;
    }

    public Proposal getPreparedProposal() {
        return preparedProposal;
    }

    public void setPreparedProposal(Proposal preparedProposal) {
        this.preparedProposal = preparedProposal;
    }

    public boolean isPrepareOK() {
        return prepareOK;
    }

    public void setPrepareOK(boolean prepareOK) {
        this.prepareOK = prepareOK;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("peerID", peerID)
                .add("instanceId", instanceId)
                .add("preparedBallot", preparedBallot)
                .add("preparedProposal", preparedProposal)
                .add("prepareOK", prepareOK)
                .toString();
    }
}
