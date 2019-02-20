package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.AcceptRQ;
import com.sirius.ds.paxos.msg.PrepareRQ;

public interface Acceptor {

    void onMessage(PrepareRQ msg);

    void onMessage(AcceptRQ msg);
}
