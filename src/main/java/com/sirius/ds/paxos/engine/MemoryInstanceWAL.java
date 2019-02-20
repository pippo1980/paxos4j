package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.IDGenerator;
import com.sirius.ds.paxos.Instance;
import com.sirius.ds.paxos.InstanceStatus;
import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.PeerID;
import com.sirius.ds.paxos.msg.Proposal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryInstanceWAL implements InstanceWAL {

    private Map<Long, Instance> instances = new ConcurrentHashMap<>();
    private Map<String, Long> index = new ConcurrentHashMap<>();

    @Override
    public Proposal getPeerProposal(long instanceId, PeerID peerID) {
        Instance instance = instances.get(instanceId);

        if (instance == null || !peerID.equals(instance.getProposal().getPeerID())) {
            return null;
        }

        return instance.getProposal();
    }

    @Override
    public boolean exist(long instanceId) {
        return instances.containsKey(instanceId);
    }

    @Override
    public Instance get(long instanceId) {
        return instances.get(instanceId);
    }

    @Override
    public Instance create(Proposal proposal) {
//        String key = proposal.getKey();
//
//        // 存在相同key的instance, 判断该instance是否已经提交
//        AtomicBoolean create = new AtomicBoolean(true);
//        if (index.containsKey(key)) {
//            instances.get(index.get(key)).read(_instance -> {
//                create.set(_instance.getStatus() != InstanceStatus.PREPARE);
//                return _instance;
//            });
//        }
//
//        return create.get()
//               ? newInstance(IDGenerator.INSTANCE.nextId(proposal.getPeerID().nodeId), proposal)
//               : updateInstance(proposal);

        return newInstance(IDGenerator.INSTANCE.nextId(proposal.getPeerID().nodeId), proposal);
    }

    @Override
    public Instance create(long instanceId, Proposal proposal) {
        return newInstance(instanceId, proposal);
    }

    private Instance newInstance(long instanceId, Proposal proposal) {
        proposal.setInstanceId(instanceId);

        Instance instance = new Instance(instanceId, proposal);
        instances.put(instanceId, instance);

        if (proposal.getKey() != null) {
            index.put(proposal.getKey(), instanceId);
        }

        return instance;
    }

    private Instance updateInstance(Proposal proposal) {
        return instances.get(index.get(proposal.getKey())).write(_instance -> {
            // 再次检查释放已经提交
            if (_instance.getStatus() == InstanceStatus.ACCEPT || _instance.getStatus() == InstanceStatus.COMMIT) {
                return create(proposal);
            }

            // 如果proposal和已有instance中的proposal中的value不一致, 那么增加ballot
            Proposal _proposal = _instance.getProposal();
            if (!proposal.getValue().equals(_proposal.getValue())) {
                _proposal.setBallot(_proposal.getBallot() + 1);
                _proposal.setValue(proposal.getValue());
            }

            return _instance;
        });
    }

}
