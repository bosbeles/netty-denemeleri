package org.deneme.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ihsan on 4/18/2015.
 */
public abstract class AbstractNode implements Node {

    private AtomicInteger count = new AtomicInteger();
    private Object sendLock = new Object();
    private int counter = -1;
    private int lastReceived = -1;

    @Override
    public void receive(String message) {
        int c = count.incrementAndGet();
        if(c % 100000 == 0) {
            System.out.println(c);
        }
        //System.out.println(message);
        String[] split = message.split("\\t");

        String seq = split[0];
        int no = Integer.parseInt(seq);

        if(no - lastReceived != 1) {
            if(no == 0 && lastReceived == 65535) {
                // System.out.println("Rollover");
            }
            else {
                System.out.println(Thread.currentThread().getName() + "\tlast: " + lastReceived + "\tnew: " + no);
            }
        }
        lastReceived = no;

        if (split.length == 2 && split[1].startsWith("+")) {
            send(seq + "\tACK");
        }
    }

    abstract protected void doSend(String message);

    @Override
    public void send(String message) {
        synchronized (sendLock) {
            doSend(counter() + "\t" + message);
        }
    }

    public int counter() {
        if (++counter >= 65536) {
            counter = 0;
        }
        return counter;
    }
}
