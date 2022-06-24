package drawingbot.files.json.presets;

import drawingbot.files.json.PresetType;
import drawingbot.files.json.IConfigData;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.Register;
import drawingbot.utils.UnitsLength;
import drawingbot.utils.UnitsTime;

import java.util.HashMap;
import java.util.Map;

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

    public boolean lineFilteringEnabled = false;
    public float lineFilteringTolerance = 0.5F;
    public UnitsLength lineFilteringUnits = UnitsLength.MILLIMETRES;

    public boolean lineSortingEnabled = true;
    public float lineSortingTolerance = 1F;
    public UnitsLength lineSortingUnits = UnitsLength.MILLIMETRES;

    ///svg settings
    public String svgLayerNaming = "%NAME%";
    public boolean exportSVGBackground = false;

    ////vpype settings
    public String pathToVPypeExecutable = "";
    public String vPypePresetName = "";

    ////image settings
    public float exportDPI = 300F;

    ////animation settings
    public float framesPerSecond = 25F;
    public int duration = 5;
    public int frameHoldStart = 1;
    public int frameHoldEnd = 1;
    public UnitsTime durationUnits = UnitsTime.SECONDS;

    ////preset settings
    public Map<String, String> defaultPresets = new HashMap<>();

    public boolean disableOpenGLRenderer = false;
    public boolean darkTheme = false;

    public int getFrameCount(){
        return (int)(framesPerSecond * durationUnits.toSeconds(duration));
    }

    public int getFrameHoldStartCount(){
        return (int)(framesPerSecond * durationUnits.toSeconds(frameHoldStart));
    }

    public int getFrameHoldEndCount(){
        return (int)(framesPerSecond * durationUnits.toSeconds(frameHoldEnd));
    }

    public int getGeometriesPerFrame(int count){
        return Math.max(1, count / getFrameCount());
    }

    public long getVerticesPerFrame(long count){
        return Math.max(1, count / getFrameCount());
    }


    @Override
    public PresetType getPresetType() {
        return Register.PRESET_TYPE_CONFIGS;
    }

    @Override
    public GenericPreset<IConfigData> updatePreset(GenericPreset<IConfigData> preset) {
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<IConfigData> preset) {}

    public void markDirty(){
        Register.PRESET_LOADER_CONFIGS.queueJsonUpdate();
    }
}
