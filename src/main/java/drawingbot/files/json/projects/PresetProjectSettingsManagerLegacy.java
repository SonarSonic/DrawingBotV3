package drawingbot.files.json.projects;

import com.google.gson.JsonObject;
import drawingbot.DrawingBotV3;
import drawingbot.api.Hooks;
import drawingbot.files.FileUtils;
import drawingbot.files.json.presets.PresetDrawingArea;
import drawingbot.files.json.presets.PresetDrawingSet;
import drawingbot.files.json.presets.PresetImageFilters;
import drawingbot.files.json.presets.PresetPFMSettings;
import drawingbot.files.loaders.AbstractFileLoader;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableVersion;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.Register;
import drawingbot.utils.EnumRescaleMode;
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

/**
 * Legacy Project Manager, to continue to support projects formatted in the old style
 */
class PresetProjectSettingsManagerLegacy {

    public static GenericPreset<PresetProjectSettings> updatePreset(DBTaskContext context, GenericPreset<PresetProjectSettings> preset) {

        PresetProjectSettingsLegacy presetData = (PresetProjectSettingsLegacy) preset.data;

        PlottedDrawing renderedDrawing = context.taskManager.getCurrentDrawing();
        presetData.imagePath = context.project.openImage.get() != null && context.project.openImage.get().getSourceFile() != null ? context.project.openImage.get().getSourceFile().getPath() : "";
        presetData.timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM));
        presetData.thumbnailID = renderedDrawing == null ? "" : UUID.randomUUID().toString();

        GenericPreset<PresetDrawingArea> presetDrawingArea = Register.PRESET_LOADER_DRAWING_AREA.createNewPreset();
        Register.PRESET_LOADER_DRAWING_AREA.getDefaultManager().updatePreset(context, presetDrawingArea);
        presetData.drawingArea = presetDrawingArea;

        GenericPreset<PresetImageFilters> presetImageFilters = Register.PRESET_LOADER_FILTERS.createNewPreset();
        Register.PRESET_LOADER_FILTERS.getDefaultManager().updatePreset(context, presetImageFilters);
        presetData.imageFilters = presetImageFilters;

        GenericPreset<PresetPFMSettings> presetPFMSettings = Register.PRESET_LOADER_PFM.createNewPreset();
        Register.PRESET_LOADER_PFM.getDefaultManager().updatePreset(context, presetPFMSettings);
        presetData.pfmSettings = presetPFMSettings;
        presetData.name = presetData.pfmSettings.presetSubType;

        GenericPreset<PresetDrawingSet> presetDrawingSet = Register.PRESET_LOADER_DRAWING_SET.createNewPreset();
        Register.PRESET_LOADER_DRAWING_SET.getDefaultManager().updatePreset(context, presetDrawingSet);
        presetData.drawingSet = presetDrawingSet;

        /*
        presetData.imageRotation = DrawingBotV3.INSTANCE.imgFilterSettings.imageRotation.get();
        presetData.imageFlipHorizontal = DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipHorizontal.get();
        presetData.imageFlipVertical = DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipVertical.get();
         */

        presetData.optimiseForPrint = context.project.getDrawingArea().getRescaleMode().shouldRescale();
        presetData.targetPenWidth = context.project.getDrawingArea().targetPenWidth.get();
        presetData.colourSplitter = context.project.getDrawingSets().getDrawingSetForSlot(0).colourSeperator.get();
        presetData.distributionType = context.project.getDrawingSets().getDrawingSetForSlot(0).distributionType.get();
        presetData.distributionOrder = context.project.getDrawingSets().getDrawingSetForSlot(0).distributionOrder.get();
        presetData.blendMode = context.project.blendMode.get();

        /*
        presetData.cyanMultiplier = DrawingBotV3.INSTANCE.cyanMultiplier.get();
        presetData.magentaMultiplier = DrawingBotV3.INSTANCE.magentaMultiplier.get();
        presetData.yellowMultiplier = DrawingBotV3.INSTANCE.yellowMultiplier.get();
        presetData.keyMultiplier = DrawingBotV3.INSTANCE.keyMultiplier.get();

         */

        presetData.drawingState = new JsonObject();

        presetData.projectVersions = new ArrayList<>();

        for(ObservableVersion projectVersion : context.project.getProjectVersions()){
            if(projectVersion.getPreset().data instanceof PresetProjectSettingsLegacy){
                presetData.projectVersions.add((PresetProjectSettingsLegacy)projectVersion.getPreset().data);
            }
        }

        presetData.drawingSets = new ArrayList<>();
        for(ObservableDrawingSet drawingSet : context.project.getDrawingSets().drawingSetSlots){
            presetData.drawingSets.add(new ObservableDrawingSet(drawingSet));
        }
        int activeSlot = presetData.drawingSets.indexOf(context.project.getDrawingSets().activeDrawingSet.get());
        presetData.activeDrawingSlot = activeSlot == -1 ? 0 : activeSlot;

        return preset;
    }

    public static void applyPreset(DBTaskContext context, GenericPreset<PresetProjectSettings> preset) {
        PresetProjectSettingsLegacy presetData = (PresetProjectSettingsLegacy) preset.data;

        Register.PRESET_LOADER_DRAWING_AREA.getDefaultManager().applyPreset(context, presetData.drawingArea);
        Register.PRESET_LOADER_FILTERS.getDefaultManager().applyPreset(context, presetData.imageFilters);
        Register.PRESET_LOADER_PFM.getDefaultManager().applyPreset(context, presetData.pfmSettings);

        /*
        DrawingBotV3.INSTANCE.imgFilterSettings.imageRotation.set(presetData.imageRotation);
        DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipHorizontal.set(presetData.imageFlipHorizontal);
        DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipVertical.set(presetData.imageFlipVertical);
         */

        context.project.getDrawingArea().rescaleMode.set(presetData.optimiseForPrint ? EnumRescaleMode.HIGH_QUALITY : EnumRescaleMode.OFF);
        context.project.getDrawingArea().targetPenWidth.set(presetData.targetPenWidth);


        context.project.blendMode.set(presetData.blendMode);

        /*
        DrawingBotV3.INSTANCE.cyanMultiplier.set(presetData.cyanMultiplier);
        DrawingBotV3.INSTANCE.magentaMultiplier.set(presetData.magentaMultiplier);
        DrawingBotV3.INSTANCE.yellowMultiplier.set(presetData.yellowMultiplier);
        DrawingBotV3.INSTANCE.keyMultiplier.set(presetData.keyMultiplier);

         */

        context.project.getDrawingSets().getDrawingSetForSlot(0).loadDrawingSet(presetData.drawingSet.data);
        context.project.getDrawingSets().getDrawingSetForSlot(0).colourSeperator.set(presetData.colourSplitter);
        context.project.getDrawingSets().getDrawingSetForSlot(0).colourSeperator.get().applySettings(context, context.project.getDrawingSets().getActiveDrawingSet());
        context.project.getDrawingSets().getDrawingSetForSlot(0).distributionType.set(presetData.distributionType);
        context.project.getDrawingSets().getDrawingSetForSlot(0).distributionOrder.set(presetData.distributionOrder);

        if(presetData.drawingSets != null && !presetData.drawingSets.isEmpty()){
            context.project.getDrawingSets().drawingSetSlots.clear();
            for(ObservableDrawingSet drawingSet : presetData.drawingSets){
                context.project.getDrawingSets().drawingSetSlots.add(new ObservableDrawingSet(drawingSet));
            }
            int activeSlot = presetData.activeDrawingSlot < context.project.getDrawingSets().drawingSetSlots.size() ? presetData.activeDrawingSlot : 0;
            context.project.getDrawingSets().activeDrawingSet.set(context.project.getDrawingSets().drawingSetSlots.get(activeSlot));
        }

        if(!presetData.isSubProject){ //don't overwrite the versions if this is just a sub version
            context.project.getProjectVersions().clear();
            if(presetData.projectVersions != null){
                for(PresetProjectSettingsLegacy projectVersion : presetData.projectVersions){
                    GenericPreset<PresetProjectSettings> newPreset = Register.PRESET_LOADER_PROJECT.createNewPreset();
                    newPreset.version = "1";
                    newPreset.data = projectVersion;
                    context.project.getProjectVersions().add(new ObservableVersion(newPreset, true));
                }
            }
        }

        String drawingVersion = "1.0.0";

        if(presetData.drawingState != null){
            if(presetData.drawingState.has("version")){
                drawingVersion = presetData.drawingState.get("version").getAsString();
            }
        }

        if(Utils.compareVersion(drawingVersion, "1.0.2", 3) >= 0){
            //the drawing state used the newer version of the Geometry Serializer and can be serialized separately to the image
            context.project.openImage.set(null);
            context.taskManager.setCurrentDrawing(null);
            if(!presetData.imagePath.isEmpty()) {
                AbstractFileLoader loadingTask = DrawingBotV3.INSTANCE.getImageLoaderTask(DrawingBotV3.context(), new File(presetData.imagePath), false, false);
                loadingTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.FAILED) {
                        FXHelper.importFile((file, chooser) -> DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, false, false), new FileChooser.ExtensionFilter[]{FileUtils.IMPORT_IMAGES}, "Locate the input image");
                    }
                });
                DrawingBotV3.INSTANCE.taskMonitor.queueTask(loadingTask);
            }
            DrawingBotV3.INSTANCE.taskService.submit(() -> Hooks.runHook(Hooks.DESERIALIZE_DRAWING_STATE, context, presetData.drawingState));
        }else {
            //to support older versions of the Geometry Serializer, we need to first load the image and only when it's loaded do we try to load the drawing state

            if (!presetData.imagePath.isEmpty()) {
                context.project.openImage.set(null);
                context.taskManager.setCurrentDrawing(null);
                Platform.runLater(() -> {
                    AbstractFileLoader loadingTask = DrawingBotV3.INSTANCE.getImageLoaderTask(DrawingBotV3.context(), new File(presetData.imagePath), false, false);
                    loadingTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue == Worker.State.FAILED) {
                            FXHelper.importFile((file, chooser) -> {
                                DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, false, false);
                                DrawingBotV3.INSTANCE.taskService.submit(() -> Hooks.runHook(Hooks.DESERIALIZE_DRAWING_STATE, context, presetData.drawingState));
                            }, new FileChooser.ExtensionFilter[]{FileUtils.IMPORT_IMAGES}, "Locate the input image");
                        }
                        if (newValue == Worker.State.SUCCEEDED) {
                            DrawingBotV3.INSTANCE.taskService.submit(() -> Hooks.runHook(Hooks.DESERIALIZE_DRAWING_STATE, context, presetData.drawingState));
                        }
                    });
                    DrawingBotV3.INSTANCE.taskMonitor.queueTask(loadingTask);
                });
            }
        }

    }
}
