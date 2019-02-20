package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.VersionedData;

import java.util.concurrent.TimeUnit;

public interface PaxosService {

    boolean propose(String key, byte[] value, long timeout, TimeUnit timeUnit);

    VersionedData get(String key);

    VersionedData remove(String key);

}
