package drawingbot.files.presets;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.presets.types.*;
import drawingbot.integrations.vpype.PresetVpypeSettingsLoader;
import drawingbot.utils.EnumJsonType;
import drawingbot.javafx.GenericPreset;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Function;
import java.util.logging.Level;

public class JsonLoaderManager {

    public static final PresetProjectSettingsLoader PROJECT = new PresetProjectSettingsLoader();
    public static final PresetPFMSettingsLoader PFM = new PresetPFMSettingsLoader();
    public static final PresetImageFiltersLoader FILTERS = new PresetImageFiltersLoader();
    public static final PresetDrawingSetLoader DRAWING_SET = new PresetDrawingSetLoader();
    public static final PresetDrawingPenLoader DRAWING_PENS = new PresetDrawingPenLoader();
    public static final PresetDrawingAreaLoader DRAWING_AREA = new PresetDrawingAreaLoader();
    public static final ConfigJsonLoader CONFIGS = new ConfigJsonLoader();
    public static final PresetGCodeSettingsLoader GCODE_SETTINGS = new PresetGCodeSettingsLoader();
    public static final PresetVpypeSettingsLoader VPYPE_SETTINGS = new PresetVpypeSettingsLoader();
    public static final AbstractJsonLoader<IJsonData>[] LOADERS = new AbstractJsonLoader[]{PROJECT, PFM, FILTERS, DRAWING_SET, DRAWING_PENS, DRAWING_AREA, CONFIGS, GCODE_SETTINGS, VPYPE_SETTINGS};

    /** used to prevent certain values from being serialized, transient achives the same thing*/
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
        builder.registerTypeAdapter(IDrawingPen.class, (InstanceCreator<IDrawingPen>) type -> new DrawingPen());
        return builder.create();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static AbstractJsonLoader<IJsonData> getManagerForType(EnumJsonType type) {
        for(AbstractJsonLoader<IJsonData> manager : LOADERS){
            if(manager.type == type){
                return manager;
            }
        }
        throw new NullPointerException("NO PRESET MANAGER for " + type);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void loadConfigFiles(){
        CONFIGS.loadFromJSON();
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
        loadDefaultPresetContainerJSON("drawing_area_defaults.json");
        loadDefaultPresetContainerJSON("gcode_settings_defaults.json");
        loadDefaultPresetContainerJSON("vpype_settings_defaults.json");

        //load user presets
        for(AbstractJsonLoader<?> manager : LOADERS){
            if(manager != CONFIGS){
                manager.loadFromJSON();
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
    public static GenericPreset<IJsonData> importPresetFile(File file, EnumJsonType targetType){
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
    public static GenericPreset<IJsonData> importPresetFile(InputStream stream, EnumJsonType targetType){
        GenericPreset<IJsonData> preset = importJsonFile(stream, GenericPreset.class);
        if(preset != null && (targetType == null || targetType == preset.presetType)){
            getManagerForType(preset.presetType).tryRegisterPreset(preset);
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
        container.jsonMap.forEach(preset -> getManagerForType(preset.presetType).registerPreset(preset));
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