# paxos4j
paxos implement with java

## service have such components below
* Proposer 
* Acceptor
* Leaner
* InstanceWAL
* DataStorage

## use snowflake distribute id algorithm to generate instanceId




## simulation currency propose with multi thread

```java

public class SimpleProposalTest {

    private int size = 3;
    private PaxosService[] services = new PaxosService[size];

    @Before
    public void init() {
        PeerID[] members = new PeerID[size];
        for (int i = 0; i < size; i++) {
            members[i] = new PeerID(i + 1, "127.0.0.1", 8080 + i);
        }
        
        PaxosServiceFactory factory = new PaxosServiceFactory(members);
        for (int i = 0; i < size; i++) {
            services[i] = factory.get(members[i]);
        }
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


```