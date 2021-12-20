package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.ExportFormats;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.PrintResolution;
import drawingbot.javafx.FXController;
import drawingbot.javafx.GenericPreset;
import drawingbot.utils.EnumColourSplitter;
import drawingbot.utils.EnumJsonType;
import javafx.application.Platform;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class PresetProjectSettingsLoader extends AbstractPresetLoader<PresetProjectSettings> {

    public PresetProjectSettingsLoader() {
        super(PresetProjectSettings.class, EnumJsonType.PROJECT_PRESET, "projects.json");
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

        preset.data.imagePath = DrawingBotV3.INSTANCE.openFile != null ? DrawingBotV3.INSTANCE.openFile.getPath() : "";
        preset.data.timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM));

        if(DrawingBotV3.INSTANCE.getActiveTask() != null && DrawingBotV3.INSTANCE.getActiveTask().isTaskFinished()){
            preset.data.thumbnailID = UUID.randomUUID().toString();
            File saveLocation = new File(FileUtils.getUserThumbnailDirectory() + preset.data.thumbnailID + ".jpg");
            PrintResolution thumbnailResolution = PrintResolution.copy(DrawingBotV3.INSTANCE.getActiveTask().resolution);
            thumbnailResolution.changePrintResolution(400, (int)((400 / thumbnailResolution.scaledWidth)*thumbnailResolution.scaledHeight));
            ExportTask task = new ExportTask(ExportFormats.EXPORT_IMAGE, DrawingBotV3.INSTANCE.getActiveTask(), IGeometry.DEFAULT_FILTER, ".jpg", saveLocation, false, true, true, true, thumbnailResolution);
            DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.backgroundService, task);
        }else{
            preset.data.thumbnailID = "";
        }

        GenericPreset<PresetDrawingArea> presetDrawingArea = JsonLoaderManager.DRAWING_AREA.createNewPreset();
        JsonLoaderManager.DRAWING_AREA.updatePreset(presetDrawingArea);
        preset.data.drawingArea = presetDrawingArea;

        GenericPreset<PresetImageFilters> presetImageFilters = JsonLoaderManager.FILTERS.createNewPreset();
        JsonLoaderManager.FILTERS.updatePreset(presetImageFilters);
        preset.data.imageFilters = presetImageFilters;

        GenericPreset<PresetPFMSettings> presetPFMSettings = JsonLoaderManager.PFM.createNewPreset();
        JsonLoaderManager.PFM.updatePreset(presetPFMSettings);
        preset.data.pfmSettings = presetPFMSettings;
        preset.data.name = preset.data.pfmSettings.presetSubType;

        GenericPreset<PresetDrawingSet> presetDrawingSet = JsonLoaderManager.DRAWING_SET.createNewPreset();
        JsonLoaderManager.DRAWING_SET.updatePreset(presetDrawingSet);
        preset.data.drawingSet = presetDrawingSet;

        preset.data.imageRotation = DrawingBotV3.INSTANCE.imageRotation.get();
        preset.data.imageFlipHorizontal = DrawingBotV3.INSTANCE.imageFlipHorizontal.get();
        preset.data.imageFlipVertical = DrawingBotV3.INSTANCE.imageFlipVertical.get();

        preset.data.optimiseForPrint = DrawingBotV3.INSTANCE.optimiseForPrint.get();
        preset.data.targetPenWidth = DrawingBotV3.INSTANCE.targetPenWidth.get();
        preset.data.colourSplitter = DrawingBotV3.INSTANCE.colourSplitter.get();
        preset.data.distributionType = DrawingBotV3.INSTANCE.observableDrawingSet.distributionType.get();
        preset.data.distributionOrder = DrawingBotV3.INSTANCE.observableDrawingSet.distributionOrder.get();
        preset.data.blendMode = DrawingBotV3.INSTANCE.observableDrawingSet.blendMode.get();

        preset.data.cyanMultiplier = DrawingBotV3.INSTANCE.cyanMultiplier.get();
        preset.data.magentaMultiplier = DrawingBotV3.INSTANCE.magentaMultiplier.get();
        preset.data.yellowMultiplier = DrawingBotV3.INSTANCE.yellowMultiplier.get();
        preset.data.keyMultiplier = DrawingBotV3.INSTANCE.keyMultiplier.get();

        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetProjectSettings> preset) {

        JsonLoaderManager.DRAWING_AREA.applyPreset(preset.data.drawingArea);
        JsonLoaderManager.FILTERS.applyPreset(preset.data.imageFilters);
        JsonLoaderManager.PFM.applyPreset(preset.data.pfmSettings);

        if(DrawingBotV3.INSTANCE.colourSplitter.get() != EnumColourSplitter.CMYK){
            JsonLoaderManager.DRAWING_SET.applyPreset(preset.data.drawingSet);
        }else{
            FXController.changeDrawingSet(DrawingBotV3.INSTANCE.colourSplitter.get().drawingSet);
        }

        DrawingBotV3.INSTANCE.imageRotation.set(preset.data.imageRotation);
        DrawingBotV3.INSTANCE.imageFlipHorizontal.set(preset.data.imageFlipHorizontal);
        DrawingBotV3.INSTANCE.imageFlipVertical.set(preset.data.imageFlipVertical);

        DrawingBotV3.INSTANCE.optimiseForPrint.set(preset.data.optimiseForPrint);
        DrawingBotV3.INSTANCE.controller.textFieldPenWidth.setText("" + preset.data.targetPenWidth); //works but ugly!
        DrawingBotV3.INSTANCE.colourSplitter.set(preset.data.colourSplitter);
        DrawingBotV3.INSTANCE.observableDrawingSet.distributionType.set(preset.data.distributionType);
        DrawingBotV3.INSTANCE.observableDrawingSet.distributionOrder.set(preset.data.distributionOrder);
        DrawingBotV3.INSTANCE.observableDrawingSet.blendMode.set(preset.data.blendMode);

        DrawingBotV3.INSTANCE.cyanMultiplier.set(preset.data.cyanMultiplier);
        DrawingBotV3.INSTANCE.magentaMultiplier.set(preset.data.magentaMultiplier);
        DrawingBotV3.INSTANCE.yellowMultiplier.set(preset.data.yellowMultiplier);
        DrawingBotV3.INSTANCE.keyMultiplier.set(preset.data.keyMultiplier);

        if(!preset.data.imagePath.isEmpty()){
            Platform.runLater(() -> DrawingBotV3.INSTANCE.openFile(new File(preset.data.imagePath), false));
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
}
