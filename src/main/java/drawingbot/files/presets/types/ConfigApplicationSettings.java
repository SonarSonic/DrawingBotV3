package drawingbot.files.presets.types;

import drawingbot.files.presets.IConfigData;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.utils.EnumJsonType;
import drawingbot.javafx.GenericPreset;
import drawingbot.utils.Units;

public class ConfigApplicationSettings implements IConfigData{

    public boolean isDeveloperMode;
    public int maxTextureSize = -1;

    public boolean pathOptimisationEnabled = true;

    public boolean lineSimplifyEnabled = true;
    public float lineSimplifyTolerance = 0.1F;
    public Units lineSimplifyUnits = Units.MILLIMETRES;

    public boolean lineMergingEnabled = true;
    public float lineMergingTolerance = 0.5F;
    public Units lineMergingUnits = Units.MILLIMETRES;

    public boolean lineFilteringEnabled = true;
    public float lineFilteringTolerance = 0.5F;
    public Units lineFilteringUnits = Units.MILLIMETRES;

    public boolean lineSortingEnabled = true;
    public float lineSortingTolerance = 1F;
    public Units lineSortingUnits = Units.MILLIMETRES;

    public boolean svgLayerRenaming = false;

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.CONFIG_SETTINGS;
    }

    @Override
    public GenericPreset<IConfigData> updatePreset(GenericPreset<IConfigData> preset) {
        /*
        if(preset.data instanceof ConfigApplicationSettings){
            //TODO?
        }
         */
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<IConfigData> preset) {
        /*
        if(preset.data instanceof ConfigApplicationSettings){
            //TODO?
        }
         */
    }

    public void markDirty(){
        JsonLoaderManager.CONFIGS.queueJsonUpdate();
    }
}
