package drawingbot.files.presets.types;

import drawingbot.files.presets.IConfigData;
import drawingbot.utils.EnumJsonType;
import drawingbot.javafx.GenericPreset;

public class ConfigApplicationSettings implements IConfigData{

    public boolean isDeveloperMode;

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.CONFIG_SETTINGS;
    }

    @Override
    public GenericPreset<IConfigData> updatePreset(GenericPreset<IConfigData> preset) {
        if(preset.data instanceof ConfigApplicationSettings){
            //TODO?
        }
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<IConfigData> preset) {
        if(preset.data instanceof ConfigApplicationSettings){
            //TODO?
        }
    }
}
