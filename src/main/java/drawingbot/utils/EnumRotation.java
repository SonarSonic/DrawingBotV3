package drawingbot.utils;

import org.imgscalr.Scalr;

public enum EnumRotation {
    AUTO(null, false),
    R0(null, false),
    R90(Scalr.Rotation.CW_90, true),
    R180(Scalr.Rotation.CW_180, false),
    R270(Scalr.Rotation.CW_270, true);

    public static final EnumRotation[] DEFAULTS = new EnumRotation[]{EnumRotation.R0, EnumRotation.R90, EnumRotation.R180, EnumRotation.R270};

    public final Scalr.Rotation scalrRotation;
    public final boolean flipAxis;

    EnumRotation(Scalr.Rotation scalrRotation, boolean flipAxis){
        this.scalrRotation = scalrRotation;
        this.flipAxis = flipAxis;
    }

    @Override
    public String toString() {
        if(this == AUTO){
            return "Auto";
        }
        return name().substring(1) + " " + '\u00B0';
    }
}
