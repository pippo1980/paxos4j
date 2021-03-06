package com.sirius.ds.paxos;

import com.sirius.ds.paxos.engine.BasePaxosCluster;
import com.sirius.ds.paxos.engine.DefaultPeerNode;
import com.sirius.ds.paxos.engine.MemoryDataStorage;
import com.sirius.ds.paxos.engine.MemoryInstanceWAL;
import com.sirius.ds.paxos.engine.MemoryPaxosCluster;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PaxosServiceFactory {

    public PaxosServiceFactory(PeerID[] members) {
        assert members != null;
        Arrays.stream(members).forEach(id -> {
            DefaultPeerNode node = new DefaultPeerNode(id);
            this.members.put(id, node);
        });
    }

    private Map<PeerID, DefaultPeerNode> members = new HashMap<>();
    private Map<PeerID, BasePaxosCluster> services = new HashMap<>();

    public synchronized BasePaxosCluster get(PeerID id) {

        BasePaxosCluster service = services.get(id);
        if (service == null) {
            InstanceWAL instanceWAL = new MemoryInstanceWAL();
            DataStorage storage = new MemoryDataStorage(id);
            service = new MemoryPaxosCluster(instanceWAL, storage);
            service.init(members.get(id), new HashSet<>(members.values()));
            services.put(id, service);
            members.get(id).setClusterDelegate(service);
        }

        return service;
    }
}
