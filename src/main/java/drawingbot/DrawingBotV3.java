/*
  DrawingBotV3 by Ollie Lansdell <ollielansdell@hotmail.co.uk
  Original by Scott Cooper, Dullbits.com, <scottslongemailaddress@gmail.com>
 */
package drawingbot;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import drawingbot.api.*;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.files.loaders.AbstractFileLoader;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.format.ImageData;
import drawingbot.javafx.FXController;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.plotting.*;
import drawingbot.javafx.*;
import drawingbot.files.*;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.modes.DisplayModeBase;
import drawingbot.render.renderer.JFXRenderer;
import drawingbot.software.SoftwareManager;
import drawingbot.utils.*;
import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class DrawingBotV3 {

    public static final Logger logger = Logger.getLogger("DrawingBotV3");
    public static DrawingBotV3 INSTANCE;

    // DISPLAY \\
    public final ObjectProperty<DisplayModeBase> projectDisplayMode = new SimpleObjectProperty<>();
    public final IntegerProperty geometryCount = new SimpleIntegerProperty(0);
    public final LongProperty vertexCount = new SimpleLongProperty(0L);
    public final LongProperty elapsedTimeMS = new SimpleLongProperty(0L);

    public final DoubleProperty imageResolutionWidth = new SimpleDoubleProperty(0);
    public final DoubleProperty imageResolutionHeight = new SimpleDoubleProperty(0);
    public final ObjectProperty<UnitsLength> imageResolutionUnits = new SimpleObjectProperty<>(UnitsLength.PIXELS);

    public final DoubleProperty plottingResolutionWidth = new SimpleDoubleProperty(0);
    public final DoubleProperty plottingResolutionHeight = new SimpleDoubleProperty(0);
    public final ObjectProperty<UnitsLength> plottingResolutionUnits = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);

    public final IntegerProperty relativeMousePosX = new SimpleIntegerProperty(0);
    public final IntegerProperty relativeMousePosY = new SimpleIntegerProperty(0);
    public final ObjectProperty<UnitsLength> relativeMouseUnits = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);

    // PROJECTS \\
    public final ObjectProperty<ObservableProject> activeProject = new SimpleObjectProperty<>();
    public final ObservableList<ObservableProject> activeProjects = FXCollections.observableArrayList();

    // BINDINGS \\
    public final StringProperty projectName = new SimpleStringProperty();
    public final ObjectProperty<DBTask<?>> projectActiveTask = new SimpleObjectProperty<>();
    public final ObjectProperty<PFMTask> projectRenderedTask = new SimpleObjectProperty<>();
    public final ObjectProperty<PlottedDrawing> projectCurrentDrawing = new SimpleObjectProperty<>();
    public final ObjectProperty<PlottedDrawing> projectExportedDrawing = new SimpleObjectProperty<>();
    public final ObjectProperty<ObservableCanvas> projectDrawingArea = new SimpleObjectProperty<>();
    public final ObjectProperty<ImageData> projectOpenImage = new SimpleObjectProperty<>();
    public final ObjectProperty<ImageFilterSettings> projectImageSettings = new SimpleObjectProperty<>();
    public final ObjectProperty<ObservableList<ObservableDrawingPen>> projectSelectedPens = new SimpleObjectProperty<>();

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
                projectName.unbindBidirectional(oldValue.name);
                projectDisplayMode.unbindBidirectional(oldValue.displayModeProperty());
                projectActiveTask.unbindBidirectional(oldValue.activeTask);
                projectRenderedTask.unbindBidirectional(oldValue.renderedTask);
                projectCurrentDrawing.unbindBidirectional(oldValue.currentDrawing);
                projectDrawingArea.unbindBidirectional(oldValue.drawingArea);
                projectOpenImage.unbindBidirectional(oldValue.openImage);
                projectImageSettings.unbindBidirectional(oldValue.imageSettings);
                projectExportedDrawing.unbindBidirectional(oldValue.exportDrawing);
                projectSelectedPens.unbindBidirectional(oldValue.selectedPens);

                oldValue.setLoaded(false);
            }
            if(newValue != null){
                projectName.bindBidirectional(newValue.name);
                projectDisplayMode.bindBidirectional(newValue.displayModeProperty());
                projectActiveTask.bindBidirectional(newValue.activeTask);
                projectRenderedTask.bindBidirectional(newValue.renderedTask);
                projectCurrentDrawing.bindBidirectional(newValue.currentDrawing);
                projectDrawingArea.bindBidirectional(newValue.drawingArea);
                projectOpenImage.bindBidirectional(newValue.openImage);
                projectImageSettings.bindBidirectional(newValue.imageSettings);
                projectExportedDrawing.bindBidirectional(newValue.exportDrawing);
                projectSelectedPens.bindBidirectional(newValue.selectedPens);

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
        ImageData openImage = project().openImage.get();

        // Tick the current tasks
        taskMonitor.tick();

        // Tick the preset loaders, sending updates to the background thread
        JsonLoaderManager.INSTANCE.tick();

        // Update recent project data
        RecentProjectHandler.tick();

        // Tick all plugins
        SoftwareManager.getLoadedPlugins().forEach(IPlugin::tick);

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
                Platform.runLater(() -> {
                    project().reRender();
                    project().getDrawingSets().updatePerPenStats(project().getDisplayedDrawing());
                });
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


    //// PLOTTING TASKS

    public void startPlotting(DBTaskContext context){
        if(context.project().activeTask.get() != null){
            context.project().activeTask.get().cancel();
        }
        if(context.project().openImage.get() != null || context.project().getPFMFactory().isGenerativePFM()){
            taskMonitor.queueTask(PFMTaskBuilder.create(context).createPFMTask());
        }
    }

    public void stopPlotting(DBTaskContext context){
        if(context.project().activeTask.get() != null){
            context.project().activeTask.get().stopElegantly();
        }
    }

    public void saveLastRun(DBTaskContext context){
        /* TODO FIXME! - last run is not used yet
        backgroundService.submit(() -> {
            GenericPreset<PresetProjectSettings> preset = Register.PRESET_MANAGER_PROJECT.createPresetFromTarget(context, project());
            context.project().lastRun.set(new ObservableVersion(preset, true));
        });
         */
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
                context.project.openImage.set((ImageData) e.getSource().getValue());
                Platform.runLater(() -> context.project().setDisplayMode(Register.INSTANCE.DISPLAY_MODE_IMAGE));
            }
            loadingTask.onFileLoaded();
        });
        return loadingTask;
    }

    public static void onPlottingTaskStageFinished(DBTaskContext context, PFMTask task, EnumTaskStage stage){
        switch (stage){
            case START:
                break;
            case PRE_PROCESSING:
                Platform.runLater(() -> {
                    //TODO CHANGE ME ?
                    if(task.context.project().getDisplayMode().getRendererFactory() == JFXRenderer.JFX_RENDERER_FACTORY || !FXApplication.isPremiumEnabled){
                        task.context.project().setDisplayMode(Register.INSTANCE.DISPLAY_MODE_DRAWING);
                    }
                });
                break;
            case DO_PROCESS:
                break;
            case POST_PROCESSING:
            case FINISH:
                DrawingBotV3.INSTANCE.saveLastRun(context);
                break;
        }
        if(task == task.context.taskManager().getRenderedTask()){
            context.project().setRenderFlag(Flags.ACTIVE_TASK_CHANGED_STATE, true);
        }
    }

    public <T> void setRenderFlag(Flags.BooleanFlag flag){
        if(controller == null || controller.viewport == null){
            return;
        }
        controller.viewport.getRenderFlags().setFlag(flag, true);
    }

    public <T> void setRenderFlag(Flags.Flag<T> flag, T value){
        if(controller == null || controller.viewport == null){
            return;
        }
        controller.viewport.getRenderFlags().setFlag(flag, value);
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

    public void resetView(){
        controller.viewport.resetView();
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
