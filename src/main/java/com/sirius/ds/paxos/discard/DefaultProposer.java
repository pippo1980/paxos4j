//package com.sirius.ds.paxos.discard;
//
//import com.sirius.ds.paxos.ClusterDelegate;
//import com.sirius.ds.paxos.InstanceException;
//import com.sirius.ds.paxos.stat.InstanceStatus;
//import com.sirius.ds.paxos.InstanceWAL;
//import com.sirius.ds.paxos.PeerID;
//import com.sirius.ds.paxos.Proposer;
//import com.sirius.ds.paxos.msg.AcceptRS;
//import com.sirius.ds.paxos.msg.PrepareRS;
//import com.sirius.ds.paxos.msg.Proposal;
//import com.sirius.ds.paxos.msg.VersionedData;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class DefaultProposer implements Proposer {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProposer.class);
//
//    public DefaultProposer(ClusterDelegate clusterDelegate) {
//        this.clusterDelegate = clusterDelegate;
//    }
//
//    private ClusterDelegate clusterDelegate;
//
//    @Override
//    public void onMessage(PrepareRS msg) {
//        LOGGER.debug("receive prepare rs:{} from node:{} to node:{}",
//                msg,
//                msg.getPeerID(),
//                clusterDelegate.getCurrent().getID());
//
//        InstanceWAL instanceWAL = clusterDelegate.getInstanceWAL();
//        if (!instanceWAL.exist(msg.getInstanceId())) {
//            throw new InstanceException(String.format("can not found instance:[%s] on node:[%s]",
//                    msg.getInstanceId(),
//                    clusterDelegate.getCurrent()));
//        }
//
//        instanceWAL.get(msg.getInstanceId()).write(_instance -> {
//
//            if (!msg.isPrepareOK()) {
//                LOGGER.debug("forbidden prepare msg:{} at node:{}", msg, clusterDelegate.getCurrent().getID());
//                return _instance;
//            }
//
//            _instance.getPreparePromised().add(msg.getPeerID());
//
//            if (_instance.getStatus() != InstanceStatus.PREPARE) {
//                LOGGER.debug("ignore prepare rs:{}, because the instance:{} status is not prepare", msg, _instance);
//                return _instance;
//            }
//
//            // if remote peer prepared ballot great then my proposal ballot, accept their proposal ballot and value
//            Proposal proposal = _instance.getProposal();
//            if (msg.getAcceptBallot() > _instance.getProposal().getBallot()) {
//                proposal.setBallot(msg.getAcceptBallot());
//                proposal.setData(msg.getPreparedProposal().getValue());
//            }
//
//            // if prepared ballot with quorum node, then accept instance proposal
//            int preparedCount = _instance.getPreparePromised().size();
//            if (preparedCount < clusterDelegate.getQuorum()) {
//                LOGGER.debug("waiting proposal:{} reach prepared quorum:{}/{}",
//                        proposal,
//                        preparedCount,
//                        clusterDelegate.getQuorum());
//                return _instance;
//            } else if (preparedCount == clusterDelegate.getQuorum()) {
//                // if remote peer accept ballot but no proposal, use current peer node proposal;
//                PeerID peerID = clusterDelegate.getCurrent().getID();
//                if (proposal == null) {
//                    proposal = instanceWAL.getPeerProposal(_instance.getInstanceId(), peerID);
//                    _instance.setProposal(proposal);
//                }
//
//                _instance.setStatus(InstanceStatus.ACCEPT);
//                LOGGER.debug("chosen proposal:{} at node:{}", proposal, clusterDelegate.getCurrent().getID());
//            }
//
//            return _instance;
//        });
//    }
//
//    @Override
//    public void onMessage(AcceptRS msg) {
//        LOGGER.debug("receive accept rs:{} from node:{} to node:{}",
//                msg,
//                msg.getPeerID(),
//                clusterDelegate.getCurrent().getID());
//
//        InstanceWAL instanceWAL = clusterDelegate.getInstanceWAL();
//        if (!instanceWAL.exist(msg.getInstanceId())) {
//            throw new InstanceException(String.format("can not found instance:[%s] on node:[%s]",
//                    msg.getInstanceId(),
//                    clusterDelegate.getCurrent()));
//        }
//
//        instanceWAL.get(msg.getInstanceId()).write(_instance -> {
//            if (!msg.isAcceptOK()) {
//                LOGGER.debug("forbidden accept msg:{} at node:{}", msg, clusterDelegate.getCurrent().getID());
//                return _instance;
//            }
//
//            _instance.getAcceptPromised().add(msg.getPeerID());
//
//            if (_instance.getStatus() != InstanceStatus.ACCEPT) {
//                LOGGER.debug("ignore accept rs:{}, because the instance:{} status is not accept", msg, _instance);
//                return _instance;
//            }
//
//            // if accepted with quorum node, then commit current proposal to data storage
//            Proposal proposal = _instance.getProposal();
//            int acceptedCount = _instance.getAcceptPromised().size();
//            if (acceptedCount >= clusterDelegate.getQuorum()) {
//                _instance.setStatus(InstanceStatus.COMMIT);
//
//                clusterDelegate.getStorage()
//                        .put(proposal.getKey(), new VersionedData(_instance.getInstanceId(), proposal.getValue()));
//                LOGGER.debug("commit proposal:{} at node:{}", proposal, clusterDelegate.getCurrent().getID());
//            } else {
//                LOGGER.debug("waiting proposal:{} reach accept quorum:{}/{}",
//                        proposal,
//                        acceptedCount,
//                        clusterDelegate.getQuorum());
//            }
//
//            return _instance;
//        });
//
//    }
//
//}
