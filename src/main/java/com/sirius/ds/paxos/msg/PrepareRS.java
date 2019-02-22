package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;
import com.sirius.ds.paxos.PeerID;

public class PrepareRS {

    public PrepareRS() {

    }

    public PrepareRS(PeerID peerID,
            long instanceId,
            int acceptBallot,
            VersionedData acceptValue,
            boolean prepareOK) {
        this.peerID = peerID;
        this.instanceId = instanceId;
        this.acceptBallot = acceptBallot;
        this.acceptValue = acceptValue;
        this.prepareOK = prepareOK;
    }

    private PeerID peerID;
    private long instanceId;
    private int acceptBallot;
    private VersionedData acceptValue;
    private boolean prepareOK;

    public PeerID getPeerID() {
        return peerID;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public int getAcceptBallot() {
        return acceptBallot;
    }

    public VersionedData getAcceptData() {
        return acceptValue;
    }

    public boolean isPrepareOK() {
        return prepareOK;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("peerID", peerID)
                .add("instanceId", instanceId)
                .add("acceptBallot", acceptBallot)
                .add("acceptValue", acceptValue)
                .add("prepareOK", prepareOK)
                .toString();
    }
}
