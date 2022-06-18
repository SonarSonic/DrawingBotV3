package drawingbot.files.json.projects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.files.loaders.AbstractFileLoader;
import drawingbot.image.format.FilteredImageData;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableProjectSettings;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.EnumRotation;
import drawingbot.utils.UnitsLength;
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
        PlottedDrawing renderedDrawing = DrawingBotV3.INSTANCE.getCurrentDrawing();
        preset.data.imagePath = DrawingBotV3.INSTANCE.openImage.get() != null && DrawingBotV3.INSTANCE.openImage.get().getSourceFile() != null ? DrawingBotV3.INSTANCE.openImage.get().getSourceFile().getPath() : "";
        preset.data.timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM));
        preset.data.thumbnailID = renderedDrawing == null ? "" : UUID.randomUUID().toString();

        if(preset.data instanceof PresetProjectSettingsLegacy){
            PresetProjectSettingsManagerLegacy.updatePreset(preset);
        }else{
            Gson gson = JsonLoaderManager.createDefaultGson();
            for(ProjectDataLoader loader : MasterRegistry.INSTANCE.dataLoaders){
                try {
                    loader.save(gson, preset);
                } catch (Exception exception) {
                    DrawingBotV3.logger.severe("Failed to save project data: " + loader.getKey());
                    exception.printStackTrace();
                }
            }
        }

        if(renderedDrawing != null){
            //run the thumbnail generation task
            File saveLocation = new File(FileUtils.getUserThumbnailDirectory() + preset.data.thumbnailID + ".jpg");
            ExportTask task = new ExportTask(Register.EXPORT_IMAGE, ExportTask.Mode.PER_DRAWING, renderedDrawing, IGeometryFilter.DEFAULT_EXPORT_FILTER, ".jpg", saveLocation, true, true, true);
            task.exportScale = 400 / renderedDrawing.canvas.getWidth(UnitsLength.PIXELS);
            DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.backgroundService, task);
        }
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetProjectSettings> preset) {
        if(preset.data instanceof PresetProjectSettingsLegacy){
            PresetProjectSettingsManagerLegacy.applyPreset(preset);
            return;
        }
        Gson gson = JsonLoaderManager.createDefaultGson();
        for(ProjectDataLoader loader : MasterRegistry.INSTANCE.dataLoaders){
            try {
                loader.load(gson, preset);
            } catch (Exception exception) {
                DrawingBotV3.logger.severe("Failed to load project data: " + loader.getKey());
                exception.printStackTrace();
            }
        }
    }

    public static class VersionData {

        public final ArrayList<GenericPreset<PresetProjectSettings>> versions = new ArrayList<>();

    }

    public static class DrawingSetData {

        public final ArrayList<ObservableDrawingSet> drawingSets = new ArrayList<>();
        public int activeSet = 0;

    }

    public static class ImageSettings{
        public EnumRotation imageRotation = EnumRotation.R0;
        public boolean imageFlipHorizontal = false;
        public boolean imageFlipVertical = false;
        public float cropStartX = 0;
        public float cropStartY = 0;
        public float cropEndX = 0;
        public float cropEndY = 0;
    }

    public static void registerDefaultDataLoaders(){
        MasterRegistry.INSTANCE.registerProjectDataLoader(new ProjectDataLoader.Preset<>(Register.PRESET_LOADER_DRAWING_AREA, 0));
        MasterRegistry.INSTANCE.registerProjectDataLoader(new ProjectDataLoader.Preset<>(Register.PRESET_LOADER_FILTERS, 0));
        MasterRegistry.INSTANCE.registerProjectDataLoader(new ProjectDataLoader.Preset<>(Register.PRESET_LOADER_PFM, 0){

            @Override
            public JsonElement saveData(Gson gson, GenericPreset<PresetProjectSettings> preset) {
                if(preset.data.name == null || preset.data.name.isEmpty()){
                    preset.data.name = DrawingBotV3.INSTANCE.pfmSettings.factory.get().getDisplayName();
                }
                return super.saveData(gson, preset);
            }

            @Override
            public void loadData(Gson gson, JsonElement element, GenericPreset<PresetProjectSettings> preset) {
                super.loadData(gson, element, preset);
            }
        });
        MasterRegistry.INSTANCE.registerProjectDataLoader(new ProjectDataLoader.Preset<>(Register.PRESET_LOADER_UI_SETTINGS, 0));
        MasterRegistry.INSTANCE.registerProjectDataLoader(new ProjectDataLoader.DataInstance<>("versions", VersionData.class, VersionData::new, 5) {

            @Override
            public void saveData(VersionData data, GenericPreset<PresetProjectSettings> preset) {
                for(ObservableProjectSettings projectVersion : DrawingBotV3.INSTANCE.projectVersions){
                    data.versions.add(projectVersion.preset.get());
                }
            }

            @Override
            public void loadData(VersionData data, GenericPreset<PresetProjectSettings> preset) {
                if(!preset.data.isSubProject){ //don't override the loaded projects if they belong to a sub project
                    DrawingBotV3.INSTANCE.projectVersions.clear();
                    for(GenericPreset<PresetProjectSettings> version : data.versions){
                        DrawingBotV3.INSTANCE.projectVersions.add(new ObservableProjectSettings(version, true));
                    }
                }
            }
        });

        MasterRegistry.INSTANCE.registerProjectDataLoader(new ProjectDataLoader.DataInstance<>("drawing_sets", DrawingSetData.class, DrawingSetData::new, 0) {

            @Override
            public void saveData(DrawingSetData data, GenericPreset<PresetProjectSettings> preset) {
                data.drawingSets.addAll(DrawingBotV3.INSTANCE.drawingSets.drawingSetSlots.get());
                data.activeSet = DrawingBotV3.INSTANCE.drawingSets.getActiveSetSlot();
            }

            @Override
            public void loadData(DrawingSetData data, GenericPreset<PresetProjectSettings> preset) {
                DrawingBotV3.INSTANCE.drawingSets.drawingSetSlots.get().clear();
                DrawingBotV3.INSTANCE.drawingSets.drawingSetSlots.get().addAll(data.drawingSets);

                //set to the first set first, so the fallback from getDrawingSetForSlot is safe.
                DrawingBotV3.INSTANCE.drawingSets.activeDrawingSet.set(data.drawingSets.get(0));
                DrawingBotV3.INSTANCE.drawingSets.activeDrawingSet.set(DrawingBotV3.INSTANCE.drawingSets.getDrawingSetForSlot(data.activeSet));
            }
        });

        MasterRegistry.INSTANCE.registerProjectDataLoader(new ProjectDataLoader.DataInstance<>("image_settings", ImageSettings.class, ImageSettings::new, 10) {

            @Override
            public void saveData(ImageSettings data, GenericPreset<PresetProjectSettings> preset) {
                if(DrawingBotV3.INSTANCE.openImage.get() != null){
                    data.cropStartX = DrawingBotV3.INSTANCE.openImage.get().cropStartX.get();
                    data.cropStartY = DrawingBotV3.INSTANCE.openImage.get().cropStartY.get();
                    data.cropEndX = DrawingBotV3.INSTANCE.openImage.get().cropEndX.get();
                    data.cropEndY = DrawingBotV3.INSTANCE.openImage.get().cropEndY.get();
                    data.imageRotation = DrawingBotV3.INSTANCE.openImage.get().imageRotation.get();
                    data.imageFlipHorizontal = DrawingBotV3.INSTANCE.openImage.get().imageFlipHorizontal.get();
                    data.imageFlipVertical = DrawingBotV3.INSTANCE.openImage.get().imageFlipVertical.get();
                }
            }

            @Override
            public void loadData(ImageSettings data, GenericPreset<PresetProjectSettings> preset) {
                if(!preset.data.imagePath.isEmpty()) {
                    AbstractFileLoader loadingTask = DrawingBotV3.INSTANCE.getImageLoaderTask(new File(preset.data.imagePath), false);
                    loadingTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue == Worker.State.FAILED) {
                            FXHelper.importFile((file, chooser) -> DrawingBotV3.INSTANCE.openFile(file, false), new FileChooser.ExtensionFilter[]{FileUtils.IMPORT_IMAGES}, "Locate the input image");
                        }
                        if(newValue == Worker.State.SUCCEEDED){
                            FilteredImageData imageData = loadingTask.getValue();
                            if(imageData != null){
                                imageData.cropStartX.set(data.cropStartX);
                                imageData.cropStartY.set(data.cropStartY);
                                imageData.cropEndX.set(data.cropEndX);
                                imageData.cropEndY.set(data.cropEndY);
                                imageData.imageRotation.set(data.imageRotation);
                                imageData.imageFlipHorizontal.set(data.imageFlipHorizontal);
                                imageData.imageFlipVertical.set(data.imageFlipVertical);
                            }
                        }
                    });
                    DrawingBotV3.INSTANCE.taskMonitor.queueTask(loadingTask);
                }
            }
        });
    }
}
