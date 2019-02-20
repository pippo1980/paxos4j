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
    public void put(String uuid, VersionedData data) {
        String key = uuid;

        lock.lock();
        try {
            VersionedData exists = _storage.get(key);
            if (exists == null) {
                _storage.put(key, data);
                return;
            }

            if (data.instanceId < exists.instanceId) {
                LOGGER.warn("ignore data:{} put, because exist newer data:{}", data, exists);
                return;
            }

            _storage.put(key, data);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(String uuid) {
        _storage.remove(uuid);
    }
}
