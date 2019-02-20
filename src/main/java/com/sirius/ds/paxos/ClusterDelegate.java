package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.LearnRQ;
import com.sirius.ds.paxos.msg.LearnRS;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.PrepareRS;

import java.util.concurrent.TimeUnit;

public interface ClusterDelegate {

    boolean propose(String key, byte[] value, long timeout, TimeUnit timeUnit);

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
}
