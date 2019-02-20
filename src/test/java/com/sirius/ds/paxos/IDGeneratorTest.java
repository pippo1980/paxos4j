package com.sirius.ds.paxos;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class IDGeneratorTest {

    @Test
    public void sameSlot() {
        long id1 = IDGenerator.INSTANCE.nextId(1);
        // System.out.println(id1);

        long id2 = IDGenerator.INSTANCE.nextId(1);
        // System.out.println(id2);

        Assert.assertTrue(id2 > id1);
    }

    @Test
    public void differentSlot() {

        long second = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        long second1 = second - 2;
        long second2 = second - 1;

        long id1 = IDGenerator.INSTANCE.nextId(1, second1);
        // System.out.println(id1);

        long id2 = IDGenerator.INSTANCE.nextId(1, second2);
        // System.out.println(id2);

        Assert.assertTrue(id2 > id1);
    }

    @Test
    public void differentWorkId() {
        long id1 = IDGenerator.INSTANCE.nextId(1);
        // System.out.println(id1);

        long id2 = IDGenerator.INSTANCE.nextId(2);
        //System.out.println(id2)

        Assert.assertTrue(id2 > id1);
    }
}
