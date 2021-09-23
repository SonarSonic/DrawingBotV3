package drawingbot.pfm;

import drawingbot.pfm.modules.position.WeightedVoronoiPositionEncoder;
import drawingbot.plotting.PlottingTask;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.Utils;
import org.imgscalr.Scalr;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PFMMosaicVoronoi extends AbstractMosaicPFM{


    public int iterations = 1;
    public int pointCount = 80;
    public int luminancePower = 5;
    public int densityPower = 5;
    public float offsetCells = 0;

    public List<Geometry> geometries = null;

    private WeightedVoronoiPositionEncoder shapeEncoder = null;

    @Override
    public int calculateTileCount() {

        PFMFactory<?> factory = MasterRegistry.INSTANCE.getPFMFactory("Voronoi Diagram");
        PlottingTask voronoiTask = new PlottingTask(factory, new ArrayList<>(), task.getDrawingSet(), task.img_plotting, task.originalFile);
        voronoiTask.isSubTask = true;
        voronoiTask.enableImageFiltering = false;

        voronoiTask.onPFMSettingsAppliedCallback = (settings, pfm) -> {
            shapeEncoder = (WeightedVoronoiPositionEncoder) ((PFMModular) pfm).positionalEncoder;
            shapeEncoder.iterations = iterations;
            shapeEncoder.pointCount = pointCount;
            shapeEncoder.luminancePower = luminancePower;
            shapeEncoder.densityPower = densityPower;
        };

        while(!voronoiTask.isTaskFinished() && !task.plottingFinished && !task.isFinished()){
            voronoiTask.doTask();
        }

        GeometryCollection collection = shapeEncoder.getGeometries();
        geometries = new ArrayList<>();

        for (int i = 0; i < collection.getNumGeometries(); i ++) {
            Geometry geometry = collection.getGeometryN(i);
            geometries.add(geometry);
        }
        return geometries.size();
    }

    @Override
    public void createMosaicTasks() {

        for (Geometry geometry : geometries) {
            geometry = geometry.buffer(offsetCells);
            Envelope envelope = geometry.getEnvelopeInternal();
            nextDrawingStyle();

            int minX = Utils.clamp((int)envelope.getMinX(), 0, task.getPixelData().getWidth());
            int maxX = Utils.clamp((int)envelope.getMaxX(), 0, task.getPixelData().getWidth());

            int minY = Utils.clamp((int)envelope.getMinY(), 0, task.getPixelData().getHeight());
            int maxY = Utils.clamp((int)envelope.getMaxY(), 0, task.getPixelData().getHeight());

            int width = maxX-minX;
            int height = maxY-minY;

            if(width == 0 || height == 0){
                return;
            }

            BufferedImage scaledImage = Scalr.crop(task.img_plotting, minX, minY, width, height);
            BufferedImage tileImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics2D = tileImage.createGraphics();

            graphics2D.setColor(Color.WHITE);
            graphics2D.drawRect(0, 0, width, height);

            ShapeWriter writer = new ShapeWriter((src, dest) -> dest.setLocation(src.x- minX, src.y- minY));
            graphics2D.setClip(writer.toShape(geometry));

            graphics2D.drawImage(scaledImage, null, 0, 0);


            graphics2D.dispose();

            PlottingTask tileTask = new PlottingTask(currentDrawingStyle.getFactory(), currentStyleSettings, evenlyDistributedDrawingSet, tileImage, task.originalFile);
            tileTask.isSubTask = true;
            tileTask.enableImageFiltering = false;
            mosaicTasks.add(new MosaicTask(currentDrawingStyle, tileTask, AffineTransform.getTranslateInstance(minX, minY)));
        }
    }
}
