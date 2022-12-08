package drawingbot.javafx;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.transform.Affine;

import java.awt.geom.AffineTransform;

//TODO MOVE COLORS INTO HERE TO
public class JFXAWTUtils {

    public static Affine getJFXAffineTransform(AffineTransform awt){
        Affine affine = new Affine();
        updateJFXAffineTransform(affine, awt);
        return affine;
    }

    public static void updateJFXAffineTransform(Affine jfx, AffineTransform awt){
        jfx.setToTransform(awt.getScaleX(), awt.getShearX(), awt.getTranslateX(), awt.getShearY(), awt.getScaleY(), awt.getTranslateY());
    }

    public static AffineTransform getAWTAffineTransform(Affine jfx){
        AffineTransform affine = new AffineTransform();
        updateAWTAffineTransform(affine, jfx);
        return affine;
    }

    public static void updateAWTAffineTransform(AffineTransform awt, Affine jfx){
        awt.setTransform(jfx.getMxx(), jfx.getMyx(), jfx.getMxy(), jfx.getMyy(), jfx.getTx(), jfx.getTy());
    }

    public static Rectangle2D getJFXRectangle(java.awt.geom.Rectangle2D awtRect){
        return new Rectangle2D(awtRect.getX(), awtRect.getY(), awtRect.getWidth(), awtRect.getHeight());
    }

    public static java.awt.geom.Rectangle2D getAWTRectangle(Rectangle2D jfxRect){
        return new java.awt.geom.Rectangle2D.Double(jfxRect.getMinX(), jfxRect.getMinY(), jfxRect.getWidth(), jfxRect.getHeight());
    }

    public static Bounds getJFXBounds(java.awt.geom.Rectangle2D awt){
        return new BoundingBox(awt.getX(), awt.getY(), awt.getWidth(), awt.getHeight());
    }
}
