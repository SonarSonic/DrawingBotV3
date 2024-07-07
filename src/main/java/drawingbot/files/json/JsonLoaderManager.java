package drawingbot.files.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.api.Hooks;
import drawingbot.api.ICanvas;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.drawing.DrawingSets;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.VersionControl;
import drawingbot.files.json.adapters.*;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.format.ImageCropping;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableVersion;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMSettings;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.software.SoftwareManager;
import drawingbot.utils.MetadataMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

public class JsonLoaderManager {

    public static JsonLoaderManager INSTANCE = new JsonLoaderManager();

    private JsonLoaderManager(){}

    public BooleanProperty loading = new SimpleBooleanProperty(true);

    /** used to prevent certain values from being serialized, transient achieves the same thing*/
    public static final ExclusionStrategy exclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(JsonExclude.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    };

    public static GsonHelper GSON_HELPER = new GsonHelper(){

        @Override
        public void setupGsonBuilder(GsonBuilder builder) {
        super.setupGsonBuilder(builder);
        builder.setExclusionStrategies(exclusionStrategy);
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(GenericPreset.class, new JsonAdapterGenericPreset());
        builder.registerTypeAdapter(PresetContainerJsonFile.class, new JsonAdapterGenericPresetContainer());
        builder.registerTypeHierarchyAdapter(ColorSeparationHandler.class, new JsonAdapterColorSeparationHandler());
        builder.registerTypeHierarchyAdapter(DrawingExportHandler.class, new JsonAdapterDrawingExportHandler());
        builder.registerTypeHierarchyAdapter(ObservableDrawingPen.class, new JsonAdapterObservableDrawingPen());
        builder.registerTypeAdapter(IDrawingPen.class, new JsonAdapterDrawingPen());
        builder.registerTypeAdapter(ObservableDrawingSet.class, new JsonAdapterObservableDrawingSet());
        builder.registerTypeAdapter(PFMFactory.class, new JsonAdapterPFMFactory());
        builder.registerTypeAdapter(ICanvas.class, (InstanceCreator<Object>) type -> new SimpleCanvas());
        builder.registerTypeAdapter(AffineTransform.class, new JsonAdapterAffineTransform());
        builder.registerTypeAdapter(MetadataMap.class, new JsonAdapterMetadataMap());
        builder.registerTypeAdapter(IDrawingSet.class, new JsonAdapterDrawingSet());
        builder.registerTypeAdapter(DrawingSets.class, new JsonAdapterDrawingSets());
        builder.registerTypeAdapter(PFMSettings.class, new JsonAdapterPFMSettings());
        builder.registerTypeAdapter(ImageCropping.class, new JsonAdapterImageCropping());
        builder.registerTypeAdapter(VersionControl.class, new JsonAdapterVersionControl());
        builder.registerTypeAdapter(ObservableVersion.class, new JsonAdapterObservableVersion());
        builder.registerTypeAdapter(File.class, new JsonAdapterFile());
        Hooks.runHook(Hooks.GSON_BUILDER_INIT_POST, builder);
        }
    };

    public static Gson createDefaultGson(){
        return GSON_HELPER.getDefaultGson();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Nullable
    public static <D> IPresetLoader<D> getJsonLoaderForPresetType(GenericPreset<D> preset) {
        for(IPresetLoader<?> manager : MasterRegistry.INSTANCE.presetLoaders){
            if(manager.canLoadPreset(preset)){
                return (IPresetLoader<D>) manager;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void loadConfigFiles(){
        Register.PRESET_LOADER_PREFERENCES.loadFromJSON();
    }

    public static void loadJSONFiles(){

        //load default presets TODO FIX DEFAULTS / FIX LOADING JSON WITH OLD NAMES!!! -TODO AUTOMATE THIS
        loadDefaultPresetContainerJSON("pre_processing_defaults.json");
        loadDefaultPresetContainerJSON("sketch_pfm_defaults.json");
        loadDefaultPresetContainerJSON("square_pfm_defaults.json");
        loadDefaultPresetContainerJSON("shapes_pfm_defaults.json");
        loadDefaultPresetContainerJSON("curves_pfm_defaults.json");
        loadDefaultPresetContainerJSON("mosaic_pfm_defaults.json");
        loadDefaultPresetContainerJSON("catmull_rom_pfm_defaults.json");
        loadDefaultPresetContainerJSON("voronoi_pfm_defaults.json");
        loadDefaultPresetContainerJSON("drawing_area_defaults.json");
        loadDefaultPresetContainerJSON("gcode_settings_defaults.json");
        loadDefaultPresetContainerJSON("vpype_settings_defaults.json");
        loadDefaultPresetContainerJSON("hpgl_settings_defaults.json");
        loadDefaultPresetContainerJSON("serial_port_defaults.json");
        loadDefaultPresetContainerJSON("adaptive_pfm_defaults.json");

        //load user presets
        for(IPresetLoader<?> loader : MasterRegistry.INSTANCE.presetLoaders){
            if(loader != Register.PRESET_LOADER_PREFERENCES){
                loader.loadFromJSON();
            }
        }
    }

    public static void loadDefaults(DBTaskContext context){
        for(IPresetLoader<?> loader : MasterRegistry.INSTANCE.presetLoaders){
            if(loader != Register.PRESET_LOADER_PREFERENCES){
                loader.loadDefaults(context);
            }
        }
    }

    public static void postInit(){
        for(IPresetLoader<?> loader : MasterRegistry.INSTANCE.presetLoaders){
            loader.setLoading(false);
        }
    }


    private final Set<IPresetLoader<?>> dirtyPresetLoaders = new HashSet<>();

    public void tick(){
        if(DrawingBotV3.INSTANCE.controller.presetManagerStage.isShowing()){
            return;
        }
        //Do all updates at the same time / prevent multiple updates
        if(!dirtyPresetLoaders.isEmpty()){
            dirtyPresetLoaders.forEach(IPresetLoader::updateJSON);
            dirtyPresetLoaders.clear();
        }
    }

    public void markDirty(IPresetLoader<?> loader){
        dirtyPresetLoaders.add(loader);
    }


    private static void loadDefaultPresetJSON(String json){
        InputStream stream = JsonLoaderManager.class.getResourceAsStream("/presets/" + json);
        if(stream != null){
            importPresetFile(stream, null);
        }
    }

    /**
     * Imports a single .json from the given file
     * @param file the location to load the json from
     * @param targetType the type of preset to import, if null all types will be accepted
     */
    public static <O> GenericPreset<O> importPresetFile(File file, PresetType targetType){
        try {
            return importPresetFile(new FileInputStream(file), targetType);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Imports a single .json from the given input stream
     * @param stream the input stream to load the json from
     * @param targetType the type of preset to import, if null all types will be accepted
     */
    public static <O> GenericPreset<O> importPresetFile(InputStream stream, PresetType targetType){
        GenericPreset<O> preset = importJsonFile(stream, GenericPreset.class);
        if(preset != null && (targetType == null || targetType == preset.presetType)){
            loadUnknownPreset(preset, false);
        }
        return preset;
    }

    public static List<GenericPreset<?>> loadDefaultPresetContainerJSON(String json){
        InputStream stream = JsonLoaderManager.class.getResourceAsStream("/presets/" + json);
        if(stream != null){
            List<GenericPreset<?>> systemPresets = importPresetContainerFile(stream, true);
            systemPresets.forEach(preset -> preset.userCreated = false);
            return systemPresets;
        }else{
            DrawingBotV3.logger.warning("Missing Preset Container JSON: " + json);
            return List.of();
        }
    }

    public static List<GenericPreset<?>> importPresetContainerFile(File file, boolean systemPresets){
        try {
            return importPresetContainerFile(new FileInputStream(file), systemPresets);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<GenericPreset<?>> importPresetContainerFile(InputStream stream, boolean systemPresets){
        PresetContainerJsonFile container = importJsonFile(stream, PresetContainerJsonFile.class);
        container.jsonMap.forEach((preset -> loadUnknownPreset(preset, systemPresets)));
        return container.jsonMap;
    }

    public static void exportPresetContainerFile(File file, PresetContainerJsonFile container){
        exportJsonFile(file, PresetContainerJsonFile.class, container);
    }

    public static <DATA> void loadUnknownPreset(GenericPreset<DATA> preset, boolean systemPreset){
        if(preset != null){
            IPresetLoader<DATA> manager = getJsonLoaderForPresetType(preset);
            if(manager != null){
                preset.userCreated = !systemPreset;
                manager.addPreset(preset);
            }
        }
    }

    /**
     * Exports a single .json for a preset
     * @param file the destination
     * @param selected the preset to export
     */
    public static void exportPresetFile(File file, GenericPreset<?> selected){
        exportJsonFile(file, GenericPreset.class, selected);
    }

    public static <T> T importJsonFile(InputStream stream, Class<T> type){
        T file = null;
        try {
            Gson gson = createDefaultGson();
            JsonReader reader = gson.newJsonReader(new InputStreamReader(stream));
            file = gson.fromJson(reader, type);
            reader.close();
        } catch (Throwable e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error importing json file");
        }
        return file;
    }


    /**
     * Imports a single .json
     * @param file the file to read from
     * @param type the data to import to the json file
     */
    public static <T> T importJsonFile(File file, Class<T> type){
        T data = null;
        try {
            Gson gson = createDefaultGson();
            JsonReader reader = gson.newJsonReader(new InputStreamReader(new FileInputStream(file)));
            data = gson.fromJson(reader, type);
            reader.close();
        } catch (Throwable e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error importing json file");
        }
        return data;
    }

    /**
     * Exports a single .json
     * @param file the destination
     * @param instance the data to export to the json file
     */
    public static <T> void exportJsonFile(File file, Class<T> type, T instance){
        try {
            Gson gson = createDefaultGson();
            JsonWriter writer = gson.newJsonWriter(new FileWriter(file));
            gson.toJson(instance, type, writer);
            writer.flush();
            writer.close();
        } catch (Throwable e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error exporting json file");
        }
    }

    public static <I> I getOrCreateJSONFile(Class<I> clazz, File file, Function<Class<I>, I> iProvider) {
        I loaded = null;
        try {
            boolean createSettingsFile = true;

            if(Files.exists(file.toPath())){
                Gson gson = JsonLoaderManager.createDefaultGson();
                JsonReader reader = gson.newJsonReader(new FileReader(file));
                loaded = gson.fromJson(reader, clazz);
                reader.close();
                if(loaded != null){
                    createSettingsFile = false;
                }
            }

            if(createSettingsFile){
                Gson gson = JsonLoaderManager.createDefaultGson();
                JsonWriter writer = gson.newJsonWriter(new FileWriter(file));
                gson.toJson(loaded = iProvider.apply(clazz), clazz, writer);
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error loading " + file.getName() + " using defaults");
            loaded = iProvider.apply(clazz);
        }
        return loaded;
    }
}