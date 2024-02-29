package drawingbot.files.json.projects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import drawingbot.DrawingBotV3;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.VersionControl;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.JsonData;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.files.json.PresetDataLoader;
import drawingbot.files.loaders.AbstractFileLoader;
import drawingbot.image.format.FilteredImageData;
import drawingbot.image.format.ImageCropping;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.util.UINodeState;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.UnitsLength;
import javafx.concurrent.Worker;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PresetProjectSettingsManager extends AbstractPresetManager<ObservableProject, PresetProjectSettings> {

    public PresetProjectSettingsManager(PresetProjectSettingsLoader presetLoader) {
        super(presetLoader, ObservableProject.class);
    }

    public List<PresetDataLoader<PresetProjectSettings>> getDataLoaders(){
        return MasterRegistry.INSTANCE.projectDataLoaders;
    }

    @Override
    public ObservableProject getTargetFromContext(DBTaskContext context) {
        return context.project();
    }

    @Override
    public void updatePreset(DBTaskContext context, ObservableProject project, GenericPreset<PresetProjectSettings> preset) {
        PlottedDrawing renderedDrawing = project.getCurrentDrawing();
        preset.data.imagePath = project.openImage.get() != null && project.openImage.get().getSourceFile() != null ? project.openImage.get().getSourceFile().getPath() : "";
        preset.data.timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM));
        preset.data.thumbnailID = renderedDrawing == null ? "" : UUID.randomUUID().toString();

        if(preset.data instanceof PresetProjectSettingsLegacy){
            PresetProjectSettingsManagerLegacy.updatePreset(project, preset);
        }else{
            Gson gson = JsonLoaderManager.createDefaultGson();
            for(PresetDataLoader<PresetProjectSettings> loader : getDataLoaders()){
                try {
                    loader.save(project.context, gson, preset);
                } catch (Exception exception) {
                    DrawingBotV3.logger.severe("Failed to save project data: " + loader.getKey());
                    exception.printStackTrace();
                }
            }
        }

        if(renderedDrawing != null){
            //run the thumbnail generation task
            File saveLocation = new File(FileUtils.getUserThumbnailDirectory() + preset.data.thumbnailID + ".jpg");
            ExportTask task = new ExportTask(project.context, Register.EXPORT_IMAGE, ExportTask.Mode.PER_DRAWING, renderedDrawing, IGeometryFilter.DEFAULT_EXPORT_FILTER, ".jpg", saveLocation, true, true, true);
            task.exportScale = 400 / renderedDrawing.canvas.getWidth(UnitsLength.PIXELS);
            DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.backgroundService, task);
        }
    }

    @Override
    public void applyPreset(DBTaskContext context, ObservableProject project, GenericPreset<PresetProjectSettings> preset, boolean changesOnly) {
        if(preset.data instanceof PresetProjectSettingsLegacy){
            PresetProjectSettingsManagerLegacy.applyPreset(project, preset);
            return;
        }
        Gson gson = JsonLoaderManager.createDefaultGson();
        for(PresetDataLoader<PresetProjectSettings> loader : getDataLoaders()){
            try {
                loader.load(project.context, gson, preset);
            } catch (Exception exception) {
                DrawingBotV3.logger.severe("Failed to load project data: " + loader.getKey());
                exception.printStackTrace();
            }
        }
    }

    @JsonData
    public static class DrawingSetData {

        public final ArrayList<ObservableDrawingSet> drawingSets = new ArrayList<>();
        public int activeSet = 0;

    }

    @JsonData
    public static class UIGlobalState {

        public ArrayList<UINodeState> nodes = new ArrayList<>();

    }

    @JsonData
    public static class OptionalData {

        public String exportDirectory = "";
        public String importDirectory = "";

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
                    FXHelper.saveUIStates(data.nodes);
                }
            }

            @Override
            public boolean isEnabled() {
                return DBPreferences.INSTANCE.restoreProjectLayout.get();
            }
        });

        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.Preset<>(PresetProjectSettings.class, Register.PRESET_MANAGER_DRAWING_AREA, 0));

        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.Preset<>(PresetProjectSettings.class, Register.PRESET_MANAGER_FILTERS, 0));

        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.Preset<>(PresetProjectSettings.class, Register.PRESET_MANAGER_PFM, 0){

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
        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.Preset<>(PresetProjectSettings.class, Register.PRESET_MANAGER_UI_SETTINGS, 0));

        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader<>(PresetProjectSettings.class,"versions", 5) {

            @Override
            public JsonElement saveData(DBTaskContext context, Gson gson, GenericPreset<PresetProjectSettings> preset) {
                if(preset.data.isSubProject) { //no need to save sub versions
                    return new JsonObject();
                }
                return gson.toJsonTree(context.project().getVersionControl(), VersionControl.class);
            }

            @Override
            public void loadData(DBTaskContext context, Gson gson, JsonElement element, GenericPreset<PresetProjectSettings> preset) {
                if(preset.data.isSubProject) { //no need to save sub versions
                    return;
                }
                context.project().setVersionControl(gson.fromJson(element, VersionControl.class));
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

        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.DataInstance<>(PresetProjectSettings.class,"image_settings", ImageCropping.class, ImageCropping::new, 10) {

            @Override
            public void saveData(DBTaskContext context, ImageCropping data, GenericPreset<PresetProjectSettings> preset) {
                if(context.project().getOpenImage() != null){
                    data.update(context.project().getOpenImage().getImageCropping());
                }
            }

            @Override
            public void loadData(DBTaskContext context, ImageCropping data, GenericPreset<PresetProjectSettings> preset) {
                if(!preset.data.imagePath.isEmpty()) {
                    AbstractFileLoader loadingTask = DrawingBotV3.INSTANCE.getImageLoaderTask(context, new File(preset.data.imagePath), false, false);
                    loadingTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue == Worker.State.FAILED) {
                            FXHelper.importFile(context, (file, chooser) -> DrawingBotV3.INSTANCE.openFile(context, file, false, false), new FileChooser.ExtensionFilter[]{FileUtils.IMPORT_ALL}, "Locate the input image");
                        }
                        if(newValue == Worker.State.SUCCEEDED){
                            FilteredImageData imageData = loadingTask.getValue();
                            if(imageData != null){
                                imageData.imageCropping.update(data);
                                imageData.markUpdate(FilteredImageData.UpdateType.FULL_UPDATE);
                            }
                        }
                    });
                    DrawingBotV3.INSTANCE.taskMonitor.queueTask(loadingTask);
                }else{
                    context.project.openImage.set(null);
                }
            }
        });
        MasterRegistry.INSTANCE.registerProjectDataLoader(new PresetDataLoader.DataInstance<>(PresetProjectSettings.class,"optional_data", OptionalData.class, OptionalData::new, 8) {

            @Override
            public void loadData(DBTaskContext context, OptionalData data, GenericPreset<PresetProjectSettings> preset) {
                context.project().lastExportDirectory.set(data.exportDirectory);
                context.project().lastImportDirectory.set(data.importDirectory);
            }

            @Override
            public void saveData(DBTaskContext context, OptionalData data, GenericPreset<PresetProjectSettings> preset) {
                data.exportDirectory = context.project().lastExportDirectory.get();
                data.importDirectory = context.project().lastImportDirectory.get();
            }
        });
    }
}
