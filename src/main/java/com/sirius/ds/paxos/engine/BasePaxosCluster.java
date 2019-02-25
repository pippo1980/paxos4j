package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.IDGenerator;
import com.sirius.ds.paxos.PaxosCluster;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.PeerNode;
import com.sirius.ds.paxos.msg.LearnRS;
import com.sirius.ds.paxos.msg.VersionedData;
import com.sirius.ds.paxos.stat.Instance;
import com.sirius.ds.paxos.stat.InstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class BasePaxosCluster implements PaxosCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePaxosCluster.class);

    public synchronized void init(PeerNode current, Set<PeerNode> nodes) {
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
    public boolean propose(String key, byte[] value, boolean syncLearn, long timeout, TimeUnit timeUnit) {
        PeerID currentID = current.getID();
        long instanceId = IDGenerator.INSTANCE.nextId(currentID.nodeId);
        VersionedData data = new VersionedData(instanceId, key, value);
        Instance instance = getInstanceWAL().create(instanceId, data);

        LearnWatcher watcher = new LearnWatcher(syncLearn);
        current.getLearner().registerLearningWatcher(instanceId, watcher);

        try {
            Semaphore block = new Semaphore(0);
            boolean accepted =
                    prepare(instance, block, timeout, timeUnit) && accept(instance, block, timeout, timeUnit);
            if (!accepted) {
                return false;
            }

            boolean learned = watcher.await(timeout, timeUnit);
            if (learned) {
                LOGGER.debug(
                        "learn instance success, the success nodes is:{}, the fail nodes is:{}, the instance is:{}",
                        watcher.learn_success,
                        watcher.learn_fail,
                        instance);
            } else {
                LOGGER.warn("learn instance fail, the success nodes is:{}, the fail nodes is:{}, the instance is:{}",
                        watcher.learn_success,
                        watcher.learn_fail,
                        instance);
            }

            return learned;
        } catch (InterruptedException e) {
            LOGGER.error("propose due to error", e);
            return false;
        } finally {
            current.getLearner().deRegisterLearningWatcher(instanceId);
        }
    }

    protected boolean prepare(Instance instance, Semaphore block, long timeout, TimeUnit timeUnit) throws
            InterruptedException {
        if (instance.isCommitted()) {
            return true;
        }

        instance.registerStatusWatcher(status -> {
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
            LOGGER.debug("prepare instance success, the instance is:{}", instance);
            return true;
        }
    }

    protected boolean accept(Instance instance, Semaphore block, long timeout, TimeUnit timeUnit) throws
            InterruptedException {
        if (instance.isCommitted()) {
            return true;
        }

        instance.registerStatusWatcher(status -> {
            if (status == InstanceStatus.ACCEPT_OK) {
                block.release();
            }
        });

        boolean success = block.tryAcquire(timeout, timeUnit);
        if (!success) {
            LOGGER.warn("accept instance timeout, the instance is:{}", instance);
        } else {
            LOGGER.debug("accept instance success, the instance is:{}", instance);
        }

        return success || prepare(instance, block, timeout, timeUnit);
    }

    class LearnWatcher implements Consumer<LearnRS> {

        LearnWatcher(boolean sync) {
            this.sync = sync;
        }

        boolean sync;
        Set<PeerID> learn_success = new HashSet<>();
        Set<PeerID> learn_fail = new HashSet<>();
        CountDownLatch block = new CountDownLatch(quorum);

        boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
            return !sync || block.await(timeout, timeUnit);
        }

        @Override
        public void accept(LearnRS rs) {
            if (rs.isLearnOK()) {
                learn_success.add(rs.getPeerID());
                block.countDown();
            } else {
                learn_fail.add(rs.getPeerID());
                LOGGER.warn("instance learn fail at node:{}, the instance is:{}",
                        rs.getPeerID(),
                        rs.getLearned().getInstanceId());
            }
        }
    }
}
