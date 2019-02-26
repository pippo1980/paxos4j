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

public class DefaultAcceptor extends DefaultWorker implements Acceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAcceptor.class);

    public DefaultAcceptor(PaxosCluster cluster) {
        super(cluster.getCurrent().getID().nodeId, "acceptor", 8);
        this.cluster = cluster;
        this.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private PaxosCluster cluster;

    @Override
    protected PaxosCluster getPaxosCluster() {
        return this.cluster;
    }

    @Override
    public void onMessage(PrepareRQ msg) {
        onMessage((PaxosMessage) msg);
    }

    @Override
    public void onMessage(AcceptRQ msg) {
        onMessage((PaxosMessage) msg);
    }

    @Override
    protected void process(PaxosMessage message) {
        if (message instanceof PrepareRQ) {
            process((PrepareRQ) message);
        } else if (message instanceof AcceptRQ) {
            process((AcceptRQ) message);
        }
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
