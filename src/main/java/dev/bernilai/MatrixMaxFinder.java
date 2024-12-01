package dev.bernilai;

import java.util.Random;

public class MatrixMaxFinder {
    public static void main(String[] args) throws InterruptedException {
        int rows = 1000;
        int cols = 1000;
        int[][] matrix = new int[rows][cols];
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(10000000);
            }
        }

        Thread[] threads = new Thread[rows];
        RowMaxFinder[] finders = new RowMaxFinder[rows];
        for (int i = 0; i < rows; i++) {
            finders[i] = new RowMaxFinder(matrix, i);
            threads[i] = new Thread(finders[i]);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        int globalMax = Integer.MIN_VALUE;
        for (RowMaxFinder finder : finders) {
            if (finder.getMax() > globalMax) {
                globalMax = finder.getMax();
            }
        }

        System.out.println("Global max: " + globalMax);
    }
}
