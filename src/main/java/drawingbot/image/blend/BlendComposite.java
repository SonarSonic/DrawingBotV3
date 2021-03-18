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

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;

public final class BlendComposite implements Composite {

    private float alpha;
    private final EnumBlendMode mode;

    public BlendComposite(EnumBlendMode mode) {
        this(mode, 1.0f);
    }

    private BlendComposite(EnumBlendMode mode, float alpha) {
        this.mode = mode;
        setAlpha(alpha);
    }

    public static BlendComposite getInstance(EnumBlendMode mode) {
        return new BlendComposite(mode);
    }

    public static BlendComposite getInstance(EnumBlendMode mode, float alpha) {
        return new BlendComposite(mode, alpha);
    }

    public BlendComposite derive(EnumBlendMode mode) {
        return this.mode == mode ? this : new BlendComposite(mode, getAlpha());
    }

    public BlendComposite derive(float alpha) {
        return this.alpha == alpha ? this : new BlendComposite(getMode(), alpha);
    }

    public float getAlpha() {
        return alpha;
    }

    private void setAlpha(float alpha) {
        if (alpha < 0.0f || alpha > 1.0f) {
            throw new IllegalArgumentException("alpha must be comprised between 0.0f and 1.0f");
        }
        this.alpha = alpha;
    }

    public EnumBlendMode getMode() {
        return mode;
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(alpha) * 31 + mode.ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlendComposite)) {
            return false;
        }

        BlendComposite bc = (BlendComposite) obj;

        if (mode != bc.mode) {
            return false;
        }

        return alpha == bc.alpha;
    }

    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new BlendingContext(this);
    }

}