package drawingbot.geom.masking;

import drawingbot.api.ICanvas;
import drawingbot.geom.shapes.IGeometry;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class GeometryMask {

    public List<IGeometry> subtract = new ArrayList<>();
    public List<IGeometry> add = new ArrayList<>();

    public Shape getClippingShape(ICanvas canvas){
        if(subtract.isEmpty() && add.isEmpty()){
            return null;
        }
        Area area = new Area();

        if(add.isEmpty()){
            area.add(new Area(new Rectangle2D.Float(0F, 0F, canvas.getWidth(), canvas.getHeight())));
        }else{
            for(IGeometry geometry : add){
                area.add(new Area(geometry.getAWTShape()));
            }
        }

        for(IGeometry geometry : subtract){
            area.subtract(new Area(geometry.getAWTShape()));
        }
        area.transform(AffineTransform.getScaleInstance(canvas.getPlottingScale(), canvas.getPlottingScale()));
        area.transform(AffineTransform.getTranslateInstance(-canvas.getScaledDrawingOffsetX(), -canvas.getScaledDrawingOffsetY()));
        return area;
    }
}
