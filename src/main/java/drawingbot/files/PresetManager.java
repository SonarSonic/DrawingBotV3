package drawingbot.files;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import drawingbot.DrawingBotV3;
import drawingbot.image.ImageFilterRegistry;
import drawingbot.pfm.PFMMasterRegistry;
import drawingbot.utils.EnumPresetType;
import drawingbot.utils.GenericFactory;
import drawingbot.utils.GenericPreset;
import drawingbot.utils.GenericSetting;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public abstract class PresetManager {

    public static final PFMPresetManager PFM = new PFMPresetManager();
    public static final FilterPresetManager FILTERS = new FilterPresetManager();
    public static final AbstractManager[] MANAGERS = new AbstractManager[]{PFM, FILTERS};

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
        try {
            URL url = PresetManager.class.getResource("/presets/");
            File[] files = new File(url.toURI()).listFiles();
            for(File file : files){
                importPresetFile(file, null);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //load user presets
        for(AbstractManager manager : MANAGERS){
            UserCreatedPresets presets = ConfigFileHandler.getOrCreateJSONFile(UserCreatedPresets.class, manager.configFile, c -> new UserCreatedPresets());
            presets.presetMap.forEach(manager::registerPreset);
        }

        for(AbstractManager manager : MANAGERS){
            manager.onJSONLoaded();
        }

    }

    public static void importPresetFile(File file, EnumPresetType targetType){
        GenericPreset preset = null;
        try {
            Gson gson = new Gson();
            JsonReader reader = gson.newJsonReader(new FileReader(file));
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
            Gson gson = new Gson();
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
            Gson gson = new Gson();
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

    public static abstract class AbstractManager {

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
            DrawingBotV3.INSTANCE.backgroundService.submit(() -> updatePresetJSON(this));
        }

        public abstract void registerPreset(GenericPreset preset);

        public abstract void unregisterPreset(GenericPreset preset);

        public abstract void saveSettings(GenericPreset preset);

        public abstract void loadSettings(GenericPreset preset);


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
                saveSettings(preset);
                updateJSON();
                return preset;
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
            loadSettings(ImageFilterRegistry.getDefaultImageFilterPreset());
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
        public void saveSettings(GenericPreset preset) {
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
        }

        @Override
        public void loadSettings(GenericPreset preset) {
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
        public void saveSettings(GenericPreset preset){
            preset.jsonObject = GenericSetting.writeJsonObject(PFMMasterRegistry.getObservablePFMSettingsList());
        }

        @Override
        public void loadSettings(GenericPreset preset){
            GenericSetting.readJsonObject(preset.jsonObject, PFMMasterRegistry.getObservablePFMSettingsList());
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
