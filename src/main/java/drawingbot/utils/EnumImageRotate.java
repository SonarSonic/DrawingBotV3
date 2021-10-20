package drawingbot.utils;

import org.imgscalr.Scalr;

public enum EnumImageRotate {
    R0(null, false),
    R90(Scalr.Rotation.CW_90, true),
    R180(Scalr.Rotation.CW_180, false),
    R270(Scalr.Rotation.CW_270, true);

    public Scalr.Rotation scalrRotation;
    public boolean flipAxis;

    EnumImageRotate(Scalr.Rotation scalrRotation, boolean flipAxis){
        this.scalrRotation = scalrRotation;
        this.flipAxis = flipAxis;
    }

    @Override
    public String toString() {
        return name().substring(1) + " " + '\u00B0';
    }
}
