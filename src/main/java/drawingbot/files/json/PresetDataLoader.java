package drawingbot.files.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.javafx.GenericPreset;

import java.lang.reflect.Type;
import java.util.function.Supplier;

public abstract class PresetDataLoader<MASTER extends AbstractJsonData> {

    public Class<MASTER> masterType;
    public final String key;
    public final int order;

    public PresetDataLoader(Class<MASTER> masterType, String key, int order){
        this.masterType = masterType;
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
    public void load(DBTaskContext context, Gson gson, GenericPreset<MASTER> preset){
        if(!isEnabled()){
            return;
        }
        JsonElement element = preset.data.settings.get(getKey());
        if(element != null){
            loadData(context, gson, element, preset);
        }
    }

    public void loadData(DBTaskContext context, Gson gson, JsonElement element, GenericPreset<MASTER> preset){}

    /**
     * Saves the project
     */
    public void save(DBTaskContext context, Gson gson, GenericPreset<MASTER> preset){
        /* Save the data anyway, so it can be loaded later if the Data Loader is re-enabled
        if(!isEnabled()){
            return;
        }
         */
        JsonElement element = saveData(context, gson, preset);
        if(element != null){
            preset.data.settings.put(getKey(), element);
        }
    }

    public JsonElement saveData(DBTaskContext context, Gson gson, GenericPreset<MASTER> preset){
        return null;
    }

    public static class Preset<SUB extends IJsonData, MASTER extends AbstractJsonData> extends PresetDataLoader<MASTER> {

        public final AbstractPresetLoader<SUB> manager;
        public Type type;

        public Preset(Class<MASTER> masterType, AbstractPresetLoader<SUB> manager, int order) {
            super(masterType, manager.getDefaultManager().getPresetType().id, order);
            this.manager = manager;
            this.type = TypeToken.getParameterized(GenericPreset.class, manager.dataType).getType();
        }

        @Override
        public void loadData(DBTaskContext context, Gson gson, JsonElement element, GenericPreset<MASTER> preset) {
            GenericPreset<SUB> data = gson.fromJson(element, type);
            if(data != null){
                manager.getDefaultManager().applyPreset(context, data, preset.data instanceof PresetProjectSettings);
            }
        }

        @Override
        public JsonElement saveData(DBTaskContext context, Gson gson, GenericPreset<MASTER> preset) {
            GenericPreset<SUB> data = manager.createNewPreset();
            manager.getDefaultManager().updatePreset(context, data, preset.data instanceof PresetProjectSettings);
            return gson.toJsonTree(data, type);
        }
    }

    public static abstract class DataInstance<MASTER extends AbstractJsonData, D> extends PresetDataLoader<MASTER> {

        public Class<D> dataType;
        public Supplier<D> supplier;

        public DataInstance(Class<MASTER> masterType, String key, Class<D> dataType, Supplier<D> supplier, int order) {
            super(masterType, key, order);
            this.dataType = dataType;
            this.supplier = supplier;
        }

        @Override
        public final void loadData(DBTaskContext context, Gson gson, JsonElement element, GenericPreset<MASTER> preset) {
            D data = gson.fromJson(element, dataType);
            loadData(context, data, preset);
        }

        public abstract void loadData(DBTaskContext context, D data, GenericPreset<MASTER> preset);

        @Override
        public final JsonElement saveData(DBTaskContext context, Gson gson, GenericPreset<MASTER> preset) {
            D data = supplier.get();
            saveData(context, data, preset);
            return gson.toJsonTree(data, dataType);
        }

        public abstract void saveData(DBTaskContext context, D data, GenericPreset<MASTER> preset);
    }
}
