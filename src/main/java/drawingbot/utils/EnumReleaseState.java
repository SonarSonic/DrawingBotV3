package drawingbot.utils;

import javafx.scene.paint.Color;

public enum EnumReleaseState {
    RELEASE(new Color(0/255F, 255/255F, 147/255F, 1.0)),
    BETA(new Color(255/255F, 213/255F, 58/255F, 1.0)),
    ALPHA(new Color(227/255F, 84/255F, 84/255F, 1.0)),
    EXPERIMENTAL(new Color(156/255F, 84/255F, 222/255F, 1.0)); // Developer Only

    public Color color;

    EnumReleaseState(Color color) {
        this.color = color;
    }

    public boolean isRelease(){
        return this == RELEASE;
    }

    public boolean isBeta(){
        return this == BETA;
    }

    public boolean isAlpha(){
        return this == ALPHA;
    }

    public boolean isExperimental(){
        return this == EXPERIMENTAL;
    }

    public String getDisplayName() {
        return Utils.capitalize(name());
    }
}
