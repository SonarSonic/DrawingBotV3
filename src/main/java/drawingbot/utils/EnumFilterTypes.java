package drawingbot.utils;

public enum EnumFilterTypes {
    BORDERS,
    BLUR,
    CHANNELS,
    COLOURS,
    DISTORT,
    EDGES,
    EFFECTS,
    KEYING,
    PIXELLATE,
    RENDER,
    STYLIZE,
    TEXTURE,
    TRANSITIONS,
    VIDEO;

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }
}
