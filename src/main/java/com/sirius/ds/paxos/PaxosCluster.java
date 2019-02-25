package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.LearnRQ;
import com.sirius.ds.paxos.msg.LearnRS;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.PrepareRS;
import com.sirius.ds.paxos.msg.VersionedData;

public interface PaxosCluster extends PaxosService {

    int getQuorum();

    InstanceWAL getInstanceWAL();

    DataStorage getStorage();

    void broadcast(PrepareRQ prepareRQ);

    void broadcast(AcceptRQ acceptRQ);

    void broadcast(PrepareRS prepareRS);

    void broadcast(AcceptRS acceptRS);

    void broadcast(LearnRQ learnRQ);

    void send(PeerID target, PrepareRQ prepareRS);

    void send(PeerID target, PrepareRS prepareRS);

    void send(PeerID target, AcceptRQ acceptRQ);

    void send(PeerID target, AcceptRS acceptRS);

    void send(PeerID target, LearnRQ msg);

    void send(PeerID target, LearnRS learnRS);

    @Override
    default VersionedData get(String key) {
        return getStorage().get(key);
    }

    @Override
    default VersionedData remove(String key) {
        return getStorage().remove(key);
    }
}
