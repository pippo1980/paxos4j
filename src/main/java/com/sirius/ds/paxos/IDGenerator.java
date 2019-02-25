package com.sirius.ds.paxos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class IDGenerator {

    // 50w年, 32节点, 每个workID每秒100w id
    public static IDGenerator INSTANCE = new IDGenerator(32, 21, 10);

    /**
     * 起始的时间戳
     */
    private static final long START_STAMP = startStamp();
    private static final int TOTAL_BITS = 1 << 6;
    private static final int signBits = 1;

    public IDGenerator(int timestampBits, int sequenceBits, int workerIdBits) {
        // make sure allocated 64 bits
        int allocateTotalBits = signBits + timestampBits + sequenceBits + workerIdBits;
        assert allocateTotalBits == TOTAL_BITS;

        this.generator = new Generator(timestampBits, sequenceBits, workerIdBits);
        this.start();
    }

    public void start() {
        long current_second = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        for (long i = -10; i < 60; i++) {
            long slot_second = current_second + i;
            slots.put(slot_second, new Slot(generator, slot_second));
        }

        slots_daemon.scheduleAtFixedRate(this::prepareSlots, 1, 1, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        slots_daemon.shutdownNow();
    }

    private void prepareSlots() {
        long second = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        // 生成10s之后的slots
        for (long i = 0; i < 10; i++) {
            long slot_second = second + i;
            slots.computeIfAbsent(slot_second, (k) -> new Slot(generator, slot_second));
        }

        // 清除60s之前的slots
        for (long i = 0; i < 60; i++) {
            long slot_second = second - 61 - i;
            Slot slot = slots.remove(slot_second);
            if (slot != null) {
                slot.generator = null;
                slot.sequence = null;
            }
        }
    }

    private ScheduledExecutorService slots_daemon = Executors.newScheduledThreadPool(1);
    private Map<Long, Slot> slots = new ConcurrentHashMap<>();
    private Generator generator;

    public long nextId(long workId) {
        return nextId(workId, System.currentTimeMillis() / 1000);
    }

    public long nextId(long workId, long slot_second) {
        assert slots.containsKey(slot_second);
        return slots.get(slot_second).nextId(workId);
    }

    public static class Generator {

        public Generator(int timestampBits, int sequenceBits, int workerIdBits) {
            // initialize max value
            this.maxDeltaSeconds = ~(-1L << timestampBits);
            this.maxSequence = ~(-1L << sequenceBits);
            this.maxWorkerId = ~(-1L << workerIdBits);

            // initialize shift
            this.timestampShift = sequenceBits + workerIdBits;
            this.sequenceShift = workerIdBits;
        }

        /**
         * Max value for workId & sequence
         */
        public final long maxDeltaSeconds;
        public final long maxSequence;
        public final long maxWorkerId;

        /**
         * Shift for timestamp & workerId
         */
        public final int timestampShift;
        public final int sequenceShift;

        public long generate(long deltaSeconds, long sequence, long workerId) {
            assert deltaSeconds < maxDeltaSeconds;
            assert workerId < maxWorkerId;
            assert sequence < maxSequence;
            return (deltaSeconds << timestampShift) | (sequence << sequenceShift) | workerId;
        }

    }

    // ont slot for every second
    public static class Slot {

        public Slot(Generator generator, long slot_second) {
            this.generator = generator;
            this.slot_second = slot_second;
            this.slot_delta_second = slot_second - TimeUnit.MILLISECONDS.toSeconds(START_STAMP);
            this.slot_time = TimeUnit.SECONDS.toMillis(slot_second);
        }

        private Generator generator;
        private long slot_second;
        private long slot_delta_second;
        private long slot_time;
        private AtomicLong sequence = new AtomicLong(0);

        public long nextId(long workId) {

            // 进行一次乐观校验, 避免时间重调的情况
            if (System.currentTimeMillis() < slot_time) {
                long refusedSeconds = System.currentTimeMillis() - slot_time;
                throw new IllegalArgumentException(String.format("Clock moved backwards. Refusing for %d seconds",
                        refusedSeconds));
            }

            // 无锁自增
            return generator.generate(slot_delta_second, sequence.incrementAndGet(), workId);
        }
    }

    private static long startStamp() {
        try {
            return new SimpleDateFormat("yyyyMMdd", Locale.CHINA).parse("20170101").getTime();
        } catch (ParseException e) {
            // 20170101 00:00:00
            return 1514736000000L;
        }
    }

    public static void main(String[] args) {
        System.out.println(INSTANCE.generator.maxDeltaSeconds);
        System.out.println(INSTANCE.generator.maxSequence);
        System.out.println(INSTANCE.generator.maxWorkerId);

        System.out.println(System.currentTimeMillis() % 1000);
        System.out.println(System.currentTimeMillis() / 1000);

    }
}
