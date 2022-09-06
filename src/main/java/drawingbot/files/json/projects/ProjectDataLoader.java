package drawingbot.files.json.projects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.IJsonData;
import drawingbot.javafx.GenericPreset;

import java.lang.reflect.Type;
import java.util.function.Supplier;

public abstract class ProjectDataLoader {

    public final String key;
    public final int order;

    public ProjectDataLoader(String key, int order){
        this.key = key;
        this.order = order;
    }

    /**
     * @return a protected key which the data loader can use to load data from the PresetData, must be unique.
     */
    public final String getKey(){
        return key;
    }

    /**
     * @return if this data loader is enabled
     */
    public boolean isEnabled(){
        return true;
    }

    /**
     * Loads the project
     */
    public void load(DBTaskContext context, Gson gson, GenericPreset<PresetProjectSettings> preset){
        if(!isEnabled()){
            return;
        }
        JsonElement element = preset.data.settings.get(getKey());
        if(element != null){
            loadData(context, gson, element, preset);
        }
    }

    public void loadData(DBTaskContext context, Gson gson, JsonElement element, GenericPreset<PresetProjectSettings> preset){}

    /**
     * Saves the project
     */
    public void save(DBTaskContext context, Gson gson, GenericPreset<PresetProjectSettings> preset){
        if(!isEnabled()){
            return;
        }
        JsonElement element = saveData(context, gson, preset);
        if(element != null){
            preset.data.settings.put(getKey(), element);
        }
    }

    public JsonElement saveData(DBTaskContext context, Gson gson, GenericPreset<PresetProjectSettings> preset){
        return null;
    }

    public static class Preset<O extends IJsonData> extends ProjectDataLoader{

        public final AbstractPresetLoader<O> manager;
        public Type type;

        public Preset(AbstractPresetLoader<O> manager, int order) {
            super(manager.getDefaultManager().getPresetType().id, order);
            this.manager = manager;
            this.type = TypeToken.getParameterized(GenericPreset.class, manager.dataType).getType();
        }

        @Override
        public void loadData(DBTaskContext context, Gson gson, JsonElement element, GenericPreset<PresetProjectSettings> preset) {
            GenericPreset<O> data = gson.fromJson(element, type);
            if(data != null){
                manager.getDefaultManager().applyPreset(context, data);
            }
        }

        @Override
        public JsonElement saveData(DBTaskContext context, Gson gson, GenericPreset<PresetProjectSettings> preset) {
            GenericPreset<O> data = manager.createNewPreset();
            manager.getDefaultManager().updatePreset(context, data);
            return gson.toJsonTree(data, type);
        }
    }

    public static abstract class DataInstance<D> extends ProjectDataLoader{

        public Class<D> dataType;
        public Supplier<D> supplier;

        public DataInstance(String key, Class<D> dataType, Supplier<D> supplier, int order) {
            super(key, order);
            this.dataType = dataType;
            this.supplier = supplier;
        }

        @Override
        public final void loadData(DBTaskContext context, Gson gson, JsonElement element, GenericPreset<PresetProjectSettings> preset) {
            D data = gson.fromJson(element, dataType);
            loadData(context, data, preset);
        }

        public abstract void loadData(DBTaskContext context, D data, GenericPreset<PresetProjectSettings> preset);

        @Override
        public final JsonElement saveData(DBTaskContext context, Gson gson, GenericPreset<PresetProjectSettings> preset) {
            D data = supplier.get();
            saveData(context, data, preset);
            return gson.toJsonTree(data, dataType);
        }

        public abstract void saveData(DBTaskContext context, D data, GenericPreset<PresetProjectSettings> preset);
    }
}
