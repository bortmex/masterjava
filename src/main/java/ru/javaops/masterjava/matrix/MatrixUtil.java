package ru.javaops.masterjava.matrix;

import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {
    static class Processor {

        private int[] matrixA;
        private int[] matrixB;

        public Processor(int[] matrixA, int[] matrixB) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
        }

        public static int[] mult(int[] matrixA, int[] matrixB) {
            int[] matrixC = new int[matrixA.length];
            for (int i = 0; i < matrixA.length; i++) {
                matrixC[i] = matrixA[i] * matrixB[i];
            }
            return matrixC;
        }
    }

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        class ColumnMultResult {
            private final int col;
            private final int[] columnC;

            private ColumnMultResult(int col, int[] columnC) {
                this.col = col;
                this.columnC = columnC;
            }
        }

        final CompletionService<ColumnMultResult> completionService = new ExecutorCompletionService<>(executor);

        for (int i = 0; i < matrixSize; i++) {
            final int col = i;
            final int[] colB = new int[matrixSize];
            for (int j = 0; j < matrixSize; j++) {
                colB[j] = matrixB[j][i];
            }
            completionService.submit(() -> {
                final int[] columResult = new int[matrixSize];

                for (int j = 0; j < matrixSize; j++) {
                    final int[] columnA = matrixA[j];
                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += colB[k] * columnA[k];
                    }
                    columResult[j] = sum;
                }

                return new ColumnMultResult(col, columResult);
            });
        }
            for (int j = 0; j < matrixSize; j++) {
                ColumnMultResult res = completionService.take().get();
                for (int k = 0; k < matrixSize; k++) {
                    matrixC[k][res.col] = res.columnC[k];
                }
            }

        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final int aColumns = matrixA.length;
        final int aRows = matrixA[0].length;
        final int bColumns = matrixB.length;
        final int bRows = matrixB[0].length;

        double thatColumn[] = new double[bRows];

        try {
            for (int j = 0; ; j++) {
                for (int k = 0; k < aColumns; k++) {
                    thatColumn[k] = matrixB[k][j];
                }

                for (int i = 0; i < aRows; i++) {
                    int thisRow[] = matrixA[i];
                    int summand = 0;
                    for (int k = 0; k < aColumns; k++) {
                        summand += thisRow[k] * thatColumn[k];
                    }
                    matrixC[i][j] = summand;
                }
            }
        } catch (IndexOutOfBoundsException ignored) {
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
