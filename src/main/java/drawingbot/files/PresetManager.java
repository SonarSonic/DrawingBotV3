package drawingbot.files;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingRegistry;
import drawingbot.drawing.UserDrawingPen;
import drawingbot.drawing.UserDrawingSet;
import drawingbot.image.ImageFilterRegistry;
import drawingbot.pfm.PFMMasterRegistry;
import drawingbot.utils.EnumPresetType;
import drawingbot.utils.GenericFactory;
import drawingbot.utils.GenericPreset;
import drawingbot.utils.GenericSetting;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public abstract class PresetManager {

    public static final PFMPresetManager PFM = new PFMPresetManager();
    public static final FilterPresetManager FILTERS = new FilterPresetManager();
    public static final DrawingSetPresetManager DRAWING_SET = new DrawingSetPresetManager();
    public static final DrawingPenPresetManager DRAWING_PENS = new DrawingPenPresetManager();
    public static final AbstractManager[] MANAGERS = new AbstractManager[]{PFM, FILTERS, DRAWING_SET, DRAWING_PENS};

    /** used to prevent certain values from being serialized */
    public static final ExclusionStrategy exclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(GsonExclude.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static AbstractManager getManagerForType(EnumPresetType type) {
        for(AbstractManager manager : MANAGERS){
            if(manager.type == type){
                return manager;
            }
        }
        return null;
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
        for(AbstractManager manager : MANAGERS){
            UserCreatedPresets presets = ConfigFileHandler.getOrCreateJSONFile(UserCreatedPresets.class, manager.configFile, c -> new UserCreatedPresets());
            presets.presetMap.forEach(manager::registerPreset);
        }

        for(AbstractManager manager : MANAGERS){
            manager.onJSONLoaded();
        }

    }

    public static void loadDefaultJSON(String json){
        InputStream stream = PresetManager.class.getResourceAsStream("/presets/" + json);
        if(stream != null)
            importPresetFile(stream, null);
    }

    public static void importPresetFile(File file, EnumPresetType targetType){
        try {
            importPresetFile(new FileInputStream(file), targetType);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void importPresetFile(InputStream stream, EnumPresetType targetType){
        GenericPreset preset = null;
        try {
            Gson gson = new GsonBuilder().setExclusionStrategies(exclusionStrategy).create();
            JsonReader reader = gson.newJsonReader(new InputStreamReader(stream));
            preset = gson.fromJson(reader, GenericPreset.class);
            reader.close();
        } catch (IOException e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error importing preset file");
        }

        if(preset != null && (targetType == null || targetType == preset.presetType)){
            getManagerForType(preset.presetType).registerPreset(preset);
        }
    }

    public static void exportPresetFile(File file, GenericPreset selected){
        try {
            Gson gson = new GsonBuilder().setExclusionStrategies(exclusionStrategy).create();
            JsonWriter writer = gson.newJsonWriter(new FileWriter(file));
            gson.toJson(selected, GenericPreset.class, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error exporting preset file");
        }
    }

    public static void updatePresetJSON(PresetManager.AbstractManager manager){
        try {
            Gson gson = new GsonBuilder().setExclusionStrategies(exclusionStrategy).create();
            UserCreatedPresets userPFMPresets = new UserCreatedPresets(manager.getUserCreatedPresets());
            JsonWriter writer = gson.newJsonWriter(new FileWriter(manager.configFile));
            gson.toJson(userPFMPresets, UserCreatedPresets.class, writer);
            writer.flush();
            writer.close();
        }catch (Exception e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error updating preset json");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static abstract class AbstractManager<O> {

        public final EnumPresetType type;
        public final File configFile;

        public AbstractManager(EnumPresetType type, File configFile) {
            this.type = type;
            this.configFile = configFile;
        }

        public GenericPreset createNewPreset(String presetSubType, String presetName, boolean userCreated){
            return new GenericPreset(type, presetSubType, presetName, userCreated, new JsonObject());
        }

        public void onJSONLoaded(){}

        public void updateJSON(){
            DrawingBotV3.backgroundService.submit(() -> updatePresetJSON(this));
        }

        public abstract void registerPreset(GenericPreset preset);

        public abstract void unregisterPreset(GenericPreset preset);

        /**saves the current settings to the preset, required by update preset, may be used as needed
         * @return the preset or null if the preset couldn't be saved*/
        public abstract GenericPreset saveSettingsToPreset(GenericPreset preset);

        /**loads the settings from the preset, may be used as needed*/
        public abstract void loadSettingsFromPreset(GenericPreset preset);

        public void onPresetRenamed(GenericPreset preset){
            updateJSON();
        }

        public abstract GenericPreset getDefaultPreset();

        public void savePreset(GenericPreset preset){
            registerPreset(preset);
            if(preset.userCreated){
                updateJSON();
            }
        }

        public boolean deletePreset(GenericPreset preset){
            if(preset != null && preset.userCreated){
                unregisterPreset(preset);
                updateJSON();
                return true;
            }
            return false;
        }

        public GenericPreset updatePreset(GenericPreset preset) {
            if(preset != null && preset.userCreated){
                preset = saveSettingsToPreset(preset);
                if(preset != null){
                    updateJSON();
                    return preset;
                }
            }
            return null;
        }

        public abstract List<GenericPreset> getUserCreatedPresets();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class FilterPresetManager extends AbstractManager{

        public FilterPresetManager() {
            super(EnumPresetType.IMAGE_FILTER_PRESET, new File(FileUtils.getUserDataDirectory(),"user_presets_img.json"));
        }

        @Override
        public void onJSONLoaded() {
            super.onJSONLoaded();
            loadSettingsFromPreset(ImageFilterRegistry.getDefaultImageFilterPreset());
        }

        @Override
        public void registerPreset(GenericPreset preset) {
            ImageFilterRegistry.registerPreset(preset);
        }

        @Override
        public void unregisterPreset(GenericPreset preset) {
            ImageFilterRegistry.imagePresets.remove(preset);
        }

        @Override
        public GenericPreset saveSettingsToPreset(GenericPreset preset) {
            JsonObject object = new JsonObject();
            JsonArray filters = new JsonArray();
            JsonArray settings = new JsonArray();

            for(ImageFilterRegistry.ObservableImageFilter filter : ImageFilterRegistry.currentFilters){
                filters.add(filter.filterFactory.getName());
                settings.add(GenericSetting.writeJsonObject(filter.filterSettings));
            }

            object.add("filters", filters);
            object.add("settings", settings);
            preset.jsonObject = object;
            return preset;
        }

        @Override
        public void loadSettingsFromPreset(GenericPreset preset) {
            ImageFilterRegistry.currentFilters.clear();

            if(!preset.jsonObject.has("filters")){
                return;
            }

            JsonArray filters = preset.jsonObject.getAsJsonArray("filters");
            JsonArray settings = preset.jsonObject.getAsJsonArray("settings");

            for(int i = 0; i < filters.size(); i ++){
                String filterName = filters.get(i).getAsString();
                JsonObject filterSettings = settings.get(i).getAsJsonObject();
                GenericFactory<ImageFilterRegistry.IImageFilter> filterFactory = ImageFilterRegistry.getFilterFromName(filterName);
                ImageFilterRegistry.ObservableImageFilter filter = new ImageFilterRegistry.ObservableImageFilter(filterFactory);
                GenericSetting.readJsonObject(filterSettings, filter.filterSettings);
                ImageFilterRegistry.currentFilters.add(filter);
            }
        }

        @Override
        public GenericPreset getDefaultPreset() {
            return ImageFilterRegistry.getDefaultImageFilterPreset();
        }

        @Override
        public List<GenericPreset> getUserCreatedPresets() {
            List<GenericPreset> userCreated = new ArrayList<>();
            for(GenericPreset preset : ImageFilterRegistry.imagePresets){
                if(preset.userCreated){
                    userCreated.add(preset);
                }
            }
            return userCreated;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class PFMPresetManager extends AbstractManager {

        public PFMPresetManager() {
            super(EnumPresetType.PFM_PRESET, new File(FileUtils.getUserDataDirectory(),"user_presets_pfm.json"));
        }

        @Override
        public void registerPreset(GenericPreset preset) {
            PFMMasterRegistry.registerPreset(preset);
        }

        @Override
        public void unregisterPreset(GenericPreset preset) {
            PFMMasterRegistry.pfmPresets.get(preset.presetSubType).remove(preset);
        }

        @Override
        public GenericPreset saveSettingsToPreset(GenericPreset preset){
            preset.jsonObject = GenericSetting.writeJsonObject(PFMMasterRegistry.getObservablePFMSettingsList());
            return preset;
        }

        @Override
        public void loadSettingsFromPreset(GenericPreset preset){
            GenericSetting.readJsonObject(preset.jsonObject, PFMMasterRegistry.getObservablePFMSettingsList());
        }

        @Override
        public GenericPreset getDefaultPreset() {
            return PFMMasterRegistry.getDefaultPFMPreset();
        }

        @Override
        public List<GenericPreset> getUserCreatedPresets() {
            List<GenericPreset> userCreated = new ArrayList<>();
            for(Map.Entry<String, ObservableList<GenericPreset>> entry : PFMMasterRegistry.pfmPresets.entrySet()){
                for(GenericPreset preset : entry.getValue()){
                    if(preset.userCreated){
                        userCreated.add(preset);
                    }
                }
            }
            return userCreated;
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class DrawingSetPresetManager extends AbstractManager {

        public DrawingSetPresetManager() {
            super(EnumPresetType.DRAWING_SET, new File(FileUtils.getUserDataDirectory(),"user_presets_sets.json"));
        }

        public GenericPreset createNewPreset(String presetSubType, String presetName, boolean userCreated){
            GenericPreset preset = super.createNewPreset(presetSubType, presetName, userCreated);
            preset.object = new UserDrawingSet(presetSubType, presetName, new ArrayList<>(), preset);
            return preset;
        }

        @Override
        public void registerPreset(GenericPreset preset) {
            if(preset.object == null){
                loadSettingsFromPreset(preset);
            }
            if(preset.object instanceof UserDrawingSet){
                UserDrawingSet set = (UserDrawingSet)preset.object;
                DrawingRegistry.INSTANCE.registerDrawingSet(set);
            }
        }

        @Override
        public void unregisterPreset(GenericPreset preset) {
            if(preset.object instanceof UserDrawingSet){
                DrawingRegistry.INSTANCE.registeredSets.get(DrawingRegistry.userType).remove((UserDrawingSet)preset.object);
            }
        }

        @Override
        public GenericPreset saveSettingsToPreset(GenericPreset preset){
            Gson gson = new GsonBuilder().setExclusionStrategies(exclusionStrategy).create();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", DrawingBotV3.observableDrawingSet.getName());

            JsonArray jsonArray = new JsonArray();
            List<IDrawingPen> pens = new ArrayList<>();
            for(IDrawingPen pen : DrawingBotV3.observableDrawingSet.getPens()){
                DrawingPen p = new DrawingPen(pen);
                pens.add(p);
                jsonArray.add(gson.toJsonTree(p));
            }
            jsonObject.add("pens", jsonArray);
            preset.jsonObject = jsonObject;

            UserDrawingSet set = (UserDrawingSet) preset.object;
            set.pens = pens;
            return preset;
        }

        @Override
        public void loadSettingsFromPreset(GenericPreset preset){
            Gson gson = new GsonBuilder().setExclusionStrategies(exclusionStrategy).create();
            String type = DrawingRegistry.userType;
            String name = preset.jsonObject.get("name").getAsString();
            List<IDrawingPen> pens = new ArrayList<>();
            JsonArray array = preset.jsonObject.get("pens").getAsJsonArray();
            for(JsonElement e : array){
                DrawingPen pen = gson.fromJson(e, DrawingPen.class);
                if(pen != null){
                    pens.add(pen);
                }
            }
            preset.object = new UserDrawingSet(type, name, pens, preset);
        }

        @Override
        public void onPresetRenamed(GenericPreset preset) {
            super.onPresetRenamed(preset);
            preset.jsonObject.addProperty("name", preset.presetName);
            if(preset.object instanceof UserDrawingSet){
                ((UserDrawingSet) preset.object).name = preset.presetName;
            }
        }

        @Override
        public GenericPreset getDefaultPreset() {
            return null;
        }

        @Override
        public List<GenericPreset> getUserCreatedPresets() {
            List<GenericPreset> userCreated = new ArrayList<>();
            for(ObservableList<IDrawingSet<IDrawingPen>> list : DrawingRegistry.INSTANCE.registeredSets.values()){
                for(IDrawingSet<IDrawingPen> set : list){
                    if(set instanceof UserDrawingSet){
                        UserDrawingSet userSet = (UserDrawingSet) set;
                        userCreated.add(userSet.preset);
                    }
                }
            }
            return userCreated;
        }

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class DrawingPenPresetManager extends AbstractManager {

        public DrawingPenPresetManager() {
            super(EnumPresetType.DRAWING_PEN, new File(FileUtils.getUserDataDirectory(),"user_presets_pens.json"));
        }

        public GenericPreset createNewPreset(IDrawingPen pen, boolean userCreated){
            if(pen == null){
                return null;
            }
            GenericPreset preset = super.createNewPreset(DrawingRegistry.userType, pen.getName(), userCreated);
            preset.object = new UserDrawingPen(pen, preset);
            return preset;
        }

        @Override
        public void registerPreset(GenericPreset preset) {
            if(preset.object == null){
                loadSettingsFromPreset(preset);
            }
            if(preset.object instanceof UserDrawingPen){
                UserDrawingPen pen = (UserDrawingPen)preset.object;
                DrawingRegistry.INSTANCE.registerDrawingPen(pen);
            }
        }

        @Override
        public void unregisterPreset(GenericPreset preset) {
            if(preset.object instanceof UserDrawingPen){
                DrawingRegistry.INSTANCE.registeredPens.get(DrawingRegistry.userType).remove((UserDrawingPen)preset.object);
            }
        }

        @Override
        public GenericPreset saveSettingsToPreset(GenericPreset preset){
            IDrawingPen selectedPen = DrawingBotV3.controller.getSelectedPen();
            if(selectedPen == null){
                return null; // can't save the preset
            }
            DrawingPen pen = new DrawingPen(selectedPen);
            pen.type = DrawingRegistry.userType;
            Gson gson = new GsonBuilder().setExclusionStrategies(exclusionStrategy).create();
            preset.jsonObject = gson.toJsonTree(pen).getAsJsonObject();

            UserDrawingPen set = (UserDrawingPen) preset.object;
            set.update(pen);
            return preset;
        }

        @Override
        public void loadSettingsFromPreset(GenericPreset preset){
            Gson gson = new GsonBuilder().setExclusionStrategies(exclusionStrategy).create();
            DrawingPen pen = gson.fromJson(preset.jsonObject, DrawingPen.class);
            preset.object = new UserDrawingPen(pen, preset);
        }

        @Override
        public void onPresetRenamed(GenericPreset preset) {
            super.onPresetRenamed(preset);
            if(preset.object instanceof UserDrawingPen){
                ((UserDrawingPen) preset.object).name = preset.presetName;
            }
        }

        @Override
        public GenericPreset getDefaultPreset() {
            return null;
        }

        @Override
        public List<GenericPreset> getUserCreatedPresets() {
            List<GenericPreset> userCreated = new ArrayList<>();
            for(ObservableList<IDrawingPen> list : DrawingRegistry.INSTANCE.registeredPens.values()){
                for(IDrawingPen pen : list){
                    if(pen instanceof UserDrawingPen){
                        UserDrawingPen userSet = (UserDrawingPen) pen;
                        userCreated.add(userSet.preset);
                    }
                }
            }
            return userCreated;
        }

    }


    /**
     * Used as an object to be saved by GSON
     */
    public static class UserCreatedPresets {
        public List<GenericPreset> presetMap;

        public UserCreatedPresets(){
            presetMap = new ArrayList<>();
        }

        public UserCreatedPresets(List<GenericPreset> presetMap) {
            this.presetMap = presetMap;
        }
    }
}
