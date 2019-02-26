package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.Learner;
import com.sirius.ds.paxos.PaxosCluster;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.msg.LearnRQ;
import com.sirius.ds.paxos.msg.LearnRS;
import com.sirius.ds.paxos.msg.PaxosMessage;
import com.sirius.ds.paxos.msg.VersionedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DefaultLearner extends DefaultWorker implements Learner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLearner.class);

    public DefaultLearner(PaxosCluster cluster) {
        super(cluster.getCurrent().getID().nodeId, "learner", 8);
        this.cluster = cluster;
        this.start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private PaxosCluster cluster;
    private Map<Long, Consumer<LearnRS>> watchers = new ConcurrentHashMap<>();

    @Override
    public void onMessage(LearnRQ msg) {
        onMessage((PaxosMessage) msg);
    }

    @Override
    public void onMessage(LearnRS msg) {
        onMessage((PaxosMessage) msg);
    }

    @Override
    public void registerLearningWatcher(long instanceId, Consumer<LearnRS> watcher) {
        watchers.put(instanceId, watcher);
    }

    @Override
    public void deRegisterLearningWatcher(long instanceId) {
        watchers.remove(instanceId);
    }

    @Override
    protected PaxosCluster getPaxosCluster() {
        return this.cluster;
    }

    @Override
    protected void process(PaxosMessage message) {
        if (message instanceof LearnRQ) {
            process((LearnRQ) message);
        } else if (message instanceof LearnRS) {
            process((LearnRS) message);
        }
    }

    private void process(LearnRQ msg) {
        PeerID currentID = cluster.getCurrent().getID();
        LOGGER.debug("receive learn rq from node:{} to node:{}, the msg is:{}", msg.getPeerID(), currentID, msg);

        VersionedData to_learn = msg.getData();
        VersionedData learned = cluster.getStorage().put(to_learn.getKey(), to_learn);
        LearnRS learnRS = new LearnRS(currentID, learned, to_learn.getInstanceId() <= learned.getInstanceId());
        cluster.send(msg.getPeerID(), learnRS);
    }

    private void process(LearnRS msg) {
        PeerID currentID = cluster.getCurrent().getID();
        LOGGER.debug("receive learn rs from node:{} to node:{}, the msg is:{}", msg.getPeerID(), currentID, msg);
        long instanceId = msg.getLearned().getInstanceId();
        if (watchers.containsKey(instanceId)) {
            watchers.computeIfPresent(instanceId, (k, v) -> {
                v.accept(msg);
                return v;
            });
        }
    }

}
