package com.sirius.ds.paxos;

public interface PeerNode {

    void start() throws Exception;

    void stop() throws Exception;

    PeerID getID();

    Proposer getProposer();

    Acceptor getAcceptor();

    Learner getLearner();
}
