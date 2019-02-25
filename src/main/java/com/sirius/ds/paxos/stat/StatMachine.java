package com.sirius.ds.paxos.stat;

import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.PrepareRQ;
import com.sirius.ds.paxos.msg.PrepareRS;

import java.util.function.Consumer;

public interface StatMachine {

    void registerStatusWatcher(Consumer<InstanceStatus> listener);

    PrepareRQ prepare(PeerID currentId);

    PrepareRS onPrepareRQ(PeerID currentId, PrepareRQ msg);

    void onPrepareRS(PrepareRS msg, Consumer<Instance> callback);

    AcceptRS onAcceptRQ(PeerID currentId, AcceptRQ msg);

    void onAcceptRS(AcceptRS msg, Consumer<Instance> callback);

    void commit();

}
