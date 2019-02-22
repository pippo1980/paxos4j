package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.Acceptor;
import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.PaxosCluster;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.stat.Instance;
import com.sirius.ds.paxos.stat.InstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAcceptor implements Acceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAcceptor.class);

    public DefaultAcceptor(PaxosCluster cluster) {
        this.cluster = cluster;
    }

    private PaxosCluster cluster;

    @Override
    public void onMessage(PrepareRQ msg) {
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

    @Override
    public void onMessage(AcceptRQ msg) {
        PeerID currentId = cluster.getCurrent().getID();

        LOGGER.debug("receive accept rq from node:{} to node:{}, the msg is:{}", msg.getPeerID(), currentId, msg);

        InstanceWAL instanceWAL = cluster.getInstanceWAL();
        Instance instance = instanceWAL.get(msg.getInstanceId());

        if (instance == null) {
            LOGGER.warn("ignore accept rq from node:{} to node:{}, because can not find the instance:{}",
                    msg.getPeerID(),
                    currentId,
                    instance);
            return;
        }

//        if (instance.getAccepted().contains(msg.getPeerID())) {
//            cluster.send(msg.getPeerID(), new AcceptRS(currentId, instance.getInstanceId(), true));
//        } else {
        System.out.println("#@$@#$");
        cluster.broadcast(instance.onAcceptRQ(currentId, msg));
//        }
    }

}
