package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.IDGenerator;
import com.sirius.ds.paxos.PaxosCluster;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.PeerNode;
import com.sirius.ds.paxos.msg.VersionedData;
import com.sirius.ds.paxos.stat.Instance;
import com.sirius.ds.paxos.stat.InstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class BasePaxosCluster implements PaxosCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePaxosCluster.class);

    public void init(PeerNode current, Set<PeerNode> nodes) {
        this.current = current;
        this.quorum = nodes.size() / 2 + 1;

        nodes.forEach(node -> this.nodes.put(node.getID(), node));
    }

    PeerNode current;
    int quorum;
    Map<PeerID, PeerNode> nodes = new HashMap<>();

    @Override
    public PeerNode getCurrent() {
        return current;
    }

    @Override
    public int getQuorum() {
        return quorum;
    }

    @Override
    public boolean propose(String key, byte[] value, long timeout, TimeUnit timeUnit) {
        PeerID currentID = current.getID();
        long instanceId = IDGenerator.INSTANCE.nextId(currentID.nodeId);
        VersionedData data = new VersionedData(key, value);
        Instance instance = getInstanceWAL().create(instanceId, data);

        Semaphore block = new Semaphore(0);
        try {
            return prepare(instance, block, timeout, timeUnit) && accept(instance, block, timeout, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean prepare(Instance instance, Semaphore block, long timeout, TimeUnit timeUnit) throws
            InterruptedException {
        if (instance.isCommitted()) {
            return true;
        }

        instance.registerStatusListener(status -> {
            if (status.ordinal() >= InstanceStatus.PREPARE_OK.ordinal()) {
                block.release();
            }
        });

        broadcast(instance.prepare(current.getID()));

        boolean success = block.tryAcquire(timeout, timeUnit);
        if (!success) {
            LOGGER.warn("prepare instance timeout, the instance is:{}", instance);
            return prepare(instance, block, timeout, timeUnit);
        } else {
            return true;
        }

    }

    protected boolean accept(Instance instance, Semaphore block, long timeout, TimeUnit timeUnit) throws
            InterruptedException {
        if (instance.isCommitted()) {
            return true;
        }

        instance.registerStatusListener(status -> {
            System.out.println("####1" + status);
            if (status == InstanceStatus.ACCEPT_OK) {
                block.release();
            }
            System.out.println("####2" + status);
        });

        boolean success = block.tryAcquire(timeout, timeUnit);
        if (!success) {
            LOGGER.warn("accept instance timeout, the instance is:{}", instance);
        }

        return success || prepare(instance, block, timeout, timeUnit);
    }

}
