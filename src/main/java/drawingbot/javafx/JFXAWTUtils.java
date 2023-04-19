package drawingbot.javafx;

import drawingbot.geom.shapes.GPath;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.*;
import javafx.scene.transform.Affine;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

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

    public static GPath convertJFXPathToGPath(Path jfxPath){
        GPath gPath = new GPath();
        addJFXPathToGPath(jfxPath, gPath);
        return gPath;
    }

    public static void addJFXPathToGPath(Path jfxPath, GPath gPath){
        for(PathElement element : jfxPath.getElements()){
            addJFXElementToAWTPath(gPath.awtPath, element);
        }
    }

    public static void addJFXElementToAWTPath(Path2D path2D, PathElement element){
        if(element instanceof MoveTo){
            MoveTo moveTo = (MoveTo) element;
            path2D.moveTo(moveTo.getX(), moveTo.getY());
        }else if(element instanceof LineTo){
            LineTo lineTo = (LineTo) element;
            path2D.lineTo(lineTo.getX(), lineTo.getY());
        }else if(element instanceof QuadCurveTo){
            QuadCurveTo quadCurveTo = (QuadCurveTo) element;
            path2D.quadTo(quadCurveTo.getControlX(), quadCurveTo.getControlY(), quadCurveTo.getX(), quadCurveTo.getY());
        }else if(element instanceof CubicCurveTo){
            CubicCurveTo cubicCurveTo = (CubicCurveTo) element;
            path2D.curveTo(cubicCurveTo.getControlX1(), cubicCurveTo.getControlY1(), cubicCurveTo.getControlX2(), cubicCurveTo.getControlY2(), cubicCurveTo.getX(), cubicCurveTo.getY());
        }else if(element instanceof ClosePath){
            path2D.closePath();
        }
    }

    public static DoubleProperty getXFromPathElement(PathElement element){
        if(element instanceof MoveTo){
            MoveTo moveTo = (MoveTo) element;
            return moveTo.xProperty();
        }else if(element instanceof LineTo){
            LineTo lineTo = (LineTo) element;
            return lineTo.xProperty();
        }else if(element instanceof QuadCurveTo){
            QuadCurveTo quadCurveTo = (QuadCurveTo) element;
            return quadCurveTo.xProperty();
        }else if(element instanceof CubicCurveTo){
            CubicCurveTo cubicCurveTo = (CubicCurveTo) element;
            return cubicCurveTo.xProperty();
        }else if(element instanceof ClosePath){
            return null;
        }
        return null;
    }

    public static DoubleProperty getYFromPathElement(PathElement element){
        if(element instanceof MoveTo){
            MoveTo moveTo = (MoveTo) element;
            return moveTo.yProperty();
        }else if(element instanceof LineTo){
            LineTo lineTo = (LineTo) element;
            return lineTo.yProperty();
        }else if(element instanceof QuadCurveTo){
            QuadCurveTo quadCurveTo = (QuadCurveTo) element;
            return quadCurveTo.yProperty();
        }else if(element instanceof CubicCurveTo){
            CubicCurveTo cubicCurveTo = (CubicCurveTo) element;
            return cubicCurveTo.yProperty();
        }else if(element instanceof ClosePath){
            return null;
        }
        return null;
    }
}
