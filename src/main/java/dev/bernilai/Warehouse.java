package dev.bernilai;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Warehouse {
    private static final int MAX_WEIGHT = 150;
    private static final int NUM_LOADERS = 3;
    private final int totalGoods = 1000;
    private AtomicInteger goodsLeftWarehouseA = new AtomicInteger(totalGoods);
    private final Lock lock = new ReentrantLock();
    private AtomicInteger currentWeightCart = new AtomicInteger(0);
    private CountDownLatch returnLatch = new CountDownLatch(NUM_LOADERS);
    private Random random = new Random();
    private boolean isWorking = true;
    private boolean[] loadersFinishedUnloading = new boolean[NUM_LOADERS];


    public void loadGoods(int loaderId) {
        while (isWorking) {
            int weight = Math.min(random.nextInt(50) + 1, goodsLeftWarehouseA.get());
            returnLatch.countDown();
            try {
                returnLatch.await();
                lock.lock();
                try {
                    if (currentWeightCart.get() + weight <= MAX_WEIGHT) {
                        currentWeightCart.addAndGet(weight);
                        goodsLeftWarehouseA.addAndGet(-weight);
                        System.out.printf("[Warehouse A] Loader %d added %d kg. Total weight: %d kg, Goods left: %d%n", loaderId, weight, currentWeightCart.get(), goodsLeftWarehouseA.get());
                    }

                    if (currentWeightCart.get() >= MAX_WEIGHT || goodsLeftWarehouseA.get() == 0) {
                        System.out.printf("[Warehouse A] Loaders are heading to unload %d kg to Warehouse B.%n", currentWeightCart.get());
                        unload(loaderId);

                        if (goodsLeftWarehouseA.get() == 0) {
                            isWorking = false;
                            System.out.println("All goods have been transferred.");
                        } else {
                            System.out.println("[Warehouse B] Loaders are returning to Warehouse A.");
                        }
                        returnLatch = new CountDownLatch(NUM_LOADERS);
                        loadersFinishedUnloading = new boolean[NUM_LOADERS];

                    }

                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void unload(int loaderId) throws InterruptedException {
        while (currentWeightCart.get() > 0) {
            int weight = Math.min(random.nextInt(50) + 1, currentWeightCart.get());
            lock.lock();
            try {
                currentWeightCart.addAndGet(-weight);
                System.out.printf("[Warehouse B] Loader %d unloaded %d kg. Remaining in cart: %d kg%n", loaderId, weight, currentWeightCart.get());
            } finally {
                lock.unlock();
            }

        }
        loadersFinishedUnloading[loaderId - 1] = true;
        boolean allFinished = true;
        for (boolean finished : loadersFinishedUnloading) {
            if (!finished) allFinished = false;
        }
        if (allFinished) {
            System.out.println("Unloading complete");
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
    }
}