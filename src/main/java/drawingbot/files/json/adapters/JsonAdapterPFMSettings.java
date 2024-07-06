package drawingbot.files.json.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import drawingbot.DrawingBotV3;
import drawingbot.files.json.GsonHelper;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMSettings;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;

import java.lang.reflect.Type;
import java.util.HashMap;

public class JsonAdapterPFMSettings implements JsonSerializer<PFMSettings>, JsonDeserializer<PFMSettings> {

    public static final Type settingsMap = TypeToken.getParameterized(HashMap.class, String.class, JsonElement.class).getType();

    @Override
    public JsonElement serialize(PFMSettings src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("pfmFactory", context.serialize(src.getPFMFactory(), PFMFactory.class));
        if(src.getSelectedPreset() != null){
            jsonObject.addProperty("selectedPreset", src.getSelectedPreset().getPresetID());
        }
        jsonObject.add("pfmSettings", context.serialize(GenericSetting.toJsonMap(src.getSettings(), new HashMap<>(), false), settingsMap));
        return jsonObject;
    }

    @Override
    public PFMSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        PFMSettings pfmSettings = new PFMSettings();

        //Legacy: old PFMSettings were just saved as a PFM Preset
        if(jsonObject.has(JsonAdapterGenericPreset.PRESET_TYPE) && jsonObject.has(JsonAdapterGenericPreset.PRESET_NAME)){
            GenericPreset<PresetData> pfmPreset = context.deserialize(jsonObject, GenericPreset.class);
            Register.PRESET_MANAGER_PFM.applyPreset(DrawingBotV3.context(), pfmSettings, pfmPreset, false);
            pfmSettings.setSelectedPreset(pfmPreset);
            return pfmSettings;
        }
        
        pfmSettings.setPFMFactory(GsonHelper.getObjectOrDefault(jsonObject, context, "pfmFactory", PFMFactory.class, MasterRegistry.INSTANCE.getDefaultPFM()));

        GenericPreset<PresetData> selectedPreset = null;
        if(jsonObject.has("selectedPreset")){
            selectedPreset = Register.PRESET_LOADER_PFM.findPresetFromID(jsonObject.get("selectedPreset").getAsString());
        }
        pfmSettings.setSelectedPreset(selectedPreset);

        HashMap<String, JsonElement> savedSettings = context.deserialize(jsonObject.getAsJsonObject("pfmSettings"), settingsMap);
        GenericSetting.applySettings(savedSettings, pfmSettings.getSettings());

        return pfmSettings;
    }

}
