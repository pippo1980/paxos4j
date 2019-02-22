package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.VersionedData;

import java.util.concurrent.TimeUnit;

public interface PaxosService {

    PeerNode getCurrent();

    boolean propose(String key, byte[] value, boolean syncLearn, long timeout, TimeUnit timeUnit);

    VersionedData get(String key);

    VersionedData remove(String key);

}
