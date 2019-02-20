package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class VersionedData implements Comparable<VersionedData> {

    public VersionedData(long instanceId, byte[] payload) {
        this.instanceId = instanceId;
        this.payload = payload;
    }

    public final long instanceId;
    public final byte[] payload;

    @Override
    public int compareTo(@Nullable VersionedData data) {
        assert data != null;
        return Long.compare(instanceId, data.instanceId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VersionedData that = (VersionedData) o;
        return instanceId == that.instanceId &&
               Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(instanceId);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instanceId", instanceId)
                .add("payload", Arrays.toString(payload))
                .toString();
    }
}
