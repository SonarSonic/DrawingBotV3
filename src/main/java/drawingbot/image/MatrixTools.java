package drawingbot.image;

import drawingbot.DrawingBotV3;

import java.awt.image.Kernel;

/**
 * A class of matrix operations from V2, currently barely used.
 */
public class MatrixTools {

    public static Kernel matrixToKernal(float[][] matrix){
        int height = matrix.length;
        int width = matrix[0].length;
        float[] kernalMatrix = new float[height*width];

        int pos = 0;
        for(float[] row : matrix){
            for(float value : row){
                kernalMatrix[pos] = value;
                pos++;
            }
        }

        return new Kernel(width, height, kernalMatrix);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Source:  https://en.wikipedia.org/wiki/Matrix_multiplication_algorithm
     * Test:    http://www.calcul.com/show/calculator/matrix-multiplication_;2;3;3;5
     * @param matrixA
     * @param matrixB
     * @return
     */
    public static float [][] multiplyMatrix(float[][] matrixA, float[][] matrixB) {

        int n = matrixA.length;      // matrixA rows
        int m = matrixA[0].length;   // matrixA columns
        int p = matrixB[0].length;

        float[][] matrixC;
        matrixC = new float[n][p];

        for (int i=0; i<n; i++) {
            for (int j=0; j<p; j++) {
                for (int k=0; k<m; k++) {
                    matrixC[i][j] = matrixC[i][j] + matrixA[i][k] * matrixB[k][j];
                }
            }
        }
        return matrixC;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Source:  https://www.taylorpetrick.com/blog/post/convolution-part2
     * Useful for keeping brightness the same.
     * Do not use on a maxtix that sums to zero, such as sobel.
     * @param matrix
     * @return The resulting matrix is the same size as the original, but the output range will be constrained between 0.0 and 1.0
     */
    public static float [][] normalizeMatrix(float[][] matrix) {
        int n = matrix.length;      // rows
        int m = matrix[0].length;   // columns
        float sum = 0;

        for (int i=0; i<n; i++) {
            for (int j=0; j<m; j++) {
                sum += matrix[i][j];
            }
        }

        for (int i=0; i<n; i++) {
            for (int j=0; j<m; j++) {
                matrix[i][j] = matrix[i][j] / Math.abs(sum);
            }
        }

        return matrix;
    }

    public static float [][] scaleMatrix(float[][] matrix, int scale) {
        int n = matrix.length;      // rows
        int p = matrix[0].length;   // columns

        float [][] nmatrix = new float[n*scale][p*scale];

        for (int i=0; i<n; i++){
            for (int j=0; j<p; j++){
                for (int si=0; si<scale; si++){
                    for (int sj=0; sj<scale; sj++){
                        int a1 = (i*scale)+si;
                        int a2 = (j*scale)+sj;
                        float a3 = matrix[i][j];
                        nmatrix[a1][a2] = a3;
                    }
                }
            }
        }
        return nmatrix;
    }

    public static void printMatrix(float[][] matrix) {
        int n = matrix.length;      // rows
        int p = matrix[0].length;   // columns
        float sum = 0;

        for (int i=0; i<n; i++){
            for (int j=0; j<p; j++){
                sum += matrix[i][j];
                DrawingBotV3.logger.fine("%10.5f " + matrix[i][j]);
            }
        }
        DrawingBotV3.logger.fine("Sum: " + sum);
    }
}
