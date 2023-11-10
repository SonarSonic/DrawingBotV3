/*
  DrawingBotV3 by Ollie Lansdell <ollielansdell@hotmail.co.uk
  Original by Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
 */
package drawingbot;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import drawingbot.api.*;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.files.loaders.AbstractFileLoader;
import drawingbot.image.format.FilteredImageData;
import drawingbot.integrations.vpype.VpypeSettings;
import drawingbot.javafx.FXController;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.plotting.ITaskManager;
import drawingbot.javafx.*;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.files.*;
import drawingbot.javafx.observables.ObservableVersion;
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
import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;
import org.jetbrains.annotations.Nullable;

public class DrawingBotV3 {

    public static final Logger logger = Logger.getLogger("DrawingBotV3");
    public static DrawingBotV3 INSTANCE;

    public static JavaFXRenderer RENDERER;
    public static IRenderer OPENGL_RENDERER;

    // DISPLAY \\
    public final SimpleObjectProperty<IDisplayMode> displayMode = new SimpleObjectProperty<>();
    public final SimpleIntegerProperty geometryCount = new SimpleIntegerProperty(0);
    public final SimpleLongProperty vertexCount = new SimpleLongProperty(0L);
    public final SimpleLongProperty elapsedTimeMS = new SimpleLongProperty(0L);

    public final SimpleFloatProperty imageResolutionWidth = new SimpleFloatProperty(0);
    public final SimpleFloatProperty imageResolutionHeight = new SimpleFloatProperty(0);
    public final SimpleObjectProperty<UnitsLength> imageResolutionUnits = new SimpleObjectProperty<>(UnitsLength.PIXELS);

    public final SimpleFloatProperty plottingResolutionWidth = new SimpleFloatProperty(0);
    public final SimpleFloatProperty plottingResolutionHeight = new SimpleFloatProperty(0);
    public final SimpleObjectProperty<UnitsLength> plottingResolutionUnits = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);

    public final SimpleIntegerProperty relativeMousePosX = new SimpleIntegerProperty(0);
    public final SimpleIntegerProperty relativeMousePosY = new SimpleIntegerProperty(0);
    public final SimpleObjectProperty<UnitsLength> relativeMouseUnits = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);

    //VPYPE SETTINGS
    public final VpypeSettings vpypeSettings = new VpypeSettings();

    // WINDOW TITLES \\
    public final StringProperty projectName = new SimpleStringProperty();

    // PROJECTS \\
    public final SimpleObjectProperty<ObservableProject> activeProject = new SimpleObjectProperty<>();
    public final ObservableList<ObservableProject> activeProjects = FXCollections.observableArrayList();

    // BINDINGS \\
    public final MonadicBinding<String> projectNameBinding = EasyBind.select(activeProject).selectObject(project -> project.name);
    public final MonadicBinding<FilteredImageData> imageBinding = EasyBind.select(activeProject).selectObject(project -> project.openImage);
    public final MonadicBinding<PlottedDrawing> drawingBinding = EasyBind.select(activeProject).selectObject(project -> project.currentDrawing);
    public final MonadicBinding<DBTask<?>> activeTaskBinding = EasyBind.select(activeProject).selectObject(project -> project.activeTask);
    public final MonadicBinding<PFMTask> renderedTaskBinding = EasyBind.select(activeProject).selectObject(project -> project.renderedTask);
    public final MonadicBinding<ObservableList<ExportedDrawingEntry>> exportedDrawingsBinding = EasyBind.select(activeProject).selectObject(project -> project.exportedDrawings);

    // VIEWPORT SETTINGS \\
    public static int SVG_DPI = 96;
    public static int PDF_DPI = 72;

    // THREADS \\
    public ExecutorService taskService = initTaskService();
    public ExecutorService backgroundService = initBackgroundService();
    public ExecutorService lazyBackgroundService = initLazyBackgroundService();
    public ExecutorService imageFilteringService = initImageFilteringService();
    //public ExecutorService parallelPlottingService = initParallelPlottingService();
    public ExecutorService serialConnectionWriteService = initSerialConnectionService();

    public TaskMonitor taskMonitor = new TaskMonitor(taskService);

    // GUI \\
    public FXController controller;
    public FlagStates globalFlags = new FlagStates(Flags.GLOBAL_CATEGORY);

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    protected DrawingBotV3() {}

    public void init(){
        activeProject.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                displayMode.unbindBidirectional(oldValue.displayModeProperty());
                oldValue.setLoaded(false);
            }
            if(newValue != null){
                displayMode.bindBidirectional(newValue.displayModeProperty());
                newValue.setLoaded(true);
                newValue.reRender();
            }
        });

    }

    public static ObservableProject project(){
        return INSTANCE.activeProject.get();
    }

    /**
     * The current DBTaskContext is an important reference used to make sure any changes to settings always happen in the correct project.
     * A reference to the current context should be kept throughout any operations off the JavaFX Thread.
     * The context should be taken before entering a Platform.runLater or any other off-thread work where the time it runs can't be determined.
     * Allowing users to switch between projects and keep background operations of each still running without interferring with the new project.
     */
    public static DBTaskContext context(){
        return project().context;
    }

    public static ITaskManager taskManager(){
        return context().taskManager();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private final SimpleDoubleProperty localProgress = new SimpleDoubleProperty(0);
    private final SimpleStringProperty localMessage = new SimpleStringProperty("");

    public void updateLocalMessage(String message){
        localMessage.set(message);
    }

    public void updateLocalProgress(double progress){
        localProgress.set(progress);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    // RENDER FLAGS \\


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void tick(){

        // Update the latest shapes/vertices counts from the active task
        PFMTask renderedTask = context().taskManager().getRenderedTask();
        PlottedDrawing currentDrawing = context().taskManager().getCurrentDrawing();
        FilteredImageData openImage = project().openImage.get();

        // Tick the current tasks
        taskMonitor.tick();

        // Tick all plugins
        MasterRegistry.PLUGINS.forEach(IPlugin::tick);

        // Tick all open projects
        DrawingBotV3.INSTANCE.activeProjects.forEach(ObservableProject::tick);

        // Check for pen distribution updates
        if(globalFlags.anyMatch(Flags.UPDATE_PEN_DISTRIBUTION)){
            globalFlags.setFlag(Flags.UPDATE_PEN_DISTRIBUTION, false);
            lazyBackgroundService.submit(() -> {
                PlottedDrawing drawing = project().getCurrentDrawing();
                if(drawing != null){
                    drawing.updatePenDistribution();
                }
                Platform.runLater(() -> project().reRender());
            });
        }

        // Auto run the PFM is the PFM Settings have changed
        //NOTE AUTO RUN, IS RUNNING EVEN FOR NON USER CHANGES!
        if(DBPreferences.INSTANCE.autoRunPFM.get() && globalFlags.anyMatch(Flags.PFM_SETTINGS_USER_EDITED)){
            globalFlags.setFlag(Flags.PFM_SETTINGS_USER_EDITED, false);
            resetTaskService();
            startPlotting(context());
        }

        // Update Drawing Stats
        if(renderedTask != null){
            geometryCount.set(renderedTask.getCurrentGeometryCount());
            vertexCount.set(renderedTask.getCurrentVertexCount());
            elapsedTimeMS.set(renderedTask.getElapsedTime());
        }else if(currentDrawing != null){
            geometryCount.set(currentDrawing.getDisplayedShapeMax() - currentDrawing.getDisplayedShapeMin());
            vertexCount.set(currentDrawing.getDisplayedVertexCount());
            //Keep the elapsed time
        }else{
            geometryCount.set(0);
            vertexCount.set(0);
            elapsedTimeMS.set(0);
        }

        // Update Image Stats
        plottingResolutionWidth.set(project().targetCanvas.getScaledDrawingWidth());
        plottingResolutionHeight.set(project().targetCanvas.getScaledDrawingHeight());
        plottingResolutionUnits.set(project().targetCanvas.getUnits());

        imageResolutionWidth.set(openImage == null ? 0 : openImage.getSourceCanvas().getWidth());
        imageResolutionHeight.set(openImage == null ? 0 : openImage.getSourceCanvas().getHeight());
        imageResolutionUnits.set(openImage == null ? UnitsLength.PIXELS : openImage.getSourceCanvas().getUnits());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ////// EVENTS

    public void onDisplayModeChanged(IDisplayMode oldValue, IDisplayMode newValue){
        if(oldValue != null){
            oldValue.resetSettings();
        }

        if(oldValue == null || newValue.getRenderer() != oldValue.getRenderer()){
            if(oldValue != null){
                oldValue.getRenderer().stopRenderer();
            }
            newValue.getRenderer().startRenderer();
        }

        newValue.applySettings();
    }

    //// PLOTTING TASKS

    public PlottedDrawing createNewPlottedDrawing() {
        return new PlottedDrawing(project().getDrawingArea(), project().getDrawingSets());
    }

    public PFMTask initPFMTask(DBTaskContext context, ICanvas canvas, PFMFactory<?> pfmFactory, @Nullable List<GenericSetting<?, ?>> pfmSettings, ObservableDrawingSet drawingPenSet, @Nullable FilteredImageData imageData, boolean isSubTask) {
        if(imageData != null){
            imageData.updateAll(context.project.imageSettings.get());
            canvas = imageData.getDestCanvas();
        }
        return initPFMTask(context, new PlottedDrawing(canvas, project().getDrawingSets()), pfmFactory, pfmSettings, drawingPenSet, imageData, isSubTask);
    }

    public PFMTask initPFMTask(DBTaskContext context, PlottedDrawing drawing, PFMFactory<?> pfmFactory, @Nullable List<GenericSetting<?, ?>> settings, ObservableDrawingSet drawingPenSet, @Nullable FilteredImageData imageData, boolean isSubTask){
        if(settings == null){
            settings = MasterRegistry.INSTANCE.getObservablePFMSettingsList(pfmFactory);
        }
        PFMTask task;
        if(!pfmFactory.isGenerativePFM()){
            task = new PFMTaskImage(context, drawing, pfmFactory, drawingPenSet, settings, context.project.imageSettings.get(), imageData);
        }else{
            task = new PFMTask(context, drawing, pfmFactory, drawingPenSet, settings);
        }
        Object[] hookReturn = Hooks.runHook(Hooks.NEW_PLOTTING_TASK, task);
        task = (PFMTask) hookReturn[0];
        task.isSubTask = isSubTask;
        return task;
    }

    public void startPlotting(DBTaskContext context){
        if(context.project().activeTask.get() != null){
            context.project().activeTask.get().cancel();
        }
        if(context.project().openImage.get() != null || context.project.getPFMSettings().factory.get().isGenerativePFM()){
            taskMonitor.queueTask(initPFMTask(context(), context.project.getDrawingArea().copy(), context.project.getPFMSettings().factory.get(), null, context.project.getDrawingSets().activeDrawingSet.get(), context.project.openImage.get(), false));
        }
    }

    public void stopPlotting(DBTaskContext context){
        if(context.project().activeTask.get() != null){
            context.project().activeTask.get().stopElegantly();
        }
    }

    public void saveLastRun(DBTaskContext context){
        backgroundService.submit(() -> {
            GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
            Register.PRESET_LOADER_PROJECT.getDefaultManager().updatePreset(context, preset, false); //TODO FIXME! - is last run even used ?
            context.project().lastRun.set(new ObservableVersion(preset, true));
        });
    }

    public void resetPlotting(DBTaskContext context){
        resetTaskService();
        context.taskManager().setActiveTask(null);
        context.taskManager().setCurrentDrawing(null);
        context.taskManager().setRenderedTask(null);
        project().reRender();
        project().displayMode.setValue(Register.INSTANCE.DISPLAY_MODE_IMAGE);
    }

    public void resetTaskService(){
        Task<?> task = taskMonitor.currentTask;
        if(task != null){
            task.cancel();
        }
        taskService.shutdownNow();
        taskService = initTaskService();
        taskMonitor.resetMonitor(taskService);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    public void openFile(DBTaskContext context, File file, boolean internal, boolean isSubTask) {
        AbstractFileLoader loadingTask = getImageLoaderTask(context, file, internal, isSubTask);
        if(loadingTask != null){
            taskMonitor.queueTask(loadingTask);
        }
    }

    public AbstractFileLoader getImageLoaderTask(DBTaskContext context, File file, boolean internal, boolean isSubTask){
        AbstractFileLoader loadingTask = MasterRegistry.INSTANCE.getFileLoader(context, file, internal, isSubTask);

        //if the file loader could provide an image, wipe the current one
        if(!isSubTask && loadingTask.hasImageData() && context.project.activeTask.get() != null){
            context.project.activeTask.get().cancel();
            context.taskManager().setActiveTask(null);
            context.project.openImage.set(null);
        }

        loadingTask.setOnSucceeded(e -> {
            if(!isSubTask && e.getSource().getValue() != null){
                context.project.openImage.set((FilteredImageData) e.getSource().getValue());
                Platform.runLater(() -> context.project().setDisplayMode(Register.INSTANCE.DISPLAY_MODE_IMAGE));
                projectName.set(file.getName());
            }
            loadingTask.onFileLoaded();
        });
        return loadingTask;
    }

    public static void onPlottingTaskStageFinished(DBTaskContext context, PFMTask task, EnumTaskStage stage){
        switch (stage){
            case QUEUED:
                break;
            case PRE_PROCESSING:
                Platform.runLater(() -> {
                    if(task.context.project().getDisplayMode().getRenderer() != OPENGL_RENDERER || !FXApplication.isPremiumEnabled){
                        task.context.project().setDisplayMode(Register.INSTANCE.DISPLAY_MODE_DRAWING);
                    }
                });
                break;
            case DO_PROCESS:
                break;
            case POST_PROCESSING:
            case FINISHING:
                break;
            case FINISHED:
                DrawingBotV3.INSTANCE.saveLastRun(context);
                break;
        }
        if(task == task.context.taskManager().getRenderedTask()){
            context.project().setRenderFlag(Flags.ACTIVE_TASK_CHANGED_STATE, true);
        }
        logger.info("Plotting Task: Finished Stage " + stage.name());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// EXPORT TASKS

    public Task<?> createExportTask(DrawingExportHandler exportHandler, ExportTask.Mode exportMode, PlottedDrawing plottedDrawing, IGeometryFilter pointFilter, String extension, File saveLocation, boolean forceBypassOptimisation){
        ExportTask task = new ExportTask(context(), exportHandler, exportMode, plottedDrawing, pointFilter, extension, saveLocation, true, forceBypassOptimisation, false);
        Object[] hookReturn = Hooks.runHook(Hooks.NEW_EXPORT_TASK, task);
        taskMonitor.queueTask((Task<?>) hookReturn[0]);
        return task;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// MOUSE EVENTS

    public void resetView(){
        controller.viewportScrollPane.setHvalue(0.5);
        controller.viewportScrollPane.setVvalue(0.5);
        controller.viewportScrollPane.setScale(project().dpiScaling.get() ? getDPIScaleFactor() / DrawingBotV3.RENDERER.canvasScaling : 1);
        displayMode.get().getRenderer().updateCanvasPosition();
        controller.viewportScrollPane.layout();
        controller.viewportScrollPane.setHvalue(0.5);
        controller.viewportScrollPane.setVvalue(0.5);
    }

    public double getDPIScaleFactor(){
        ICanvas canvas = DrawingBotV3.project().displayMode.get().getRenderer().getRefCanvas();
        if(canvas == null || canvas.getUnits()==UnitsLength.PIXELS){
            return 1;
        }
        double screenDPI = Screen.getPrimary().getDpi();
        double widthPixels = canvas.getWidth(UnitsLength.INCHES) * screenDPI;
        double normalWidth = canvas.getScaledWidth();
        return (widthPixels/normalWidth);
    }

    /**
     * The viewport centre relative to the scene
     */
    public Point2D getViewportCentre(){

        return controller.viewportScrollPane.localToScene(
                controller.viewportScrollPane.getWidth()/2,
                controller.viewportScrollPane.getHeight()/2);
    }

    public void onMouseMovedViewport(MouseEvent event){
        controller.onMouseMovedColourPicker(event);
        Point2D mouse = new Point2D(event.getSceneX(), event.getSceneY());
        Point2D position = project().displayMode.get().getRenderer().sceneToRenderer(mouse);

        if(project().getDrawingArea().useOriginalSizing.get()){
            relativeMousePosX.set((int)position.getX());
            relativeMousePosY.set((int)position.getY());
            relativeMouseUnits.set(UnitsLength.PIXELS);
        }else{
            double printScale = 1;

            if(project().displayMode.get() != Register.INSTANCE.DISPLAY_MODE_IMAGE && context().taskManager().getCurrentDrawing() != null){
                printScale = context().taskManager().getCurrentDrawing().getCanvas().getPlottingScale();
            }
            if(project().displayMode.get() == Register.INSTANCE.DISPLAY_MODE_IMAGE && project().openImage.get() != null){
                printScale = project().openImage.get().getTargetCanvas().getPlottingScale();
            }

            position = position.multiply(1F/printScale);

            relativeMousePosX.set((int)position.getX());
            relativeMousePosY.set((int)position.getY());
            relativeMouseUnits.set(UnitsLength.MILLIMETRES);
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

    public ExecutorService initTaskService(){
        return Executors.newSingleThreadExecutor(threadFactory("DrawingBotV3 - Task Runner"));
    }

    public ExecutorService initBackgroundService(){
        return Executors.newSingleThreadExecutor(threadFactory("DrawingBotV3 - Main Background"));
    }

    public ExecutorService initLazyBackgroundService(){
        return Executors.newCachedThreadPool(threadFactory("DrawingBotV3 - Lazy Background"));
    }

    public ExecutorService initImageFilteringService(){
        return Executors.newSingleThreadExecutor(threadFactory("DrawingBotV3 - Image Filtering"));
    }

    public ExecutorService initParallelPlottingService(){
        return Executors.newFixedThreadPool(5, threadFactory("DrawingBotV3 - Parallel Plotting"));
    }

    public ExecutorService initSerialConnectionService(){
        return Executors.newSingleThreadExecutor(threadFactory("DrawingBotV3 - Serial Connection Writing"));
    }

    public final static Thread.UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> {
        DrawingBotV3.logger.log(Level.SEVERE, "Thread Exception: " + thread.getName(), throwable);
    };

    public static ThreadFactory threadFactory(String name){
        return r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(exceptionHandler);
            return t;
        };
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
}
