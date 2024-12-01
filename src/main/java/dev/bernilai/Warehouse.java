package dev.bernilai;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Warehouse {
    private static final int MAX_WEIGHT = 150;
    private static final int NUM_LOADERS = 3;
    private final int totalGoods = 1000;
    private AtomicInteger goodsLeft = new AtomicInteger(totalGoods);
    private final Lock lock = new ReentrantLock();
    private AtomicInteger currentWeight = new AtomicInteger(0);
    private CountDownLatch returnLatch = new CountDownLatch(NUM_LOADERS);
    private CountDownLatch unloadLatch = new CountDownLatch(1);
    private Random random = new Random();
    private boolean isWorking = true;

    public void loadGoods(int loaderId) {
        while (isWorking && goodsLeft.get() > 0) {
            int weight = Math.min(random.nextInt(50) + 1, goodsLeft.get());
            returnLatch.countDown();
            try {
                returnLatch.await();
                lock.lock();
                try {
                    if (currentWeight.get() + weight <= MAX_WEIGHT) {
                        currentWeight.addAndGet(weight);
                        goodsLeft.addAndGet(-weight);
                        System.out.printf("%s Loader %d added %d kg. Total weight: %d kg, Goods left: %d%n", new Date(), loaderId, weight, currentWeight.get(), goodsLeft.get());
                    }
                    if (currentWeight.get() >= MAX_WEIGHT || goodsLeft.get() == 0) {
                        System.out.printf("%s Loaders are heading to unload %d kg.%n", new Date(), currentWeight.get());
                        unloadLatch.countDown();
                        unloadLatch.await();
                        currentWeight.set(0);
                        returnLatch = new CountDownLatch(NUM_LOADERS);
                        unloadLatch = new CountDownLatch(1);
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Warehouse warehouse = new Warehouse();
        Thread[] loaders = new Thread[NUM_LOADERS];

        for (int i = 1; i <= NUM_LOADERS; i++) {
            final int loaderId = i;
            loaders[i - 1] = new Thread(() -> warehouse.loadGoods(loaderId));
            loaders[i - 1].start();
        }

        for (Thread loader : loaders) {
            loader.join();
        }
        System.out.println("All goods have been transferred.");
    }
}