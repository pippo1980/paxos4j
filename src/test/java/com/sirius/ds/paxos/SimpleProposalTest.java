package com.sirius.ds.paxos;

import com.sirius.ds.paxos.engine.BaseClusterDelegate;
import com.sirius.ds.paxos.engine.DefaultAcceptor;
import com.sirius.ds.paxos.engine.DefaultLearner;
import com.sirius.ds.paxos.engine.DefaultPeerNode;
import com.sirius.ds.paxos.engine.DefaultProposer;
import com.sirius.ds.paxos.engine.MemoryDataStorage;
import com.sirius.ds.paxos.engine.MemoryInstanceWAL;
import com.sirius.ds.paxos.engine.MockClusterDelegate;
import com.sirius.ds.paxos.msg.VersionedData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimpleProposalTest {

    private int size = 3;
    private BaseClusterDelegate[] delegates;

    @Before
    public void init() {
        delegates = new BaseClusterDelegate[size];
        PeerNode[] nodes = new PeerNode[size];

        for (int i = 0; i < size; i++) {
            delegates[i] = create();
            nodes[i] = new DefaultPeerNode(new PeerID(i + 1, null),
                    new DefaultProposer(delegates[i]),
                    new DefaultAcceptor(delegates[i]),
                    new DefaultLearner(delegates[i]));
        }

        for (int i = 0; i < size; i++) {
            delegates[i].init(nodes[i], new HashSet<>(Arrays.asList(nodes)));
        }
    }

    private BaseClusterDelegate create() {
        InstanceWAL instanceWAL = new MemoryInstanceWAL();
        DataStorage storage = new MemoryDataStorage();

        BaseClusterDelegate cluster = new MockClusterDelegate(instanceWAL, storage);
        return cluster;
    }

    @Test
    public void proposeOne() {
        String key = UUID.randomUUID().toString();
        boolean success = delegates[0].propose(key, "pippo".getBytes(), 1, TimeUnit.SECONDS);
        Assert.assertTrue(success);
        Assert.assertEquals("pippo", new String(delegates[0].getStorage().get(key).payload));
    }

    @Test
    public void proposeCurrency() throws InterruptedException {
        String key = UUID.randomUUID().toString();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final int _i = i;
            tasks.add(() -> delegates[_i].propose(key, ("pippo" + _i).getBytes(), 1, TimeUnit.SECONDS));
        }

        Executors.newFixedThreadPool(size).invokeAll(tasks).forEach(f -> {

            try {
                Assert.assertTrue(f.get());
            } catch (Throwable e) {
                e.printStackTrace();
            }

        });

        Thread.sleep(1000 * 5);

        assertConsistency(key);

    }

    @Test
    public void proposeCurrencyAndBatch() throws InterruptedException {
        String key = UUID.randomUUID().toString();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final int _i = i;
            tasks.add(() -> {
                for (int j = 0; j < 10; j++) {
                    delegates[_i].propose(key, ("pippo" + j).getBytes(), 1, TimeUnit.SECONDS);
                }

                return true;
            });

            Executors.newFixedThreadPool(size).invokeAll(tasks).forEach(f -> {

                try {
                    Assert.assertTrue(f.get());
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            });

            Thread.sleep(1000 * 5);

            assertConsistency(key);
        }
    }

    private void assertConsistency(String key) {
        for (BaseClusterDelegate delegate : delegates) {
            VersionedData target = delegate.getStorage().get(key);
            String payload = new String(target.payload);
            System.out.println(target.instanceId + "#" + payload);
            Arrays.stream(delegates).forEach(d -> Assert.assertEquals(target, d.getStorage().get(key)));
        }
    }
}
