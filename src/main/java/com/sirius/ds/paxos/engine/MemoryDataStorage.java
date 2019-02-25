package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.DataStorage;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.msg.VersionedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MemoryDataStorage implements DataStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryDataStorage.class);

    public MemoryDataStorage(PeerID peerID) {
        this.peerID = peerID;
    }

    private PeerID peerID;
    private Map<String, VersionedData> _storage = new ConcurrentHashMap<>();
    private Lock lock = new ReentrantLock();

    @Override
    public VersionedData get(String uuid) {
        return _storage.get(uuid);
    }

    @Override
    public VersionedData put(String uuid, VersionedData data) {

        lock.lock();
        try {
            VersionedData exists = _storage.get(uuid);
            if (exists == null || data.getInstanceId() > exists.getInstanceId()) {
                _storage.put(uuid, data);
                return data;
            }

            LOGGER.warn("ignore put data on node:{}, because exist newer data:{}, old data is:{}",
                    peerID,
                    data,
                    exists);
            return exists;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public VersionedData remove(String uuid) {
        return _storage.remove(uuid);
    }
}
