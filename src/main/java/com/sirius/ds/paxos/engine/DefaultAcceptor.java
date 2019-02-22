package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.Acceptor;
import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.PaxosCluster;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.PaxosMessage;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.stat.Instance;
import com.sirius.ds.paxos.stat.InstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DefaultAcceptor implements Acceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAcceptor.class);

    public DefaultAcceptor(PaxosCluster cluster) {
        this.cluster = cluster;

        Executors.newFixedThreadPool(1).execute(() -> {
            while (true) {
                try {
                    PaxosMessage message = queue.poll(10, TimeUnit.MILLISECONDS);
                    if (message instanceof PrepareRQ) {
                        process((PrepareRQ) message);
                    } else if (message instanceof AcceptRQ) {
                        process((AcceptRQ) message);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private PaxosCluster cluster;
    private BlockingQueue<PaxosMessage> queue = new LinkedBlockingQueue<>();

    @Override
    public void onMessage(PrepareRQ msg) {
        // process(msg);
        queue.offer(msg);
    }

    @Override
    public void onMessage(AcceptRQ msg) {
        // process(msg);
        queue.offer(msg);
    }

    private void process(PrepareRQ msg) {
        PeerID currentId = cluster.getCurrent().getID();

        LOGGER.debug("receive prepare rq from node:{} to node:{}, the msg is:{}", msg.getPeerID(), currentId, msg);

        InstanceWAL instanceWAL = cluster.getInstanceWAL();
        Instance instance = instanceWAL.get(msg.getInstanceId());
        if (instance == null) {
            synchronized (this) {
                if (!instanceWAL.exists(msg.getInstanceId())) {
                    instance = instanceWAL.create(msg.getInstanceId(), null);
                    instance.setStatus(InstanceStatus.PREPARE);
                    LOGGER.debug("persist new instance at node:{}, the instance is:{}", currentId, instance);
                } else {
                    instance = instanceWAL.get(msg.getInstanceId());
                }
            }
        }

        cluster.send(msg.getPeerID(), instance.onPrepareRQ(currentId, msg));
        //        cluster.broadcast(instance.onPrepareRQ(currentId, msg));
    }

    private void process(AcceptRQ msg) {
        PeerID currentId = cluster.getCurrent().getID();

        LOGGER.debug("receive accept rq from node:{} to node:{}, the msg is:{}", msg.getPeerID(), currentId, msg);

        InstanceWAL instanceWAL = cluster.getInstanceWAL();
        Instance instance = instanceWAL.get(msg.getInstanceId());

        if (instance == null) {
            LOGGER.warn("ignore accept rq from node:{} to node:{}, because can not find the instance:{}",
                    msg.getPeerID(),
                    currentId,
                    msg.getInstanceId());
            return;
        }

        // cluster.broadcast(instance.onAcceptRQ(currentId, msg));
        cluster.send(msg.getPeerID(), instance.onAcceptRQ(currentId, msg));
    }

}
