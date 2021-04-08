package drawingbot.pfm.modules.position;

import drawingbot.api.IPixelData;
import drawingbot.geom.GeometryUtils;
import drawingbot.geom.basic.GRectangle;
import drawingbot.image.ImageTools;
import drawingbot.image.PixelDataLuminance;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.utils.Utils;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SRC: https://www.cs.ubc.ca/labs/imager/tr/2002/secord2002b/secord.2002b.pdf
 */
public class WeightedVoronoiPositionEncoder extends PositionEncoder {

    public final EnumExportType exportType;
    public int iterations = 5;

    public int pointCount = 5000;

    public int luminancePower = 5;
    public int densityPower = 10;

    public List<Coordinate> coordinates;
    public List<Integer> luminance;

    public VoronoiDiagramBuilder voronoiDiagramBuilder;
    public GeometryCollection voronoiDiagram;

    //extra outputs
    public GeometryCollection voronoiEdges;
    public List<Coordinate> voronoiPoints;
    public GeometryCollection pointCollection;

    public WeightedVoronoiPositionEncoder(EnumExportType exportType){
        this.exportType = exportType;
    }

    @Override
    public int getIterations() {
        return iterations;
    }

    @Override
    public void preProcess(IPixelData data) {
        //generate initial array
        coordinates = new ArrayList<>();
        luminance = new ArrayList<>();
        int i = 0;
        IPixelData lumData = ImageTools.copy(data, new PixelDataLuminance(data.getWidth(), data.getHeight()));
        int minLum = 255;
        int maxLum = 0;
        for(int x = 0; x < lumData.getWidth(); x++){
            for(int y = 0; y < lumData.getHeight(); y++){
                int lum = lumData.getLuminance(x, y);
                minLum = Math.min(minLum, lum);
                maxLum = Math.max(maxLum, lum);
            }
        }

        while (i < pointCount) {
            int randX = pfmModular.randomSeed(0, data.getWidth() - 1);
            int randY = pfmModular.randomSeed(0, data.getHeight() - 1);
            float lum = lumData.getLuminance(randX, randY);
            if (pfmModular.randomSeedF(minLum, maxLum) <= Math.pow(255 - lum, luminancePower) / Math.pow(255, luminancePower-1)) {
                coordinates.add(new CoordinateXY(randX, randY));
                luminance.add(0);
                pfmModular.task.addGeometry(new GRectangle(randX, randY, 1, 1));
                lumData.setLuminance(randX, randY, 255);
                i++;
            }
        }
    }

    @Override
    public void doProcess(IPixelData data) {
        pfmModular.task.updateMessage("Calculating Voronoi Diagram: " + (pfmModular.currentIteration+1) + " of " + pfmModular.targetIteration);
        voronoiDiagramBuilder = new VoronoiDiagramBuilder();
        voronoiDiagramBuilder.setSites(coordinates);
        voronoiDiagram = (GeometryCollection) voronoiDiagramBuilder.getDiagram(GeometryUtils.factory);

        generateCentroids(data, voronoiDiagram, coordinates, luminance);

        pointCollection = null;
        voronoiPoints = null;
        voronoiEdges = null;
    }

    @Override
    public List<Coordinate> getCoordinates() {
        switch (exportType){
            case CENTROIDS:
                return coordinates;
            case VORONOI_GEOMETRIES:
                if(voronoiPoints == null){
                    voronoiPoints = Arrays.asList(voronoiDiagram.getCoordinates());
                }
                return voronoiPoints;
            case VORONOI_EDGES:
                if(voronoiEdges == null){
                    voronoiEdges = GeometryUtils.factory.createGeometryCollection(new Geometry[]{voronoiDiagramBuilder.getSubdivision().getEdges(GeometryUtils.factory)});
                }
                if(voronoiPoints == null){
                    voronoiPoints = Arrays.asList(voronoiEdges.getCoordinates());
                }
                return voronoiPoints;
        }
        return null;
    }

    @Override
    public GeometryCollection getGeometries() {
        switch (exportType){
            case CENTROIDS:
                if(pointCollection == null){
                    List<Geometry> geometries = new ArrayList<>();
                    coordinates.forEach(p -> geometries.add(GeometryUtils.factory.createPoint(p)));
                    pointCollection = GeometryUtils.factory.createGeometryCollection(geometries.toArray(new Geometry[0]));
                }
                return pointCollection;
            case VORONOI_GEOMETRIES:
                return voronoiDiagram;
            case VORONOI_EDGES:
                if(voronoiEdges == null){
                    voronoiEdges = GeometryUtils.factory.createGeometryCollection(new Geometry[]{voronoiDiagramBuilder.getSubdivision().getEdges(GeometryUtils.factory)});
                }
                return voronoiEdges;
        }
        return null;
    }

    /**
     * Modified from: https://github.com/evil-mad/stipplegen
     */
    public void generateCentroids(IPixelData data, GeometryCollection voronoiDiagram, List<Coordinate> coordinates, List<Integer> luminance) {
        for (int i = 0; i < voronoiDiagram.getNumGeometries(); i++) {

            if(pfmModular.task.isFinished()){
                break;
            }

            Coordinate sourceCoord = coordinates.get(i);
            Geometry geometry = voronoiDiagram.getGeometryN(i);
            Envelope envelope = geometry.getEnvelopeInternal();

            double xMax = Utils.clamp(envelope.getMaxX(), 0, data.getWidth() - 1);
            double xMin = Utils.clamp(envelope.getMinX(), 0, data.getWidth() - 1);
            double yMax = Utils.clamp(envelope.getMaxY(), 0, data.getHeight() - 1);
            double yMin = Utils.clamp(envelope.getMinY(), 0, data.getHeight() - 1);

            double xDiff = xMax - xMin;
            double yDiff = yMax - yMin;
            double maxSize = Math.max(xDiff, yDiff);
            double minSize = Math.min(xDiff, yDiff);


            float scaleFactor = 1.0F;

            // Maximum voronoi cell extent should be between
            // cellBuffer/2 and cellBuffer in size.
            int cellBuffer = 100;

            while (maxSize > cellBuffer) {
                scaleFactor *= 0.5;
                maxSize *= 0.5;
            }

            while (maxSize < (cellBuffer / 2D)) {
                scaleFactor *= 2;
                maxSize *= 2;
            }

            if ((minSize * scaleFactor) > (cellBuffer / 2D)) {
                // Special correction for objects of near-unity (square-like) aspect ratio,
                // which have larger area *and* where it is less essential to find the exact centroid:
                scaleFactor *= 0.5;
            }

            float stepSize = (1 / scaleFactor);

            double xSum = 0;
            double ySum = 0;
            double dSum = 0;
            double lumSum = 0;
            int coordCount = 0;

            for (double x = xMin; x <= xMax; x += stepSize) {
                for (double y = yMin; y <= yMax; y += stepSize) {
                    CoordinateXY coord = new CoordinateXY(x, y);
                    if (SimplePointInAreaLocator.isContained(coord, geometry)) {
                        float lum = 255 - data.getLuminance((int) coord.x, (int) coord.y);
                        double density = Math.pow(lum, densityPower);

                        xSum += density * x;
                        ySum += density * y;
                        dSum += density;
                        lumSum += lum;
                        coordCount++;
                    }
                }
            }

            if (dSum > 0) {
                xSum /= dSum;
                ySum /= dSum;
            }

            double xTemp = xSum;
            double yTemp = ySum;

            if ((xTemp <= 0) || (xTemp >= data.getWidth() - 1) || (yTemp <= 0) || (yTemp >= data.getHeight() - 1)) {
                //if the centroid lies outside of the image use the geometric one instead
                Coordinate centroid = geometry.getCentroid().getCoordinate();
                xTemp = Utils.clamp(centroid.x, 0, data.getWidth() - 1);
                yTemp = Utils.clamp(centroid.y, 0, data.getHeight() - 1);
            }

            sourceCoord.x = xTemp;
            sourceCoord.y = yTemp;
            luminance.set(i, (int) (lumSum / coordCount));
            pfmModular.updatePositionEncoderProgess(i, voronoiDiagram.getNumGeometries()-1);
        }
    }

    public enum EnumExportType{
        CENTROIDS,
        VORONOI_GEOMETRIES,
        VORONOI_EDGES

    }

}
