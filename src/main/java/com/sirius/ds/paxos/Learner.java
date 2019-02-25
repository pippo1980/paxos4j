package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.LearnRQ;
import com.sirius.ds.paxos.msg.LearnRS;

import java.util.function.Consumer;

public interface Learner {

    void onMessage(LearnRQ msg);

    void onMessage(LearnRS msg);

    void registerLearningWatcher(long instanceId, Consumer<LearnRS> watcher);

    void deRegisterLearningWatcher(long instanceId);

}
