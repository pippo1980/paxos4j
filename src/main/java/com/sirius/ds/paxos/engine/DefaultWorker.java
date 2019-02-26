package com.sirius.ds.paxos.engine;

import com.sirius.ds.paxos.PaxosCluster;
import com.sirius.ds.paxos.msg.PaxosMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DefaultWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAcceptor.class);
    private static final AtomicInteger T_COUNT = new AtomicInteger(0);

    public DefaultWorker(int workerId, String name, int currency) {
        this.workerId = workerId;
        this.name = name;
        this.currency = currency;

        this.queue = new ArrayBlockingQueue<>(4096);
    }

    protected int workerId;
    protected String name;
    protected int currency;
    protected ExecutorService executor;
    protected BlockingQueue<PaxosMessage> queue;

    public void start() {
        executor = Executors.newFixedThreadPool(currency,
                r -> new Thread(r, String.format("worker-%s%s-%s", name, workerId, T_COUNT.incrementAndGet())));

        executor.execute(() -> {
            boolean flag = true;
            PaxosMessage message = null;
            long instanceId = -1;
            while (flag) {
                try {
                    message = queue.poll(10, TimeUnit.MILLISECONDS);
                    if (message == null) {
                        continue;
                    }

                    instanceId = message.getInstanceId();
                    process(message);
                } catch (InterruptedException e) {
                    flag = false;
                    LOGGER.error("{}:{} process termination with error",
                            name,
                            workerId,
                            e);
                } catch (Throwable e) {
                    LOGGER.error("{}:{} process msg:{} due to error:{}, the instance is:{}",
                            name,
                            workerId,
                            message,
                            e,
                            getPaxosCluster().getInstanceWAL().get(instanceId));
                }
            }
        });
    }

    public void stop() {
        this.executor.shutdownNow();
        this.queue.clear();
    }

    protected void onMessage(PaxosMessage message) {
        try {
            queue.offer(message, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("on message:{} due to error:{}", message, e);
        }
    }

    protected abstract PaxosCluster getPaxosCluster();

    protected abstract void process(PaxosMessage message);

}
