package drawingbot.image.filters;

import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.PixelUtils;
import com.jhlabs.image.WholeImageFilter;

import java.awt.*;

public class SplitEdgeFilter extends WholeImageFilter {

    protected float[] matrix = EdgeFilter.SOBEL_H;

    public SplitEdgeFilter() {}

    public void setEdgeMatrix(float[] matrix) {
        this.matrix = matrix;
    }

    public float[] getEdgeMatrix() {
        return matrix;
    }

    protected int[] filterPixels( int width, int height, int[] inPixels, Rectangle transformedSpace ) {
        int index = 0;
        int[] outPixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = 0, g = 0, b = 0;
                int rh = 0, gh = 0, bh = 0;
                int a = inPixels[y*width+x] & 0xff000000;

                for (int row = -1; row <= 1; row++) {
                    int iy = y+row;
                    int ioffset;
                    if (0 <= iy && iy < height)
                        ioffset = iy*width;
                    else
                        ioffset = y*width;
                    int moffset = 3*(row+1)+1;
                    for (int col = -1; col <= 1; col++) {
                        int ix = x+col;
                        if (!(0 <= ix && ix < width))
                            ix = x;
                        int rgb = inPixels[ioffset+ix];
                        float h = matrix[moffset+col];

                        r = (rgb & 0xff0000) >> 16;
                        g = (rgb & 0x00ff00) >> 8;
                        b = rgb & 0x0000ff;
                        rh += (int)(h * r);
                        gh += (int)(h * g);
                        bh += (int)(h * b);
                    }
                }
                r = (int)(Math.sqrt(rh*rh) / 1.8);
                g = (int)(Math.sqrt(gh*gh) / 1.8);
                b = (int)(Math.sqrt(bh*bh) / 1.8);
                r = PixelUtils.clamp(r);
                g = PixelUtils.clamp(g);
                b = PixelUtils.clamp(b);
                outPixels[index++] = a | (r << 16) | (g << 8) | b;
            }

        }
        return outPixels;
    }
}