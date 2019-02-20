package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.Instance;
import com.sirius.ds.paxos.InstanceStatus;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.Proposal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class MessageInitFunc4PrepareRQ implements Function<Instance, Instance> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageInitFunc4PrepareRQ.class);

    public MessageInitFunc4PrepareRQ(ScheduledFuture<Boolean> callback, PrepareRQ msg, AtomicBoolean send) {
        this.callback = callback;
        this.msg = msg;
        this.send = send;
    }

    ScheduledFuture<Boolean> callback;
    PrepareRQ msg;
    AtomicBoolean send;

    @Override
    public Instance apply(Instance _instance) {
        switch (_instance.getStatus()) {
            case INIT:
                _instance.setStatus(InstanceStatus.PREPARE);
                Proposal proposal = _instance.getProposal();
                proposal.setBallot(proposal.getBallot() + 1);
                break;
            case ACCEPT:
                LOGGER.warn("cancel prepare rq, because instance:{} status is accept", _instance);
                callback.cancel(true);
                break;
            case COMMIT:
                LOGGER.warn("cancel prepare rq, because instance:{} status is commit", _instance);
                callback.cancel(true);
                break;
        }

        if (_instance.getStatus() != InstanceStatus.PREPARE) {
            LOGGER.warn("ignore send prepare rq, because the instance:{} status is not prepare", _instance);
            return _instance;
        } else {
            _instance.registerCallback(callback, InstanceStatus.ACCEPT, InstanceStatus.COMMIT);
            msg.setBallot(_instance.getProposal().getBallot());
            send.set(true);
        }

        return _instance;
    }
}