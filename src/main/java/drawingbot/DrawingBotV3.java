/*
  DrawingBotV3 by Ollie Lansdell <ollielansdell@hotmail.co.uk
  Original by Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
 */
package drawingbot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import drawingbot.api.Hooks;
import drawingbot.api.IGeometryFilter;
import drawingbot.api.IPlugin;
import drawingbot.drawing.ColourSplitterHandler;
import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.presets.types.PresetProjectSettings;
import drawingbot.image.DrawingArea;
import drawingbot.javafx.GenericPreset;
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
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.IRenderer;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.utils.*;
import drawingbot.plotting.PlottingTask;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class DrawingBotV3 {

    public static final Logger logger = Logger.getLogger("DrawingBotV3");
    public static DrawingBotV3 INSTANCE;

    public static JavaFXRenderer RENDERER;
    public static IRenderer OPENGL_RENDERER;

    //DRAWING AREA
    public DrawingArea drawingArea = new DrawingArea();
    public final SimpleObjectProperty<Color> canvasColor = new SimpleObjectProperty<>(Color.WHITE);

    //VPYPE SETTINGS
    public final SimpleStringProperty vPypeExecutable = new SimpleStringProperty();
    public final SimpleStringProperty vPypeCommand = new SimpleStringProperty();
    public final SimpleBooleanProperty vPypeBypassOptimisation = new SimpleBooleanProperty();

    //GCODE SETTINGS
    public final SimpleFloatProperty gcodeOffsetX = new SimpleFloatProperty(0);
    public final SimpleFloatProperty gcodeOffsetY = new SimpleFloatProperty(0);
    public final SimpleObjectProperty<UnitsLength> gcodeUnits = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);
    public final SimpleStringProperty gcodeStartCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodeEndCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodePenDownCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodePenUpCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodeStartLayerCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodeEndLayerCode = new SimpleStringProperty();
    public final SimpleFloatProperty gcodeCurveFlatness = new SimpleFloatProperty(0.1F);
    public final SimpleBooleanProperty gcodeEnableFlattening = new SimpleBooleanProperty(true);
    public final SimpleBooleanProperty gcodeCenterZeroPoint = new SimpleBooleanProperty(false);
    public final SimpleObjectProperty<GCodeBuilder.CommentType> gcodeCommentType = new SimpleObjectProperty<>(GCodeBuilder.CommentType.BRACKETS);

    //HPGL SETTINGS
    public final SimpleIntegerProperty hpglUnits = new SimpleIntegerProperty(40);

    public final SimpleIntegerProperty hpglXMin = new SimpleIntegerProperty(0);
    public final SimpleIntegerProperty hpglXMax = new SimpleIntegerProperty(0);
    public final SimpleIntegerProperty hpglYMin = new SimpleIntegerProperty(0);
    public final SimpleIntegerProperty hpglYMax = new SimpleIntegerProperty(0);

    public final SimpleBooleanProperty hpglXAxisMirror = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty hpglYAxisMirror = new SimpleBooleanProperty(true);

    public final SimpleObjectProperty<EnumAlignment> hpglAlignX = new SimpleObjectProperty<>(EnumAlignment.CENTER);
    public final SimpleObjectProperty<EnumAlignment> hpglAlignY = new SimpleObjectProperty<>(EnumAlignment.CENTER);

    public final SimpleObjectProperty<EnumRotation> hpglRotation = new SimpleObjectProperty<>(EnumRotation.AUTO);
    public final SimpleFloatProperty hpglCurveFlatness = new SimpleFloatProperty(0.1F);

    public final SimpleIntegerProperty hpglPenSpeed = new SimpleIntegerProperty(0);
    public final SimpleIntegerProperty hpglPenNumber = new SimpleIntegerProperty(0);


    //PRE-PROCESSING\\
    public final ObservableList<ObservableImageFilter> currentFilters = FXCollections.observableArrayList();
    public final SimpleObjectProperty<EnumRotation> imageRotation = new SimpleObjectProperty<>(EnumRotation.R0);
    public final SimpleBooleanProperty imageFlipHorizontal = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty imageFlipVertical = new SimpleBooleanProperty(false);

    //PATH FINDING \\
    public final SimpleObjectProperty<PFMFactory<?>> pfmFactory = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<ColourSplitterHandler> colourSplitter = new SimpleObjectProperty<>();
    public final SimpleFloatProperty cyanMultiplier = new SimpleFloatProperty(1F);
    public final SimpleFloatProperty magentaMultiplier = new SimpleFloatProperty(1F);
    public final SimpleFloatProperty yellowMultiplier = new SimpleFloatProperty(1F);
    public final SimpleFloatProperty keyMultiplier = new SimpleFloatProperty(0.75F);

    // PEN SETS \\
    public final SimpleBooleanProperty observableDrawingSetFlag = new SimpleBooleanProperty(false); //just a marker flag can be binded
    public ObservableDrawingSet observableDrawingSet = null;

    // VERSION CONTROL \\
    public final ObservableList<ObservableProjectSettings> projectVersions = FXCollections.observableArrayList();
    public final SimpleObjectProperty<ObservableProjectSettings> lastRun = new SimpleObjectProperty<>();

    // DISPLAY \\
    public final SimpleObjectProperty<EnumDisplayMode> display_mode = new SimpleObjectProperty<>(EnumDisplayMode.IMAGE);

    //VIEWPORT SETTINGS \\
    public static int SVG_DPI = 96;
    public static int PDF_DPI = 72;

    public final SimpleBooleanProperty exportRange = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty displayGrid = new SimpleBooleanProperty(false);

    //the default JFX background colour
    public Color backgroundColour = new Color(244 / 255F, 244 / 255F, 244 / 255F, 1F);

    //// VARIABLES \\\\

    // THREADS \\
    public ExecutorService taskService = initTaskService();
    public ExecutorService backgroundService = initBackgroundService();
    public ExecutorService imageFilteringService = initImageFilteringService();
    public ExecutorService parallelPlottingService = initParallelPlottingService();
    public ExecutorService serialConnectionWriteService = initSerialConnectionService();

    public TaskMonitor taskMonitor = new TaskMonitor(taskService);

    // TASKS \\
    public final SimpleObjectProperty<FilteredBufferedImage> openImage = new SimpleObjectProperty<>(null);
    public final SimpleObjectProperty<PlottingTask> activeTask = new SimpleObjectProperty<>(null);

    public PlottingTask renderedTask = null; //for tasks which generate sub tasks e.g. colour splitter, batch processing

    public File openFile = null;

    // GUI \\
    public FXController controller;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public DrawingBotV3() {}

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

        taskMonitor.tick();

        if(getActiveTask() != null){
            if(getActiveTask().isRunning()){
                int geometryCount = getActiveTask().getCurrentGeometryCount();
                long vertexCount = getActiveTask().getCurrentVertexCount();

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

        //tick all plugins
        MasterRegistry.PLUGINS.forEach(IPlugin::tick);
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
                    controller.rangeSliderDisplayedLines.setLowValue(0.0F);
                    controller.rangeSliderDisplayedLines.setHighValue(1.0F);
                    controller.textFieldDisplayedShapesMin.setText(String.valueOf(0));
                    controller.textFieldDisplayedShapesMax.setText(String.valueOf(task.plottedDrawing.getGeometryCount()));
                    controller.labelPlottedShapes.setText(Utils.defaultNF.format(task.plottedDrawing.getGeometryCount()));
                    controller.labelPlottedVertices.setText(Utils.defaultNF.format(task.plottedDrawing.getVertexCount()));
                });
                break;
            case POST_PROCESSING:
                break;
            case FINISHING:
                break;
            case FINISHED:
                saveLastRun(task);
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

    public PlottingTask initPlottingTask(DrawingArea drawingArea, PFMFactory<?> pfmFactory, ObservableDrawingSet drawingPenSet, BufferedImage image, File originalFile, ColourSplitterHandler splitter){
        //only update the distribution type the first time the PFM is changed, also only trigger the update when Start Plotting is hit again, so the current drawing doesn't get re-rendered
        Platform.runLater(() -> {
            if(updateDistributionType != null && splitter.isDefault()){
                drawingPenSet.distributionType.set(updateDistributionType);
                updateDistributionType = null;
            }
        });

        PlottingTask task = new PlottingTask(drawingArea, pfmFactory, MasterRegistry.INSTANCE.getObservablePFMSettingsList(pfmFactory), drawingPenSet, image, originalFile);
        Object[] hookReturn = Hooks.runHook(Hooks.NEW_PLOTTING_TASK, task);
        return (PlottingTask) hookReturn[0];
    }

    public void startPlotting(){
        if(activeTask.get() != null){
            activeTask.get().cancel();
        }
        if(openImage.get() != null){
            taskMonitor.queueTask(initPlottingTask(drawingArea.copy(), pfmFactory.get(), observableDrawingSet, openImage.get().getSource(), openFile, colourSplitter.get()));
        }
    }

    public void stopPlotting(){
        if(activeTask.get() != null){
            activeTask.get().stopElegantly();
        }
    }

    public void saveVersion(){
        DrawingBotV3.INSTANCE.backgroundService.submit(() -> {
            GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
            Register.PRESET_LOADER_PROJECT.updatePreset(preset);
            DrawingBotV3.INSTANCE.projectVersions.add(new ObservableProjectSettings(preset, true));
        });
    }

    public void saveLastRun(PlottingTask plottingTask){
        DrawingBotV3.INSTANCE.backgroundService.submit(() -> {
            GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
            Register.PRESET_LOADER_PROJECT.updatePreset(preset, plottingTask);
            DrawingBotV3.INSTANCE.lastRun.set(new ObservableProjectSettings(preset, true));
        });
    }

    public void resetPlotting(){
        resetTaskService();
        setActivePlottingTask(null);
    }

    public void resetTaskService(){
        taskService.shutdownNow();
        taskService = initTaskService();
        taskMonitor.resetMonitor(taskService);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    public void openFile(File file, boolean internal) {
        BufferedImageLoader.Filtered loadingTask = getImageLoaderTask(file, internal);
        if(loadingTask != null){
            taskMonitor.queueTask(loadingTask);
        }
    }

    public BufferedImageLoader.Filtered getImageLoaderTask(File file, boolean internal){
        String extension = FileUtils.getExtension(file.toString());
        if(extension.equalsIgnoreCase(".drawingbotv3")){
            FXHelper.loadPresetFile(Register.PRESET_TYPE_PROJECT, file, true);
            return null;
        }

        if(activeTask.get() != null){
            activeTask.get().cancel();
            setActivePlottingTask(null);
            openImage.set(null);
        }
        openFile = file;
        BufferedImageLoader.Filtered loadingImage = new BufferedImageLoader.Filtered(file.getPath(), internal);
        loadingImage.setOnSucceeded(e -> {
            DrawingBotV3.INSTANCE.openImage.set((FilteredBufferedImage) e.getSource().getValue());
            DrawingBotV3.INSTANCE.display_mode.set(EnumDisplayMode.IMAGE);

            DrawingBotV3.RENDERER.shouldRedraw = true;
            DrawingBotV3.RENDERER.canvasNeedsUpdate = true;
            FXApplication.primaryStage.setTitle(DBConstants.versionName + ", Version: " + DBConstants.appVersion + ", '" + file.getName() + "'");
        });
        return loadingImage;
    }

    public void setActivePlottingTask(PlottingTask task){
        if(activeTask.get() == task){
            return;
        }
        if(activeTask.get() != null){
            final PlottingTask toReset = activeTask.get();
            backgroundService.submit(toReset::reset); //help GC by removing references to Geometries, run after other queue tasks have finished
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

    public Task<?> createExportTask(DrawingExportHandler exportHandler, PlottingTask plottingTask, IGeometryFilter pointFilter, String extension, File saveLocation, boolean seperatePens, boolean forceBypassOptimisation){
        return createExportTask(exportHandler, plottingTask, pointFilter, extension, saveLocation, seperatePens, forceBypassOptimisation, PrintResolution.copy(plottingTask.resolution));
    }

    public Task<?> createExportTask(DrawingExportHandler exportHandler, PlottingTask plottingTask, IGeometryFilter pointFilter, String extension, File saveLocation, boolean seperatePens, boolean forceBypassOptimisation, PrintResolution resolution){
        ExportTask task = new ExportTask(exportHandler, plottingTask, pointFilter, extension, saveLocation, seperatePens, true, forceBypassOptimisation, false, resolution);
        Object[] hookReturn = Hooks.runHook(Hooks.NEW_EXPORT_TASK, task);
        taskMonitor.queueTask((Task<?>) hookReturn[0]);
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

    //// MOUSE EVENTS

    public void resetView(){
        controller.viewportScrollPane.scaleValue = 1;
        controller.viewportScrollPane.updateScale();
        controller.viewportScrollPane.setHvalue(0.5);
        controller.viewportScrollPane.setVvalue(0.5);
        FXApplication.drawTimer.resetLayoutTimer = 2;
    }

    /**
     * The viewport centre relative to the scene
     */
    public Point2D getViewportCentre(){

        return DrawingBotV3.INSTANCE.controller.viewportScrollPane.localToScene(
                DrawingBotV3.INSTANCE.controller.viewportScrollPane.getWidth()/2,
                DrawingBotV3.INSTANCE.controller.viewportScrollPane.getHeight()/2);
    }

    public void onMouseMoved(MouseEvent event){
        Point2D mouse = new Point2D(event.getSceneX(), event.getSceneY());
        Point2D position = !display_mode.get().isOpenGL() ? RENDERER.sceneToRenderer(mouse) : OPENGL_RENDERER.sceneToRenderer(mouse);

        if(drawingArea.useOriginalSizing.get()){
            controller.labelCurrentPosition.setText(((int)position.getX())  + ", " + ((int)position.getY()) + " px");
        }else{
            double printScale = 1;

            if(display_mode.get() != EnumDisplayMode.IMAGE && getActiveTask() != null){
                printScale = getActiveTask().resolution.getPrintScale();
            }
            if(display_mode.get() == EnumDisplayMode.IMAGE && openImage.get() != null){
                printScale = openImage.get().resolution.getPrintScale();
            }

            position = position.multiply(printScale);

            controller.labelCurrentPosition.setText(((int)position.getX())  + ", " + ((int)position.getY()) + " mm");
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// SERVICES

    public void startTask(ExecutorService service, Task<?> task){
        service.submit(task);
        taskMonitor.logTask(task);
    }

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

    public ExecutorService initSerialConnectionService(){
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Serial Connection Writing Service");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        });
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
}