package com.sirius.ds.paxos;

import com.sirius.ds.paxos.msg.VersionedData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimpleProposalTest {

    private int size = 3;
    private PaxosService[] services = new PaxosService[size];

    @Before
    public void init() {
        PeerID[] members = new PeerID[size];
        for (int i = 0; i < size; i++) {
            members[i] = new PeerID(i + 1, "127.0.0.1", 8080 + i);
        }
        System.out.println(Arrays.toString(members));

        PaxosServiceFactory factory = new PaxosServiceFactory(members);
        for (int i = 0; i < size; i++) {
            services[i] = factory.get(members[i]);
        }
        System.out.println("##");
    }

    // @Test
    public void proposeOne() {
        String key = UUID.randomUUID().toString();
        boolean success = services[0].propose(key, "pippo".getBytes(), 1, TimeUnit.SECONDS);
        Assert.assertTrue(success);
        Assert.assertEquals("pippo", new String(services[0].get(key).payload));
    }

    @Test
    public void proposeCurrency() throws InterruptedException {
        String key = UUID.randomUUID().toString();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final int _i = i;
            tasks.add(() -> services[_i].propose(key, ("pippo" + _i).getBytes(), 1, TimeUnit.SECONDS));
        }

        Executors.newFixedThreadPool(size).invokeAll(tasks).forEach(f -> {

            try {
                Assert.assertTrue(f.get());
            } catch (Throwable e) {
                e.printStackTrace();
            }

        });

        Thread.sleep(1000);

        assertConsistency(key);

    }

    @Test
    public void proposeCurrencyAndBatch() throws InterruptedException {
        String key = UUID.randomUUID().toString();

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final int _i = i;

            // currency put value with same key on different node
            tasks.add(() -> {
                for (int j = 0; j < 10; j++) {
                    services[_i].propose(key, ("pippo" + j).getBytes(), 1, TimeUnit.SECONDS);
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

            Thread.sleep(1000);

            // check if data is consistency on all node
            assertConsistency(key);
        }
    }

    private void assertConsistency(String key) {
        for (PaxosService service : services) {
            VersionedData target = service.get(key);
            String payload = new String(target.payload);
            System.out.println(target.instanceId + "#" + payload);
            Arrays.stream(services).forEach(s -> Assert.assertEquals(target, s.get(key)));
        }
    }
}
