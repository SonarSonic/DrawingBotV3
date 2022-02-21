package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.*;
import drawingbot.pfm.AbstractSketchPFM;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.utils.DBTask;
import drawingbot.geom.GeometryUtils;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.image.*;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.GenericSetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.utils.EnumTaskStage;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import org.imgscalr.Scalr;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Geometry;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PlottingTask extends DBTask<PlottingTask> implements IPlottingTask {

    public DrawingArea drawingArea;
    public PFMFactory<?> pfmFactory;
    public List<GenericSetting<?, ?>> pfmSettings;
    public PlottedDrawing plottedDrawing;
    public File originalFile;

    // STATUS \\
    public EnumTaskStage stage = EnumTaskStage.QUEUED;
    public long startTime;
    public long finishTime = -1;
    public List<String> comments = new ArrayList<>();

    // IMAGES \\
    public BufferedImage imgOriginal;              // The original image
    public BufferedImage imgReference;             // After pre_processing, croped, scaled, boarder, etc.  This is what we will try to draw.
    public BufferedImage imgPlotting;              // Used during drawing for current brightness levels.  Gets damaged during drawing.

    // PIXEL DATA \\
    public IPixelData pixelDataReference;
    public IPixelData pixelDataPlotting;

    // PATH FINDING \\
    public IPathFindingModule pfm;
    public boolean plottingFinished = false;
    public PathBuilder pathBuilder = new PathBuilder(this);

    // RENDERING \\\
    public PrintResolution resolution;

    public boolean enableImageFiltering = true;
    public boolean isSubTask = false;
    public int defaultPen = 0;

    public PlottedGroup currentGroup;

    // SPECIAL \\
    public boolean useLowQuality = false;
    public int parallelPlots = 3;

    // CLIPPING \\
    public Geometry clippingShape = null;

    public PlottingTask(DrawingArea drawingArea, PFMFactory<?> pfmFactory, List<GenericSetting<?, ?>> pfmSettings, ObservableDrawingSet drawingPenSet, BufferedImage image, File originalFile){
        updateTitle("Plotting Image (" + pfmFactory.getName() + ")");
        this.drawingArea = drawingArea;
        this.pfmSettings = pfmSettings;
        this.pfmFactory = pfmFactory;
        this.plottedDrawing = new PlottedDrawing(drawingPenSet, pfmFactory);
        this.currentGroup = plottedDrawing.getDefaultGroup();
        this.imgOriginal = image;
        this.originalFile = originalFile;
        this.resolution = new PrintResolution(drawingArea, image);
    }

    public boolean doTask(){
        switch (stage){
            case QUEUED:
                startTime = System.currentTimeMillis();
                finishStage();
                break;
            case PRE_PROCESSING:
                updateMessage("Pre-Processing - Copying Original Image");
                plottingFinished = false;

                DrawingBotV3.logger.fine("PFM - Pre-Processing - Started");

                DrawingBotV3.logger.fine("PFM - Create Instance");
                pfm = pfmFactory.instance();
                DrawingBotV3.logger.fine("PFM - Apply Settings");
                GenericSetting.applySettingsToInstance(pfmSettings, pfm);
                onPFMSettingsApplied(pfmSettings, pfm);

                DrawingBotV3.logger.fine("Copying Original Image");
                imgPlotting = ImageTools.deepCopy(imgOriginal);

                DrawingBotV3.logger.fine("Updating Resolution");
                resolution.plottingResolution = pfm.getPlottingResolution();
                resolution.updateAll();

                if(enableImageFiltering){
                    DrawingBotV3.logger.fine("Applying Cropping");
                    updateMessage("Pre-Processing - Cropping");
                    imgPlotting = FilteredBufferedImage.applyCropping(imgPlotting, resolution);

                    DrawingBotV3.logger.fine("Applying Filters");
                    for(ObservableImageFilter filter : DrawingBotV3.INSTANCE.currentFilters){
                        if(filter.enable.get()){
                            BufferedImageOp instance = filter.filterFactory.instance();
                            filter.filterSettings.forEach(setting -> setting.applySetting(instance));

                            updateMessage("Pre-Processing - " + filter.name.getValue());
                            imgPlotting = instance.filter(imgPlotting, null);
                        }
                    }

                    updateMessage("Pre-Processing - Resize");
                    imgPlotting = Scalr.resize(imgPlotting, Scalr.Method.ULTRA_QUALITY, (int)(imgPlotting.getWidth() * pfm.getPlottingResolution()), (int)(imgPlotting.getHeight()* pfm.getPlottingResolution()));
                }
                imgPlotting = pfm.preFilter(imgPlotting);

                DrawingBotV3.logger.fine("Creating Pixel Data");
                pixelDataReference = ImageTools.newPixelData(imgPlotting.getWidth(), imgPlotting.getHeight(), pfm.getColourMode());
                pixelDataPlotting = ImageTools.newPixelData(imgPlotting.getWidth(), imgPlotting.getHeight(), pfm.getColourMode());
                pixelDataPlotting.setTransparentARGB(pfm.getTransparentARGB());

                DrawingBotV3.logger.fine("Setting Pixel Data");
                ImageTools.copyToPixelData(imgPlotting, pixelDataReference);
                ImageTools.copyToPixelData(imgPlotting, pixelDataPlotting);

                clippingShape = clippingShape != null ? clippingShape : ShapeReader.read(new Rectangle2D.Double(0, -imgPlotting.getHeight(), imgPlotting.getWidth(), imgPlotting.getHeight()), 6F, GeometryUtils.factory);
                currentGroup.setPFMFactory(pfmFactory);

                DrawingBotV3.logger.fine("PFM - Init");
                pfm.init(this);

                DrawingBotV3.logger.fine("PFM - Pre-Process");
                updateMessage("Pre-Processing - PFM");
                pfm.preProcess();
                finishStage();
                updateMessage("Processing"); //here to avoid excessive task updates
                break;
            case DO_PROCESS:
                if(plottingFinished || isFinished()){
                    finishStage();
                    break;
                }

                if(useLowQuality){
                    final CountDownLatch latch = new CountDownLatch(parallelPlots);

                    ExecutorService newService = Executors.newFixedThreadPool(parallelPlots, r -> {
                        Thread t = new Thread(r, "DrawingBotV3 - Parallel Plotting Service v2");
                        t.setDaemon(true);
                        return t;
                    });

                    for(int i = 0; i < parallelPlots; i ++){
                        newService.submit(() -> {
                            while(!plottingFinished && !isFinished()){
                                pfm.doProcess();
                            }
                            latch.countDown();
                        });
                    }

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        return false;
                    } finally {
                        newService.shutdown();
                    }

                    finishStage();
                }else{
                    pfm.doProcess();
                }
                break;
            case POST_PROCESSING:
                updateMessage("Post-Processing - PFM");
                pfm.postProcess();

                DrawingBotV3.logger.fine("Plotting Task - Distributing Pens - Started");
                updateMessage("Post-Processing - Distributing Pens");
                plottedDrawing.updatePenDistribution();
                DrawingBotV3.logger.fine("Plotting Task - Distributing Pens - Finished");

                updateMessage("Post-Processing - Converting Reference Images");
                imgReference = ImageTools.getBufferedImage(pixelDataReference);
                imgPlotting = ImageTools.getBufferedImage(pixelDataPlotting);

                finishStage();
                break;
            case FINISHING:
                finishTime = (System.currentTimeMillis() - startTime);
                updateMessage("Finished - Elapsed Time: " + finishTime/1000 + " s");
                updateProgress(1, 1);
                finishStage();
                break;
            case FINISHED:
                break;
        }
        return true;
    }

    public void comment(String comment){
        comments.add(comment);
        DrawingBotV3.logger.info("Task Comment: " + comment);
    }

    public long getElapsedTime(){
        if(finishTime != -1){
            return finishTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    public void finishStage(){
        if(!isSubTask){
            DrawingBotV3.INSTANCE.onPlottingTaskStageFinished(this, stage);
        }
        stage = EnumTaskStage.values()[stage.ordinal()+1];
    }

    public boolean isTaskFinished(){
        return stage == EnumTaskStage.FINISHED;
    }

    public BufferedImage getOriginalImage() {
        return imgOriginal;
    }

    public BufferedImage getReferenceImage() {
        return imgReference;
    }

    public BufferedImage getPlottingImage() {
        return imgPlotting;
    }

    public PrintResolution getResolution(){
        return resolution;
    }

    public void stopElegantly(){
        DrawingBotV3.logger.info(stage.toString());
        if(stage.ordinal() < EnumTaskStage.DO_PROCESS.ordinal()){
            cancel();
        }else if(stage == EnumTaskStage.DO_PROCESS){
            finishProcess();
        }
        if(pfm != null){ //rare case for mosaic tasks where stopElegantly can be called on already finished pfms
            pfm.onStopped();
        }
    }

    public AffineTransform createPrintTransform(){
        AffineTransform transform = new AffineTransform();
        transform.scale(resolution.getPrintScale(), resolution.getPrintScale());
        return transform;
    }

    @Override
    public PlottingTask call() {
        if(!isSubTask){
            Platform.runLater(() -> DrawingBotV3.INSTANCE.setActivePlottingTask(this));
        }
        while(!isTaskFinished() && !isCancelled()){
            if(!doTask()){
                cancel();
            }
        }
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// IPlottingTask
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isFinished() {
        return isCancelled() || plottingFinished;
    }

    @Override
    public PathBuilder getPathBuilder() {
        return pathBuilder;
    }

    @Override
    public IGeometry getLastGeometry() {
        return plottedDrawing.geometries.isEmpty() ? null : plottedDrawing.geometries.get(plottedDrawing.geometries.size()-1);
    }

    @Override
    public void addGeometry(IGeometry geometry) {
        addGeometry(geometry, -1, -1);
    }

    @Override
    public void addGeometry(IGeometry geometry, int penIndex, int rgba) {
        if(geometry.getSampledRGBA() == -1){
            geometry.setSampledRGBA(rgba);
        }
        if(geometry.getPenIndex() == -1){
            geometry.setPenIndex(penIndex == -1 ? defaultPen : penIndex);
        }
        geometry.setPFMPenIndex(geometry.getPenIndex()); //store the pfm pen index for later reference
        geometry.setGroupID(currentGroup.getGroupID());

        //transform geometry back to the images size
        if(resolution.plottingResolution != 1F){
            geometry.transform(resolution.plottingTransform);
        }
        plottedDrawing.addGeometry(geometry);
    }

    @Override
    public IPixelData getPixelData() {
        return pixelDataPlotting;
    }

    @Override
    public IPixelData getReferencePixelData() {
        return pixelDataReference;
    }

    @Override
    public int getTotalPens() {
        return currentGroup.drawingSet.getPens().size();
    }

    @Override
    public ObservableDrawingSet getDrawingSet() {
        return currentGroup.drawingSet;
    }

    @Override
    public void updatePlottingProgress(double progress, double max) {
        double actual = Math.min(Utils.roundToPrecision(progress / max, 3), 1D);
        updateProgress(actual, 1D);
    }

    @Override
    public void finishProcess() {
        plottingFinished = true;
    }

    public void reset(){
        pfmFactory = null;
        plottedDrawing.reset();
        originalFile = null;

        startTime = 0;
        finishTime = -1;
        comments.clear();

        imgOriginal = null;
        imgReference = null;
        imgPlotting = null;

        pixelDataReference = null;
        pixelDataPlotting = null;

        pfm = null;
        plottingFinished = false;

        resolution = null;
    }

    //// CALLBACKS

    public BiConsumer<List<GenericSetting<?, ?>>, IPathFindingModule> onPFMSettingsAppliedCallback = null;
    public Consumer<AbstractSketchPFM> sketchPFMProgressCallback = null;

    public void onPFMSettingsApplied(List<GenericSetting<?, ?>> src, IPathFindingModule pfm){
        if(onPFMSettingsAppliedCallback != null){
            onPFMSettingsAppliedCallback.accept(src, pfm);
        }
    }

    /**
     * Allows the PlottingTask to override the SketchPFMs progress.
     */
    public boolean applySketchPFMProgressCallback(AbstractSketchPFM sketchPFM){
        if(sketchPFMProgressCallback != null){
            sketchPFMProgressCallback.accept(sketchPFM);
            return true;
        }
        return false;
    }



    //// GEOMETRY UI HOOKS \\\\

    public int getCurrentGeometryCount(){
        return plottedDrawing.getGeometryCount();
    }

    public long getCurrentVertexCount(){
        return plottedDrawing.getVertexCount();
    }

    @Override
    public boolean isPlottingTask() {
        return true;
    }

    //// CUSTOM RENDERING \\\\

    public boolean handlesProcessRendering(){
        return false;
    }

    public void renderProcessing(JavaFXRenderer renderer, PlottingTask renderedTask){
        //NOP
    }

    public void clearProcessingRender(JavaFXRenderer renderer, PlottingTask renderedTask){
        //NOP
    }
}
