package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.PaxosCluster;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.Proposer;
import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.LearnRQ;
import com.sirius.ds.paxos.msg.PaxosMessage;
import com.sirius.ds.paxos.msg.PrepareRS;
import com.sirius.ds.paxos.msg.VersionedData;
import com.sirius.ds.paxos.stat.Instance;
import com.sirius.ds.paxos.stat.InstanceStatus;
import com.sirius.ds.paxos.stat.InvalidInstanceStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultProposer extends DefaultWorker implements Proposer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProposer.class);

    public DefaultProposer(PaxosCluster cluster) {
        super(cluster.getCurrent().getID().nodeId, "proposer", 8);
        this.cluster = cluster;
        this.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private PaxosCluster cluster;

    @Override
    public void onMessage(PrepareRS msg) {
        onMessage((PaxosMessage) msg);
    }

    @Override
    public void onMessage(AcceptRS msg) {
        onMessage((PaxosMessage) msg);
    }

    @Override
    protected PaxosCluster getPaxosCluster() {
        return this.cluster;
    }

    @Override
    protected void process(PaxosMessage message) {
        if (message instanceof PrepareRS) {
            process((PrepareRS) message);
        } else if (message instanceof AcceptRS) {
            process((AcceptRS) message);
        }
    }

    private void process(PrepareRS msg) {
        PeerID currentId = cluster.getCurrent().getID();

        LOGGER.debug("receive prepare rs from node:{} to node:{}, the msg is:{}", msg.getPeerID(), currentId, msg);

        InstanceWAL instanceWAL = cluster.getInstanceWAL();
        long instanceId = msg.getInstanceId();
        if (!instanceWAL.exists(instanceId)) {
            throw new InvalidInstanceStatusException(String.format("can not found instance:[%s] on node:[%s]",
                    instanceId,
                    currentId));
        }

        Instance instance = instanceWAL.get(instanceId);

        AtomicBoolean ok = new AtomicBoolean(false);
        AcceptRQ acceptRQ = new AcceptRQ();
        instance.onPrepareRS(msg, _instance -> {
            if (_instance.isCommitted()) {
                return;
            }

            int count = _instance.getPrepared().size();
            if (count < cluster.getQuorum()) {
                LOGGER.debug("waiting instance prepared quorum:{}/{} at node:{}, the instance is:{}",
                        count,
                        cluster.getQuorum(),
                        currentId,
                        _instance);
                return;
            }

            if (count == cluster.getQuorum()) {
                LOGGER.debug("reach instance prepared quorum:{}/{} at node:{}, the instance is:{}",
                        count,
                        cluster.getQuorum(),
                        currentId,
                        _instance);

                _instance.setStatus(InstanceStatus.PREPARE_OK);

                acceptRQ.setPeerID(currentId);
                acceptRQ.setInstanceId(instanceId);
                acceptRQ.setBallot(_instance.getPromisedBallot());
                acceptRQ.setData(_instance.getAcceptData());
                ok.set(true);
            }

        });

        LOGGER.debug("change instance stat at node:{}, the instance is:{}", currentId, instance);
        if (ok.get()) {
            // cluster.send(msg.getPeerID(), acceptRQ);
            cluster.broadcast(acceptRQ);
        }
    }

    private void process(AcceptRS msg) {
        PeerID currentId = cluster.getCurrent().getID();

        LOGGER.debug("receive accept rs from node:{} to node:{}, the msg is:{}", msg.getPeerID(), currentId, msg);

        InstanceWAL instanceWAL = cluster.getInstanceWAL();
        long instanceId = msg.getInstanceId();
        if (!instanceWAL.exists(instanceId)) {
            LOGGER.warn("can not found instance:[{}] on node:[{}]", instanceId, currentId);
            return;
        }

        Instance instance = instanceWAL.get(instanceId);
        if (instance.isCommitted()) {
            return;
        }

        instance.onAcceptRS(msg, _instance -> {
            int count = _instance.getAccepted().size();
            if (count < cluster.getQuorum()) {
                LOGGER.debug("waiting instance accepted quorum:{}/{} at node:{}, the instance is:{}",
                        count,
                        cluster.getQuorum(),
                        currentId,
                        _instance);
                return;
            }

            if (count == cluster.getQuorum()) {
                LOGGER.debug("reach instance accepted quorum:{}/{} at node:{}, the instance is:{}",
                        count,
                        cluster.getQuorum(),
                        currentId,
                        _instance);

                VersionedData data = _instance.getAcceptData();
                data.setInstanceId(instanceId);
                cluster.getStorage().put(data.getKey(), data);
                _instance.commit();
            }
        });

        LOGGER.debug("change instance stat at node:{}, the instance is:{}", currentId, instance);
        if (instance.isCommitted()) {
            cluster.broadcast(new LearnRQ(currentId, instance.getAcceptData()));
        }
    }

}
