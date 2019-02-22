//package com.sirius.ds.paxos.discard;
//
//import com.google.common.base.MoreObjects;
//import com.sirius.ds.paxos.InstanceException;
//import com.sirius.ds.paxos.stat.InstanceStatus;
//import com.sirius.ds.paxos.PeerID;
//import com.sirius.ds.paxos.msg.Proposal;
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.Future;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//public class Instance {
//
//    public Instance() {
//
//    }
//
//    public Instance(long instanceId, Proposal proposal) {
//        this.instanceId = instanceId;
//        this.proposal = proposal;
//    }
//
//    // TODO 在有大量instance时, 会有大量锁导致cpu利用率下降, 后续可用队列分区及cas进行优化
//    private transient ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
//
//    public <R> R read(Function<Instance, R> reader) {
//        lock.readLock().lock();
//        try {
//            return reader.apply(new ImmutableInstance(this));
//        } catch (Exception e) {
//            throw new InstanceException(e);
//        } finally {
//            lock.readLock().unlock();
//        }
//    }
//
//    public <R> R write(Function<Instance, R> writer) {
//        lock.writeLock().lock();
//        try {
//            return writer.apply(this);
//        } catch (Exception e) {
//            throw new InstanceException(e);
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    protected long instanceId;
//    protected int preparedBallot = 0;
//    protected InstanceStatus status = InstanceStatus.INIT;
//    protected Set<PeerID> preparePromised = new HashSet<>();
//    protected Set<PeerID> acceptPromised = new HashSet<>();
//    protected Proposal proposal;
//
//    public long getInstanceId() {
//        return instanceId;
//    }
//
//    public int getPreparedBallot() {
//        lock.readLock().lock();
//        try {
//            return preparedBallot;
//        } finally {
//            lock.readLock().unlock();
//        }
//    }
//
//    public InstanceStatus getStatus() {
//        lock.readLock().lock();
//        try {
//            return status;
//        } finally {
//            lock.readLock().unlock();
//        }
//    }
//
//    public Set<PeerID> getPreparePromised() {
//        lock.readLock().lock();
//        try {
//            return preparePromised;
//        } finally {
//            lock.readLock().unlock();
//        }
//    }
//
//    public Set<PeerID> getAcceptPromised() {
//        lock.readLock().lock();
//        try {
//            return acceptPromised;
//        } finally {
//            lock.readLock().unlock();
//        }
//    }
//
//    public Proposal getProposal() {
//        lock.readLock().lock();
//        try {
//            return proposal;
//        } finally {
//            lock.readLock().unlock();
//        }
//    }
//
//    public void setPreparedBallot(int preparedBallot) {
//        lock.writeLock().lock();
//        try {
//            this.preparedBallot = preparedBallot;
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    public void setStatus(InstanceStatus status) {
//        lock.writeLock().lock();
//        try {
//            this.status = status;
//
//            if (callback != null) {
//                callback.accept(this);
//            }
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    public void setPreparePromised(Set<PeerID> preparePromised) {
//        lock.writeLock().lock();
//        try {
//            this.preparePromised = preparePromised;
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    public void setAcceptPromised(Set<PeerID> acceptPromised) {
//        lock.writeLock().lock();
//        try {
//            this.acceptPromised = acceptPromised;
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    public void setProposal(Proposal proposal) {
//        lock.writeLock().lock();
//        try {
//            this.proposal = proposal;
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    Consumer<Instance> callback = null;
//
//    public void registerCallback(Future<Boolean> future, InstanceStatus... expect) {
//        lock.writeLock().lock();
//
//        try {
//            callback = (instance -> {
//                for (InstanceStatus instanceStatus : expect) {
//                    if (instanceStatus == instance.getStatus()) {
//                        future.cancel(true);
//                        return;
//                    }
//                }
//            });
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    public void deRegisterCallback() {
//        lock.writeLock().lock();
//        try {
//            callback = null;
//        } finally {
//            lock.writeLock().unlock();
//
//        }
//    }
//
//    @Override
//    public String toString() {
//        return MoreObjects.toStringHelper(this)
//                .add("instanceId", instanceId)
//                .add("preparedBallot", preparedBallot)
//                .add("status", status)
////                .add("preparePromised", preparePromised)
////                .add("acceptPromised", acceptPromised)
//                .add("proposal", proposal)
//                .toString();
//    }
//}
