package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingPen;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.VertexIterator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class AnimationExporter {

    public static DecimalFormat framePaddingFormat = new DecimalFormat("000000");

    public static void exportAnimation(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation) {
        int width = (int)plottingTask.resolution.getScaledWidth();
        int height = (int)plottingTask.resolution.getScaledHeight();

        boolean useAlpha = !extension.equals(".jpg");
        BufferedImage image = new BufferedImage(width, height, useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = image.createGraphics();
        if(!useAlpha){
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.dispose();
        }

        int frameCount = 0;
        long totalFrameCount = ConfigFileHandler.getApplicationSettings().getFrameCount();
        int verticesPerFrame = (int)ConfigFileHandler.getApplicationSettings().getVerticesPerFrame(plottingTask.plottedDrawing.getVertexCount());

        if(verticesPerFrame == 1 && totalFrameCount > verticesPerFrame){
            totalFrameCount = plottingTask.plottedDrawing.getVertexCount();
        }

        VertexIterator vertexIterator = new VertexIterator(plottingTask.plottedDrawing.geometries, null, true);

        while(!vertexIterator.isDone()){
            graphics = image.createGraphics();
            Graphics2DExporter.preDraw(exportTask, graphics, width, height, plottingTask);

            vertexIterator.renderVerticesAWT(plottingTask.plottedDrawing, graphics, frameCount == totalFrameCount - 1 ? Integer.MAX_VALUE : verticesPerFrame);

            Graphics2DExporter.postDraw(exportTask, graphics, width, height, plottingTask);
            frameCount++;

            try {
                File path = FileUtils.removeExtension(saveLocation);
                File fileName = new File(path.getPath() + "_F" + framePaddingFormat.format(frameCount) + extension);
                ImageIO.write(image, extension.substring(1), fileName);
            } catch (IOException e) {
                exportTask.setError(e.getMessage());
                e.printStackTrace();
            }
            exportTask.updateMessage("Frames: " + frameCount + " / " + totalFrameCount);
            exportTask.updateProgress(frameCount, totalFrameCount);
        }
    }

}
