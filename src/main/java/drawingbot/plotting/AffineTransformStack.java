package drawingbot.plotting;

import java.awt.geom.AffineTransform;

//following the designs in JOML
public class AffineTransformStack extends AffineTransform {

    private final AffineTransform[] transforms;
    private int curr;

    public AffineTransformStack(int stackSize) {
        if (stackSize < 1) {
            throw new IllegalArgumentException("stackSize must be >= 1");
        }
        transforms = new AffineTransform[stackSize - 1];
        // Allocate all matrices up front to keep the promise of being "allocation-free"
        for (int i = 0; i < transforms.length; i++) {
            transforms[i] = new AffineTransform();
        }
    }

    public void clear() {
        curr = 0;
        setToIdentity();
    }

    public void pushMatrix() {
        if (curr == transforms.length) {
            throw new IllegalStateException("max stack size of " + (curr + 1) + " reached");
        }
        transforms[curr++].setTransform(this);
    }

    public void popMatrix() {
        if (curr == 0) {
            throw new IllegalStateException("already at the bottom of the stack");
        }
        setTransform(transforms[--curr]);
    }

}
