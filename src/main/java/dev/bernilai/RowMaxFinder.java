package dev.bernilai;

class RowMaxFinder implements Runnable {
    private int[][] matrix;
    private int row;
    private int max;

    public RowMaxFinder(int[][] matrix, int row) {
        this.matrix = matrix;
        this.row = row;
        this.max = Integer.MIN_VALUE;
    }

    @Override
    public void run() {
        for (int val : matrix[row]) {
            if (val > max) {
                max = val;
            }
        }
    }

    public int getMax() {
        return max;
    }
}
