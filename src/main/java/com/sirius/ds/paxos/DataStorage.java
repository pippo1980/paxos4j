package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.VersionedData;

public interface DataStorage {

    VersionedData get(String uuid);

    VersionedData put(String uuid, VersionedData data);

    VersionedData remove(String uuid);

}
