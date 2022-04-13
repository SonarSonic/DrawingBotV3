package drawingbot.files.json.presets;

import com.google.gson.JsonObject;
import drawingbot.DrawingBotV3;
import drawingbot.api.Hooks;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.image.BufferedImageLoader;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableProjectSettings;
import drawingbot.plotting.PFMTask;
import drawingbot.registry.Register;
import drawingbot.utils.UnitsLength;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.UUID;

public class PresetProjectSettingsManager extends AbstractPresetManager<PresetProjectSettings> {

    public PresetProjectSettingsManager(PresetProjectSettingsLoader presetLoader) {
        super(presetLoader);
    }

    @Override
    public GenericPreset<PresetProjectSettings> updatePreset(GenericPreset<PresetProjectSettings> preset) {
        PFMTask plottingTask = DrawingBotV3.INSTANCE.getActiveTask();

        preset.data.imagePath = DrawingBotV3.INSTANCE.openFile != null ? DrawingBotV3.INSTANCE.openFile.getPath() : "";
        preset.data.timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM));

        if(plottingTask != null && plottingTask.isTaskFinished()){
            preset.data.thumbnailID = UUID.randomUUID().toString();
        }else{
            preset.data.thumbnailID = "";
        }

        GenericPreset<PresetDrawingArea> presetDrawingArea = Register.PRESET_LOADER_DRAWING_AREA.createNewPreset();
        Register.PRESET_LOADER_DRAWING_AREA.getDefaultManager().updatePreset(presetDrawingArea);
        preset.data.drawingArea = presetDrawingArea;

        GenericPreset<PresetImageFilters> presetImageFilters = Register.PRESET_LOADER_FILTERS.createNewPreset();
        Register.PRESET_LOADER_FILTERS.getDefaultManager().updatePreset(presetImageFilters);
        preset.data.imageFilters = presetImageFilters;

        GenericPreset<PresetPFMSettings> presetPFMSettings = Register.PRESET_LOADER_PFM.createNewPreset();
        Register.PRESET_LOADER_PFM.getDefaultManager().updatePreset(presetPFMSettings);
        preset.data.pfmSettings = presetPFMSettings;
        preset.data.name = preset.data.pfmSettings.presetSubType;

        GenericPreset<PresetDrawingSet> presetDrawingSet = Register.PRESET_LOADER_DRAWING_SET.createNewPreset();
        Register.PRESET_LOADER_DRAWING_SET.getDefaultManager().updatePreset(presetDrawingSet);
        preset.data.drawingSet = presetDrawingSet;

        preset.data.imageRotation = DrawingBotV3.INSTANCE.imgFilterSettings.imageRotation.get();
        preset.data.imageFlipHorizontal = DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipHorizontal.get();
        preset.data.imageFlipVertical = DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipVertical.get();

        preset.data.optimiseForPrint = DrawingBotV3.INSTANCE.drawingArea.optimiseForPrint.get();
        preset.data.targetPenWidth = DrawingBotV3.INSTANCE.drawingArea.targetPenWidth.get();
        preset.data.colourSplitter = DrawingBotV3.INSTANCE.drawingSets.getDrawingSetForSlot(0).colourSeperator.get();
        preset.data.distributionType = DrawingBotV3.INSTANCE.drawingSets.getDrawingSetForSlot(0).distributionType.get();
        preset.data.distributionOrder = DrawingBotV3.INSTANCE.drawingSets.getDrawingSetForSlot(0).distributionOrder.get();
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
        for(ObservableDrawingSet drawingSet : DrawingBotV3.INSTANCE.drawingSets.drawingSetSlots.get()){
            preset.data.drawingSets.add(new ObservableDrawingSet(drawingSet));
        }
        int activeSlot = preset.data.drawingSets.indexOf(DrawingBotV3.INSTANCE.drawingSets.activeDrawingSet.get());
        preset.data.activeDrawingSlot = activeSlot == -1 ? 0 : activeSlot;

        //run the thumbnail generation task
        if(plottingTask != null && !preset.data.thumbnailID.isEmpty()){
            File saveLocation = new File(FileUtils.getUserThumbnailDirectory() + preset.data.thumbnailID + ".jpg");
            ExportTask task = new ExportTask(Register.EXPORT_IMAGE, ExportTask.Mode.PER_DRAWING, plottingTask.drawing, IGeometryFilter.DEFAULT_EXPORT_FILTER, ".jpg", saveLocation, true, true, true);
            task.exportScale = 400 / plottingTask.drawing.canvas.getWidth(UnitsLength.PIXELS);
            DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.backgroundService, task);
        }

        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetProjectSettings> preset) {


        // Register.PRESET_LOADER_DRAWING_AREA.applyPreset(preset.data.drawingArea);
        Register.PRESET_LOADER_FILTERS.getDefaultManager().applyPreset(preset.data.imageFilters);
        Register.PRESET_LOADER_PFM.getDefaultManager().applyPreset(preset.data.pfmSettings);

        DrawingBotV3.INSTANCE.imgFilterSettings.imageRotation.set(preset.data.imageRotation);
        DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipHorizontal.set(preset.data.imageFlipHorizontal);
        DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipVertical.set(preset.data.imageFlipVertical);

        DrawingBotV3.INSTANCE.drawingArea.optimiseForPrint.set(preset.data.optimiseForPrint);
        DrawingBotV3.INSTANCE.drawingArea.targetPenWidth.set(preset.data.targetPenWidth); //TODO TEST ME


        DrawingBotV3.INSTANCE.blendMode.set(preset.data.blendMode);

        DrawingBotV3.INSTANCE.cyanMultiplier.set(preset.data.cyanMultiplier);
        DrawingBotV3.INSTANCE.magentaMultiplier.set(preset.data.magentaMultiplier);
        DrawingBotV3.INSTANCE.yellowMultiplier.set(preset.data.yellowMultiplier);
        DrawingBotV3.INSTANCE.keyMultiplier.set(preset.data.keyMultiplier);

        DrawingBotV3.INSTANCE.drawingSets.getDrawingSetForSlot(0).loadDrawingSet(preset.data.drawingSet.data);
        DrawingBotV3.INSTANCE.drawingSets.getDrawingSetForSlot(0).colourSeperator.set(preset.data.colourSplitter);
        DrawingBotV3.INSTANCE.drawingSets.getDrawingSetForSlot(0).colourSeperator.get().applySettings(DrawingBotV3.INSTANCE.drawingSets);
        DrawingBotV3.INSTANCE.drawingSets.getDrawingSetForSlot(0).distributionType.set(preset.data.distributionType);
        DrawingBotV3.INSTANCE.drawingSets.getDrawingSetForSlot(0).distributionOrder.set(preset.data.distributionOrder);

        if(preset.data.drawingSets != null && !preset.data.drawingSets.isEmpty()){
            DrawingBotV3.INSTANCE.drawingSets.drawingSetSlots.get().clear();
            for(ObservableDrawingSet drawingSet : preset.data.drawingSets){
                DrawingBotV3.INSTANCE.drawingSets.drawingSetSlots.get().add(new ObservableDrawingSet(drawingSet));
            }
            int activeSlot = preset.data.activeDrawingSlot < DrawingBotV3.INSTANCE.drawingSets.drawingSetSlots.get().size() ? preset.data.activeDrawingSlot : 0;
            DrawingBotV3.INSTANCE.drawingSets.activeDrawingSet.set(DrawingBotV3.INSTANCE.drawingSets.drawingSetSlots.get().get(activeSlot));
        }

        if(!preset.data.isSubProject){ //don't overwrite the versions if this is just a sub version
            DrawingBotV3.INSTANCE.projectVersions.clear();
            if(preset.data.projectVersions != null){
                for(PresetProjectSettings projectVersion : preset.data.projectVersions){
                    GenericPreset<PresetProjectSettings> newPreset = Register.PRESET_LOADER_PROJECT.createNewPreset();
                    newPreset.data = projectVersion;
                    DrawingBotV3.INSTANCE.projectVersions.add(new ObservableProjectSettings(newPreset, true));
                }
            }
        }

        String drawingVersion = "1.0.0";

        if(preset.data.drawingState != null){
            if(preset.data.drawingState.has("version")){
                drawingVersion = preset.data.drawingState.get("version").getAsString();
            }
        }

        if(Utils.compareVersion(drawingVersion, "1.0.2", 3) >= 0){
            //the drawing state used the newer version of the Geometry Serializer and can be serialized separately to the image
            DrawingBotV3.INSTANCE.taskService.submit(() -> Hooks.runHook(Hooks.DESERIALIZE_DRAWING_STATE, preset.data.drawingState));
            if(!preset.data.imagePath.isEmpty()) {
                BufferedImageLoader.Filtered loadingTask = DrawingBotV3.INSTANCE.getImageLoaderTask(new File(preset.data.imagePath), false);
                loadingTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.FAILED) {
                        FXHelper.importFile((file, chooser) -> DrawingBotV3.INSTANCE.openFile(file, false), new FileChooser.ExtensionFilter[]{FileUtils.IMPORT_IMAGES}, "Locate the input image");
                    }
                });
                DrawingBotV3.INSTANCE.taskMonitor.queueTask(loadingTask);
            }
        }else{
            //to support older versions of the Geometry Serializer, we need to first load the image and only when it's loaded do we try to load the drawing state
            if(!preset.data.imagePath.isEmpty()){
                Platform.runLater(() -> {
                    BufferedImageLoader.Filtered loadingTask = DrawingBotV3.INSTANCE.getImageLoaderTask(new File(preset.data.imagePath), false);
                    loadingTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                        if(newValue == Worker.State.FAILED){
                            FXHelper.importFile((file, chooser) -> {
                                DrawingBotV3.INSTANCE.openFile(file, false);
                                DrawingBotV3.INSTANCE.taskService.submit(() -> Hooks.runHook(Hooks.DESERIALIZE_DRAWING_STATE, preset.data.drawingState));
                            }, new FileChooser.ExtensionFilter[]{FileUtils.IMPORT_IMAGES}, "Locate the input image");
                        }
                        if(newValue == Worker.State.SUCCEEDED){
                            DrawingBotV3.INSTANCE.taskService.submit(() -> Hooks.runHook(Hooks.DESERIALIZE_DRAWING_STATE, preset.data.drawingState));
                        }
                    });
                    DrawingBotV3.INSTANCE.taskMonitor.queueTask(loadingTask);
                });
            }
        }

    }
}
