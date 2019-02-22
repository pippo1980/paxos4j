//package com.sirius.ds.paxos.discard;
//
//import com.sirius.ds.paxos.ClusterDelegate;
//import com.sirius.ds.paxos.stat.InstanceStatus;
//import com.sirius.ds.paxos.Learner;
//import com.sirius.ds.paxos.msg.LearnRQ;
//import com.sirius.ds.paxos.msg.LearnRS;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.concurrent.atomic.AtomicBoolean;
//
//public class DefaultLearner implements Learner {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLearner.class);
//
//    public DefaultLearner(ClusterDelegate clusterDelegate) {
//        this.clusterDelegate = clusterDelegate;
//    }
//
//    private ClusterDelegate clusterDelegate;
//
//    @Override
//    public void onMessage(LearnRQ msg) {
//        LOGGER.debug("receive learn rq:{} from node:{}", msg, clusterDelegate.getCurrent().getID());
//        if (msg.getPeerID().equals(clusterDelegate.getCurrent().getID())) {
//            // TODO logger
//            return;
//        }
//
//        Instance instance = clusterDelegate.getInstanceWAL().get(msg.getData().instanceId);
//        if (instance == null) {
//            LOGGER.warn("ignore learn rq:{}, because can not find instance at node:{}",
//                    msg,
//                    clusterDelegate.getCurrent().getID());
//            return;
//        }
//
//        AtomicBoolean learn = new AtomicBoolean(false);
//        instance.read(_instance -> {
//            learn.set(_instance.getStatus() == InstanceStatus.ACCEPT);
//            return _instance;
//        });
//
//        if (learn.get()) {
//            instance.write(_instance -> {
//
//                _instance.setStatus(InstanceStatus.COMMIT);
//                _instance.getProposal().setData(msg.getData().payload);
//                clusterDelegate.getStorage().put(msg.getUuid(), msg.getData());
//
//                LOGGER.debug("learn proposal:{} at node:{}",
//                        instance.getProposal(),
//                        clusterDelegate.getCurrent().getID());
//
//                return _instance;
//            });
//        } else {
//            LOGGER.debug("ignore learn instance:{}, because the status is not accept or committed", instance);
//        }
//    }
//
//    @Override
//    public void onMessage(LearnRS msg) {
//
//    }
//}
