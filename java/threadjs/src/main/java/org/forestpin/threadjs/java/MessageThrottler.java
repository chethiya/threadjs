package org.forestpin.threadjs.java;

import java.util.LinkedList;
import java.util.Queue;

public class MessageThrottler extends Thread {
    private int maxThreads = 1;
    private int runningCount = 0;
    private final Queue<Thread> threads = new LinkedList<Thread>();

    public MessageThrottler(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void push(Thread thread) {
        synchronized (threads) {
            threads.add(thread);
        }
    }

    public void onThreadExit() {
        synchronized (this) {
            runningCount--;
        }

    }

    @Override
    public void run() {
        while (true) {
            synchronized (threads) {
                synchronized (this) {
                    while (runningCount < maxThreads && threads.peek() != null) {
                        threads.poll().start();
                        runningCount++;
                    }
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
