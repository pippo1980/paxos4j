package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.DataStorage;
import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.LearnRQ;
import com.sirius.ds.paxos.msg.LearnRS;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.PrepareRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryPaxosCluster extends BasePaxosCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryPaxosCluster.class);

    public MemoryPaxosCluster(InstanceWAL instanceWAL, DataStorage storage) {
        this.instanceWAL = instanceWAL;
        this.storage = storage;
    }

    InstanceWAL instanceWAL;
    DataStorage storage;

    @Override
    public InstanceWAL getInstanceWAL() {
        return instanceWAL;
    }

    @Override
    public DataStorage getStorage() {
        return storage;
    }

    @Override
    public void broadcast(PrepareRQ msg) {
        LOGGER.debug("broadcast prepare rq from node:{}, the msg is:{}", current.getID(), msg);
        nodes.values().forEach(node -> {
            send(node.getID(), msg);
        });
    }

    @Override
    public void broadcast(PrepareRS msg) {
        LOGGER.debug("broadcast prepare rs from node:{}, the msg is:{}", current.getID(), msg);
        nodes.values().forEach(node -> {
            send(node.getID(), msg);
        });
    }

    @Override
    public void broadcast(AcceptRQ msg) {
        LOGGER.debug("broadcast accept rq from node:{}, the msg is:{}", current.getID(), msg);
        nodes.values().forEach(node -> {
            send(node.getID(), msg);
        });
    }

    @Override
    public void broadcast(AcceptRS msg) {
        LOGGER.debug("broadcast accept rs from node:{}, the msg is:{}", current.getID(), msg);
        nodes.values().forEach(node -> {
            send(node.getID(), msg);
        });
    }

    @Override
    public void broadcast(LearnRQ msg) {
        LOGGER.debug("broadcast learn rq from node:{} to node:{}, the msg is:{}", current.getID(), msg);
        nodes.values().forEach(node -> {
            send(node.getID(), msg);
        });
    }

    @Override
    public void send(PeerID target, PrepareRQ msg) {
        LOGGER.debug("send prepare rq from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
        try {
            nodes.get(target).getAcceptor().onMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(PeerID target, PrepareRS msg) {
        LOGGER.debug("send prepare rs from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
        try {
            nodes.get(target).getProposer().onMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(PeerID target, AcceptRQ msg) {
        LOGGER.debug("send accept rq from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
        try {
            nodes.get(target).getAcceptor().onMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(PeerID target, AcceptRS msg) {
        LOGGER.debug("send accept rs from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
        try {
            nodes.get(target).getProposer().onMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(PeerID target, LearnRQ msg) {
        LOGGER.debug("send learn rs from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
        try {
            nodes.get(target).getLearner().onMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(PeerID target, LearnRS msg) {
        LOGGER.debug("send learn rs from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
        try {
            nodes.get(target).getLearner().onMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
