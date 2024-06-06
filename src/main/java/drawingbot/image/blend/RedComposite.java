package drawingbot.image.blend;

import com.jhlabs.composite.RGBComposite;

import java.awt.*;
import java.awt.image.ColorModel;

public class RedComposite extends RGBComposite {

    public RedComposite( float alpha ) {
        super( alpha );
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new Context(extraAlpha, srcColorModel, dstColorModel);
    }

    static class Context extends RGBCompositeContext {
        public Context( float alpha, ColorModel srcColorModel, ColorModel dstColorModel ) {
            super( alpha, srcColorModel, dstColorModel );
        }

        public void composeRGB( int[] src, int[] dst, float alpha ) {
            int w = src.length;

            for ( int i = 0; i < w; i += 4 ) {
                float srcR = src[i] / 255.0f;
                float srcA = src[i+3] / 255.0f;

                float dstR = dst[i] / 255.0f;
                float dstA = dst[i+3] / 255.0f;

                float outR = srcR * srcA + dstR * dstA * (1 - srcA);
                float outA = srcA + dstA * (1 - srcA);

                dst[i] = Math.round(outR * 255);
                dst[i+3] = Math.round(outA * 255);
            }

        }
    }
}
