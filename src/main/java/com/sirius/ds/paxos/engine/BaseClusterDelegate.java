package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.ClusterDelegate;
import com.sirius.ds.paxos.DataStorage;
import com.sirius.ds.paxos.Instance;
import com.sirius.ds.paxos.InstanceStatus;
import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.PeerNode;
import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.LearnRQ;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.Proposal;
import com.sirius.ds.paxos.msg.VersionedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public abstract class BaseClusterDelegate implements ClusterDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClusterDelegate.class);

    public void init(PeerNode current, Set<PeerNode> nodes) {
        this.current = current;
        this.nodes = nodes;
        this.peerCount = nodes.size();
        this.quorum = this.peerCount / 2 + 1;
    }

    @Override
    public boolean propose(String key, byte[] value, long timeout, TimeUnit timeUnit) {
        Proposal proposal = new Proposal(current.getID(), key, value);
        Instance instance = instanceWAL.create(proposal);

        long instanceId = instance.getInstanceId();
        boolean success = prepare(instanceId, timeout, timeUnit);

        if (success) {
            success = accept(instanceId, timeout, timeUnit);
            if (success) {
                learn(instanceId);
            }
        }

        return success;
    }

    @Override
    public boolean prepare(long instanceId, long timeout, TimeUnit timeUnit) {
        if (!instanceWAL.exist(instanceId)) {
            // TODO logger
            return false;
        }

        // 乐观检查
        if (isExcept(instanceWAL.get(instanceId).getStatus(), InstanceStatus.ACCEPT, InstanceStatus.COMMIT)) {
            return true;
        }

        ScheduledFuture<Boolean> future = executor.schedule(new InstanceStatusWatcher(instanceId,
                InstanceStatus.ACCEPT,
                InstanceStatus.COMMIT), timeout, timeUnit);

        // 发送prepare消息
        final PrepareRQ msg = new PrepareRQ(current.getID(), instanceId, -1);
        final AtomicBoolean send = new AtomicBoolean(false);

        instanceWAL.get(instanceId).write(new MessageInitFunc4PrepareRQ(future, msg, send));
        if (send.get()) {
            send(msg);
        }

        try {
            return future.get() || doPrepare(instanceId, timeout, timeUnit);
        } catch (CancellationException e) {
            LOGGER.debug("the instance:{} status is changed", instanceWAL.get(instanceId));
            return true;
        } catch (Throwable e) {
            LOGGER.error("prepare instance:{} due to error:{}", instanceId, e);
            return isExcept(instanceWAL.get(instanceId).getStatus(),
                    InstanceStatus.ACCEPT,
                    InstanceStatus.COMMIT) || doPrepare(instanceId, timeout, timeUnit);
        } finally {
            instanceWAL.get(instanceId).write(_instance -> {
                _instance.deRegisterCallback();
                return _instance;
            });
        }
    }

    private void send(PrepareRQ msg) {
        for (PeerNode node : nodes) {
            executor.submit(() -> send(node, msg));
        }
    }

    private boolean doPrepare(long instanceId, long timeout, TimeUnit timeUnit) {
        instanceWAL.get(instanceId).write(ballotIncreaseFunc);
        return prepare(instanceId, timeout, timeUnit);
    }

    @Override
    public boolean accept(long instanceId, long timeout, TimeUnit timeUnit) {
        if (!instanceWAL.exist(instanceId)) {
            // TODO logger
            return false;
        }

        // 乐观检查
        if (instanceWAL.get(instanceId).getStatus() == InstanceStatus.COMMIT) {
            return true;
        }

        ScheduledFuture<Boolean> future = executor.schedule(new InstanceStatusWatcher(instanceId,
                InstanceStatus.COMMIT), timeout, timeUnit);
        AcceptRQ msg = new AcceptRQ(current.getID(), null);
        AtomicBoolean send = new AtomicBoolean(false);
        instanceWAL.get(instanceId).write(new MessageInitFunc4AcceptRQ(future, msg, send));

        if (send.get()) {
            send(msg);
        }

        try {
            return future.get() || doAccept(instanceId, timeout, timeUnit);
        } catch (CancellationException e) {
            LOGGER.debug("the instance:{} status is changed", instanceWAL.get(instanceId));
            return true;
        } catch (Throwable e) {
            LOGGER.error("accept instance:{} due to error:{}", instanceId, e);
            return isExcept(instanceWAL.get(instanceId).getStatus(), InstanceStatus.COMMIT) ||
                   doAccept(instanceId, timeout, timeUnit);
        } finally {
            instanceWAL.get(instanceId).write(_instance -> {
                _instance.deRegisterCallback();
                return _instance;
            });
        }
    }

    private boolean doAccept(long instanceId, long timeout, TimeUnit timeUnit) {
        instanceWAL.get(instanceId).write(ballotIncreaseFunc);
        return accept(instanceId, timeout, timeUnit);
    }

    private void send(AcceptRQ msg) {
        for (PeerNode node : nodes) {
            executor.submit(() -> send(node, msg));
        }
    }

    public void learn(long instanceId) {
        LearnRQ msg = new LearnRQ();
        instanceWAL.get(instanceId).read(_instance -> {
            msg.setPeerID(current.getID());
            msg.setUuid(_instance.getProposal().getKey());
            msg.setData(new VersionedData(instanceId, _instance.getProposal().getValue()));
            return _instance;
        });

        for (PeerNode node : nodes) {
            executor.submit(() -> send(node, msg));
        }
    }

    protected ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);
    protected InstanceWAL instanceWAL;
    protected DataStorage storage;
    protected PeerNode current;
    protected Set<PeerNode> nodes;
    protected int peerCount;
    protected int quorum;

    @Override
    public InstanceWAL getInstanceWAL() {
        return instanceWAL;
    }

    @Override
    public DataStorage getStorage() {
        return storage;
    }

    @Override
    public PeerNode getCurrent() {
        return current;
    }

    @Override
    public int getQuorum() {
        return quorum;
    }

    protected boolean isExcept(InstanceStatus target, InstanceStatus... expect) {
        for (InstanceStatus instanceStatus : expect) {
            if (target == instanceStatus) {
                return true;
            }
        }

        return false;
    }

    protected Function<Instance, Instance> ballotIncreaseFunc = _instance -> {
        Proposal proposal = _instance.getProposal();
        proposal.setBallot(_instance.getProposal().getBallot() + 1);
        return _instance;
    };

    protected class InstanceStatusWatcher implements Callable<Boolean> {

        protected InstanceStatusWatcher(long instanceId, InstanceStatus... expect) {
            this.instanceId = instanceId;
            this.expect = expect;
        }

        long instanceId;
        InstanceStatus[] expect;

        @Override
        public Boolean call() {
            if (isExcept(getInstanceWAL().get(instanceId).getStatus(), expect)) {
                return true;
            }

            AtomicBoolean success = new AtomicBoolean(false);
            getInstanceWAL().get(instanceId).read(_instance -> {
                success.set(isExcept(_instance.getStatus(), expect));
                return _instance;
            });

            return success.get();
        }

    }
}
