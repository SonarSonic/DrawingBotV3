package drawingbot.utils;

import processing.core.PConstants;

public enum EnumBlendMode {

    NONE(PConstants.REPLACE, false), //NOT SUPPORTED BY JAVAFX
    BLEND(PConstants.BLEND, false),
    ADD(PConstants.ADD, true),
    SUBTRACT(PConstants.SUBTRACT, false), //NOT SUPPORTED BY JAVAFX
    LIGHTEST(PConstants.LIGHTEST, true),
    DARKEST(PConstants.DARKEST, false),
    DIFFERENCE(PConstants.DIFFERENCE, false),
    EXCLUSION(PConstants.EXCLUSION, false),
    MULTIPLY(PConstants.MULTIPLY, false),
    SCREEN(PConstants.SCREEN, true),
    OVERLAY(PConstants.OVERLAY, true),
    HARD_LIGHT(PConstants.HARD_LIGHT, true),
    SOFT_LIGHT(PConstants.SOFT_LIGHT, true),
    DODGE(PConstants.DODGE, true),
    BURN(PConstants.BURN, true);

    public int constant;
    public boolean additive; //additive renderers should begin with black

    EnumBlendMode(int constant, boolean additive) {
        this.constant = constant;
        this.additive = additive;
    }

    @Override
    public String toString() {
        return "Blend Mode: " + Utils.capitalize(name());
    }
}