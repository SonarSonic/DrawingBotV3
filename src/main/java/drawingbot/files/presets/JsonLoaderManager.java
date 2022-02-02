package drawingbot.files.presets;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.ColourSplitterHandler;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.javafx.GenericPreset;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Function;
import java.util.logging.Level;

public class JsonLoaderManager {

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

    public static Gson createDefaultGson(){
        GsonBuilder builder = new GsonBuilder();
        builder.setExclusionStrategies(exclusionStrategy);
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(GenericPreset.class, new JsonAdapterGenericPreset());
        builder.registerTypeAdapter(ColourSplitterHandler.class, new JsonAdapterColourSplitter());
        builder.registerTypeAdapter(IDrawingPen.class, new JsonAdapterDrawingPen());
        return builder.create();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Nullable
    public static AbstractJsonLoader<IJsonData> getJsonLoaderForPresetType(PresetType type) {
        for(AbstractJsonLoader<IJsonData> manager : MasterRegistry.INSTANCE.presetLoaders){
            if(manager.type == type){
                return manager;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void loadConfigFiles(){
        Register.PRESET_LOADER_CONFIGS.loadFromJSON();
    }

    public static void loadJSONFiles(){

        //load default presets TODO FIX DEFAULTS / FIX LOADING JSON WITH OLD NAMES!!!
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

        //load user presets
        for(AbstractJsonLoader<?> manager : MasterRegistry.INSTANCE.presetLoaders){
            if(manager != Register.PRESET_LOADER_CONFIGS){
                manager.loadFromJSON();
            }
        }

    }
    public static void loadDefaults(){
        for(AbstractJsonLoader<?> manager : MasterRegistry.INSTANCE.presetLoaders){
            if(manager != Register.PRESET_LOADER_CONFIGS){
                manager.loadDefaults();
            }
        }
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
    public static GenericPreset<IJsonData> importPresetFile(File file, PresetType targetType){
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
    public static GenericPreset<IJsonData> importPresetFile(InputStream stream, PresetType targetType){
        GenericPreset<IJsonData> preset = importJsonFile(stream, GenericPreset.class);
        if(preset != null && (targetType == null || targetType == preset.presetType)){
            AbstractJsonLoader<IJsonData> manager = getJsonLoaderForPresetType(preset.presetType);
            if(manager != null){
                manager.trySavePreset(preset);
            }
        }
        return preset;
    }

    private static void loadDefaultPresetContainerJSON(String json){
        InputStream stream = JsonLoaderManager.class.getResourceAsStream("/presets/" + json);
        if(stream != null){
            importPresetContainerFile(stream);
        }
    }

    public static void importPresetContainerFile(File file){
        try {
            importPresetContainerFile(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void importPresetContainerFile(InputStream stream){
        PresetContainerJsonFile<IJsonData> container = importJsonFile(stream, PresetContainerJsonFile.class);
        container.jsonMap.forEach(preset -> {
            if(preset != null){
                AbstractJsonLoader<IJsonData> manager = getJsonLoaderForPresetType(preset.presetType);
                if(manager != null){
                    manager.registerPreset(preset);
                }
            }
        });
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
     * Exports a single .json for a preset
     * @param file the destination
     * @param selected the preset to export
     */
    public static void exportPresetFile(File file, GenericPreset<?> selected){
        try {
            Gson gson = createDefaultGson();
            JsonWriter writer = gson.newJsonWriter(new FileWriter(file));
            gson.toJson(selected, GenericPreset.class, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error exporting preset file");
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