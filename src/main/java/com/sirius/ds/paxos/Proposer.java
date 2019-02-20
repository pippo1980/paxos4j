package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.AcceptRS;
import com.sirius.ds.paxos.msg.PrepareRS;

public interface Proposer {

    void onMessage(PrepareRS msg);

    void onMessage(AcceptRS msg);

}
