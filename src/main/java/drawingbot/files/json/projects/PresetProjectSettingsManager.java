package drawingbot.files.json.projects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.files.json.PresetDataLoader;
import drawingbot.files.loaders.AbstractFileLoader;
import drawingbot.image.format.FilteredImageData;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableVersion;
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
import java.util.*;

public class PresetProjectSettingsManager extends AbstractPresetManager<PresetProjectSettings> {

    public PresetProjectSettingsManager(PresetProjectSettingsLoader presetLoader) {
        super(presetLoader);
    }

    @Override
    public GenericPreset<PresetProjectSettings> updatePreset(DBTaskContext context, GenericPreset<PresetProjectSettings> preset) {
        PlottedDrawing renderedDrawing = context.taskManager().getCurrentDrawing();
        preset.data.imagePath = context.project.openImage.get() != null && context.project.openImage.get().getSourceFile() != null ? context.project.openImage.get().getSourceFile().getPath() : "";
        preset.data.timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM));
        preset.data.thumbnailID = renderedDrawing == null ? "" : UUID.randomUUID().toString();

        if(preset.data instanceof PresetProjectSettingsLegacy){
            PresetProjectSettingsManagerLegacy.updatePreset(context, preset);
        }else{
            Gson gson = JsonLoaderManager.createDefaultGson();
            for(PresetDataLoader<PresetProjectSettings> loader : MasterRegistry.INSTANCE.projectDataLoaders){
                try {
                    loader.save(context, gson, preset);
                } catch (Exception exception) {
                    DrawingBotV3.logger.severe("Failed to save project data: " + loader.getKey());
                    exception.printStackTrace();
                }
            }
        }

        if(renderedDrawing != null){
            //run the thumbnail generation task
            File saveLocation = new File(FileUtils.getUserThumbnailDirectory() + preset.data.thumbnailID + ".jpg");
            ExportTask task = new ExportTask(context, Register.EXPORT_IMAGE, ExportTask.Mode.PER_DRAWING, renderedDrawing, IGeometryFilter.DEFAULT_EXPORT_FILTER, ".jpg", saveLocation, true, true, true);
            task.exportScale = 400 / renderedDrawing.canvas.getWidth(UnitsLength.PIXELS);
            DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.backgroundService, task);
        }
        return preset;
    }

    @Override
    public void applyPreset(DBTaskContext context, GenericPreset<PresetProjectSettings> preset) {
        if(preset.data instanceof PresetProjectSettingsLegacy){
            PresetProjectSettingsManagerLegacy.applyPreset(context, preset);
            return;
        }
        Gson gson = JsonLoaderManager.createDefaultGson();
        for(PresetDataLoader<PresetProjectSettings> loader : MasterRegistry.INSTANCE.projectDataLoaders){
            try {
                loader.load(context, gson, preset);
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

    public static class UIGlobalState {

        public List<FXHelper.UINodeState> nodes = new ArrayList<>();

    }


    public static void registerDefaultDataLoaders(){
        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.DataInstance<>(PresetProjectSettings.class, "ui_state", UIGlobalState.class, UIGlobalState::new, 0){

            @Override
            public void loadData(DBTaskContext context, UIGlobalState data, GenericPreset<PresetProjectSettings> preset) {
                if(!preset.data.isSubProject){
                    FXHelper.loadUIStates(data.nodes);
                }
            }

            @Override
            public void saveData(DBTaskContext context, UIGlobalState data, GenericPreset<PresetProjectSettings> preset) {
                if(!preset.data.isSubProject) {
                    data.nodes.clear();
                    FXHelper.saveUIStates(data.nodes);
                }
            }
        });

        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.Preset<>(PresetProjectSettings.class,Register.PRESET_LOADER_DRAWING_AREA, 0));
        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.Preset<>(PresetProjectSettings.class,Register.PRESET_LOADER_FILTERS, 0));
        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.Preset<>(PresetProjectSettings.class,Register.PRESET_LOADER_PFM, 0){

            @Override
            public JsonElement saveData(DBTaskContext context, Gson gson, GenericPreset<PresetProjectSettings> preset) {
                if(preset.data.name == null || preset.data.name.isEmpty()){
                    preset.data.name = context.project.getPFMSettings().getPFMFactory().getDisplayName();
                }
                return super.saveData(context, gson, preset);
            }

            @Override
            public void loadData(DBTaskContext context, Gson gson, JsonElement element, GenericPreset<PresetProjectSettings> preset) {
                super.loadData(context, gson, element, preset);
            }
        });
        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.Preset<>(PresetProjectSettings.class,Register.PRESET_LOADER_UI_SETTINGS, 0));
        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.DataInstance<>(PresetProjectSettings.class,"versions", VersionData.class, VersionData::new, 5) {

            @Override
            public void saveData(DBTaskContext context, VersionData data, GenericPreset<PresetProjectSettings> preset) {
                for(ObservableVersion projectVersion : context.project().getProjectVersions()){
                    projectVersion.updatePreset();
                    data.versions.add(projectVersion.getPreset());
                }
            }

            @Override
            public void loadData(DBTaskContext context, VersionData data, GenericPreset<PresetProjectSettings> preset) {
                if(!preset.data.isSubProject){ //don't override the loaded projects if they belong to a sub project
                    context.project().getProjectVersions().clear();
                    for(GenericPreset<PresetProjectSettings> version : data.versions){
                        context.project().getProjectVersions().add(new ObservableVersion(version, true));
                    }
                }
            }
        });

        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.DataInstance<>(PresetProjectSettings.class,"drawing_sets", DrawingSetData.class, DrawingSetData::new, 0) {

            @Override
            public void saveData(DBTaskContext context, DrawingSetData data, GenericPreset<PresetProjectSettings> preset) {
                data.drawingSets.addAll(context.project().getDrawingSets().getDrawingSetSlots());
                data.activeSet = context.project().getDrawingSets().getActiveSetSlot();
            }

            @Override
            public void loadData(DBTaskContext context, DrawingSetData data, GenericPreset<PresetProjectSettings> preset) {
                context.project().getDrawingSets().getDrawingSetSlots().clear();
                context.project().getDrawingSets().getDrawingSetSlots().addAll(data.drawingSets);

                //set to the first set first, so the fallback from getDrawingSetForSlot is safe.
                context.project().getDrawingSets().setActiveDrawingSet(data.drawingSets.get(0));
                context.project().getDrawingSets().setActiveDrawingSet(context.project().getDrawingSets().getDrawingSetForSlot(data.activeSet));
            }
        });

        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.DataInstance<>(PresetProjectSettings.class,"image_settings", ImageSettings.class, ImageSettings::new, 10) {

            @Override
            public void saveData(DBTaskContext context, ImageSettings data, GenericPreset<PresetProjectSettings> preset) {
                if(context.project.openImage.get() != null){
                    data.cropStartX = context.project.openImage.get().cropStartX.get();
                    data.cropStartY = context.project.openImage.get().cropStartY.get();
                    data.cropEndX = context.project.openImage.get().cropEndX.get();
                    data.cropEndY = context.project.openImage.get().cropEndY.get();
                    data.imageRotation = context.project.openImage.get().imageRotation.get();
                    data.imageFlipHorizontal = context.project.openImage.get().imageFlipHorizontal.get();
                    data.imageFlipVertical = context.project.openImage.get().imageFlipVertical.get();
                }
            }

            @Override
            public void loadData(DBTaskContext context, ImageSettings data, GenericPreset<PresetProjectSettings> preset) {
                if(!preset.data.imagePath.isEmpty()) {
                    AbstractFileLoader loadingTask = DrawingBotV3.INSTANCE.getImageLoaderTask(context, new File(preset.data.imagePath), false, false);
                    loadingTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue == Worker.State.FAILED) {
                            FXHelper.importFile((file, chooser) -> DrawingBotV3.INSTANCE.openFile(context, file, false, false), new FileChooser.ExtensionFilter[]{FileUtils.IMPORT_IMAGES}, "Locate the input image");
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
                }else{
                    context.project.openImage.set(null);
                }
            }
        });
    }
}
