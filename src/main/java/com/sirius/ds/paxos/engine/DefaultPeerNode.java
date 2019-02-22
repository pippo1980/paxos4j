package com.sirius.ds.paxos.engine;

import com.google.common.base.MoreObjects;
import com.sirius.ds.paxos.Acceptor;
import com.sirius.ds.paxos.Learner;
import com.sirius.ds.paxos.PaxosCluster;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.PeerNode;
import com.sirius.ds.paxos.Proposer;

import java.util.Objects;

public class DefaultPeerNode implements PeerNode {

    public DefaultPeerNode(PeerID peerID) {
        this.peerID = peerID;
    }

    public void setClusterDelegate(PaxosCluster cluster) {
        this.proposer = new DefaultProposer(cluster);
        this.acceptor = new DefaultAcceptor(cluster);
        this.learner = null;
    }

    private PeerID peerID;
    private Proposer proposer;
    private Acceptor acceptor;
    private Learner learner;

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public PeerID getID() {
        return peerID;
    }

    public Proposer getProposer() {
        return proposer;
    }

    public Acceptor getAcceptor() {
        return acceptor;
    }

    public Learner getLearner() {
        return learner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultPeerNode that = (DefaultPeerNode) o;
        return Objects.equals(peerID, that.peerID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerID);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("peerID", peerID)
                .toString();
    }
}
