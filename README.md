# paxos4j
paxos implement with java

## service have such components below
* Proposer 
* Acceptor
* Leaner
* InstanceWAL
* DataStorage

### the paxos working theory

### the instance stat machine
![](https://github.com/pippo1980/paxos4j/blob/master/doc/statmachine.png)

## Generate an instanceId using the snowflake allocation id algorithm
When the currency raises values ​​on different working nodes to avoid the central node, the snowflake algorithm is used to generate instanceId on the working node.
![](https://github.com/pippo1980/paxos4j/blob/master/doc/snowflake.png)

## The optimizer proposal currency is submitted to the actual storage
1. use versioned data storage to store the real proposal value
2. Use instanceId for the data version, meaning that learning proposals with the same key will be submitted in the order of their instanceId.
3. If the proposal is learned, but the proposal instanceId is less than the instanceId using the same key to submit the value, the submission will fail.

## Simulated currency proposed multi threading
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

## REFERENCES
* L. Lamport, “Specifying concurrent program modules,” Trans. on Programming Languages and Systems, vol. 5, no. 2, pp. 190–222, Apr. 1983.
* N. A. Lynch and F. W. Vaandrager, “Forward and backward simulations, ii: Timing-based systems,” Inf. Comput., vol. 128, no. 1, pp. 1–25, 1996
* Lamport, Leslie. "The part-time parliament." ACM Transactions on Computer Systems (TOCS) 16.2 (1998): 133-169.
* Lamport, Leslie. "Paxos made simple." ACM Sigact News 32.4 (2001): 18-25.
* Primi, Marco. Paxos made code. Diss. University of Lugano, 2009.
* Chandra, Tushar D., Robert Griesemer, and Joshua Redstone. "Paxos made live: an engineering perspective." Proceedings of the twenty-sixth annual ACM symposium on Principles of distributed computing. ACM, 2007.
* [微信自研生产级paxos类库PhxPaxos实现原理介绍](http://mp.weixin.qq.com/s?__biz=MzI4NDMyNTU2Mw==&mid=2247483695&idx=1&sn=91ea422913fc62579e020e941d1d059e#rd)
* [Paxos理论介绍(1): 朴素Paxos算法理论推导与证明](https://zhuanlan.zhihu.com/p/21438357?refer=lynncui)
* [Paxos理论介绍(2): Multi-Paxos与Leader](https://zhuanlan.zhihu.com/p/21466932?refer=lynncui)