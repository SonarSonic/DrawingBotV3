package drawingbot.image.blend;

import com.jhlabs.composite.*;
import drawingbot.utils.Utils;
import javafx.scene.effect.BlendMode;

import java.awt.*;
import java.awt.image.ColorModel;

public enum EnumBlendMode implements Composite{

    NORMAL(BlendMode.SRC_OVER, AlphaComposite.SrcOver, 255),
    ADD(BlendMode.ADD, new AddComposite(1.0F), 0),
    MULTIPLY(BlendMode.MULTIPLY, new MultiplyComposite(1.0F), 255),
    SCREEN(BlendMode.SCREEN, new ScreenComposite(1.0F), 0),
    OVERLAY(BlendMode.OVERLAY, new OverlayComposite(1.0F), 255),
    DARKEN(BlendMode.DARKEN, new DarkenComposite(1.0F), 255),
    LIGHTEN(BlendMode.LIGHTEN, new LightenComposite(1.0F), 128),
    COLOR_DODGE(BlendMode.COLOR_DODGE, new ColorDodgeComposite(1.0F), 128),
    COLOR_BURN(BlendMode.COLOR_BURN, new ColorBurnComposite(1.0F), 128),
    HARD_LIGHT(BlendMode.HARD_LIGHT, new HardLightComposite(1.0F), 128),
    SOFT_LIGHT(BlendMode.SOFT_LIGHT, new SoftLightComposite(1.0F), 128),
    DIFFERENCE(BlendMode.DIFFERENCE, new DifferenceComposite(1.0F), 255),
    EXCLUSION(BlendMode.EXCLUSION, new ExclusionComposite(1.0F), 255),
    RED(BlendMode.RED, new RedComposite(1.0F), 255),
    GREEN(BlendMode.GREEN, new GreenComposite(1.0F), 255),
    BLUE(BlendMode.BLUE, new BlueComposite(1.0F), 255);

    public final BlendMode jfxBlend;
    public final Composite awtComposite;
    public final int base;

    public static EnumBlendMode[] ACTIVE_MODES = new EnumBlendMode[]{NORMAL, ADD, MULTIPLY, SCREEN, DARKEN, LIGHTEN, HARD_LIGHT, DIFFERENCE, EXCLUSION, RED, GREEN, BLUE};

    EnumBlendMode(BlendMode javaFXVersion, Composite awtComposite, int base) {
        this.jfxBlend = javaFXVersion;
        this.awtComposite = awtComposite;
        this.base = base;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return awtComposite.createContext(srcColorModel, dstColorModel, hints);
    }
}
