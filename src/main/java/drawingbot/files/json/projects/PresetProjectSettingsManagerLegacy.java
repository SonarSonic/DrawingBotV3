package drawingbot.files.json.projects;

import com.google.gson.JsonObject;
import drawingbot.DrawingBotV3;
import drawingbot.api.Hooks;
import drawingbot.files.FileUtils;
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

    public static GenericPreset<PresetProjectSettings> updatePreset(ObservableProject project, GenericPreset<PresetProjectSettings> preset) {

        PresetProjectSettingsLegacy presetData = (PresetProjectSettingsLegacy) preset.data;

        PlottedDrawing renderedDrawing = project.getCurrentDrawing();
        presetData.imagePath = project.openImage.get() != null && project.openImage.get().getSourceFile() != null ? project.openImage.get().getSourceFile().getPath() : "";
        presetData.timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM));
        presetData.thumbnailID = renderedDrawing == null ? "" : UUID.randomUUID().toString();

        presetData.drawingArea = Register.PRESET_MANAGER_DRAWING_AREA.createPresetFromTarget(project.context, project.getDrawingArea());
        presetData.imageFilters = Register.PRESET_MANAGER_FILTERS.createPresetFromTarget(project.context, project.getImageSettings());
        presetData.pfmSettings = Register.PRESET_MANAGER_PFM.createPresetFromTarget(project.context, project.getPFMSettings());
        presetData.drawingSet = Register.PRESET_MANAGER_DRAWING_SET.createPresetFromTarget(project.context, project.getActiveDrawingSet());
        /*
        presetData.imageRotation = DrawingBotV3.INSTANCE.imgFilterSettings.imageRotation.get();
        presetData.imageFlipHorizontal = DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipHorizontal.get();
        presetData.imageFlipVertical = DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipVertical.get();
         */

        presetData.optimiseForPrint = project.getDrawingArea().getRescaleMode().shouldRescale();
        presetData.targetPenWidth = project.getDrawingArea().targetPenWidth.get();
        presetData.colourSplitter = project.getDrawingSets().getDrawingSetForSlot(0).colorHandler.get();
        presetData.distributionType = project.getDrawingSets().getDrawingSetForSlot(0).distributionType.get();
        presetData.distributionOrder = project.getDrawingSets().getDrawingSetForSlot(0).distributionOrder.get();
        presetData.blendMode = project.blendMode.get();

        /*
        presetData.cyanMultiplier = DrawingBotV3.INSTANCE.cyanMultiplier.get();
        presetData.magentaMultiplier = DrawingBotV3.INSTANCE.magentaMultiplier.get();
        presetData.yellowMultiplier = DrawingBotV3.INSTANCE.yellowMultiplier.get();
        presetData.keyMultiplier = DrawingBotV3.INSTANCE.keyMultiplier.get();

         */

        presetData.drawingState = new JsonObject();

        presetData.projectVersions = new ArrayList<>();

        for(ObservableVersion projectVersion : project.getVersionControl().getProjectVersions()){
            if(projectVersion.getPreset().data instanceof PresetProjectSettingsLegacy){
                presetData.projectVersions.add((PresetProjectSettingsLegacy)projectVersion.getPreset().data);
            }
        }

        presetData.drawingSets = new ArrayList<>();
        for(ObservableDrawingSet drawingSet : project.getDrawingSets().drawingSetSlots){
            presetData.drawingSets.add(new ObservableDrawingSet(drawingSet));
        }
        int activeSlot = presetData.drawingSets.indexOf(project.getDrawingSets().activeDrawingSet.get());
        presetData.activeDrawingSlot = activeSlot == -1 ? 0 : activeSlot;

        return preset;
    }

    public static void applyPreset(ObservableProject project, GenericPreset<PresetProjectSettings> preset) {
        PresetProjectSettingsLegacy presetData = (PresetProjectSettingsLegacy) preset.data;

        Register.PRESET_MANAGER_DRAWING_AREA.applyPreset(project.context, project.getDrawingArea(), presetData.drawingArea, false);
        Register.PRESET_MANAGER_FILTERS.applyPreset(project.context, project.getImageSettings(), presetData.imageFilters, false);
        Register.PRESET_MANAGER_PFM.applyPreset(project.context, project.getPFMSettings(), presetData.pfmSettings, false);

        /*
        DrawingBotV3.INSTANCE.imgFilterSettings.imageRotation.set(presetData.imageRotation);
        DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipHorizontal.set(presetData.imageFlipHorizontal);
        DrawingBotV3.INSTANCE.imgFilterSettings.imageFlipVertical.set(presetData.imageFlipVertical);
         */

        project.getDrawingArea().rescaleMode.set(presetData.optimiseForPrint ? EnumRescaleMode.HIGH_QUALITY : EnumRescaleMode.OFF);
        project.getDrawingArea().targetPenWidth.set(presetData.targetPenWidth);


        project.blendMode.set(presetData.blendMode);

        /*
        DrawingBotV3.INSTANCE.cyanMultiplier.set(presetData.cyanMultiplier);
        DrawingBotV3.INSTANCE.magentaMultiplier.set(presetData.magentaMultiplier);
        DrawingBotV3.INSTANCE.yellowMultiplier.set(presetData.yellowMultiplier);
        DrawingBotV3.INSTANCE.keyMultiplier.set(presetData.keyMultiplier);

         */

        project.getDrawingSets().getDrawingSetForSlot(0).loadDrawingSet(presetData.drawingSet.data);
        project.getDrawingSets().getDrawingSetForSlot(0).colorHandler.set(presetData.colourSplitter);
        project.getDrawingSets().getDrawingSetForSlot(0).colorHandler.get().applySettings(project.context, project.getDrawingSets().getActiveDrawingSet());
        project.getDrawingSets().getDrawingSetForSlot(0).distributionType.set(presetData.distributionType);
        project.getDrawingSets().getDrawingSetForSlot(0).distributionOrder.set(presetData.distributionOrder);

        if(presetData.drawingSets != null && !presetData.drawingSets.isEmpty()){
            project.getDrawingSets().drawingSetSlots.clear();
            for(ObservableDrawingSet drawingSet : presetData.drawingSets){
                project.getDrawingSets().drawingSetSlots.add(new ObservableDrawingSet(drawingSet));
            }
            int activeSlot = presetData.activeDrawingSlot < project.getDrawingSets().drawingSetSlots.size() ? presetData.activeDrawingSlot : 0;
            project.getDrawingSets().activeDrawingSet.set(project.getDrawingSets().drawingSetSlots.get(activeSlot));
        }

        if(!presetData.isSubProject){ //don't overwrite the versions if this is just a sub version
            project.getVersionControl().getProjectVersions().clear();
            if(presetData.projectVersions != null){
                for(PresetProjectSettingsLegacy projectVersion : presetData.projectVersions){
                    GenericPreset<PresetProjectSettings> newPreset = Register.PRESET_LOADER_PROJECT.createNewPreset();
                    newPreset.version = "1";
                    newPreset.data = projectVersion;
                    project.getVersionControl().getProjectVersions().add(new ObservableVersion(newPreset, true));
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
            project.openImage.set(null);
            project.context.taskManager.setCurrentDrawing(null);
            if(!presetData.imagePath.isEmpty()) {
                AbstractFileLoader loadingTask = DrawingBotV3.INSTANCE.getImageLoaderTask(DrawingBotV3.context(), new File(presetData.imagePath), false, true);
                loadingTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.FAILED) {
                        FXHelper.importFile(project.context, (file, chooser) -> DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, false, true), new FileChooser.ExtensionFilter[]{FileUtils.IMPORT_IMAGES}, "Locate the input image");
                    }
                });
                DrawingBotV3.INSTANCE.taskMonitor.queueTask(loadingTask);
            }
            DrawingBotV3.INSTANCE.taskService.submit(() -> Hooks.runHook(Hooks.DESERIALIZE_DRAWING_STATE, project.context, presetData.drawingState));
        }else {
            //to support older versions of the Geometry Serializer, we need to first load the image and only when it's loaded do we try to load the drawing state

            if (!presetData.imagePath.isEmpty()) {
                project.openImage.set(null);
                project.context.taskManager.setCurrentDrawing(null);
                Platform.runLater(() -> {
                    AbstractFileLoader loadingTask = DrawingBotV3.INSTANCE.getImageLoaderTask(DrawingBotV3.context(), new File(presetData.imagePath), false, true);
                    loadingTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue == Worker.State.FAILED) {
                            FXHelper.importFile(project.context, (file, chooser) -> {
                                DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, false, true);
                                DrawingBotV3.INSTANCE.taskService.submit(() -> Hooks.runHook(Hooks.DESERIALIZE_DRAWING_STATE, project.context, presetData.drawingState));
                            }, new FileChooser.ExtensionFilter[]{FileUtils.IMPORT_IMAGES}, "Locate the input image");
                        }
                        if (newValue == Worker.State.SUCCEEDED) {
                            DrawingBotV3.INSTANCE.taskService.submit(() -> Hooks.runHook(Hooks.DESERIALIZE_DRAWING_STATE, project.context, presetData.drawingState));
                        }
                    });
                    DrawingBotV3.INSTANCE.taskMonitor.queueTask(loadingTask);
                });
            }
        }

    }
}
