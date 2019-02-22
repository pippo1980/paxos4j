package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.InstanceWAL;
import com.sirius.ds.paxos.msg.VersionedData;
import com.sirius.ds.paxos.stat.Instance;
import com.sirius.ds.paxos.stat.InstanceStatMachine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryInstanceWAL implements InstanceWAL {

    private Map<Long, Instance> instances = new ConcurrentHashMap<>();
    private Map<String, Long> index = new ConcurrentHashMap<>();

//    @Override
//    public Proposal getPeerProposal(long instanceId, PeerID peerID) {
//        Instance instance = instances.get(instanceId);
//
//        if (instance == null || !peerID.equals(instance.getProposal().getPeerID())) {
//            return null;
//        }
//
//        return instance.getProposal();
//    }

    @Override
    public boolean exists(long instanceId) {
        return instances.containsKey(instanceId);
    }

    @Override
    public Instance get(long instanceId) {
        return instances.get(instanceId);
    }

    @Override
    public Instance create(long instanceId, VersionedData data) {
        return newInstance(instanceId, data);
    }

    private Instance newInstance(long instanceId, VersionedData data) {
        Instance instance = new InstanceStatMachine(instanceId, data);
        instances.put(instanceId, instance);
        return instance;
    }

//    private Instance updateInstance(Proposal proposal) {
//        return instances.get(index.get(proposal.getKey())).write(_instance -> {
//            // 再次检查释放已经提交
//            if (_instance.getStatus() == InstanceStatus.ACCEPT || _instance.getStatus() == InstanceStatus.COMMIT) {
//                return create(proposal);
//            }
//
//            // 如果proposal和已有instance中的proposal中的value不一致, 那么增加ballot
//            Proposal _proposal = _instance.getProposal();
//            if (!proposal.getValue().equals(_proposal.getValue())) {
//                _proposal.setBallot(_proposal.getBallot() + 1);
//                _proposal.setData(proposal.getValue());
//            }
//
//            return _instance;
//        });
//    }

}
