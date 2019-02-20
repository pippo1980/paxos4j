package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.Instance;
import com.sirius.ds.paxos.InstanceStatus;
import com.sirius.ds.paxos.msg.AcceptRQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class MessageInitFunc4AcceptRQ implements Function<Instance, Instance> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageInitFunc4AcceptRQ.class);

    ScheduledFuture<Boolean> callback;
    AcceptRQ msg;
    AtomicBoolean send;

    public MessageInitFunc4AcceptRQ(ScheduledFuture<Boolean> callback, AcceptRQ msg, AtomicBoolean send) {
        this.callback = callback;
        this.msg = msg;
        this.send = send;
    }

    @Override
    public Instance apply(Instance _instance) {
        switch (_instance.getStatus()) {
            case PREPARE:
                _instance.setStatus(InstanceStatus.ACCEPT);
                send.set(true);
                break;
            case COMMIT:
                LOGGER.warn("cancel accept rq, because instance:{} status is commit", _instance);
                callback.cancel(true);
                break;
        }

        if (_instance.getStatus() != InstanceStatus.ACCEPT) {
            LOGGER.warn("ignore send accept rq, because the instance:{} status is not accept", _instance);
            return _instance;
        } else {
            _instance.registerCallback(callback, InstanceStatus.COMMIT);
            msg.setAcceptedProposal(_instance.getProposal());
            send.set(true);
        }

        return _instance;
    }
}