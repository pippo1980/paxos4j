package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.Proposal;

/**
 * instance work ahead log
 */
public interface InstanceWAL {

    Proposal getPeerProposal(long instanceId, PeerID peerID);

    boolean exist(long instanceId);

    Instance get(long instanceId);

    Instance create(Proposal proposal);

    Instance create(long instanceId, Proposal proposal);
}
