package drawingbot.javafx.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.files.json.JsonData;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.FXHelper;

import java.util.HashMap;

@JsonData
public class UINodeState {

    public String id = "";
    public HashMap<String, JsonElement> properties = new HashMap<>();

    public UINodeState() {} //for gson

    public UINodeState(String id, Object obj) {
        this.id = id;
        saveState(obj);
    }

    public void saveState(Object node) {
        Gson gson = JsonLoaderManager.createDefaultGson();
        HashMap<String, JsonElement> properties = new HashMap<>();

        for (PropertyAccessorAbstract propertyAccessor : FXHelper.nodePropertyAccessors) {
            if (propertyAccessor.canAccess(node)) {
                properties.put(propertyAccessor.getRegistryKey(), propertyAccessor.readProperty(node, gson));
            }
        }

        this.properties = properties;
    }

    public void loadState(Object node) {
        Gson gson = JsonLoaderManager.createDefaultGson();

        for (PropertyAccessorAbstract propertyAccessor : FXHelper.nodePropertyAccessors) {
            if (properties.containsKey(propertyAccessor.getRegistryKey()) && propertyAccessor.canAccess(node)) {
                propertyAccessor.writeProperty(node, properties.get(propertyAccessor.getRegistryKey()), gson);
            }
        }

    }

    public String getID() {
        return id;
    }
}
