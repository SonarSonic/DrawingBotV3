package drawingbot.geom.operation;

import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.GPath;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottedGroup;
import drawingbot.utils.Utils;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public class PlottedDrawingSplitter {

    private final double targetDistance;

    private final PlottedDrawing inputDrawing;
    private PlottedGroup inputGroup;
    private IGeometry inputGeometry;

    private PlottedDrawing outDrawing;
    private PlottedGroup outGroup;
    private GPath outPath;

    private List<PlottedDrawing> outputDrawings;

    private boolean processed = false;

    public PlottedDrawingSplitter(double targetDistance, PlottedDrawing inputDrawing) {
        this.targetDistance = targetDistance;
        this.inputDrawing = inputDrawing;
        this.outputDrawings = new ArrayList<>();
    }

    public List<PlottedDrawing> getOutputDrawings() {
        split();
        return outputDrawings;
    }

    private void split(){
        if(processed){
            return;
        }
        processed = true;
        double currentDistance = 0;

        startPlottedDrawing();
        for(PlottedGroup group : inputDrawing.groups.values()) {
            inputGroup = group;
            startGroup();
            for(IGeometry geometry : group.geometries) {
                inputGeometry = geometry;
                startPath();

                PathIterator pathIterator = geometry.getAWTShape().getPathIterator(null);
                float[] coords = new float[6];

                float nextX, nextY;
                float lastX = Float.NaN, lastY = Float.NaN;
                float lastMoveX = Float.NaN, lastMoveY = Float.NaN;
                boolean hasLastSegment = false;
                double segmentLength;

                while (!pathIterator.isDone()){
                    int type = pathIterator.currentSegment(coords);

                    switch (type){
                        case PathIterator.SEG_MOVETO -> {
                            lastMoveX = nextX = coords[0];
                            lastMoveY = nextY = coords[1];
                            segmentLength = 0;
                        }
                        case PathIterator.SEG_LINETO -> {
                            nextX = coords[0];
                            nextY = coords[1];
                            segmentLength = Utils.distance(lastX, lastY, nextX, nextY);
                        }
                        case PathIterator.SEG_QUADTO -> {
                            nextX = coords[2];
                            nextY = coords[3];
                            segmentLength = Utils.distance(lastX, lastY, nextX, nextY);
                        }
                        case PathIterator.SEG_CUBICTO -> {
                            nextX = coords[3];
                            nextY = coords[4];
                            segmentLength = Utils.distance(lastX, lastY, nextX, nextY);
                        }
                        case PathIterator.SEG_CLOSE -> {
                            nextX = lastMoveX;
                            nextY = lastMoveY;
                            segmentLength = Utils.distance(lastX, lastY, nextX, nextY);
                        }
                        default -> throw new UnsupportedOperationException("Unknown segment type:" + type);
                    }

                    if (hasLastSegment && currentDistance + segmentLength > targetDistance) {
                        endPath();
                        endGroup();
                        endPlottedDrawing();

                        startPlottedDrawing();
                        startGroup();
                        startPath();

                        currentDistance = 0;

                        if(type != PathIterator.SEG_MOVETO){
                            outPath.moveTo(lastX, lastY);
                        }
                    }

                    switch (type){
                        case PathIterator.SEG_MOVETO -> {
                            outPath.moveTo(coords[0], coords[1]);
                        }
                        case PathIterator.SEG_LINETO -> {
                            outPath.lineTo(coords[0], coords[1]);
                        }
                        case PathIterator.SEG_QUADTO -> {
                            outPath.quadTo(coords[0], coords[1], coords[2], coords[3]);
                        }
                        case PathIterator.SEG_CUBICTO -> {
                            outPath.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                        }
                        case PathIterator.SEG_CLOSE -> {
                            outPath.closePath();
                        }
                        default -> throw new UnsupportedOperationException("Unknown segment type:" + type);
                    }


                    lastX = nextX;
                    lastY = nextY;
                    hasLastSegment = true;
                    currentDistance += segmentLength;

                    pathIterator.next();
                }

                endPath();
            }
            endGroup();
        }
        endPlottedDrawing();
    }

    private void startPlottedDrawing(){
        outDrawing = inputDrawing.newPlottedDrawing();
    }

    private void endPlottedDrawing(){
        if(!outDrawing.geometries.isEmpty()){
            outputDrawings.add(outDrawing);
        }
        outDrawing = null;
    }

    private void startGroup(){
        outGroup = outDrawing.addPlottedGroup(new PlottedGroup(inputGroup));
    }

    private void endGroup(){
        outGroup = null;
    }

    private void startPath(){
        outPath = new GPath();
        GeometryUtils.copyGeometryData(outPath, inputGeometry);
    }

    private void endPath(){
        outDrawing.addGeometry(outPath, outGroup);
        outPath = null;
    }

}