package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.VersionedData;

public interface DataStorage {

    VersionedData get(String uuid);

    void put(String uuid, VersionedData data);

    void remove(String uuid);

}
