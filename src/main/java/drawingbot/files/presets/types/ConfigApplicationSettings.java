package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.presets.IConfigData;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.utils.EnumJsonType;
import drawingbot.javafx.GenericPreset;
import drawingbot.utils.UnitsLength;
import drawingbot.utils.UnitsTime;

public class ConfigApplicationSettings implements IConfigData {

    public boolean isDeveloperMode;
    public int maxTextureSize = -1;

    ///path optimisation
    public boolean pathOptimisationEnabled = true;

    public boolean lineSimplifyEnabled = true;
    public float lineSimplifyTolerance = 0.1F;
    public UnitsLength lineSimplifyUnits = UnitsLength.MILLIMETRES;

    public boolean lineMergingEnabled = true;
    public float lineMergingTolerance = 0.5F;
    public UnitsLength lineMergingUnits = UnitsLength.MILLIMETRES;

    public boolean lineFilteringEnabled = true;
    public float lineFilteringTolerance = 0.5F;
    public UnitsLength lineFilteringUnits = UnitsLength.MILLIMETRES;

    public boolean lineSortingEnabled = true;
    public float lineSortingTolerance = 1F;
    public UnitsLength lineSortingUnits = UnitsLength.MILLIMETRES;

    ///svg settings
    public boolean svgLayerRenaming = false;

    ////vpype settings
    public String pathToVPypeExecutable = "";
    public String vPypePresetName = "";

    ////animation settings
    public float framesPerSecond = 25F;
    public int duration = 5;
    public UnitsTime durationUnits = UnitsTime.SECONDS;

    public int getFrameCount(){
        return (int)(framesPerSecond * durationUnits.toSeconds(duration));
    }

    public int getGeometriesPerFrame(int count){
        return Math.max(1, count / getFrameCount());
    }

    public long getVerticesPerFrame(long count){
        return Math.max(1, count / getFrameCount());
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.CONFIG_SETTINGS;
    }

    @Override
    public GenericPreset<IConfigData> updatePreset(GenericPreset<IConfigData> preset) {
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<IConfigData> preset) {}

    public void markDirty(){
        JsonLoaderManager.CONFIGS.queueJsonUpdate();
    }
}
