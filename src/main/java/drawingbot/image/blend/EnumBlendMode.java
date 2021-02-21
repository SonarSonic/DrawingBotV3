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

import drawingbot.utils.Utils;
import javafx.scene.effect.BlendMode;

public enum EnumBlendMode implements Blender {

    NORMAL(BlendMode.SRC_OVER, false, (src, dst) -> src),

    ADD(BlendMode.ADD, true, (src, dst) -> new int[]{
            Math.min(255, src[0] + dst[0]),
            Math.min(255, src[1] + dst[1]),
            Math.min(255, src[2] + dst[2]),
            Math.min(255, src[3] + dst[3])
    }),
    MULTIPLY(BlendMode.MULTIPLY, false, (src, dst) -> new int[]{
            (src[0] * dst[0]) >> 8,
            (src[1] * dst[1]) >> 8,
            (src[2] * dst[2]) >> 8,
            Math.min(255, src[3] + dst[3])
    }),
    SCREEN(BlendMode.SCREEN, true, (src, dst) -> new int[]{
            255 - ((255 - src[0]) * (255 - dst[0]) >> 8),
            255 - ((255 - src[1]) * (255 - dst[1]) >> 8),
            255 - ((255 - src[2]) * (255 - dst[2]) >> 8),
            Math.min(255, src[3] + dst[3])
    }),
    /*  TODO MAYBE THESE ONES SHOULD RENDER TWICE
    OVERLAY(BlendMode.OVERLAY, true, (src, dst) -> new int[]{ //TEST
            dst[0] < 128 ? dst[0] * src[0] >> 7 :255 - ((255 - dst[0]) * (255 - src[0]) >> 7),
            dst[1] < 128 ? dst[1] * src[1] >> 7 : 255 - ((255 - dst[1]) * (255 - src[1]) >> 7),
            dst[2] < 128 ? dst[2] * src[2] >> 7 : 255 - ((255 - dst[2]) * (255 - src[2]) >> 7),
            Math.min(255, src[3] + dst[3])
    }),
     */
    DARKEN(BlendMode.DARKEN, false, (src, dst) -> new int[]{
            Math.min(src[0], dst[0]),
            Math.min(src[1], dst[1]),
            Math.min(src[2], dst[2]),
            Math.min(255, src[3] + dst[3])
    }),
    LIGHTEN(BlendMode.LIGHTEN, true, (src, dst) -> new int[]{
            Math.max(src[0], dst[0]),
            Math.max(src[1], dst[1]),
            Math.max(src[2], dst[2]),
            Math.min(255, src[3] + dst[3])
    }),
    /*
    COLOR_DODGE(BlendMode.COLOR_DODGE, true, (src, dst) -> new int[]{ //TEST
            src[0] == 255 ? 255 : Math.min((dst[0] << 8) / (255 - src[0]), 255),
            src[1] == 255 ? 255 : Math.min((dst[1] << 8) / (255 - src[1]), 255),
            src[2] == 255 ? 255 : Math.min((dst[2] << 8) / (255 - src[2]), 255),
            Math.min(255, src[3] + dst[3])
    }),
    COLOR_BURN(BlendMode.COLOR_BURN, true, (src, dst) -> new int[]{ //TEST
            src[0] == 0 ? 0 : Math.max(0, 255 - (((255 - dst[0]) << 8) / src[0])),
            src[1] == 0 ? 0 : Math.max(0, 255 - (((255 - dst[1]) << 8) / src[1])),
            src[2] == 0 ? 0 : Math.max(0, 255 - (((255 - dst[2]) << 8) / src[2])),
            Math.min(255, src[3] + dst[3])
    }),
    */
    HARD_LIGHT(BlendMode.HARD_LIGHT, true, (src, dst) -> new int[]{
            src[0] < 128 ? dst[0] * src[0] >> 7 : 255 - ((255 - src[0]) * (255 - dst[0]) >> 7),
            src[1] < 128 ? dst[1] * src[1] >> 7 : 255 - ((255 - src[1]) * (255 - dst[1]) >> 7),
            src[2] < 128 ? dst[2] * src[2] >> 7 : 255 - ((255 - src[2]) * (255 - dst[2]) >> 7),
            Math.min(255, src[3] + dst[3])
    }),
    DIFFERENCE(BlendMode.DIFFERENCE, false, (src, dst) -> new int[]{
            Math.abs(dst[0] - src[0]),
            Math.abs(dst[1] - src[1]),
            Math.abs(dst[2] - src[2]),
            Math.min(255, src[3] + dst[3])
    }),
    EXCLUSION(BlendMode.EXCLUSION, false, (src, dst) -> new int[]{
            dst[0] + src[0] - (dst[0] * src[0] >> 7),
            dst[1] + src[1] - (dst[1] * src[1] >> 7),
            dst[2] + src[2] - (dst[2] * src[2] >> 7),
            Math.min(255, src[3] + dst[3])
    }),
    RED(BlendMode.RED, false, (src, dst) -> new int[]{
            src[0],
            dst[1],
            dst[2],
            Math.min(255, src[3] + dst[3])
    }),
    GREEN(BlendMode.GREEN, false, (src, dst) -> new int[]{
            dst[0],
            dst[1],
            src[2],
            Math.min(255, src[3] + dst[3])
    }),
    BLUE(BlendMode.BLUE, false, (src, dst) -> new int[]{
            dst[0],
            src[1],
            dst[2],
            Math.min(255, src[3] + dst[3])
    });

    /* TODO Should we try and support these other blend modes, if so we need to use a Swing Canvas which has a Graphics2D instead of a javafx canvas
    SOFT_LIGHT(((src, dst) -> src)), //NOT SUPPORTED

    SOFT_BURN((src, dst) -> new int[]{
            dst[0] + src[0] < 256 ? (dst[0] == 255 ? 255 : Math.min(255, (src[0] << 7) / (255 - dst[0]))) : Math.max(0, 255 - (((255 - dst[0]) << 7) / src[0])),
            dst[1] + src[1] < 256 ? (dst[1] == 255 ? 255 : Math.min(255, (src[1] << 7) / (255 - dst[1]))) : Math.max(0, 255 - (((255 - dst[1]) << 7) / src[1])),
            dst[2] + src[2] < 256 ? (dst[2] == 255 ? 255 : Math.min(255, (src[2] << 7) / (255 - dst[2]))) : Math.max(0, 255 - (((255 - dst[2]) << 7) / src[2])),
            Math.min(255, src[3] + dst[3])
    }),
    SOFT_DODGE((src, dst) -> new int[]{
            dst[0] + src[0] < 256 ? (src[0] == 255 ? 255 : Math.min(255, (dst[0] << 7) / (255 - src[0]))) : Math.max(0, 255 - (((255 - src[0]) << 7) / dst[0])),
            dst[1] + src[1] < 256 ? (src[1] == 255 ? 255 : Math.min(255, (dst[1] << 7) / (255 - src[1]))) : Math.max(0, 255 - (((255 - src[1]) << 7) / dst[1])),
            dst[2] + src[2] < 256 ? (src[2] == 255 ? 255 : Math.min(255, (dst[2] << 7) / (255 - src[2]))) : Math.max(0, 255 - (((255 - src[2]) << 7) / dst[2])),
            Math.min(255, src[3] + dst[3])
    }),
    AVERAGE((src, dst) -> new int[]{
            (src[0] + dst[0]) >> 1,
            (src[1] + dst[1]) >> 1,
            (src[2] + dst[2]) >> 1,
            Math.min(255, src[3] + dst[3])
    }),
    COLOR((src, dst) -> {
        float[] srcHSL = new float[3];
        Blender.RGBtoHSL(src[0], src[1], src[2], srcHSL);
        float[] dstHSL = new float[3];
        Blender.RGBtoHSL(dst[0], dst[1], dst[2], dstHSL);

        int[] result = new int[4];
        Blender.HSLtoRGB(srcHSL[0], srcHSL[1], dstHSL[2], result);
        result[3] = Math.min(255, src[3] + dst[3]);
        return result;
    }),
    FREEZE((src, dst) -> new int[]{
            src[0] == 0 ? 0 : Math.max(0, 255 - (255 - dst[0]) * (255 - dst[0]) / src[0]),
            src[1] == 0 ? 0 : Math.max(0, 255 - (255 - dst[1]) * (255 - dst[1]) / src[1]),
            src[2] == 0 ? 0 : Math.max(0, 255 - (255 - dst[2]) * (255 - dst[2]) / src[2]),
            Math.min(255, src[3] + dst[3])
    }),
    GLOW((src, dst) -> new int[]{
            dst[0] == 255 ? 255 : Math.min(255, src[0] * src[0] / (255 - dst[0])),
            dst[1] == 255 ? 255 : Math.min(255, src[1] * src[1] / (255 - dst[1])),
            dst[2] == 255 ? 255 : Math.min(255, src[2] * src[2] / (255 - dst[2])),
            Math.min(255, src[3] + dst[3])
    }),
    HEAT((src, dst) -> new int[]{
            dst[0] == 0 ? 0 : Math.max(0, 255 - (255 - src[0]) * (255 - src[0]) / dst[0]),
            dst[1] == 0 ? 0 : Math.max(0, 255 - (255 - src[1]) * (255 - src[1]) / dst[1]),
            dst[2] == 0 ? 0 : Math.max(0, 255 - (255 - src[2]) * (255 - src[2]) / dst[2]),
            Math.min(255, src[3] + dst[3])
    }),
    HUE((src, dst) -> {
        float[] srcHSL = new float[3];
        Blender.RGBtoHSL(src[0], src[1], src[2], srcHSL);
        float[] dstHSL = new float[3];
        Blender.RGBtoHSL(dst[0], dst[1], dst[2], dstHSL);

        int[] result = new int[4];
        Blender.HSLtoRGB(srcHSL[0], dstHSL[1], dstHSL[2], result);
        result[3] = Math.min(255, src[3] + dst[3]);

        return result;
    }),
    INVERSE_COLOR_BURN((src, dst) -> new int[]{
            dst[0] == 0 ? 0 : Math.max(0, 255 - (((255 - src[0]) << 8) / dst[0])),
            dst[1] == 0 ? 0 : Math.max(0, 255 - (((255 - src[1]) << 8) / dst[1])),
            dst[2] == 0 ? 0 : Math.max(0, 255 - (((255 - src[2]) << 8) / dst[2])),
            Math.min(255, src[3] + dst[3])
    }),
    INVERSE_COLOR_DODGE((src, dst) -> new int[]{
            dst[0] == 255 ? 255 : Math.min((src[0] << 8) / (255 - dst[0]), 255),
            dst[1] == 255 ? 255 : Math.min((src[1] << 8) / (255 - dst[1]), 255),
            dst[2] == 255 ? 255 : Math.min((src[2] << 8) / (255 - dst[2]), 255),
            Math.min(255, src[3] + dst[3])
    }),
    LUMINOSITY((src, dst) -> {
        float[] srcHSL = new float[3];
        Blender.RGBtoHSL(src[0], src[1], src[2], srcHSL);
        float[] dstHSL = new float[3];
        Blender.RGBtoHSL(dst[0], dst[1], dst[2], dstHSL);

        int[] result = new int[4];
        Blender.HSLtoRGB(dstHSL[0], dstHSL[1], srcHSL[2], result);
        result[3] = Math.min(255, src[3] + dst[3]);

        return result;
    }),
    NEGATION((src, dst) -> new int[]{
            255 - Math.abs(255 - dst[0] - src[0]),
            255 - Math.abs(255 - dst[1] - src[1]),
            255 - Math.abs(255 - dst[2] - src[2]),
            Math.min(255, src[3] + dst[3])
    }),
    REFLECT((src, dst) -> new int[]{
            src[0] == 255 ? 255 : Math.min(255, dst[0] * dst[0] / (255 - src[0])),
            src[1] == 255 ? 255 : Math.min(255, dst[1] * dst[1] / (255 - src[1])),
            src[2] == 255 ? 255 : Math.min(255, dst[2] * dst[2] / (255 - src[2])),
            Math.min(255, src[3] + dst[3])
    }),
    SATURATION((src, dst) -> {
        float[] srcHSL = new float[3];
        Blender.RGBtoHSL(src[0], src[1], src[2], srcHSL);
        float[] dstHSL = new float[3];
        Blender.RGBtoHSL(dst[0], dst[1], dst[2], dstHSL);

        int[] result = new int[4];
        Blender.HSLtoRGB(dstHSL[0], srcHSL[1], dstHSL[2], result);
        result[3] = Math.min(255, src[3] + dst[3]);

        return result;
    }),

    STAMP((src, dst) -> new int[]{
            Math.max(0, Math.min(255, dst[0] + 2 * src[0] - 256)),
            Math.max(0, Math.min(255, dst[1] + 2 * src[1] - 256)),
            Math.max(0, Math.min(255, dst[2] + 2 * src[2] - 256)),
            Math.min(255, src[3] + dst[3])
    }),

    SUBTRACT((src, dst) -> new int[]{
            Math.max(0, src[0] + dst[0] - 256),
            Math.max(0, src[1] + dst[1] - 256),
            Math.max(0, src[2] + dst[2] - 256),
            Math.min(255, src[3] + dst[3])
    });

     */

    public BlendMode javaFXVersion;
    public boolean additive;
    public Blender blender;

    EnumBlendMode(BlendMode javaFXVersion, boolean additive, Blender blender) {
        this.javaFXVersion = javaFXVersion;
        this.additive = additive;
        this.blender = blender;
    }

    @Override
    public int[] blend(int[] src, int[] dst) {
        return blender.blend(src, dst);
    }

    @Override
    public String toString() {
        return "Blend Mode: " + Utils.capitalize(name());
    }
}
