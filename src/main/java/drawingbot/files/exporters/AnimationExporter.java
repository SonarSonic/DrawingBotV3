package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.UnitsLength;
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
        if (!exportTask.exportResolution.useOriginalSizing() && DrawingBotV3.INSTANCE.optimiseForPrint.get() && DrawingBotV3.INSTANCE.targetPenWidth.get() > 0){
            int DPI = (int)ConfigFileHandler.getApplicationSettings().exportDPI;
            int exportWidth = (int)Math.ceil((exportTask.exportResolution.printPageWidth / UnitsLength.INCHES.convertToMM) * DPI);
            int exportHeight = (int)Math.ceil((exportTask.exportResolution.printPageHeight / UnitsLength.INCHES.convertToMM) * DPI);
            exportTask.exportResolution.changePrintResolution(exportWidth, exportHeight);
        }
        int width = (int)exportTask.exportResolution.getScaledWidth();
        int height = (int)exportTask.exportResolution.getScaledHeight();

        BufferedImage image = ImageExporter.createFreshBufferedImage(exportTask, false);
        Graphics2D graphics = ImageExporter.createFreshGraphics2D(exportTask, image, false);
        graphics.dispose(); //writes background colour to the image.

        int frameCount = 0;
        long totalFrameCount = ConfigFileHandler.getApplicationSettings().getFrameCount();
        int verticesPerFrame = (int)ConfigFileHandler.getApplicationSettings().getVerticesPerFrame(plottingTask.plottedDrawing.getVertexCount());

        if(verticesPerFrame == 1 && totalFrameCount > verticesPerFrame){
            totalFrameCount = plottingTask.plottedDrawing.getVertexCount();
        }

        VertexIterator vertexIterator = new VertexIterator(plottingTask.plottedDrawing.geometries, null, true);

        while(!vertexIterator.isDone()){
            graphics = image.createGraphics();
            ImageExporter.setBlendMode(exportTask, graphics);

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
        exportTask.exportResolution.updatePrintScale();
    }

}
