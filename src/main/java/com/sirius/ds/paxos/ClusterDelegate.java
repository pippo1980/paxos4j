package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.LearnRQ;
import com.sirius.ds.paxos.msg.LearnRS;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.PrepareRS;
import com.sirius.ds.paxos.msg.VersionedData;

import java.util.concurrent.TimeUnit;

public interface ClusterDelegate extends PaxosService {

    InstanceWAL getInstanceWAL();

    DataStorage getStorage();

    boolean prepare(long instanceId, long timeout, TimeUnit timeUnit);

    boolean accept(long instanceId, long timeout, TimeUnit timeUnit);

    void send(PeerNode target, PrepareRQ prepareRQ);

    void reply(PeerID target, PrepareRS prepareRS);

    void send(PeerNode target, AcceptRQ acceptRQ);

    void reply(PeerID target, AcceptRS acceptRS);

    void send(PeerNode target, LearnRQ learnRQ);

    void reply(PeerID target, LearnRS learnRS);

    PeerNode getCurrent();

    int getQuorum();

    @Override
    default VersionedData get(String key) {
        return getStorage().get(key);
    }

    @Override
    default VersionedData remove(String key) {
        return getStorage().remove(key);
    }
}
