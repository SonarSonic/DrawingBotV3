/*
 * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package drawingbot.image.blend;

import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

final class BlendingContext implements CompositeContext {
    private final Blender blender;
    private final BlendComposite composite;

    BlendingContext(BlendComposite composite) {
        this.composite = composite;
        this.blender = composite.getMode();
    }

    public void dispose() {}

    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        if (src.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
                dstIn.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
                dstOut.getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
            throw new IllegalStateException("Source and destination must store pixels as INT.");
        }

        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());

        float alpha = composite.getAlpha();

        int[] srcPixel = new int[4];
        int[] dstPixel = new int[4];
        int[] srcPixels = new int[width];
        int[] dstPixels = new int[width];

        for (int y = 0; y < height; y++) {
            src.getDataElements(0, y, width, 1, srcPixels);
            dstIn.getDataElements(0, y, width, 1, dstPixels);
            for (int x = 0; x < width; x++) {
                // pixels are stored as INT_ARGB
                // our arrays are [R, G, B, A]
                int pixel = srcPixels[x];
                srcPixel[0] = (pixel >> 16) & 0xFF;
                srcPixel[1] = (pixel >> 8) & 0xFF;
                srcPixel[2] = (pixel) & 0xFF;
                srcPixel[3] = (pixel >> 24) & 0xFF;

                pixel = dstPixels[x];
                dstPixel[0] = (pixel >> 16) & 0xFF;
                dstPixel[1] = (pixel >> 8) & 0xFF;
                dstPixel[2] = (pixel) & 0xFF;
                dstPixel[3] = (pixel >> 24) & 0xFF;

                int[] result = blender.blend(srcPixel, dstPixel);

                // mixes the result with the opacity
                dstPixels[x] = ((int) (dstPixel[3] + (result[3] - dstPixel[3]) * alpha) & 0xFF) << 24 |
                        ((int) (dstPixel[0] + (result[0] - dstPixel[0]) * alpha) & 0xFF) << 16 |
                        ((int) (dstPixel[1] + (result[1] - dstPixel[1]) * alpha) & 0xFF) << 8 |
                        (int) (dstPixel[2] + (result[2] - dstPixel[2]) * alpha) & 0xFF;
            }
            dstOut.setDataElements(0, y, width, 1, dstPixels);
        }
    }
}
