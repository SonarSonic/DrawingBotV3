package drawingbot.files.presets.types;

import com.google.gson.JsonObject;
import drawingbot.DrawingBotV3;
import drawingbot.api.Hooks;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.files.presets.PresetType;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.PrintResolution;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableProjectSettings;
import drawingbot.plotting.PlottingTask;
import drawingbot.registry.Register;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class PresetProjectSettingsLoader extends AbstractPresetLoader<PresetProjectSettings> {

    public PresetProjectSettingsLoader(PresetType presetType) {
        super(PresetProjectSettings.class, presetType, "projects.json");
    }

    @Override
    public PresetProjectSettings getPresetInstance(GenericPreset<PresetProjectSettings> preset) {
        return new PresetProjectSettings();
    }

    @Override
    public void registerPreset(GenericPreset<PresetProjectSettings> preset) {
        //MasterRegistry.INSTANCE.registerPFMPreset(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetProjectSettings> preset) {
        //MasterRegistry.INSTANCE.pfmPresets.get(preset.presetSubType).remove(preset);
    }

    @Override
    public GenericPreset<PresetProjectSettings> updatePreset(GenericPreset<PresetProjectSettings> preset) {
        return updatePreset(preset, DrawingBotV3.INSTANCE.getActiveTask());
    }

    public GenericPreset<PresetProjectSettings> updatePreset(GenericPreset<PresetProjectSettings> preset, @Nullable PlottingTask plottingTask) {

        preset.data.imagePath = DrawingBotV3.INSTANCE.openFile != null ? DrawingBotV3.INSTANCE.openFile.getPath() : "";
        preset.data.timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM));

        if(plottingTask != null && plottingTask.isTaskFinished()){
            preset.data.thumbnailID = UUID.randomUUID().toString();
        }else{
            preset.data.thumbnailID = "";
        }

        GenericPreset<PresetDrawingArea> presetDrawingArea = Register.PRESET_LOADER_DRAWING_AREA.createNewPreset();
        Register.PRESET_LOADER_DRAWING_AREA.updatePreset(presetDrawingArea);
        preset.data.drawingArea = presetDrawingArea;

        GenericPreset<PresetImageFilters> presetImageFilters = Register.PRESET_LOADER_FILTERS.createNewPreset();
        Register.PRESET_LOADER_FILTERS.updatePreset(presetImageFilters);
        preset.data.imageFilters = presetImageFilters;

        GenericPreset<PresetPFMSettings> presetPFMSettings = Register.PRESET_LOADER_PFM.createNewPreset();
        Register.PRESET_LOADER_PFM.updatePreset(presetPFMSettings);
        preset.data.pfmSettings = presetPFMSettings;
        preset.data.name = preset.data.pfmSettings.presetSubType;

        GenericPreset<PresetDrawingSet> presetDrawingSet = Register.PRESET_LOADER_DRAWING_SET.createNewPreset();
        Register.PRESET_LOADER_DRAWING_SET.updatePreset(presetDrawingSet);
        preset.data.drawingSet = presetDrawingSet;

        preset.data.imageRotation = DrawingBotV3.INSTANCE.imageRotation.get();
        preset.data.imageFlipHorizontal = DrawingBotV3.INSTANCE.imageFlipHorizontal.get();
        preset.data.imageFlipVertical = DrawingBotV3.INSTANCE.imageFlipVertical.get();

        preset.data.optimiseForPrint = DrawingBotV3.INSTANCE.drawingArea.optimiseForPrint.get();
        preset.data.targetPenWidth = DrawingBotV3.INSTANCE.drawingArea.targetPenWidth.get();
        preset.data.colourSplitter = DrawingBotV3.INSTANCE.drawingSetSlots.get(0).colourSeperator.get();
        preset.data.distributionType = DrawingBotV3.INSTANCE.drawingSetSlots.get(0).distributionType.get();
        preset.data.distributionOrder = DrawingBotV3.INSTANCE.drawingSetSlots.get(0).distributionOrder.get();
        preset.data.blendMode = DrawingBotV3.INSTANCE.blendMode.get();

        preset.data.cyanMultiplier = DrawingBotV3.INSTANCE.cyanMultiplier.get();
        preset.data.magentaMultiplier = DrawingBotV3.INSTANCE.magentaMultiplier.get();
        preset.data.yellowMultiplier = DrawingBotV3.INSTANCE.yellowMultiplier.get();
        preset.data.keyMultiplier = DrawingBotV3.INSTANCE.keyMultiplier.get();

        preset.data.drawingState = new JsonObject();

        preset.data.projectVersions = new ArrayList<>();
        for(ObservableProjectSettings projectVersion : DrawingBotV3.INSTANCE.projectVersions){
            preset.data.projectVersions.add(projectVersion.preset.get().data);
        }

        ///run the drawing state generation task
        if(plottingTask != null){
            preset.data.drawingState = (JsonObject) Hooks.runHook(Hooks.SERIALIZE_DRAWING_STATE, plottingTask, new JsonObject())[1];
        }

        preset.data.drawingSets = new ArrayList<>();
        for(ObservableDrawingSet drawingSet : DrawingBotV3.INSTANCE.drawingSetSlots){
            preset.data.drawingSets.add(new ObservableDrawingSet(drawingSet));
        }
        int activeSlot = preset.data.drawingSets.indexOf(DrawingBotV3.INSTANCE.activeDrawingSet.get());
        preset.data.activeDrawingSlot = activeSlot == -1 ? 0 : activeSlot;

        //run the thumbnail generation task
        if(plottingTask != null && !preset.data.thumbnailID.isEmpty()){
            File saveLocation = new File(FileUtils.getUserThumbnailDirectory() + preset.data.thumbnailID + ".jpg");
            PrintResolution thumbnailResolution = PrintResolution.copy(plottingTask.resolution);
            thumbnailResolution.changePrintResolution(400, (int)((400 / thumbnailResolution.scaledWidth)*thumbnailResolution.scaledHeight));
            ExportTask task = new ExportTask(Register.EXPORT_IMAGE, plottingTask, IGeometryFilter.DEFAULT_EXPORT_FILTER, ".jpg", saveLocation, false, true, true, true, thumbnailResolution);
            DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.backgroundService, task);
        }

        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetProjectSettings> preset) {

        Register.PRESET_LOADER_DRAWING_AREA.applyPreset(preset.data.drawingArea);
        Register.PRESET_LOADER_FILTERS.applyPreset(preset.data.imageFilters);
        Register.PRESET_LOADER_PFM.applyPreset(preset.data.pfmSettings);

        DrawingBotV3.INSTANCE.imageRotation.set(preset.data.imageRotation);
        DrawingBotV3.INSTANCE.imageFlipHorizontal.set(preset.data.imageFlipHorizontal);
        DrawingBotV3.INSTANCE.imageFlipVertical.set(preset.data.imageFlipVertical);

        DrawingBotV3.INSTANCE.drawingArea.optimiseForPrint.set(preset.data.optimiseForPrint);
        DrawingBotV3.INSTANCE.controller.textFieldPenWidth.setText("" + preset.data.targetPenWidth); //works but ugly!

        DrawingBotV3.INSTANCE.blendMode.set(preset.data.blendMode);

        DrawingBotV3.INSTANCE.cyanMultiplier.set(preset.data.cyanMultiplier);
        DrawingBotV3.INSTANCE.magentaMultiplier.set(preset.data.magentaMultiplier);
        DrawingBotV3.INSTANCE.yellowMultiplier.set(preset.data.yellowMultiplier);
        DrawingBotV3.INSTANCE.keyMultiplier.set(preset.data.keyMultiplier);

        DrawingBotV3.INSTANCE.drawingSetSlots.get(0).loadDrawingSet(preset.data.drawingSet.data);
        DrawingBotV3.INSTANCE.drawingSetSlots.get(0).colourSeperator.set(preset.data.colourSplitter);
        DrawingBotV3.INSTANCE.drawingSetSlots.get(0).colourSeperator.get().applySettings();
        DrawingBotV3.INSTANCE.drawingSetSlots.get(0).distributionType.set(preset.data.distributionType);
        DrawingBotV3.INSTANCE.drawingSetSlots.get(0).distributionOrder.set(preset.data.distributionOrder);

        if(preset.data.drawingSets != null && !preset.data.drawingSets.isEmpty()){
            DrawingBotV3.INSTANCE.drawingSetSlots.clear();
            for(ObservableDrawingSet drawingSet : preset.data.drawingSets){
                DrawingBotV3.INSTANCE.drawingSetSlots.add(new ObservableDrawingSet(drawingSet));
            }
            int activeSlot = preset.data.activeDrawingSlot < DrawingBotV3.INSTANCE.drawingSetSlots.size() ? preset.data.activeDrawingSlot : 0;
            DrawingBotV3.INSTANCE.activeDrawingSet.set(DrawingBotV3.INSTANCE.drawingSetSlots.get(activeSlot));
        }

        if(!preset.data.isSubProject){ //don't overwrite the versions if this is just a sub version
            DrawingBotV3.INSTANCE.projectVersions.clear();
            if(preset.data.projectVersions != null){
                for(PresetProjectSettings projectVersion : preset.data.projectVersions){
                    GenericPreset<PresetProjectSettings> newPreset = createNewPreset();
                    newPreset.data = projectVersion;
                    DrawingBotV3.INSTANCE.projectVersions.add(new ObservableProjectSettings(newPreset, true));
                }
            }
        }

        if(!preset.data.imagePath.isEmpty()){
            Platform.runLater(() -> {
                BufferedImageLoader.Filtered loadingTask = DrawingBotV3.INSTANCE.getImageLoaderTask(new File(preset.data.imagePath), false);
                loadingTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                    if(loadingTask.isDone()){
                        Hooks.runHook(Hooks.DESERIALIZE_DRAWING_STATE, preset.data.drawingState);
                    }
                });
                DrawingBotV3.INSTANCE.taskMonitor.queueTask(loadingTask);
            });
        }



    }

    @Override
    public GenericPreset<PresetProjectSettings> getDefaultPreset() {
        return null;
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        /*

        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (Map.Entry<String, ObservableList<GenericPreset<PresetProjectSettings>>> entry : MasterRegistry.INSTANCE.pfmPresets.entrySet()) {
            for (GenericPreset<PresetProjectSettings> preset : entry.getValue()) {
                if (preset.userCreated) {
                    userCreated.add(preset);
                }
            }
        }
        return userCreated;
         */
        return new ArrayList<>();
    }

    @Override
    public Collection<GenericPreset<PresetProjectSettings>> getAllPresets() {
        return new ArrayList<>();
    }
}
