package dev.bernilai;

import java.util.Arrays;
import java.util.Random;

public class ArraySumCalculator {
    public static void main(String[] args) throws InterruptedException {
        int[] array = new int[1000000];
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(100);
        }

        int mid = array.length / 2;
        SumCalculator calculator1 = new SumCalculator(array, 0, mid);
        SumCalculator calculator2 = new SumCalculator(array, mid, array.length);

        Thread thread1 = new Thread(calculator1);
        Thread thread2 = new Thread(calculator2);
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        int totalSum = calculator1.getSum() + calculator2.getSum();

        System.out.println("Total sum: " + totalSum);


        int sequentialSum = Arrays.stream(array).sum();
        System.out.println("Sequential sum: " + sequentialSum);
        System.out.println("Sums are equal: " + (totalSum == sequentialSum));
    }
}
