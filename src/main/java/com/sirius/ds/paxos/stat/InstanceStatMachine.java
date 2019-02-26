package com.sirius.ds.paxos.stat;

import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.PrepareRS;
import com.sirius.ds.paxos.msg.VersionedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class InstanceStatMachine extends Instance {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceStatMachine.class);
    private static final VersionedData NULL_VALE = null;

    public InstanceStatMachine() {
    }

    public InstanceStatMachine(long instanceId, VersionedData acceptValue) {
        super(instanceId, acceptValue);
    }

    public PrepareRQ prepare(PeerID currentId) {
        if (isCommitted()) {
            return new PrepareRQ(currentId, instanceId, promisedBallot);
        }

        rwl.writeLock().lock();
        try {
            promisedBallot++;
            prepared.clear();
            accepted.clear();
            setStatus(InstanceStatus.PREPARE);
            return new PrepareRQ(currentId, instanceId, promisedBallot);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public PrepareRS onPrepareRQ(PeerID currentId, PrepareRQ msg) {
        if (isCommitted()) {
            return new PrepareRS(currentId, instanceId, acceptBallot, acceptData, true);
        }

        rwl.writeLock().lock();
        try {
            if (msg.getBallot() >= promisedBallot) {
                promisedBallot = msg.getBallot();
                return new PrepareRS(currentId, instanceId, acceptBallot, acceptData, true);
            } else {
                return new PrepareRS(currentId, instanceId, 0, NULL_VALE, false);
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void onPrepareRS(PrepareRS msg, Consumer<Instance> callback) {
        rwl.writeLock().lock();
        try {
            if (prepared.contains(msg.getPeerID())) {
                return;
            }

            if (msg.getAcceptBallot() > acceptBallot && msg.getAcceptData() != null) {
                acceptBallot = msg.getAcceptBallot();
                acceptData = msg.getAcceptData();
            }

            if (msg.isPrepareOK()) {
                prepared.add(msg.getPeerID());
            }

            callback.accept(this);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public AcceptRS onAcceptRQ(PeerID currentId, AcceptRQ msg) {
        if (isCommitted()) {
            return new AcceptRS(currentId, instanceId, true);
        }

        rwl.writeLock().lock();
        try {
            boolean accept = msg.getBallot() == promisedBallot;
            if (accept) {
                acceptBallot = msg.getBallot();
                acceptData = msg.getValue();
            }

            if (status != InstanceStatus.ACCEPT) {
                setStatus(InstanceStatus.ACCEPT);
            }

            return new AcceptRS(currentId, instanceId, accept);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void onAcceptRS(AcceptRS msg, Consumer<Instance> callback) {
        if (committed.get()) {
            return;
        }

        rwl.writeLock().lock();
        try {
            if (accepted.contains(msg.getPeerID())) {
                return;
            }

            if (msg.isAcceptOK()) {
                accepted.add(msg.getPeerID());
            }

            callback.accept(this);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void commit() {
        while (!committed.get()) {
            if (committed.compareAndSet(false, true)) {
                setStatus(InstanceStatus.ACCEPT_OK);
            }
        }
    }

}
