package drawingbot.drawing;

import drawingbot.FXApplication;
import drawingbot.api.IGeometryFilter;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import drawingbot.registry.Register;
import drawingbot.utils.UnitsLength;
import drawingbot.utils.Utils;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DrawingStats {

    public LinkedHashMap<DrawingPen, Double> penStats = new LinkedHashMap<>();

    //Distance travelled in Metres
    public double distanceUpM = 0;
    public double distanceDownM = 0;

    public UnitsLength drawingUnits = UnitsLength.MILLIMETRES;
    public double pageWidth = 0;
    public double pageHeight = 0;
    public double drawingWidth = 0;
    public double drawingHeight = 0;

    public double maxX = 0;
    public double maxY = 0;
    public double minX = 0;
    public double minY = 0;

    public long geometryCount = 0;
    public long coordCount = 0;
    public long penLifts = 0;
    public long penChanges = 0;

    public DrawingStats(PlottedDrawing exportDrawing){
        updateDrawingStats(exportDrawing, IGeometryFilter.BYPASS_FILTER);
    }

    public DrawingStats(PlottedDrawing exportDrawing, IGeometryFilter filter){
        updateDrawingStats(exportDrawing, filter);
    }

    public void addDrawingStats(DrawingStats other){
        this.distanceUpM += other.distanceUpM;
        this.distanceDownM += other.distanceDownM;
        this.pageWidth = Math.max(UnitsLength.convert(other.pageWidth, other.drawingUnits, drawingUnits), this.pageWidth);
        this.pageHeight = Math.max(UnitsLength.convert(other.pageHeight, other.drawingUnits, drawingUnits), this.pageHeight);
        this.drawingWidth = Math.max(UnitsLength.convert(other.drawingWidth, other.drawingUnits, drawingUnits), this.drawingWidth);
        this.drawingHeight = Math.max(UnitsLength.convert(other.drawingHeight, other.drawingUnits, drawingUnits), this.drawingHeight);
        this.minX = Math.min(UnitsLength.convert(other.minX, other.drawingUnits, drawingUnits), this.minX);
        this.minY = Math.min(UnitsLength.convert(other.minY, other.drawingUnits, drawingUnits), this.minY);
        this.maxX = Math.max(UnitsLength.convert(other.maxX, other.drawingUnits, drawingUnits), this.maxX);
        this.maxY = Math.max(UnitsLength.convert(other.maxY, other.drawingUnits, drawingUnits), this.maxY);
        this.geometryCount += other.geometryCount;
        this.coordCount += other.coordCount;
        this.penLifts += other.penLifts;
        this.penChanges += other.penChanges;

        for(Map.Entry<DrawingPen, Double> penStat : penStats.entrySet()){
            double total = penStats.getOrDefault(penStat.getKey(), 0D) + penStat.getValue();
            penStats.put(penStat.getKey(), total);
        }
    }

    public String getDrawingStatsComment(){
        StringBuilder comment = new StringBuilder();

        comment.append("Created with %s Version: %s \n".formatted(FXApplication.getSoftware().getDisplayName(), FXApplication.getSoftware().getDisplayVersion()));
        comment.append("Date: %s  \n".formatted(Utils.getDateAndTime()));
        comment.append("Shapes: %s  \n".formatted(geometryCount));
        comment.append("Total Travel: %s m \n".formatted(distanceUpM + distanceDownM));
        comment.append("Distance Down: %s m \n".formatted(distanceDownM));
        comment.append("Distance Up: %s m \n".formatted(distanceUpM));
        comment.append("Pen Lifts: %s \n".formatted(penLifts));
        comment.append("Page Size: %s %s x %s %s \n".formatted(pageWidth, drawingUnits.getSuffix(), pageHeight, drawingUnits.getSuffix()));
        comment.append("Drawing Size: %s %s x %s %s \n".formatted(drawingWidth, drawingUnits.getSuffix(), drawingHeight, drawingUnits.getSuffix()));

        comment.append("\n");
        comment.append("Pens:\n");

        int penIndex = 1;
        for(Map.Entry<DrawingPen, Double> drawingPenEntry : penStats.entrySet()){
            comment.append(" Pen %s: %s - %s m \n".formatted(penIndex, drawingPenEntry.getKey().getName(), drawingPenEntry.getValue()));
            penIndex++;
        }

        return comment.toString();
    }

    public void updateDrawingStats(PlottedDrawing exportDrawing, IGeometryFilter filter){

        double distanceUpMM = 0;
        double distanceDownMM = 0;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        boolean hasMove;
        double lastX = 0;
        double lastY = 0;

        long geometryCount = 0;
        long coordCount = 0;
        long penLifts = 0;
        long penChanges = 0;

        double drawingScale = 1/exportDrawing.getCanvas().getPlottingScale();
        AffineTransform printTransform = AffineTransform.getScaleInstance(drawingScale, drawingScale);

        for(PlottedGroup group : exportDrawing.groups.values()) {
            if(group.drawingSet == Register.INSTANCE.EXPORT_PATH_DRAWING_SET){
                continue;
            }

            for(ObservableDrawingPen pen : PlottedDrawing.getGlobalRenderOrder(List.of(group))){

                hasMove = false; // don't count the first move.
                double perPenDistance = 0;
                for(IGeometry geometry : group.geometries) {
                    if(geometry.getPenIndex() == pen.penNumber.get() && filter.filter(exportDrawing, geometry, pen)){
                        PathIterator it = geometry.getAWTShape().getPathIterator(printTransform, 1F);
                        float[] coords = new float[6];
                        while(!it.isDone()){
                            int type = it.currentSegment(coords);
                            double distance = Point2D.distance(lastX, lastY, coords[0], coords[1]);
                            switch (type) {
                                case PathIterator.SEG_MOVETO -> {
                                    if (hasMove) {
                                        distanceUpMM += distance;
                                        penLifts++;
                                    }
                                    hasMove = true;
                                }
                                case PathIterator.SEG_LINETO -> {
                                    distanceDownMM += distance;
                                    perPenDistance += distance;
                                }
                            }
                            lastX = coords[0];
                            lastY = coords[1];

                            maxX = Math.max(maxX, lastX);
                            maxY = Math.max(maxY, lastY);
                            minX = Math.min(minX, lastX);
                            minY = Math.min(minY, lastY);

                            it.next();
                            coordCount++;
                        }
                        geometryCount++;
                    }
                }
                if(perPenDistance != 0){
                    penStats.put(new DrawingPen(pen), Utils.roundToPrecision(perPenDistance*0.001F,2));
                    penChanges++;
                }
            }
        }
        this.distanceUpM = Utils.roundToPrecision(distanceUpMM*0.001F, 2);
        this.distanceDownM = Utils.roundToPrecision(distanceDownMM*0.001F, 2);
        this.minX = Utils.roundToPrecision(minX,3);
        this.minY = Utils.roundToPrecision(minY,3);
        this.maxX = Utils.roundToPrecision(maxX,3);
        this.maxY = Utils.roundToPrecision(maxY,3);

        this.drawingUnits = exportDrawing.getCanvas().getUnits();
        this.pageWidth = Utils.roundToPrecision(exportDrawing.getCanvas().getWidth(exportDrawing.getCanvas().getUnits()),1);
        this.pageHeight = Utils.roundToPrecision(exportDrawing.getCanvas().getHeight(exportDrawing.getCanvas().getUnits()),1);
        this.drawingWidth = Utils.roundToPrecision(UnitsLength.convert(maxX - minX, UnitsLength.MILLIMETRES, drawingUnits),1);
        this.drawingHeight = Utils.roundToPrecision(UnitsLength.convert(maxY - minY, UnitsLength.MILLIMETRES, drawingUnits),1);

        this.geometryCount = geometryCount;
        this.coordCount = coordCount;
        this.penLifts = penLifts;
        this.penChanges = penChanges;
    }

    public void reset() {
        distanceUpM = 0;
        distanceDownM = 0;
        drawingUnits = UnitsLength.MILLIMETRES;
        pageWidth = 0;
        pageHeight = 0;
        drawingWidth = 0;
        drawingHeight = 0;
        minX = 0;
        minY = 0;
        maxX = 0;
        maxY = 0;
        geometryCount = 0;
        coordCount = 0;
        penLifts = 0;
        penChanges = 0;
        penStats.clear();
    }
}
