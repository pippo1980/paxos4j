package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.msg.VersionedData;
import com.sirius.ds.paxos.stat.Instance;
import com.sirius.ds.paxos.stat.InstanceStatMachine;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryInstanceWAL implements InstanceWAL {

    public MemoryInstanceWAL() {

        AtomicLong lastId = new AtomicLong(0);

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {

            TreeSet<Long> ids = new TreeSet<>(Long::compareTo);
            ids.addAll(instances.keySet());

            SortedSet<Long> _ids = ids.tailSet(lastId.get());
            for (Long id : _ids) {
                Instance instance = instances.get(id);
                if (instance.isCommitted()) {
                    instance.clean();
                    lastId.set(id);
                } else {
                    break;
                }
            }

        }, 1, 1, TimeUnit.SECONDS);

    }

    private Map<Long, Instance> instances = new ConcurrentHashMap<>();
    private Map<String, Long> index = new ConcurrentHashMap<>();

    @Override
    public boolean exists(long instanceId) {
        return instances.containsKey(instanceId);
    }

    @Override
    public Instance get(long instanceId) {
        return instances.get(instanceId);
    }

    @Override
    public Instance create(long instanceId, VersionedData data) {
        return newInstance(instanceId, data);
    }

    private Instance newInstance(long instanceId, VersionedData data) {
        Instance instance = new InstanceStatMachine(instanceId, data);
        instances.put(instanceId, instance);
        return instance;
    }

}
