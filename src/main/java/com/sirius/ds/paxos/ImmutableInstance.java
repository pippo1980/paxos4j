package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.ImmutablePropose;
import com.sirius.ds.paxos.msg.Proposal;

import java.util.Collections;
import java.util.function.Function;

public class ImmutableInstance extends Instance {

    public ImmutableInstance(Instance instance) {
        this.instanceId = instance.instanceId;
        this.preparedBallot = instance.preparedBallot;
        this.preparePromised = Collections.unmodifiableSet(instance.preparePromised);
        this.acceptPromised = Collections.unmodifiableSet(instance.acceptPromised);
        this.status = instance.status;
        this.proposal = new ImmutablePropose(instance.proposal);
    }

    public <R> R write(Function<Instance, R> writer) {
        throw new RuntimeException("not writable method");
    }

    @Override
    public Proposal getProposal() {
        return new ImmutablePropose(super.getProposal());
    }
}
