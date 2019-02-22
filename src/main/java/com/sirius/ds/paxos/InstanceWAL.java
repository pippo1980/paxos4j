package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.VersionedData;
import com.sirius.ds.paxos.stat.Instance;

/**
 * instance work ahead log
 */
public interface InstanceWAL {

    boolean exists(long instanceId);

    Instance get(long instanceId);

    Instance create(long instanceId, VersionedData data);
}
