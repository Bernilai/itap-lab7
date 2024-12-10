package dev.bernilai;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Warehouse {
    private static final int MAX_WEIGHT = 150;
    private static final int NUM_LOADERS = 3;
    private final int[] goods = {49, 2, 38, 11, 42, 27, 8, 31, 45, 16, 1, 35, 23, 19, 47, 3, 29, 14, 40, 0};
    private final Queue<Integer> goodsQueue = new ConcurrentLinkedQueue<>(); //Use ConcurrentLinkedQueue
    private AtomicInteger currentWeightCart = new AtomicInteger(0);
    private final Semaphore cartSemaphore = new Semaphore(1);
    private final CountDownLatch allLoadersFinished = new CountDownLatch(NUM_LOADERS);
    private final CountDownLatch allGoodsLoadedLatch = new CountDownLatch(1);
    private boolean allLoadersTried = false;

    public Warehouse() {
        for (int good : goods) {
            goodsQueue.offer(good);
        }
    }

    public void loadGoods(int loaderId) {
        try {
            while (!goodsQueue.isEmpty()) { // Simpler loop condition
                loadItem(loaderId);
                synchronized (this) {
                    if (allLoadersTried && currentWeightCart.get() < MAX_WEIGHT && !goodsQueue.isEmpty()) {
                        unloadCart();
                        allLoadersTried = false;
                    } else if (currentWeightCart.get() >= MAX_WEIGHT) {
                        unloadCart();
                    }
                }
            }
            unloadCart(); // Final unload
            allGoodsLoadedLatch.countDown(); // Count down latch after loop
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            allLoadersFinished.countDown();
        }
        System.out.printf("Loader %d finished.%n", loaderId);
    }

    private void loadItem(int loaderId) throws InterruptedException {
        cartSemaphore.acquire();
        try {
            Integer weight = goodsQueue.poll();
            if (weight == null) {
                return; // No more items
            }
            if (currentWeightCart.get() + weight <= MAX_WEIGHT) {
                addItemToCart(loaderId, weight);
            } else {
                goodsQueue.offer(weight); // Return to queue
                synchronized (this) {
                    allLoadersTried = true;
                }
                System.out.printf("[Warehouse A] Loader %d couldn't add %d kg (exceeds weight limit).%n", loaderId, weight);
            }
        } finally {
            cartSemaphore.release();
        }
    }

    private void addItemToCart(int loaderId, int weight) {
        currentWeightCart.addAndGet(weight);
        System.out.printf("[Warehouse A] Loader %d added %d kg. Total weight: %d kg, Goods left: %d%n", loaderId, weight, currentWeightCart.get(), goodsQueue.size());
    }

    private synchronized void unloadCart() {
        if (currentWeightCart.get() > 0) {
            System.out.printf("[Warehouse A] Loaders are heading to unload %d kg to Warehouse B.%n", currentWeightCart.get());
            System.out.println("[Warehouse B] Cart unloaded.");
            currentWeightCart.set(0);
            allLoadersTried = false; // Reset flag after unloading
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Warehouse warehouse = new Warehouse();
        Thread[] loaders = new Thread[NUM_LOADERS];

        for (int i = 1; i <= NUM_LOADERS; i++) {
            int loaderId = i;
            loaders[i - 1] = new Thread(() -> warehouse.loadGoods(loaderId));
            loaders[i - 1].start();
        }

        warehouse.allLoadersFinished.await(); // Wait for all loaders to finish
        warehouse.allGoodsLoadedLatch.await(); // Wait for all goods to be loaded

        System.out.println("All goods have been transferred.");
    }
}