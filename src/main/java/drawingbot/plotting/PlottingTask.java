package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.*;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.image.*;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumTaskStage;
import drawingbot.javafx.GenericFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.imgscalr.Scalr;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PlottingTask extends Task<PlottingTask> implements IPlottingTask {

    public GenericFactory<IPathFindingModule> pfmFactory;
    public PlottedDrawing plottedDrawing;
    public File originalFile;

    // STATUS \\
    public EnumTaskStage stage = EnumTaskStage.QUEUED;
    public long startTime;
    public long finishTime = -1;
    public List<String> comments = new ArrayList<>();

    // IMAGES \\
    public BufferedImage img_original;              // The original image
    public BufferedImage img_reference;             // After pre_processing, croped, scaled, boarder, etc.  This is what we will try to draw.
    public BufferedImage img_plotting;              // Used during drawing for current brightness levels.  Gets damaged during drawing.

    // PIXEL DATA \\
    public IPixelData reference;
    public IPixelData plotting;

    // PATH FINDING \\
    public IPathFindingModule pfm;
    public double plottingProgress = 0;
    public boolean plottingFinished = false;
    public float old_x = 0;
    public float old_y = 0;
    public int currentPen = 0;
    public boolean useCustomARGB = false;
    public int customARGB = -1;
    public int pathIndex = 0;

    // RENDERING \\\
    public PrintResolution resolution;

    // GCODE \\
    private float gcode_offset_x;
    private float gcode_offset_y;

    public PlottingTask(GenericFactory<IPathFindingModule> pfmFactory, ObservableDrawingSet drawingPenSet, BufferedImage image, File originalFile){
        updateTitle("Processing Image");
        this.pfmFactory = pfmFactory;
        this.plottedDrawing = new PlottedDrawing(drawingPenSet);
        this.img_original = image;
        this.originalFile = originalFile;
        this.resolution = new PrintResolution(image);
    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t);
        DrawingBotV3.logger.log(Level.SEVERE, "Plotting Task Failed", t);
    }

    public boolean doTask(){
        switch (stage){

            case QUEUED:
                startTime = System.currentTimeMillis();
                finishStage();
                break;
            case PRE_PROCESSING:
                updateMessage("Pre-Processing Image");

                currentPen = 0;
                useCustomARGB = false;
                customARGB = 0;
                pathIndex = 0;
                plottingProgress = 0;
                plottingFinished = false;

                DrawingBotV3.logger.fine("PFM - Pre-Processing - Started");

                DrawingBotV3.logger.fine("PFM - Create Instance");
                pfm = pfmFactory.instance();
                DrawingBotV3.logger.fine("PFM - Apply Settings");
                GenericSetting.applySettingsToInstance(MasterRegistry.INSTANCE.getObservablePFMSettingsList(pfmFactory), pfm);

                DrawingBotV3.logger.fine("Copying Original Image");
                img_plotting = ImageTools.deepCopy(img_original);

                DrawingBotV3.logger.fine("Applying Filters");
                resolution.plottingResolution = pfm.getPlottingResolution();
                resolution.updateAll();
                img_plotting = FilteredBufferedImage.applyAll(img_plotting, resolution);
                img_plotting = Scalr.resize(img_plotting, Scalr.Method.QUALITY, (int)(img_plotting.getWidth() * pfm.getPlottingResolution()), (int)(img_plotting.getHeight()* pfm.getPlottingResolution()));

                DrawingBotV3.logger.fine("Creating Pixel Data");
                reference = ImageTools.newPixelData(img_plotting.getWidth(), img_plotting.getHeight(), pfm.getColourMode());
                plotting = ImageTools.newPixelData(img_plotting.getWidth(), img_plotting.getHeight(), pfm.getColourMode());
                plotting.setTransparentARGB(pfm.getTransparentARGB());

                DrawingBotV3.logger.fine("Setting Pixel Data");
                ImageTools.copyToPixelData(img_plotting, reference);
                ImageTools.copyToPixelData(img_plotting, plotting);

                DrawingBotV3.logger.fine("PFM - Init");
                pfm.init(this);

                DrawingBotV3.logger.fine("PFM - Pre-Process");
                pfm.preProcess(this);
                finishStage();
                updateMessage("Plotting Image: " + pfmFactory.getName()); //here to avoid excessive task updates
                break;

            case DO_PROCESS:
                if(plottingFinished || isFinished()){
                    finishStage();
                    break;
                }
                pfm.doProcess(this);
                break;
            case POST_PROCESSING:

                pfm.postProcess(this);

                DrawingBotV3.logger.fine("Plotting Task - Distributing Pens - Started");
                plottedDrawing.updateWeightedDistribution();
                DrawingBotV3.logger.fine("Plotting Task - Distributing Pens - Finished");

                img_reference = ImageTools.getBufferedImage(reference);
                img_plotting = ImageTools.getBufferedImage(plotting);


                finishStage();
                break;

            case FINISHING:
                finishTime = (System.currentTimeMillis() - startTime);
                updateMessage("Finished: " + finishTime/1000 + " s");
                finishStage();
                break;

            case FINISHED:
                finishStage();
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
        DrawingBotV3.INSTANCE.onTaskStageFinished(this, stage);
        stage = EnumTaskStage.values()[stage.ordinal()+1];
    }

    public boolean isTaskFinished(){
        return stage == EnumTaskStage.FINISHED;
    }

    public BufferedImage getOriginalImage() {
        return img_original;
    }

    public BufferedImage getReferenceImage() {
        return img_reference;
    }

    public BufferedImage getPlottingImage() {
        return img_plotting;
    }

    public PrintResolution getResolution(){
        return resolution;
    }

    public void stopElegantly(){
        if(stage.ordinal() < EnumTaskStage.DO_PROCESS.ordinal()){
            cancel();
        }else if(stage == EnumTaskStage.DO_PROCESS){
            finishProcess();
            finishStage();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        DrawingBotV3.INSTANCE.onTaskCancelled();
        return super.cancel(mayInterruptIfRunning);
    }

    public AffineTransform createPrintTransform(){
        AffineTransform transform = new AffineTransform();
        transform.scale(resolution.getPrintScale(), resolution.getPrintScale());
        transform.translate(getGCodeXOffset(), getGCodeYOffset());
        return transform;
    }

    public float getGCodeXOffset(){
        return gcode_offset_x + DrawingBotV3.INSTANCE.gcodeOffsetX.get();
    }

    public float getGCodeYOffset(){
        return gcode_offset_y + DrawingBotV3.INSTANCE.gcodeOffsetY.get();
    }

    @Override
    public PlottingTask call() {
        Platform.runLater(() -> DrawingBotV3.INSTANCE.setActivePlottingTask(this));
        while(!isTaskFinished() && !isCancelled()){
            if(!doTask()){
                cancel();
            }
        }
        return this;
    }

    public void reset(){
        img_original = null;
        img_reference = null;
        img_plotting = null;
        plottedDrawing.reset();
        plottedDrawing = null;
        comments.clear();
        old_x = 0;
        old_y = 0;
        gcode_offset_x = 0;
        gcode_offset_y = 0;
        pfm = null;
        resolution = null;
        pfmFactory = null;
        startTime = 0;
        finishTime = -1;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// IPlottingTask
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isFinished() {
        return isCancelled() || plottingFinished;
    }

    private boolean isDrawingPath = false;

    @Override
    public void openPath() {
        isDrawingPath = true;
    }

    @Override
    public void closePath() {
        isDrawingPath = false;
        pathIndex++;
    }

    @Override
    public void addToPath(float x1, float y1) {
        if(!isDrawingPath){
            openPath();
        }
        PlottedPoint line = plottedDrawing.addPoint(pathIndex, currentPen, x1, y1);
        if(useCustomARGB){
            line.rgba = customARGB;
        }
    }

    @Override
    public IPixelData getPixelData() {
        return plotting;
    }

    @Override
    public IPixelData getReferencePixelData() {
        return reference;
    }

    @Override
    public void useCustomARGB(boolean useARGB) {
        useCustomARGB = useARGB;
    }

    @Override
    public void setCustomARGB(int argb) {
        customARGB = argb;
    }

    @Override
    public void setActivePen(int index) {
        currentPen = index;
    }

    @Override
    public int getActivePen() {
        return currentPen;
    }

    @Override
    public int getTotalPens() {
        return plottedDrawing.drawingPenSet.getPens().size();
    }

    @Override
    public IDrawingPen getDrawingPen() {
        return plottedDrawing.drawingPenSet.getPens().get(getActivePen());
    }

    @Override
    public IDrawingSet<?> getDrawingSet() {
        return plottedDrawing.drawingPenSet;
    }

    @Override
    public void updateProgess(double progress, double max) {
        plottingProgress = progress/max;
    }

    @Override
    public void finishProcess() {
        plottingFinished = true;
    }
}
