package com.sirius.ds.paxos.msg;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class VersionedData implements Comparable<VersionedData> {

    public VersionedData() {

    }

    public VersionedData(String key, byte[] payload) {
        this.key = key;
        this.payload = payload;
    }

    private long instanceId;
    private String key;
    private byte[] payload;

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

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
               Objects.equals(key, that.key) &&
               Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(instanceId, key);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("instanceId", instanceId)
                .add("key", key)
                .add("payload", payload)
                .toString();
    }
}
