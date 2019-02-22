package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.DataStorage;
import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.PrepareRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockPaxosCluster extends BasePaxosCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockPaxosCluster.class);

    public MockPaxosCluster(InstanceWAL instanceWAL, DataStorage storage) {
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
        nodes.values().forEach(node -> {
            LOGGER.debug("send prepare rq from node:{} to node:{}, the msg is:{}",
                    current.getID(),
                    node.getID(),
                    msg);
            node.getAcceptor().onMessage(msg);
        });
    }

    @Override
    public void broadcast(PrepareRS msg) {
        nodes.values().forEach(node -> {
            LOGGER.debug("send prepare rs from node:{} to node:{}, the msg is:{}",
                    current.getID(),
                    node.getID(),
                    msg);
            node.getProposer().onMessage(msg);
        });
    }

    @Override
    public void broadcast(AcceptRQ msg) {
        nodes.values().forEach(node -> {
            LOGGER.debug("send accept rq from node:{} to node:{}, the msg is:{}",
                    current.getID(),
                    node.getID(),
                    msg);
            node.getAcceptor().onMessage(msg);
        });
    }

    @Override
    public void broadcast(AcceptRS msg) {
        nodes.values().forEach(node -> {
            LOGGER.debug("send accept rs from node:{} to node:{}, the msg is:{}",
                    current.getID(),
                    node.getID(),
                    msg);
            node.getProposer().onMessage(msg);
        });
    }

    @Override
    public void send(PeerID target, PrepareRS msg) {
        LOGGER.debug("send prepare rs from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
        nodes.get(target).getProposer().onMessage(msg);
    }

    @Override
    public void send(PeerID target, AcceptRQ msg) {
        LOGGER.debug("send accept rq from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
        nodes.get(target).getAcceptor().onMessage(msg);
    }

    @Override
    public void send(PeerID target, AcceptRS msg) {
        LOGGER.debug("send accept rs from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
        nodes.get(target).getProposer().onMessage(msg);
    }

}
