package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.DataStorage;
import com.sirius.ds.paxos.msg.VersionedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MemoryDataStorage implements DataStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryDataStorage.class);
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
            if (exists == null) {
                return _storage.put(uuid, data);
            }

            if (data.instanceId < exists.instanceId) {
                LOGGER.warn("ignore data:{} put, because exist newer data:{}", data, exists);
                return data;
            }

            return _storage.put(uuid, data);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public VersionedData remove(String uuid) {
        return _storage.remove(uuid);
    }
}
