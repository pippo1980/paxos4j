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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DefaultLearner implements Learner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLearner.class);

    public DefaultLearner(PaxosCluster cluster) {
        this.cluster = cluster;

        Executors.newFixedThreadPool(4).execute(() -> {
            boolean flag = true;
            while (flag) {
                try {
                    PaxosMessage message = queue.poll(10, TimeUnit.MILLISECONDS);
                    if (message instanceof LearnRQ) {
                        process((LearnRQ) message);
                    } else if (message instanceof LearnRS) {
                        process((LearnRS) message);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("learner process termination with error", e);
                    flag = false;
                }
            }
        });
    }

    private PaxosCluster cluster;
    private BlockingQueue<PaxosMessage> queue = new LinkedBlockingQueue<>();
    private Map<Long, Consumer<LearnRS>> watchers = new ConcurrentHashMap<>();

    @Override
    public void onMessage(LearnRQ msg) {
        queue.offer(msg);
    }

    @Override
    public void onMessage(LearnRS msg) {
        queue.offer(msg);
    }

    @Override
    public void registerLearningWatcher(long instanceId, Consumer<LearnRS> watcher) {
        watchers.put(instanceId, watcher);
    }

    @Override
    public void deRegisterLearningWatcher(long instanceId) {
        watchers.remove(instanceId);
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
