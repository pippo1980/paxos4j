package com.sirius.ds.paxos;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceTest extends SimpleProposalTest {

    @Before
    @Override
    public void init() {
        super.size = 5;
        super.init();
    }

    @Test
    public void batchWithoutWaitLearn() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            batch();
        }
    }

    private void batch() throws InterruptedException {
        String key = UUID.randomUUID().toString();
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            tasks.add(() -> services[new Random().nextInt(size)].propose(key,
                    ("pippo" + new Random().nextInt()).getBytes(),
                    100,
                    TimeUnit.MILLISECONDS));
        }

        AtomicInteger count = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        Executors.newFixedThreadPool(32).invokeAll(tasks).forEach(f -> {
            try {
                f.get();
                count.incrementAndGet();
//                System.out.println(count.incrementAndGet());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        tasks.clear();

        long cost = System.currentTimeMillis() - start;
        System.out.println(String.format("cost:%s, success:%s, tps:%s",
                cost,
                count.get(),
                (1000 / (double) cost) * count.get()));
    }
}
