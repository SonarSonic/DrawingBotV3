/*
  DrawingBotV3 by Ollie Lansdell <ollielansdell@hotmail.co.uk
  Original by Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
 */
package drawingbot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import drawingbot.api.Hooks;
import drawingbot.api.IGeometryFilter;
import drawingbot.api.IPlugin;
import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.json.presets.PresetProjectSettings;
import drawingbot.api.ICanvas;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.*;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.files.*;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.FilteredBufferedImage;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.observables.ObservableProjectSettings;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PFMTaskImage;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.IDisplayMode;
import drawingbot.render.IRenderer;
import drawingbot.render.jfx.JavaFXRenderer;
import drawingbot.utils.*;
import drawingbot.plotting.PFMTask;
import drawingbot.utils.flags.Flags;
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
    public ObservableCanvas drawingArea = new ObservableCanvas();
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
    public final SimpleFloatProperty cyanMultiplier = new SimpleFloatProperty(1F);
    public final SimpleFloatProperty magentaMultiplier = new SimpleFloatProperty(1F);
    public final SimpleFloatProperty yellowMultiplier = new SimpleFloatProperty(1F);
    public final SimpleFloatProperty keyMultiplier = new SimpleFloatProperty(0.75F);

    // PEN SETS \\
    public ObservableDrawingPen invisibleDrawingPen = null;
    public SimpleObjectProperty<ObservableDrawingSet> activeDrawingSet = new SimpleObjectProperty<>();
    public ObservableList<ObservableDrawingSet> drawingSetSlots = FXCollections.observableArrayList();

    // VERSION CONTROL \\
    public final ObservableList<ObservableProjectSettings> projectVersions = FXCollections.observableArrayList();
    public final SimpleObjectProperty<ObservableProjectSettings> lastRun = new SimpleObjectProperty<>();

    // DISPLAY \\
    public final SimpleObjectProperty<IDisplayMode> displayMode = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<EnumBlendMode> blendMode = new SimpleObjectProperty<>(EnumBlendMode.NORMAL);

    //VIEWPORT SETTINGS \\
    public static int SVG_DPI = 96;
    public static int PDF_DPI = 72;

    public final SimpleBooleanProperty exportRange = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty displayGrid = new SimpleBooleanProperty(false);

    //the default JFX viewport background colours
    public Color backgroundColourDefault = new Color(244 / 255F, 244 / 255F, 244 / 255F, 1F);
    public Color backgroundColourDark = new Color(65 / 255F, 65 / 255F, 65 / 255F, 1F);

    //// VARIABLES \\\\

    // THREADS \\
    public ExecutorService taskService = initTaskService();
    public ExecutorService backgroundService = initBackgroundService();
    public ExecutorService imageFilteringService = initImageFilteringService();
    public ExecutorService parallelPlottingService = initParallelPlottingService();
    public ExecutorService serialConnectionWriteService = initSerialConnectionService();

    public TaskMonitor taskMonitor = new TaskMonitor(taskService);

    // TASKS \\
    public final ObjectProperty<FilteredBufferedImage> openImage = new SimpleObjectProperty<>(null);
    public final ObjectProperty<PFMTask> activeTask = new SimpleObjectProperty<>(null);
    public final ObjectProperty<PFMTask> renderedTask = new SimpleObjectProperty<>(null);
    public final ObjectProperty<PlottedDrawing> renderedDrawing = new SimpleObjectProperty<>(null);

    public File openFile = null;

    // GUI \\
    public FXController controller;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public DrawingBotV3() {}

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private final SimpleDoubleProperty localProgress = new SimpleDoubleProperty(0);
    private final SimpleStringProperty localMessage = new SimpleStringProperty("");

    public void updateLocalMessage(String message){
        localMessage.set(message);
    }

    public void updateLocalProgress(double progress){
        localProgress.set(progress);
    }

    public EnumDistributionType nextDistributionType = null;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    // RENDER FLAGS \\

    public <T> void setRenderFlag(Flags.BooleanFlag flag){
        for(IDisplayMode displayMode : MasterRegistry.INSTANCE.displayModes){
            displayMode.getRenderFlags().setFlag(flag, true);
        }
    }

    public <T> void setRenderFlag(Flags.Flag<T> flag, T value){
        for(IDisplayMode displayMode : MasterRegistry.INSTANCE.displayModes){
            displayMode.getRenderFlags().setFlag(flag, value);
        }
    }

    {
        activeTask.addListener((observable, oldValue, newValue) -> setRenderFlag(Flags.TASK_CHANGED, true));
        renderedTask.addListener((observable, oldValue, newValue) -> setRenderFlag(Flags.TASK_CHANGED, true));
        renderedDrawing.addListener((observable, oldValue, newValue) -> setRenderFlag(Flags.TASK_CHANGED, true));
        displayMode.addListener((observable, oldValue, newValue) -> {
            if(oldValue == null || newValue.getRenderer() == oldValue.getRenderer())
                setRenderFlag(Flags.FORCE_REDRAW, true);
        });
        openImage.addListener((observable, oldValue, newValue) -> setRenderFlag(Flags.FORCE_REDRAW, true));
    }

    public void reRender(){
        setRenderFlag(Flags.FORCE_REDRAW);
    }

    public void onCanvasChanged(){
        setRenderFlag(Flags.CANVAS_CHANGED, true);
    }

    public void clearDrawingRender(){
        setRenderFlag(Flags.CLEAR_DRAWING, true);
    }

    public void onImageFiltersChanged(){
        setRenderFlag(Flags.IMAGE_FILTERS_FULL_UPDATE, true);
    }

    public void onImageFilterDirty(){
        setRenderFlag(Flags.IMAGE_FILTERS_PARTIAL_UPDATE, true);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void updateUI(){

        taskMonitor.tick();

        //TODO FIX THIS updatinmg on every tick

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
            controller.labelPlottingResolution.setText((int)(openImage.get().getCanvas().getScaledWidth()) + " x " + (int)(openImage.get().getCanvas().getScaledHeight()));
        }else{
            controller.labelImageResolution.setText("0 x 0");
            controller.labelPlottingResolution.setText("0 x 0");
        }

        //tick all plugins
        MasterRegistry.PLUGINS.forEach(IPlugin::tick);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ////// EVENTS

    public void onDisplayModeChanged(IDisplayMode oldValue, IDisplayMode newValue){
        if(oldValue != null){
            oldValue.resetSettings();
        }

        if(oldValue == null || newValue.getRenderer() != oldValue.getRenderer()){
            newValue.getRenderer().switchToRenderer();
        }

        newValue.applySettings();
    }

    public void onPlottingTaskStageFinished(PFMTask task, EnumTaskStage stage){
        switch (stage){
            case QUEUED:
                break;
            case PRE_PROCESSING:
                Platform.runLater(() -> {
                    if(displayMode.get().getRenderer() != OPENGL_RENDERER || !FXApplication.isPremiumEnabled){
                        displayMode.setValue(Register.INSTANCE.DISPLAY_MODE_DRAWING);
                    }
                });
                break;
            case DO_PROCESS:
                Platform.runLater(() -> {
                    controller.rangeSliderDisplayedLines.setLowValue(0.0F);
                    controller.rangeSliderDisplayedLines.setHighValue(1.0F);
                    controller.textFieldDisplayedShapesMin.setText(String.valueOf(0));
                    controller.textFieldDisplayedShapesMax.setText(String.valueOf(task.drawing.getGeometryCount()));
                    controller.labelPlottedShapes.setText(Utils.defaultNF.format(task.drawing.getGeometryCount()));
                    controller.labelPlottedVertices.setText(Utils.defaultNF.format(task.drawing.getVertexCount()));
                });
                break;
            case POST_PROCESSING:
            case FINISHING:
                break;
            case FINISHED:
                saveLastRun(task);
                break;
        }
        if(task == getRenderedTask()){
            setRenderFlag(Flags.TASK_CHANGED_STATE, true);
        }
       logger.info("Plotting Task: Finished Stage " + stage.name());
    }

    public void onDrawingPenChanged(){
        updatePenDistribution();
    }

    public void onDrawingSetChanged(){
        if(activeDrawingSet.get() == null || activeDrawingSet.get().loadingDrawingSet){
            //prevents events being fired for every pen addition
            return;
        }
        updatePenDistribution();
        if(controller != null){ //may not be initilized yet
            controller.onDrawingSetChanged();
        }
    }

    public void onImageFilterChanged(ObservableImageFilter filter){
        filter.dirty.set(true);
        onImageFilterDirty();
    }

    public void updatePenDistribution(){
        if(activeTask.get() != null && activeTask.get().isTaskFinished()){
            activeTask.get().drawing.updatePenDistribution();
            reRender();
        }
    }

    //// PLOTTING TASKS


    public PFMTask initPlottingTask(ICanvas canvas, PFMFactory<?> pfmFactory, ObservableDrawingSet drawingPenSet, boolean isSubTask) {
        return initPlottingTask(canvas, pfmFactory, MasterRegistry.INSTANCE.getObservablePFMSettingsList(pfmFactory), drawingPenSet, null, null, isSubTask);
    }

    public PFMTask initPlottingTask(ICanvas canvas, PFMFactory<?> pfmFactory, ObservableDrawingSet drawingPenSet, BufferedImage image, File originalFile, boolean isSubTask) {
        return initPlottingTask(canvas, pfmFactory, MasterRegistry.INSTANCE.getObservablePFMSettingsList(pfmFactory), drawingPenSet, image, originalFile, isSubTask);
    }

    public PFMTask initPlottingTask(ICanvas canvas, PFMFactory<?> pfmFactory, List<GenericSetting<?, ?>> pfmSettings, ObservableDrawingSet drawingPenSet, boolean isSubTask) {
        return initPlottingTask(canvas, pfmFactory, pfmSettings, drawingPenSet, null, null, isSubTask);
    }

    public PFMTask initPlottingTask(ICanvas canvas, PFMFactory<?> pfmFactory, List<GenericSetting<?, ?>> pfmSettings, ObservableDrawingSet drawingPenSet, BufferedImage image, File originalFile, boolean isSubTask){
        //only update the distribution type the first time the PFM is changed, also only trigger the update when Start Plotting is hit again, so the current drawing doesn't get re-rendered
        if(!isSubTask){
            Platform.runLater(() -> {
                if(drawingPenSet.colourSeperator.get().isDefault()){
                    if(nextDistributionType != null){
                        drawingPenSet.distributionType.set(nextDistributionType);
                        nextDistributionType = null;
                    }
                }else{
                    drawingPenSet.distributionType.set(EnumDistributionType.getRecommendedType(drawingPenSet, pfmFactory));
                }
            });
        }
        PFMTask task;

        if(!pfmFactory.isGenerativePFM()){
            task = new PFMTaskImage(canvas, pfmFactory, pfmSettings, drawingPenSet, image, originalFile);
        }else{
            task = new PFMTask(canvas, pfmFactory, pfmSettings, drawingPenSet);
        }

        Object[] hookReturn = Hooks.runHook(Hooks.NEW_PLOTTING_TASK, task);
        task = (PFMTask) hookReturn[0];
        task.isSubTask = isSubTask;
        return task;
    }

    public void startPlotting(){
        if(activeTask.get() != null){
            activeTask.get().cancel();
        }
        if(openImage.get() != null){
            taskMonitor.queueTask(initPlottingTask(drawingArea.copy(), pfmFactory.get(), activeDrawingSet.get(), openImage.get().getSource(), openFile, false));
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

    public void saveLastRun(PFMTask plottingTask){
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
            openImage.set((FilteredBufferedImage) e.getSource().getValue());
            Platform.runLater(() -> displayMode.set(Register.INSTANCE.DISPLAY_MODE_IMAGE));
            FXApplication.primaryStage.setTitle(DBConstants.versionName + ", Version: " + DBConstants.appVersion + ", '" + file.getName() + "'");
        });
        return loadingImage;
    }

    public void setActivePlottingTask(PFMTask task){
        if(activeTask.get() == task){
            return;
        }
        if(activeTask.get() != null){
            final PFMTask toReset = activeTask.get();
            backgroundService.submit(toReset::reset); //help GC by removing references to Geometries, run after other queue tasks have finished
        }
        activeTask.set(task);

        if(activeTask.get() == null){
            displayMode.setValue(Register.INSTANCE.DISPLAY_MODE_IMAGE);
        }
        renderedTask.set(null);
    }

    public PFMTask getActiveTask(){
        return activeTask.get();
    }

    public PFMTask getRenderedTask(){
        return renderedTask.get() == null ? activeTask.get() : renderedTask.get();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// EXPORT TASKS

    public Task<?> createExportTask(DrawingExportHandler exportHandler, ExportTask.Mode exportMode, PFMTask plottingTask, IGeometryFilter pointFilter, String extension, File saveLocation, boolean forceBypassOptimisation){
        return createExportTask(exportHandler, exportMode, plottingTask.drawing, pointFilter, extension, saveLocation, forceBypassOptimisation);
    }

    public Task<?> createExportTask(DrawingExportHandler exportHandler, ExportTask.Mode exportMode, PlottedDrawing plottedDrawing, IGeometryFilter pointFilter, String extension, File saveLocation, boolean forceBypassOptimisation){
        ExportTask task = new ExportTask(exportHandler, exportMode, plottedDrawing, pointFilter, extension, saveLocation, true, forceBypassOptimisation, false);
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

    public void onMouseMovedViewport(MouseEvent event){
        controller.onMouseMovedColourPicker(event);
        Point2D mouse = new Point2D(event.getSceneX(), event.getSceneY());
        Point2D position = displayMode.get().getRenderer().sceneToRenderer(mouse);

        if(drawingArea.useOriginalSizing.get()){
            controller.labelCurrentPosition.setText(((int)position.getX())  + ", " + ((int)position.getY()) + " px");
        }else{
            double printScale = 1;

            if(displayMode.get() != Register.INSTANCE.DISPLAY_MODE_IMAGE && getActiveTask() != null){
                printScale = getActiveTask().drawing.getCanvas().getPlottingScale();
            }
            if(displayMode.get() == Register.INSTANCE.DISPLAY_MODE_IMAGE && openImage.get() != null){
                printScale = openImage.get().getCanvas().getPlottingScale();
            }

            position = position.multiply(1F/printScale);

            controller.labelCurrentPosition.setText(((int)position.getX())  + ", " + ((int)position.getY()) + " mm");
        }

    }

    public void onMousePressedViewport(MouseEvent event){
        controller.onMousePressedColourPicker(event);
    }

    public void onKeyPressedViewport(KeyEvent event){
        controller.onKeyPressedColourPicker(event);
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