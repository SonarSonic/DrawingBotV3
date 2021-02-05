package drawingbot.image;

public class ConvolutionMatrices {

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final float[][] MATRIX_GAUSSIAN_BLUR = new float[][]{
            {1, 4, 6, 4, 1},//
            {4, 16, 24, 16, 4},//
            {6, 24, 36, 24, 6},//
            {4, 16, 24, 16, 4},//
            {1, 4, 6, 4, 1}};


    public static final float[][] MATRIX_UNSHARP_MASK = new float[][] {
            {-1/256F, -4/256F, -6/256F, -4/256F, -1/256F},//
            {-4/256F, -16/256F, -24/256F, -16/256F, -4/256F},//
            {-6/256F, -24/256F,  476/256F, -24/256F, -6/256F},//
            {-4/256F, -16/256F, -24/256F, -16/256F, -4/256F},//
            {-1/256F, -4/256F, -6/256F, -4/256F, -1/256F}};

    public static final float[][] MATRIX_UNSHARP_MASK_NORMALISED = new float[][]{
            {1, 4, 6, 4, 1},//
            {4, 16, 24, 16, 4},//
            {6, 24, -476, 24, 6},//
            {4, 16, 24, 16, 4},//
            {1, 4, 6, 4, 1}};

    public static final float[][] MATRIX_MOTION_BLUR = new float[][]{
            {1, 0, 0, 0, 0, 0, 0, 0, 0},//
            {0, 1, 0, 0, 0, 0, 0, 0, 0},//
            {0, 0, 1, 0, 0, 0, 0, 0, 0},//
            {0, 0, 0, 1, 0, 0, 0, 0, 0},//
            {0, 0, 0, 0, 1, 0, 0, 0, 0},//
            {0, 0, 0, 0, 0, 1, 0, 0, 0},//
            {0, 0, 0, 0, 0, 0, 1, 0, 0},//
            {0, 0, 0, 0, 0, 0, 0, 1, 0},//
            {0, 0, 0, 0, 0, 0, 0, 0, 1}};

    public static final float[][] MATRIX_OUTLINE = new float[][]{
            {1,  1,  1,  1,  1},//
            {1,  0,  0,  0,  1},//
            {1,  0, -16, 0,  1},//
            {1,  0,   0,  0,  1},//
            {1,  1,   1,  1,  1 }};

    public static final float[][] MATRIX_EDGE_DETECT = new float[][]{
            {0,  1,  0},//
            {1, -4,  1},//
            {0,  1,  0}};

    public static final float[][] MATRIX_EMBOSS = new float[][]{
            {-2, -1, 0},//
            {-1, 1, 1},//
            {0, 1, 2}};

    public static final float[][] MATRIX_SHARPEN = new float[][]{
            {0, -1,  0},//
            {-1,  5, -1},//
            {0, -1,  0 }};

    public static final float[][] MATRIX_BLUR = new float[][]{
            {1, 1, 1},//
            {1, 1, 1},//
            {1, 1, 1 }};

    public static final float[][] MATRIX_SOBEL_X = new float[][]{
            {-1, 0, 1},//
            {-2, 0, 2},//
            {-1, 0, 1 }};

    public static final float[][] MATRIX_SOBEL_Y = new float[][]{
            {-1, -2, -1},//
            {0,  0,  0},//
            {1,  2,  1 }};

    ///////////////////////////////////////////////////////////////////////////////////////////////////////



}
