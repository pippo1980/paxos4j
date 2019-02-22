package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.DataStorage;
import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.PeerNode;
import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.PrepareRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MockPaxosCluster extends BasePaxosCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockPaxosCluster.class);

    public MockPaxosCluster(InstanceWAL instanceWAL, DataStorage storage) {
        this.instanceWAL = instanceWAL;
        this.storage = storage;
    }

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(32);
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
        executor.execute(() -> {
            for (PeerNode node : nodes.values()) {
                LOGGER.debug("send prepare rq from node:{} to node:{}, the msg is:{}",
                        current.getID(),
                        node.getID(),
                        msg);
                node.getAcceptor().onMessage(msg);
            }
        });
    }

    @Override
    public void broadcast(PrepareRS msg) {
        executor.execute(() -> {
            for (PeerNode node : nodes.values()) {
                LOGGER.debug("send prepare rs from node:{} to node:{}, the msg is:{}",
                        current.getID(),
                        node.getID(),
                        msg);
                node.getProposer().onMessage(msg);
            }
        });
    }

    @Override
    public void broadcast(AcceptRQ msg) {
        executor.execute(() -> {
            for (PeerNode node : nodes.values()) {

                LOGGER.debug("send accept rq from node:{} to node:{}, the msg is:{}",
                        current.getID(),
                        node.getID(),
                        msg);
                node.getAcceptor().onMessage(msg);
            }
        });

    }

    @Override
    public void broadcast(AcceptRS msg) {
        executor.execute(() -> {
            for (PeerNode node : nodes.values()) {

                LOGGER.debug("send accept rs from node:{} to node:{}, the msg is:{}",
                        current.getID(),
                        node.getID(),
                        msg);
                node.getProposer().onMessage(msg);
            }
        });
    }

    @Override
    public void send(PeerID target, PrepareRS msg) {
        executor.execute(() -> {
            LOGGER.debug("send prepare rs from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
            nodes.get(target).getProposer().onMessage(msg);
        });
    }

    @Override
    public void send(PeerID target, AcceptRQ msg) {
        executor.execute(() -> {
            LOGGER.debug("send accept rq from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
            nodes.get(target).getAcceptor().onMessage(msg);
        });
    }

    @Override
    public void send(PeerID target, AcceptRS msg) {
        executor.execute(() -> {
            LOGGER.debug("send accept rs from node:{} to node:{}, the msg is:{}", current.getID(), target, msg);
            nodes.get(target).getProposer().onMessage(msg);
        });
    }

    //    @Override
//    public void send(PeerNode target, PrepareRQ prepareRQ) {
//        LOGGER.debug("send prepare rq:{} from node:{} to node:{}", prepareRQ, current.getID(), target.getID());
//        target.getAcceptor().onMessage(prepareRQ);
//    }
//
//    @Override
//    public void send(PeerNode target, AcceptRQ acceptRQ) {
//        LOGGER.debug("send accept rq:{} from node:{} to node:{}", acceptRQ, current.getID(), target.getID());
//        target.getAcceptor().onMessage(acceptRQ);
//    }
//
//    @Override
//    public void reply(PeerID target, PrepareRS msg) {
//        for (PeerNode node : nodes) {
//            if (!target.equals(node.getID())) {
//                continue;
//            }
//
//            executor.submit(() -> {
//                LOGGER.debug("reply prepare rs:{} from node:{} to node:{}", msg, current.getID(), target);
//                node.getProposer().onMessage(msg);
//            });
//        }
//    }
//
//    @Override
//    public void reply(PeerID target, AcceptRS msg) {
//        for (PeerNode node : nodes) {
//            if (!target.equals(node.getID())) {
//                continue;
//            }
//
//            executor.submit(() -> {
//                LOGGER.debug("reply accept rs:{} from node:{} to node:{}", msg, current.getID(), target);
//                node.getProposer().onMessage(msg);
//            });
//        }
//    }
//
//    @Override
//    public void send(PeerNode target, LearnRQ msg) {
//        LOGGER.debug("send learn rq:{} from node:{} to node:{}", msg, current.getID(), target);
//        target.getLearner().onMessage(msg);
//    }
//
//    @Override
//    public void reply(PeerID target, LearnRS learnRS) {
//
//    }
}
