/*
  DrawingBotV3 by Ollie Lansdell <ollielansdell@hotmail.co.uk
  Original by Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
 */
package drawingbot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import drawingbot.api.IGeometryFilter;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.files.*;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.FilteredBufferedImage;
import drawingbot.image.PrintResolution;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.FXController;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.TaskMonitor;
import drawingbot.javafx.observables.ObservableProjectSettings;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.SplitPlottingTask;
import drawingbot.registry.MasterRegistry;
import drawingbot.render.AbstractRenderer;
import drawingbot.utils.*;
import drawingbot.plotting.PlottingTask;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class DrawingBotV3 {

    public static final Logger logger = Logger.getLogger("DrawingBotV3");
    public static DrawingBotV3 INSTANCE;

    public static AbstractRenderer RENDERER;

    //DRAWING AREA
    public final SimpleBooleanProperty useOriginalSizing = new SimpleBooleanProperty(true);
    public final SimpleObjectProperty<EnumScalingMode> scalingMode = new SimpleObjectProperty<>(EnumScalingMode.CROP_TO_FIT);
    public final SimpleObjectProperty<UnitsLength> inputUnits = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);

    public final SimpleFloatProperty drawingAreaWidth = new SimpleFloatProperty(0);
    public final SimpleFloatProperty drawingAreaHeight = new SimpleFloatProperty(0);
    public final SimpleFloatProperty drawingAreaPaddingLeft = new SimpleFloatProperty(0);
    public final SimpleFloatProperty drawingAreaPaddingRight = new SimpleFloatProperty(0);
    public final SimpleFloatProperty drawingAreaPaddingTop = new SimpleFloatProperty(0);
    public final SimpleFloatProperty drawingAreaPaddingBottom = new SimpleFloatProperty(0);
    public final SimpleStringProperty drawingAreaPaddingGang = new SimpleStringProperty("0");

    public final SimpleBooleanProperty optimiseForPrint = new SimpleBooleanProperty(true);
    public final SimpleFloatProperty targetPenWidth = new SimpleFloatProperty(0.5F);
    
    public final SimpleObjectProperty<Color> canvasColor = new SimpleObjectProperty<>(Color.WHITE);

    //VPYPE SETTINGS
    public final SimpleStringProperty vPypeExecutable = new SimpleStringProperty();
    public final SimpleStringProperty vPypeCommand = new SimpleStringProperty();
    public final SimpleBooleanProperty vPypeBypassOptimisation = new SimpleBooleanProperty();

    //GCODE SETTINGS
    public final SimpleFloatProperty gcodeOffsetX = new SimpleFloatProperty(0);
    public final SimpleFloatProperty gcodeOffsetY = new SimpleFloatProperty(0);
    //public final SimpleObjectProperty<EnumDirection> gcodeXDirection = new SimpleObjectProperty<>();
    //public final SimpleObjectProperty<EnumDirection> gcodeYDirection = new SimpleObjectProperty<>();
    public final SimpleStringProperty gcodeStartCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodeEndCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodePenDownCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodePenUpCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodeStartLayerCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodeEndLayerCode = new SimpleStringProperty();

    //HPGL SETTINGS
    public final SimpleFloatProperty hpglUnits = new SimpleFloatProperty(40);
    public final SimpleFloatProperty hpglCurveFlatness = new SimpleFloatProperty(6);
    public final SimpleFloatProperty hpglXMin = new SimpleFloatProperty(0);
    public final SimpleFloatProperty hpglYMin = new SimpleFloatProperty(11040);
    public final SimpleBooleanProperty hpglYAxisFlip  = new SimpleBooleanProperty(true);
    public final SimpleObjectProperty<EnumImageRotate> hpglRotation = new SimpleObjectProperty<>(EnumImageRotate.R0);

    //PRE-PROCESSING\\
    public final ObservableList<ObservableImageFilter> currentFilters = FXCollections.observableArrayList();
    public final SimpleObjectProperty<EnumImageRotate> imageRotation = new SimpleObjectProperty<>(EnumImageRotate.R0);
    public final SimpleBooleanProperty imageFlipHorizontal = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty imageFlipVertical = new SimpleBooleanProperty(false);

    //PATH FINDING \\
    public final SimpleObjectProperty<PFMFactory<?>> pfmFactory = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<EnumColourSplitter> colourSplitter = new SimpleObjectProperty<>();
    public final SimpleFloatProperty cyanMultiplier = new SimpleFloatProperty(1F);
    public final SimpleFloatProperty magentaMultiplier = new SimpleFloatProperty(1F);
    public final SimpleFloatProperty yellowMultiplier = new SimpleFloatProperty(1F);
    public final SimpleFloatProperty keyMultiplier = new SimpleFloatProperty(0.75F);

    // PEN SETS \\
    public final SimpleBooleanProperty observableDrawingSetFlag = new SimpleBooleanProperty(false); //just a marker flag can be binded
    public ObservableDrawingSet observableDrawingSet = null;

    // VERSION CONTROL \\
    public final ObservableList<ObservableProjectSettings> projectVersions = FXCollections.observableArrayList();

    // DISPLAY \\
    public final SimpleObjectProperty<EnumDisplayMode> display_mode = new SimpleObjectProperty<>(EnumDisplayMode.IMAGE);

    //VIEWPORT SETTINGS \\
    public static int SVG_DPI = 96;

    public final SimpleBooleanProperty displayGrid = new SimpleBooleanProperty(false);
    public final SimpleDoubleProperty scaleMultiplier = new SimpleDoubleProperty(1.0F);
    public static double minScale = 0.1;

    //// VARIABLES \\\\

    // THREADS \\
    public ExecutorService taskService = initTaskService();
    public ExecutorService backgroundService = initBackgroundService();
    public ExecutorService imageFilteringService = initImageFilteringService();
    public ExecutorService parallelPlottingService = initParallelPlottingService();

    public TaskMonitor taskMonitor = new TaskMonitor(taskService);

    // TASKS \\
    public final SimpleObjectProperty<FilteredBufferedImage> openImage = new SimpleObjectProperty<>(null);
    public final SimpleObjectProperty<PlottingTask> activeTask = new SimpleObjectProperty<>(null);

    public PlottingTask renderedTask = null; //for tasks which generate sub tasks e.g. colour splitter, batch processing
    public BatchProcessingTask batchProcessingTask = null; //TODO REMOVE ME?

    public BufferedImageLoader.Filtered loadingImage = null;
    public File openFile = null;

    // GUI \\
    public FXController controller;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public DrawingBotV3() {}

    public float getDrawingAreaWidthMM(){
        return drawingAreaWidth.getValue() * inputUnits.get().convertToMM;
    }

    public float getDrawingAreaHeightMM(){
        return drawingAreaHeight.getValue() * inputUnits.get().convertToMM;
    }

    public float getDrawingWidthMM(){
        return (drawingAreaWidth.getValue() - drawingAreaPaddingLeft.get() - drawingAreaPaddingRight.get()) * inputUnits.get().convertToMM;
    }

    public float getDrawingHeightMM(){
        return (drawingAreaHeight.getValue() - drawingAreaPaddingTop.get() - drawingAreaPaddingBottom.get()) * inputUnits.get().convertToMM;
    }

    public float getDrawingOffsetXMM(){
        return drawingAreaPaddingLeft.get() * inputUnits.get().convertToMM;
    }

    public float getDrawingOffsetYMM(){
        return drawingAreaPaddingTop.get() * inputUnits.get().convertToMM;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private SimpleDoubleProperty localProgress = new SimpleDoubleProperty(0);
    private SimpleStringProperty localMessage = new SimpleStringProperty("");

    public void updateLocalMessage(String message){
        localMessage.set(message);
    }

    public void updateLocalProgress(double progress){
        localProgress.set(progress);
    }

    public EnumDistributionType updateDistributionType = null;


    public void updateUI(){
        if(getActiveTask() != null){
            if(getActiveTask().isRunning()){
                int geometryCount = getActiveTask().plottedDrawing.getGeometryCount();
                long vertexCount = getActiveTask().plottedDrawing.getVertexCount();

                if(getActiveTask() instanceof SplitPlottingTask){
                    geometryCount = ((SplitPlottingTask) getActiveTask()).getCurrentGeometryCount();
                    vertexCount = ((SplitPlottingTask) getActiveTask()).getCurrentVertexCount();
                }
                controller.labelPlottedShapes.setText(Utils.defaultNF.format(geometryCount));
                controller.labelPlottedVertices.setText(Utils.defaultNF.format(vertexCount));

                long minutes = (getActiveTask().getElapsedTime() / 1000) / 60;
                long seconds = (getActiveTask().getElapsedTime() / 1000) % 60;
                controller.labelElapsedTime.setText(minutes + " m " + seconds + " s");
            }
        }else{
            controller.labelElapsedTime.setText("0 s");
            controller.labelPlottedShapes.setText("0");
            controller.labelPlottedVertices.setText("0");
        }


        if(openImage.get() != null){
            controller.labelImageResolution.setText(openImage.get().getSource().getWidth() + " x " + openImage.get().getSource().getHeight());
            controller.labelPlottingResolution.setText(openImage.get().resolution.imageWidth + " x " + openImage.get().resolution.imageHeight);
        }else{
            controller.labelImageResolution.setText("0 x 0");
            controller.labelPlottingResolution.setText("0 x 0");
        }

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ////// EVENTS

    public void onPlottingTaskStageFinished(PlottingTask task, EnumTaskStage stage){
        switch (stage){
            case QUEUED:
                break;
            case PRE_PROCESSING:
                Platform.runLater(() -> display_mode.setValue(EnumDisplayMode.DRAWING));
                break;
            case DO_PROCESS:
                Platform.runLater(() -> {
                    controller.sliderDisplayedLines.setValue(1.0F);
                    controller.textFieldDisplayedLines.setText(String.valueOf(task.plottedDrawing.getGeometryCount()));
                    controller.labelPlottedShapes.setText(Utils.defaultNF.format(task.plottedDrawing.getGeometryCount()));
                    controller.labelPlottedVertices.setText(Utils.defaultNF.format(task.plottedDrawing.getVertexCount()));
                });
                break;
            case POST_PROCESSING:
                break;
            case FINISHING:
                break;
            case FINISHED:
                break;
        }
       logger.info("Plotting Task: Finished Stage " + stage.name());
    }

    public void onDrawingAreaChanged(){
        RENDERER.croppingDirty = true;
        //forces "Plotting Size" to stay updated.
        if(DrawingBotV3.INSTANCE.openImage.get() != null){
            DrawingBotV3.INSTANCE.openImage.get().resolution.updateAll();
        }
    }

    public void onDrawingPenChanged(){
        updatePenDistribution();
    }

    public void onDrawingSetChanged(){
        updatePenDistribution();
        observableDrawingSetFlag.set(!observableDrawingSetFlag.get());
    }

    public void onImageFilterChanged(ObservableImageFilter filter){
        filter.dirty.set(true);
        RENDERER.imageFilterDirty = true;
    }

    public void onImageFiltersChanged(){
        RENDERER.imageFiltersChanged = true;
    }

    public void updatePenDistribution(){
        if(activeTask.get() != null && activeTask.get().isTaskFinished()){
            activeTask.get().plottedDrawing.updatePenDistribution();
            reRender();
        }
    }

    public void reRender(){
        RENDERER.reRender();
    }

    //// PLOTTING TASKS

    public PlottingTask initPlottingTask(PFMFactory<?> pfmFactory, ObservableDrawingSet drawingPenSet, BufferedImage image, File originalFile, EnumColourSplitter splitter){
        //only update the distribution type the first time the PFM is changed, also only trigger the update when Start Plotting is hit again, so the current drawing doesn't get re-rendered
        Platform.runLater(() -> {
            if(updateDistributionType != null && colourSplitter.get() == EnumColourSplitter.DEFAULT){
                drawingPenSet.distributionType.set(updateDistributionType);
                updateDistributionType = null;
            }
        });
        return colourSplitter.get() == EnumColourSplitter.DEFAULT ? new PlottingTask(pfmFactory, MasterRegistry.INSTANCE.getObservablePFMSettingsList(pfmFactory), drawingPenSet, image, originalFile) : new SplitPlottingTask(pfmFactory, MasterRegistry.INSTANCE.getObservablePFMSettingsList(pfmFactory), drawingPenSet, image, originalFile, splitter);
    }

    public void startPlotting(){
        if(activeTask.get() != null){
            activeTask.get().cancel();
        }
        if(openImage.get() != null){
            taskMonitor.queueTask(initPlottingTask(pfmFactory.get(), observableDrawingSet, openImage.get().getSource(), openFile, colourSplitter.get()));
        }
    }

    public void stopPlotting(){
        if(activeTask.get() != null){
            activeTask.get().stopElegantly();
        }
    }

    public void resetPlotting(){
        taskService.shutdownNow();
        setActivePlottingTask(null);
        taskService = initTaskService();
        taskMonitor.resetMonitor(taskService);

        updateLocalProgress(0D);
        updateLocalMessage("");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void openImage(File file, boolean internal){
        if(FileUtils.getExtension(file.toString()).equalsIgnoreCase(".drawingbotv3")){
            FXHelper.loadPresetFile(EnumJsonType.PROJECT_PRESET, file, true);
            return;
        }
        if(activeTask.get() != null){
            activeTask.get().cancel();
            setActivePlottingTask(null);
            openImage.set(null);
            loadingImage = null;
        }
        openFile = file;
        loadingImage = new BufferedImageLoader.Filtered(file.getAbsolutePath(), internal);
        taskMonitor.queueTask(loadingImage);

        FXApplication.primaryStage.setTitle(DBConstants.appName + ", Version: " + DBConstants.appVersion + ", '" + file.getName() + "'");
    }

    public void setActivePlottingTask(PlottingTask task){
        if(activeTask.get() != null){
            activeTask.get().reset(); //help GC by removing references to PlottedLines
        }
        activeTask.set(task);

        if(activeTask.get() == null){
            display_mode.setValue(EnumDisplayMode.IMAGE);
        }
    }

    public PlottingTask getActiveTask(){
        return activeTask.get();
    }

    public PlottingTask getRenderedTask(){
        return renderedTask == null ? activeTask.get() : renderedTask;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// EXPORT TASKS

    public ExportTask createExportTask(ExportFormats format, PlottingTask plottingTask, IGeometryFilter pointFilter, String extension, File saveLocation, boolean seperatePens, boolean forceBypassOptimisation){
        return createExportTask(format, plottingTask, pointFilter, extension, saveLocation, seperatePens, forceBypassOptimisation, plottingTask.resolution);
    }

    public ExportTask createExportTask(ExportFormats format, PlottingTask plottingTask, IGeometryFilter pointFilter, String extension, File saveLocation, boolean seperatePens, boolean forceBypassOptimisation, PrintResolution resolution){
        ExportTask task = new ExportTask(format, plottingTask, pointFilter, extension, saveLocation, seperatePens, true, forceBypassOptimisation, resolution);
        taskMonitor.queueTask(task);
        return task;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// INTERACTION EVENTS

    private boolean ctrl_down = false;

    public void keyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) { ctrl_down = false; }
    }

    public void keyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) { ctrl_down = true; }
        if (event.getCode() == KeyCode.X) { GridOverlay.mouse_point(); }

        if (event.getCode().isArrowKey()) {
            double delta = 0.01;
            double currentH = controller.viewportScrollPane.getHvalue();
            double currentV = controller.viewportScrollPane.getVvalue();
            if (event.getCode() == KeyCode.UP)    {
                controller.viewportScrollPane.setVvalue(currentV - delta);
            }
            if (event.getCode() == KeyCode.DOWN)  {
                controller.viewportScrollPane.setVvalue(currentV + delta);
            }
            if (event.getCode() == KeyCode.RIGHT) {
                controller.viewportScrollPane.setHvalue(currentH - delta);
            }
            if (event.getCode() == KeyCode.LEFT)  {
                controller.viewportScrollPane.setHvalue(currentH + delta);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    double pressX = 0;
    double pressY = 0;
    double locX = 0;
    double locY = 0;

    public void mousePressedJavaFX(MouseEvent event) {    // record a delta distance for the drag and drop operation.
        pressX = event.getX();
        pressY = event.getY();
        locX = controller.viewportScrollPane.getHvalue();
        locY = controller.viewportScrollPane.getVvalue();
    }

    public void mouseDraggedJavaFX(MouseEvent event) {
        double relativeX = (pressX - event.getX()) / controller.viewportStackPane.getWidth();
        double relativeY = (pressY - event.getY()) / controller.viewportStackPane.getHeight();
        controller.viewportScrollPane.setHvalue(locX + relativeX);
        controller.viewportScrollPane.setVvalue(locY + relativeY);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// SERVICES

    public final Thread.UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> {
        DrawingBotV3.logger.log(Level.SEVERE, "Thread Exception: " + thread.getName(), throwable);
    };

    public ExecutorService initTaskService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Task Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }

    public ExecutorService initBackgroundService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Background Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t ;
        });
    }

    public ExecutorService initImageLoadingService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Image Loading Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }

    public ExecutorService initImageFilteringService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Image Filtering Thread");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }

    public ExecutorService initParallelPlottingService(){
        return Executors.newFixedThreadPool(5, r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Parallel Plotting Service");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
}