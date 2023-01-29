package drawingbot.drawing;

import drawingbot.api.IGeometryFilter;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.registry.Register;
import drawingbot.utils.Utils;
import javafx.beans.property.*;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.*;

public class DrawingStats {

    //calculated
    public double distanceUpMM = 0;
    public double distanceDownMM = 0;

    public double maxX = 0;
    public double maxY = 0;

    public long geometryCount = 0;
    public long coordCount = 0;
    public long penLifts = 0;
    public LinkedHashMap<DrawingPen, Double> penStats = new LinkedHashMap<>();

    public DrawingStats(PlottedDrawing exportDrawing){
        updateDrawingStats(exportDrawing, IGeometryFilter.BYPASS_FILTER);
    }

    public DrawingStats(PlottedDrawing exportDrawing, IGeometryFilter filter){
        updateDrawingStats(exportDrawing, filter);
    }

    public void addDrawingStats(DrawingStats other){
        this.distanceUpMM += other.distanceUpMM;
        this.distanceDownMM += other.distanceDownMM;
        this.maxX = Math.max(this.maxX, other.maxX);
        this.maxX = Math.max(this.maxY, other.maxY);
        this.geometryCount += other.geometryCount;
        this.coordCount += other.coordCount;
        this.penLifts += other.penLifts;
    }

    public void updateDrawingStats(PlottedDrawing exportDrawing, IGeometryFilter filter){

        double distanceUpMM = 0;
        double distanceDownMM = 0;
        double maxX = 0;
        double maxY = 0;

        boolean hasMove;
        double lastX = 0;
        double lastY = 0;

        long geometryCount = 0;
        long coordCount = 0;
        long penLifts = 0;
        long penChanges = 0;

        //AffineTransform printTransform = CanvasUtils.createCanvasScaleTransform(exportDrawing.getCanvas());

        for(PlottedGroup group : exportDrawing.groups.values()) {
            if(group.drawingSet == Register.INSTANCE.EXPORT_PATH_DRAWING_SET){
                continue;
            }

            for(ObservableDrawingPen pen : PlottedDrawing.getGlobalRenderOrder(List.of(group))){

                hasMove = false; // don't count the first move.
                double perPenDistance = 0;
                for(IGeometry geometry : group.geometries) {
                    if(geometry.getPenIndex() == pen.penNumber.get() && filter.filter(exportDrawing, geometry, pen)){
                        PathIterator it = geometry.getAWTShape().getPathIterator(null, 1F);
                        float[] coords = new float[6];
                        while(!it.isDone()){
                            int type = it.currentSegment(coords);
                            double distance = Point2D.distance(lastX, lastY, coords[0], coords[1]);
                            switch (type){
                                case PathIterator.SEG_MOVETO:
                                    if(hasMove){
                                        distanceUpMM += distance;
                                        penLifts ++;
                                    }
                                    hasMove = true;
                                    break;
                                case PathIterator.SEG_LINETO:
                                    distanceDownMM += distance;
                                    perPenDistance += distance;
                                    break;
                            }
                            lastX = coords[0];
                            lastY = coords[1];
                            maxX = Math.max(maxX, lastX);
                            maxY = Math.max(maxY, lastY);
                            it.next();
                            coordCount++;
                        }
                        geometryCount++;
                    }
                }
                if(perPenDistance != 0){
                    penStats.put(new DrawingPen(pen), Utils.roundToPrecision(perPenDistance*0.001F,3));
                    penChanges++;
                }
            }
        }
        this.distanceUpMM = Utils.roundToPrecision(distanceUpMM*0.001F, 3);
        this.distanceDownMM = Utils.roundToPrecision(distanceDownMM*0.001F, 3);
        this.maxX = Utils.roundToPrecision(maxX,3);
        this.maxY = Utils.roundToPrecision(maxY,3);
        this.geometryCount = geometryCount;
        this.coordCount = coordCount;
        this.penLifts = penLifts;
    }

    public void reset() {
        distanceUpMM = 0;
        distanceDownMM = 0;
        maxX = 0;
        maxY = 0;
        geometryCount = 0;
        coordCount = 0;
        penLifts = 0;
        penStats.clear();
    }
}
