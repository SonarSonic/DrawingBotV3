package drawingbot.files.json.projects;

import drawingbot.DrawingBotV3;
import drawingbot.api.Hooks;
import drawingbot.api.ICanvas;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.drawing.DrawingSets;
import drawingbot.files.ExportedDrawingEntry;
import drawingbot.files.FileUtils;
import drawingbot.files.VersionControl;
import drawingbot.files.json.PresetData;
import drawingbot.geom.MaskingSettings;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.image.format.FilteredImageData;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.observables.ObservableVersion;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.javafx.util.UINodeState;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMSettings;
import drawingbot.plotting.ITaskManager;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PFMTaskBuilder;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.canvas.ImageCanvas;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.IDisplayMode;
import drawingbot.utils.*;
import drawingbot.utils.flags.Flags;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.control.Tab;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ObservableProject implements ITaskManager, DrawingSets.Listener, ImageFilterSettings.Listener, ObservableCanvas.Listener, PFMSettings.Listener, VersionControl.Listener  {

    public static final String DEFAULT_NAME = "Untitled Project";

    public final SimpleStringProperty name = new SimpleStringProperty();
    public final SimpleObjectProperty<File> file = new SimpleObjectProperty<>(null);

    public final SimpleObjectProperty<Tab> tab = new SimpleObjectProperty<>();

    ////////////////////////////

    public final SimpleBooleanProperty loaded = new SimpleBooleanProperty();

    public boolean isLoaded() {
        return loaded.get();
    }

    public SimpleBooleanProperty loadedProperty() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded.set(loaded);
    }

    ////////////////////////////

    /**
     * Drawing Area
     */
    public final SimpleObjectProperty<ObservableCanvas> drawingArea = new SimpleObjectProperty<>(new ObservableCanvas());

    public ICanvas targetCanvas;

    public ObservableCanvas getDrawingArea(){
        return drawingArea.get();
    }

    public SimpleObjectProperty<ObservableCanvas> drawingAreaProperty() {
        return drawingArea;
    }

    public void setDrawingArea(ObservableCanvas drawingArea) {
        this.drawingArea.set(drawingArea);
    }

    /**
     * Image Settings
     */
    public final SimpleObjectProperty<ImageFilterSettings> imageSettings = new SimpleObjectProperty<>(new ImageFilterSettings());

    public ImageFilterSettings getImageSettings() {
        return imageSettings.get();
    }

    public SimpleObjectProperty<ImageFilterSettings> imageSettingsProperty() {
        return imageSettings;
    }

    public void setImageSettings(ImageFilterSettings imageSettings) {
        this.imageSettings.set(imageSettings);
    }

    /**
     * PFM Settings
     */
    public final SimpleObjectProperty<PFMSettings> pfmSettings = new SimpleObjectProperty<>(new PFMSettings());

    public PFMSettings getPFMSettings() {
        return pfmSettings.get();
    }

    public PFMFactory<?> getPFMFactory() {
        return getPFMSettings().getPFMFactory();
    }

    public SimpleObjectProperty<PFMSettings> pfmSettingsProperty() {
        return pfmSettings;
    }

    public void setPFMSettings(PFMSettings pfmSettings) {
        this.pfmSettings.set(pfmSettings);
    }

    public List<GenericSetting<?, ?>> getPFMSettings(PFMFactory<?> factory){
        if(getPFMFactory() == factory){
            return getPFMSettings().getSettings();
        }
        return MasterRegistry.INSTANCE.getObservablePFMSettingsList(factory);
    }
    /**
     * Drawing Sets
     */
    public SimpleObjectProperty<DrawingSets> drawingSets = new SimpleObjectProperty<>(new DrawingSets());

    public DrawingSets getDrawingSets(){
        return drawingSets.get();
    }

    public ObservableDrawingSet getActiveDrawingSet() {
        return getDrawingSets().getActiveDrawingSet();
    }

    public SimpleObjectProperty<DrawingSets> drawingSetsProperty() {
        return drawingSets;
    }

    public void setDrawingSets(DrawingSets drawingSets) {
        this.drawingSets.set(drawingSets);
    }

    /**
     * Version Control
     */
    public final ObjectProperty<VersionControl> versionControl = new SimpleObjectProperty<>(new VersionControl());

    public VersionControl getVersionControl() {
        return versionControl.get();
    }

    public ObjectProperty<VersionControl> versionControlProperty() {
        return versionControl;
    }

    public void setVersionControl(VersionControl versionControl) {
        this.versionControl.set(versionControl);
    }

    /**
     * Masking Settings
     */
    public final SimpleObjectProperty<MaskingSettings> maskingSettings = new SimpleObjectProperty<>(new MaskingSettings());

    public MaskingSettings getMaskingSettings() {
        return maskingSettings.get();
    }

    public SimpleObjectProperty<MaskingSettings> maskingSettingsProperty() {
        return maskingSettings;
    }

    public void setMaskingSettings(MaskingSettings maskingSettings) {
        this.maskingSettings.set(maskingSettings);
    }

    /**
     * Display Settings
     */
    public final SimpleObjectProperty<IDisplayMode> displayMode = new SimpleObjectProperty<>();

    public IDisplayMode getDisplayMode() {
        return displayMode.get();
    }

    public SimpleObjectProperty<IDisplayMode> displayModeProperty() {
        return displayMode;
    }

    public void setDisplayMode(IDisplayMode displayMode) {
        this.displayMode.set(displayMode);
    }


    /**
     * Open Image
     */

    public final ObjectProperty<FilteredImageData> openImage = new SimpleObjectProperty<>(null);

    public FilteredImageData getOpenImage() {
        return openImage.get();
    }

    public ObjectProperty<FilteredImageData> openImageProperty() {
        return openImage;
    }

    public void setOpenImage(FilteredImageData openImage) {
        this.openImage.set(openImage);
    }

    public final SimpleBooleanProperty dpiScaling = new SimpleBooleanProperty(false);
    public final SimpleObjectProperty<EnumBlendMode> blendMode = new SimpleObjectProperty<>(EnumBlendMode.NORMAL);
    public final SimpleObjectProperty<Bounds> canvasBoundsInScene = new SimpleObjectProperty<>(new BoundingBox(0, 0, 0, 0));

    public final SimpleBooleanProperty exportRange = new SimpleBooleanProperty(DBPreferences.INSTANCE.defaultRangeExport.get());
    public final SimpleBooleanProperty displayGrid = new SimpleBooleanProperty(false);

    // TASKS \\
    public final ObjectProperty<DBTask<?>> activeTask = new SimpleObjectProperty<>(null);
    public final ObjectProperty<PFMTask> renderedTask = new SimpleObjectProperty<>(null);
    public final ObjectProperty<PlottedDrawing> currentDrawing = new SimpleObjectProperty<>(null);
    public final ObjectProperty<PlottedDrawing> exportDrawing = new SimpleObjectProperty<>(null);
    public final ObjectProperty<ObservableList<ExportedDrawingEntry>> exportedDrawings = new SimpleObjectProperty<>(FXCollections.observableArrayList());
    public final ObjectProperty<PlottedDrawing> displayedDrawing = new SimpleObjectProperty<>(null);

    // ADDITIONAL DATA \\
    public MetadataMap metadata = new MetadataMap(new LinkedHashMap<>());
    //public FlagStates projectFlags = new FlagStates(Flags.PROJECT_CATEGORY);

    // EXTRA DATA \\
    public final SimpleStringProperty lastExportDirectory = new SimpleStringProperty("");
    public final SimpleStringProperty lastImportDirectory = new SimpleStringProperty("");

    public final DBTaskContext context = new DBTaskContext(this, this);

    public ObservableProject(){
        this(DEFAULT_NAME, null);
    }

    public ObservableProject(ObservableVersion version){
        this(version.name.get(), new File(version.file.get()));
    }

    public ObservableProject(ObservableProject project){
        this(project.name.get(), project.file.get());
        copy(project);
    }

    public ObservableProject(String name, File file){
        this.name.set(name);
        this.file.set(file);
        init();
    }

    public void copy(ObservableProject project){
        drawingArea.set(project.getDrawingArea().copy());
        imageSettings.set(project.getImageSettings().copy());
        pfmSettings.set(project.getPFMSettings().copy());
        drawingSets.set(project.getDrawingSets().copy());
        versionControl.set(project.getVersionControl().copy());

        displayMode.set(project.getDisplayMode());
        dpiScaling.set(project.dpiScaling.get());
        blendMode.set(project.blendMode.get());
        canvasBoundsInScene.set(project.canvasBoundsInScene.get());
        exportRange.set(project.exportRange.get());
        displayGrid.set(project.displayGrid.get());

        if(openImage.get() != null){
            DrawingBotV3.INSTANCE.openFile(context, openImage.get().getSourceFile(), false, true);
        }
        if(currentDrawing.get() != null) {
            currentDrawing.set(project.currentDrawing.get().copy());
        }
        if(exportDrawing.get() != null) {
            exportDrawing.set(project.exportDrawing.get().copy());
        }
        Hooks.runHook(Hooks.COPY_OBSERVABLE_PROJECT, this, project);
    }

    public void init(){

        // Register for canvas events
        PropertyUtil.addSpecialListener(drawingArea, this);

        // Register for Drawing Sets / Drawing Pen / Drawing Slot events
        PropertyUtil.addSpecialListener(drawingSets, this);

        // Register for Image Filter Addition / Removal / Update event
        PropertyUtil.addSpecialListener(imageSettings, this);

        // Register for PFM Settings User Edited event
        PropertyUtil.addSpecialListener(pfmSettings, this);

        // Listener to automate version thumbnail Loading
        PropertyUtil.addSpecialListener(versionControl, this);

        // Set the default PMF
        pfmSettings.get().factory.set(MasterRegistry.INSTANCE.getDefaultPFM());

        // Set the default Display Mode
        displayMode.set(Register.INSTANCE.DISPLAY_MODE_IMAGE);

        // Add the default Drawing Sets
        drawingSets.get().drawingSetSlots.add(new ObservableDrawingSet(MasterRegistry.INSTANCE.getDefaultDrawingSet()));
        drawingSets.get().activeDrawingSet.set(drawingSets.get().drawingSetSlots.get(0));
        drawingSets.get().activeDrawingSet.get().name.set("Default");

        // Viewport / Display Listeners
        blendMode.addListener((observable, oldValue, newValue) -> reRender());
        dpiScaling.addListener((observable, oldValue, newValue) -> resetView());

        // Task / Drawing render bindings / listeners
        activeTask.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null && renderedTask.get() != oldValue){
                oldValue.tryDestroy(); //TODO PROPER HANDLING OF DESTROYING TASKS / DRAWINGS
            }
            setRenderFlag(Flags.ACTIVE_TASK_CHANGED, true);
        });
        renderedTask.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null && activeTask.get() != oldValue){
                oldValue.tryDestroy();
            }
            setRenderFlag(Flags.ACTIVE_TASK_CHANGED, true);
        });
        currentDrawing.addListener((observable, oldValue, newValue) -> {
            //Clear up the old drawing.
            if(oldValue != null){
                //oldValue.reset(); TODO - DO WE NEED THIS?
            }
            setRenderFlag(Flags.CURRENT_DRAWING_CHANGED, true);
        });
        exportDrawing.addListener((observable, oldValue, newValue) -> {
            //If we override the export drawing, we are done with the previous one.
            if(displayMode.get() == Register.INSTANCE.DISPLAY_MODE_EXPORT_DRAWING){
                setRenderFlag(Flags.CURRENT_DRAWING_CHANGED, true);
            }
        });
        displayedDrawing.bind(Bindings.createObjectBinding(() -> displayMode.get() == Register.INSTANCE.DISPLAY_MODE_EXPORT_DRAWING ? exportDrawing.get() : currentDrawing.get(), displayMode, currentDrawing, exportDrawing));

        displayedDrawing.addListener((observable, oldValue, newValue) -> {
            getDrawingSets().updatePerPenStats(newValue);
        });

        InvalidationListener imagePropertyListener = observable -> onCanvasChanged();
        openImage.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.getPropertyList().forEach(prop -> prop.removeListener(imagePropertyListener));
            }
            if(newValue != null){
                newValue.getPropertyList().forEach(prop -> prop.addListener(imagePropertyListener));
            }
            onImageRenderingUpdated();

            if(newValue != null && (this.name.get().equals(DEFAULT_NAME) || oldValue != null && oldValue.getSourceFile().getName().equals(this.name.get()))){
                this.name.set(FileUtils.removeExtension(newValue.getSourceFile().getName()));
            }
        });

        loaded.addListener((observable, oldValue, newValue) -> {
            if(oldValue && !newValue){
                unload();
            }
            if(!oldValue && newValue){
                load();
            }
        });

        //generate the target canvas, which will always display the correct Plotting Resolution
        targetCanvas = new ImageCanvas(drawingArea.get(), new SimpleCanvas(0, 0){
            @Override
            public double getWidth() {
                return openImage.get() != null ? openImage.get().getSourceCanvas().getWidth() : 0;
            }

            @Override
            public double getHeight() {
                return openImage.get() != null ? openImage.get().getSourceCanvas().getHeight() : 0;
            }

            @Override
            public UnitsLength getUnits() {
                return openImage.get() != null ? openImage.get().getSourceCanvas().getUnits() : UnitsLength.PIXELS;
            }
        }, false){

            @Override
            public boolean flipAxis() {
                return openImage.get() != null && openImage.get().getImageCropping().getImageRotation().flipAxis;
            }
        };

        Hooks.runHook(Hooks.INIT_OBSERVABLE_PROJECT, this);
    }

    public void tick(){

    }


    ////////////////////////////

    private final List<UINodeState> nodeStates = new ArrayList<>();

    public void unload(){
        nodeStates.clear();
        FXHelper.saveUIStates(nodeStates);
    }

    public void load(){
        FXHelper.loadUIStates(nodeStates);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// DRAWING SET EVENTS \\\\

    @Override
    public void onActiveSlotChanged(ObservableDrawingSet activeSet) {
        //onDrawingSetChanged();
    }

    @Override
    public void onDrawingSetAdded(ObservableDrawingSet set) {
        onDrawingSetChanged();
    }

    @Override
    public void onDrawingSetRemoved(ObservableDrawingSet set) {
        onDrawingSetChanged();
    }

    @Override
    public void onDrawingSetPropertyChanged(ObservableDrawingSet set, Observable property) {
        if(set.distributionType == property || set.distributionOrder == property || set.colorHandler == property){
            onDrawingSetChanged();
        }
    }

    @Override
    public void onColourSeparatorChanged(ObservableDrawingSet set, ColorSeparationHandler oldValue, ColorSeparationHandler newValue) {
        if(oldValue == newValue){
            return;
        }
        set.colorHandler.get().resetSettings(context, set);
        if(oldValue.wasApplied()){
            oldValue.resetSettings(context, set);
            oldValue.setApplied(false);
        }

        if(newValue.onUserSelected()){
            newValue.applySettings(context, set);
            newValue.setApplied(true);
        }
    }

    @Override
    public void onDrawingPenPropertyChanged(ObservableDrawingPen pen, Observable property) {
        onDrawingPenChanged();
    }

    @Override
    public void onDrawingPenAdded(ObservableDrawingPen pen) {
        onDrawingSetChanged();
    }

    @Override
    public void onDrawingPenRemoved(ObservableDrawingPen pen) {
        onDrawingSetChanged();
    }

    //// IMAGE FILTER EVENTS \\\\
    @Override
    public void onImageFilterAdded(ObservableImageFilter filter) {
        onImageFiltersChanged();
    }

    @Override
    public void onImageFilterRemoved(ObservableImageFilter filter) {
        onImageFiltersChanged();
    }

    @Override
    public void onImageFilterPropertyChanged(ObservableImageFilter filter, Observable property) {
        onImageFilterChanged(filter);
    }

    //// CANVAS EVENTS \\\\

    @Override
    public void onCanvasPropertyChanged(ObservableCanvas canvas, Observable property) {
        if(property == canvas.canvasColor){
            reRender();
        }else{
            onCanvasChanged();
        }
    }

    @Override
    public void onSettingUserEdited(GenericSetting<?, ?> setting) {
        onPFMSettingsUserEdited();
    }

    @Override
    public void onPFMChanged(PFMFactory<?> oldValue, PFMFactory<?> newValue) {
        onPFMSettingsUserEdited();
    }

    @Override
    public void onUserChangedPFMPreset(GenericPreset<PresetData> pfmPreset) {
        //onPFMSettingsUserEdited(); If we've just created a new Preset we don't want to fire the event, as no settings will have changed, on onSettingUserEdited will fire it anyway.
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onVersionAdded(ObservableVersion version) {

    }

    @Override
    public void onVersionRemoved(ObservableVersion version) {

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public <T> void setRenderFlag(Flags.BooleanFlag flag){
        if(!isLoaded()){
            return;
        }
        for(IDisplayMode displayMode : MasterRegistry.INSTANCE.displayModes){
            displayMode.getRenderFlags().setFlag(flag, true);
        }
    }

    public <T> void setRenderFlag(Flags.Flag<T> flag, T value){
        if(!isLoaded()){
            return;
        }
        for(IDisplayMode displayMode : MasterRegistry.INSTANCE.displayModes){
            displayMode.getRenderFlags().setFlag(flag, value);
        }
    }

    public void reRender(){
        setRenderFlag(Flags.FORCE_REDRAW);
    }

    public void onImageRenderingUpdated(){
        setRenderFlag(Flags.OPEN_IMAGE_UPDATED, true);
    }

    public void onCanvasChanged(){
        setRenderFlag(Flags.CANVAS_CHANGED, true);
        markOpenImageForUpdate(FilteredImageData.UpdateType.FULL_UPDATE);
    }

    public void onDrawingCleared(EnumRendererType rendererType){
        if(rendererType.reRenderJFX()){
            setRenderFlag(Flags.CLEAR_DRAWING_JFX, true);
        }
        if(rendererType.reRenderOpenGL()) {
            setRenderFlag(Flags.CLEAR_DRAWING_OPENGL, true);
        }
    }

    public void onImageFiltersChanged(){
        setRenderFlag(Flags.IMAGE_FILTERS_FULL_UPDATE, true);
        markOpenImageForUpdate(FilteredImageData.UpdateType.ALL_FILTERS);
    }

    public void onImageFilterDirty(){
        setRenderFlag(Flags.IMAGE_FILTERS_PARTIAL_UPDATE, true);
        markOpenImageForUpdate(FilteredImageData.UpdateType.PARTIAL_FILTERS);
    }

    public void resetView(){
        if(isLoaded()){
            DrawingBotV3.INSTANCE.resetView();
        }
    }

    public void onDrawingPenChanged(){
        updatePenDistribution();
    }

    public void onDrawingSetChanged(){
        updatePenDistribution();
    }

    public void onImageFilterChanged(ObservableImageFilter filter){
        filter.dirty.set(true);
        onImageFilterDirty();
    }

    public void updatePenDistribution(){
        if(currentDrawing.get() != null){
            DrawingBotV3.INSTANCE.globalFlags.setFlag(Flags.UPDATE_PEN_DISTRIBUTION, true);
        }
    }

    public void onPFMSettingsChanged(){
        DrawingBotV3.INSTANCE.globalFlags.setFlag(Flags.PFM_SETTINGS_UPDATE, true);
    }

    public void onPFMSettingsUserEdited(){
        DrawingBotV3.INSTANCE.globalFlags.setFlag(Flags.PFM_SETTINGS_USER_EDITED, true);
    }


    public void markOpenImageForUpdate(FilteredImageData.UpdateType updateType){
        if(openImage.get() != null){
            openImage.get().markUpdate(updateType);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public File getImportDirectory(){
        if(lastImportDirectory.get() != null && !lastImportDirectory.get().isEmpty()){
            File importDirectory = new File(lastImportDirectory.get());
            if(importDirectory.exists()){
                return importDirectory;
            }
        }
        if(DBPreferences.INSTANCE.defaultImportDirectory.get() != null && !DBPreferences.INSTANCE.defaultImportDirectory.get().isEmpty()){
            File importDirectory = new File(DBPreferences.INSTANCE.defaultImportDirectory.get());
            if(importDirectory.exists()){
                return importDirectory;
            }
        }
        return new File(FileUtils.getUserHomeDirectory());
    }

    public void updateImportDirectory(File directory){
        if(!directory.exists()){
            return;
        }
        lastImportDirectory.set(directory.toString());
    }

    public void updateImportDirectoryFromFile(File file){
        if(file.toString().toLowerCase().endsWith(".drawingbotv3")){
            return;
        }
        updateImportDirectory(file.getParentFile());
    }

    public File getExportDirectory(){
        if(lastExportDirectory.get() != null && !lastExportDirectory.get().isEmpty()){
            File exportDirectory = new File(lastExportDirectory.get());
            if(exportDirectory.exists()){
                return exportDirectory;
            }
        }
        if(DBPreferences.INSTANCE.defaultExportDirectory.get() != null && !DBPreferences.INSTANCE.defaultExportDirectory.get().isEmpty()){
            File exportDirectory = new File(DBPreferences.INSTANCE.defaultExportDirectory.get());
            if(exportDirectory.exists()){
                return exportDirectory;
            }
        }
        return new File(FileUtils.getUserHomeDirectory());
    }

    public void updateExportDirectory(File directory){
        if(!directory.exists()){
            return;
        }
        lastExportDirectory.set(directory.toString());
    }

    public void updateExportDirectoryFromFile(File file){
        if(file.toString().toLowerCase().endsWith(".drawingbotv3")){
            return;
        }
        updateExportDirectory(file.getParentFile());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public <T> void setMetadata(Metadata<T> metadata, T value){
        this.metadata.setMetadata(metadata, value);
    }

    public <T> T getMetadata(Metadata<T> metadata){
        return this.metadata.getMetadata(metadata);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// DRAWING MANAGER

    @Override
    public DBTask<?> getActiveTask(){
        return activeTask.get();
    }

    @Override
    public PlottedDrawing createNewPlottedDrawing() {
        return new PlottedDrawing(getDrawingArea(), getDrawingSets());
    }

    @Override
    public PFMTaskBuilder createPFMTaskBuilder() {
        return PFMTaskBuilder.create(context);
    }

    @Override
    public void onPlottingTaskStageFinished(PFMTask task, EnumTaskStage stage) {
        DrawingBotV3.onPlottingTaskStageFinished(context, task, stage);
    }

    @Override
    public void setActiveTask(DBTask<?> task) {
        if(activeTask.get() == task){
            return;
        }
        activeTask.set(task);
        renderedTask.set(null);
    }

    @Override
    public void setRenderedTask(PFMTask task) {
        if(renderedTask.isBound()){
            return;
        }
        renderedTask.set(task);
    }

    @Override
    public PFMTask getRenderedTask(){
        if(renderedTask.get() != null){
            return renderedTask.get();
        }
        if(activeTask.get() instanceof PFMTask){
            return (PFMTask) activeTask.get();
        }
        return null;
    }

    @Override
    public void setCurrentDrawing(PlottedDrawing drawing) {
        if(currentDrawing.isBound()){
            return;
        }
        currentDrawing.set(drawing);
    }

    @Override
    public PlottedDrawing getCurrentDrawing() {
        return currentDrawing.get();
    }

    public PlottedDrawing getExportDrawing() {
        return exportDrawing.get();
    }

    public void setExportDrawing(PlottedDrawing drawing) {
        exportDrawing.set(drawing);
    }

    public ObservableList<ExportedDrawingEntry> getExportedDrawings() {
        return exportedDrawings.get();
    }

    public PlottedDrawing getDisplayedDrawing() {
        return displayedDrawing.get();
    }


    @Override
    public void clearDrawingRender(EnumRendererType rendererType){
        Platform.runLater(() -> onDrawingCleared(rendererType));
    }
}
