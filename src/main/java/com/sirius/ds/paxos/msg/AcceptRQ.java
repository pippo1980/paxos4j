package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;
import com.sirius.ds.paxos.PeerID;

public class AcceptRQ implements PaxosMessage {

    public AcceptRQ() {

    }

    public AcceptRQ(PeerID peerID, Proposal acceptedProposal) {
        this.peerID = peerID;
        this.acceptedProposal = acceptedProposal;
    }

    private PeerID peerID;
    private Proposal acceptedProposal;

    public PeerID getPeerID() {
        return peerID;
    }

    public Proposal getAcceptedProposal() {
        return acceptedProposal;
    }

    public void setAcceptedProposal(Proposal acceptedProposal) {
        this.acceptedProposal = acceptedProposal;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("peerID", peerID)
                .add("acceptedProposal", acceptedProposal)
                .toString();
    }
}
