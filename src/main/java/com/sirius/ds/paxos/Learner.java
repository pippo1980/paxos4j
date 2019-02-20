package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.LearnRQ;
import com.sirius.ds.paxos.msg.LearnRS;

public interface Learner {

    void onMessage(LearnRQ msg);

    void onMessage(LearnRS msg);
}
