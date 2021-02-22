package drawingbot.files.presets;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.presets.types.*;
import drawingbot.utils.EnumJsonType;
import drawingbot.utils.GenericPreset;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Function;
import java.util.logging.Level;

public class JsonLoaderManager {

    public static final PresetPFMSettingsLoader PFM = new PresetPFMSettingsLoader();
    public static final PresetImageFiltersLoader FILTERS = new PresetImageFiltersLoader();
    public static final PresetDrawingSetLoader DRAWING_SET = new PresetDrawingSetLoader();
    public static final PresetDrawingPenLoader DRAWING_PENS = new PresetDrawingPenLoader();
    public static final ConfigJsonLoader CONFIGS = new ConfigJsonLoader();
    public static final AbstractJsonLoader<IJsonData>[] LOADERS = new AbstractJsonLoader[]{PFM, FILTERS, DRAWING_SET, DRAWING_PENS, CONFIGS};

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

    public static void loadJSONFiles(){

        //load default presets
        loadDefaultJSON("pre_processing_default.json");
        loadDefaultJSON("pre_processing_original.json");
        loadDefaultJSON("sketch_pfm_glitchy_horizontal.json");
        loadDefaultJSON("sketch_pfm_glitchy_vertical.json");
        loadDefaultJSON("sketch_pfm_messy_lines.json");
        loadDefaultJSON("sketch_pfm_sketchy.json");

        //load user presets
        for(AbstractJsonLoader<?> manager : LOADERS){
            manager.loadFromJSON();
        }

    }

    private static void loadDefaultJSON(String json){
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
    public static void importPresetFile(File file, EnumJsonType targetType){
        try {
            importPresetFile(new FileInputStream(file), targetType);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Gson createDefaultGson(){
        GsonBuilder builder = new GsonBuilder();
        builder.setExclusionStrategies(exclusionStrategy);
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(GenericPreset.class, new JsonAdapterGenericPreset());
        builder.registerTypeAdapter(IDrawingPen.class, (InstanceCreator<IDrawingPen>) type -> new DrawingPen());
        return builder.create();
    }
    /**
     * Imports a single .json from the given input stream
     * @param stream the input stream to load the json from
     * @param targetType the type of preset to import, if null all types will be accepted
     */
    public static void importPresetFile(InputStream stream, EnumJsonType targetType){
        GenericPreset<IJsonData> preset = null;
        try {
            Gson gson = createDefaultGson();
            JsonReader reader = gson.newJsonReader(new InputStreamReader(stream));
            preset = gson.fromJson(reader, GenericPreset.class);
            reader.close();
        } catch (Throwable e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error importing preset file");
        }

        if(preset != null && (targetType == null || targetType == preset.presetType)){
            getManagerForType(preset.presetType).tryRegisterPreset(preset);
        }
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