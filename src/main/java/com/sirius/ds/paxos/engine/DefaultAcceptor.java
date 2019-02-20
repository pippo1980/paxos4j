package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.Acceptor;
import com.sirius.ds.paxos.ClusterDelegate;
import com.sirius.ds.paxos.Instance;
import com.sirius.ds.paxos.InstanceStatus;
import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.PrepareRS;
import com.sirius.ds.paxos.msg.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAcceptor implements Acceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAcceptor.class);

    public DefaultAcceptor(ClusterDelegate clusterDelegate) {
        this.clusterDelegate = clusterDelegate;
    }

    private ClusterDelegate clusterDelegate;

    @Override
    public void onMessage(PrepareRQ msg) {
        LOGGER.debug("receive prepare rq:{} from node:{} to node:{}",
                msg,
                clusterDelegate.getCurrent().getID(),
                clusterDelegate.getCurrent().getID());

        InstanceWAL instanceWAL = clusterDelegate.getInstanceWAL();
        if (!instanceWAL.exist(msg.getInstanceId())) {
            synchronized (this) {
                if (!instanceWAL.exist(msg.getInstanceId())) {
                    Proposal proposal = new Proposal(msg.getPeerID(), msg.getInstanceId(), msg.getBallot(), null, null);
                    Instance instance = instanceWAL.create(msg.getInstanceId(), proposal);
                    LOGGER.debug("persist new instance:{} at node:{}", instance, clusterDelegate.getCurrent().getID());
                }
            }
        }

        final PrepareRS prepareRS = new PrepareRS(clusterDelegate.getCurrent().getID(),
                msg.getInstanceId(),
                -1,
                null,
                false);

        instanceWAL.get(msg.getInstanceId()).write(_instance -> {
            boolean preparePromise = false;

            if (msg.getBallot() > _instance.getPreparedBallot()) {
                // 如果消息中的ballot大于本地的ballot, 那么接受消息中的ballot
                preparePromise = true;
                _instance.setPreparedBallot(msg.getBallot());
            }

            // 如果本地instance状态为init, 说明是接受了远程的prepare, 那么修改本地instance状态
            if (preparePromise && _instance.getStatus() == InstanceStatus.INIT) {
                _instance.setStatus(InstanceStatus.PREPARE);
            }

            // 如果接受了msg中的ballot prepare, 那么返回之前曾经接受的preparedBallot和proposal
            // 否则返回当前proposal的ballot和空值
            prepareRS.setPreparedBallot(preparePromise
                                        ? _instance.getPreparedBallot()
                                        : _instance.getProposal().getBallot());
            prepareRS.setPreparedProposal(preparePromise ? _instance.getProposal() : null);
            prepareRS.setPrepareOK(preparePromise);
            return _instance;
        });

        clusterDelegate.reply(msg.getPeerID(), prepareRS);
    }

    @Override
    public void onMessage(AcceptRQ msg) {
        LOGGER.debug("receive accept rq:{} from node:{} to node:{}",
                msg,
                clusterDelegate.getCurrent().getID(),
                clusterDelegate.getCurrent().getID());

        Proposal proposal = msg.getAcceptedProposal();

        InstanceWAL instanceWAL = clusterDelegate.getInstanceWAL();
        if (!instanceWAL.exist(proposal.getInstanceId())) {
            AcceptRS acceptRS = new AcceptRS(clusterDelegate.getCurrent().getID(),
                    proposal.getInstanceId(),
                    false);
            clusterDelegate.reply(msg.getPeerID(), acceptRS);
            return;
        }

        final AcceptRS acceptRS = new AcceptRS(clusterDelegate.getCurrent().getID(), proposal.getInstanceId(), false);
        instanceWAL.get(proposal.getInstanceId()).write(_instance -> {
            Proposal _proposal = _instance.getProposal();

            if (proposal.getBallot() != _proposal.getBallot()) {
                LOGGER.warn(
                        "ignore accept rq:{}, because the remote proposal ballot:{} not equal local proposal:{}",
                        proposal.getBallot(),
                        _proposal.getBallot(),
                        msg);
                return _instance;
            }

            acceptRS.setAcceptOK(true);

            // 如果本地instance状态为prepare, 说明是接受了远程的accept请求, 那么修改本地instance状态
            if (_instance.getStatus() == InstanceStatus.PREPARE) {
                _instance.setStatus(InstanceStatus.ACCEPT);
            }

            _proposal.setBallot(proposal.getBallot());
            _proposal.setKey(proposal.getKey());
            _proposal.setValue(proposal.getValue());
            LOGGER.debug("accept proposal:{} at node:{}",
                    _proposal,
                    clusterDelegate.getCurrent().getID());

            return _instance;
        });

        clusterDelegate.reply(msg.getPeerID(), acceptRS);
    }

}
