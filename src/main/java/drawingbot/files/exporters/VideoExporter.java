package drawingbot.files.exporters;

import drawingbot.files.ConfigFileHandler;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.geom.basic.IGeometry;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.VertexIterator;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.Codec;
import org.jcodec.common.Format;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class VideoExporter {

    public static void exportMP4(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation){
        exportVideo(exportTask, plottingTask, geometries, extension, saveLocation, Format.MOV, Codec.H264);
    }

    public static void exportProRes(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation){
        exportVideo(exportTask, plottingTask, geometries, extension, saveLocation, Format.MOV, Codec.PRORES);
    }

    public static void exportVideo(ExportTask exportTask, PlottingTask plottingTask, Map<Integer, List<IGeometry>> geometries, String extension, File saveLocation, Format format, Codec codec){

        int width = ImageExporter.getRasterWidth(exportTask);
        int height = ImageExporter.getRasterHeight(exportTask);

        BufferedImage image = ImageExporter.createFreshBufferedImage(exportTask);
        Graphics2D graphics = ImageExporter.createFreshGraphics2D(exportTask, image);
        graphics.dispose(); //writes background colour to the image.

        int frameCount = 0;
        long totalFrameCount = ConfigFileHandler.getApplicationSettings().getFrameCount();
        int verticesPerFrame = (int)ConfigFileHandler.getApplicationSettings().getVerticesPerFrame(plottingTask.plottedDrawing.getVertexCount());

        if(verticesPerFrame == 1 && totalFrameCount > verticesPerFrame){
            totalFrameCount = plottingTask.plottedDrawing.getVertexCount();
        }

        SeekableByteChannel out = null;
        try {
            out = NIOUtils.writableFileChannel(saveLocation.toString());
            SequenceEncoder encoder = new SequenceEncoder(out, Rational.R((int)ConfigFileHandler.getApplicationSettings().framesPerSecond, 1), format, codec, null);
            VertexIterator vertexIterator = new VertexIterator(plottingTask.plottedDrawing.geometries, null, true);
            while(!vertexIterator.isDone()){
                graphics = image.createGraphics();
                ImageExporter.setBlendMode(exportTask, graphics);

                Graphics2DExporter.preDraw(exportTask, graphics, width, height, plottingTask);
                vertexIterator.renderVerticesAWT(plottingTask.plottedDrawing, graphics, frameCount == totalFrameCount - 1 ? Integer.MAX_VALUE : verticesPerFrame);
                Graphics2DExporter.postDraw(exportTask, graphics, width, height, plottingTask);

                frameCount++;

                encoder.encodeNativeFrame(AWTUtil.fromBufferedImageRGB(image));

                exportTask.updateMessage("Frames: " + frameCount + " / " + totalFrameCount);
                exportTask.updateProgress(frameCount, totalFrameCount);
            }
            encoder.finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            NIOUtils.closeQuietly(out);
        }
    }

}
