package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.image.PrintResolution;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.EnumColourSplitter;
import drawingbot.utils.Utils;
import javafx.concurrent.Task;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class VideoProcessingTask extends Task<Boolean> {

    public final File originalFile;
    public final PFMFactory<?> pfmFactory;
    public final ObservableDrawingSet drawingPenSet;
    public final EnumColourSplitter splitter;

    public final ExportFormats format;
    public final String extension;
    public final PlottingTask plottingTask;
    public final IGeometryFilter pointFilter;
    public final File saveLocation;
    public final boolean seperatePens;
    public final boolean overwrite;
    public final boolean forceBypassOptimisation;
    public PrintResolution exportResolution;

    public VideoProcessingTask(File originalFile, ExportFormats format, PlottingTask plottingTask, IGeometryFilter pointFilter, String extension, File saveLocation, boolean seperatePens, boolean overwrite, boolean forceBypassOptimisation, PrintResolution exportResolution){
        this.originalFile = originalFile;
        this.pfmFactory = DrawingBotV3.INSTANCE.pfmFactory.get();
        this.drawingPenSet = new ObservableDrawingSet(DrawingBotV3.INSTANCE.observableDrawingSet);
        this.splitter = DrawingBotV3.INSTANCE.colourSplitter.get();


        this.format = format;
        this.plottingTask = plottingTask;
        this.pointFilter = pointFilter;
        this.extension = extension;
        this.saveLocation = saveLocation;
        this.seperatePens = seperatePens;
        this.overwrite = overwrite;
        this.forceBypassOptimisation = forceBypassOptimisation;
        this.exportResolution = exportResolution;

    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t);
        DrawingBotV3.logger.log(Level.SEVERE, "Video Processing Task Failed", t);
    }

    @Override
    protected Boolean call() throws Exception {

        updateTitle("Video Processing");
        ExecutorService service = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Video Export Thread");
            t.setDaemon(true);
            return t;
        });

        updateMessage("Loading Frames");
        FileChannelWrapper ch = NIOUtils.readableChannel(originalFile);
        FrameGrab videoData = FrameGrab.createFrameGrab(ch);
        int totalFrames = videoData.getVideoTrack().getMeta().getTotalFrames();
        BufferedImage[] videoFrames = new BufferedImage[totalFrames];
        ArrayList<SortedImage> li = new ArrayList<>();
        for (int i = 0; i < videoFrames.length; i++) {
            PictureWithMetadata src = videoData.getNativeFrameWithMetadata();
            li.add(new SortedImage(src));
            updateProgress(i, videoFrames.length);
        }
        Collections.sort(li);
        for (int i = 0; i < videoFrames.length; i++) {
            videoFrames[i] = li.get(i).data;
        }

        loop: for (int i = 0; i < totalFrames; i++) {

            File path = FileUtils.removeExtension(saveLocation);
            String frameCountS = String.format("%04d", (i+1)).substring(0, 4);
            File fileName = new File(path.getPath() + "_F" + frameCountS + extension);
            updateMessage("Processing Frames " + (i+1) + " / " + totalFrames + " - File: " + path);
            updateProgress(i, totalFrames);

            if(Files.notExists(fileName.toPath())){
                PlottingTask internalTask = DrawingBotV3.INSTANCE.initPlottingTask(pfmFactory, new ObservableDrawingSet(drawingPenSet), videoFrames[i], originalFile, splitter);
                internalTask.isSubTask = true;
                DrawingBotV3.INSTANCE.renderedTask = internalTask;
                Future<?> futurePlottingTask = service.submit(internalTask);
                while(!futurePlottingTask.isDone()){
                    ///wait
                    if(isCancelled()){
                        futurePlottingTask.cancel(true);
                        service.shutdown();
                        break loop;
                    }
                }

                ExportTask exportTask = new ExportTask(format, internalTask, pointFilter, extension, fileName, seperatePens, /*overwrite*/ false, forceBypassOptimisation, true, exportResolution);
                Future<?> futureExportTask = service.submit(exportTask);
                while(!futureExportTask.isDone()){
                    ///wait
                    if(isCancelled()){
                        futureExportTask.cancel(true);
                        service.shutdown();
                        break loop;
                    }
                }
            }
        }

        service.shutdown();
        NIOUtils.closeQuietly(ch);

        return null;
    }

    //src : https://github.com/jcodec/jcodec/issues/165
    private static class SortedImage implements Comparable<SortedImage> {

        private final double timestamp;
        private final BufferedImage data;

        private SortedImage(PictureWithMetadata p) {
            data = AWTUtil.toBufferedImage(p.getPicture());
            timestamp = p.getTimestamp();
        }

        @Override
        public int compareTo(SortedImage o2) {
            return Double.compare(timestamp, o2.timestamp);
        }

    }
}
