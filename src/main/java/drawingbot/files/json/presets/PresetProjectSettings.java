package drawingbot.files.json.presets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.EnumRotation;
import drawingbot.drawing.ColourSeperationHandler;
import drawingbot.files.json.AbstractJsonData;
import drawingbot.files.json.PresetType;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.Register;

import java.util.HashMap;
import java.util.List;

public class PresetProjectSettings extends AbstractJsonData {

    public String name;
    public String imagePath;
    public String timeStamp;
    public String thumbnailID;

    public GenericPreset<PresetDrawingArea> drawingArea;
    public GenericPreset<PresetImageFilters> imageFilters;
    public GenericPreset<PresetPFMSettings> pfmSettings;

    public EnumBlendMode blendMode;
    public EnumRotation imageRotation = EnumRotation.R0;
    public boolean imageFlipHorizontal = false;
    public boolean imageFlipVertical = false;

    public boolean optimiseForPrint;
    public float targetPenWidth;

    public GenericPreset<PresetDrawingSet> drawingSet; //legacy
    public ColourSeperationHandler colourSplitter;
    public EnumDistributionType distributionType;
    public EnumDistributionOrder distributionOrder;

    public float cyanMultiplier = 1F;
    public float magentaMultiplier = 1F;
    public float yellowMultiplier = 1F;
    public float keyMultiplier = 0.75F;

    public List<PresetProjectSettings> projectVersions;

    public int activeDrawingSlot = 0;
    public List<ObservableDrawingSet> drawingSets;

    public JsonObject drawingState;

    public transient boolean isSubProject = false;

    public PresetProjectSettings(){
        super();
    }

    public PresetProjectSettings(HashMap<String, JsonElement> settingList){
        super(settingList);
    }

    @Override
    public PresetType getPresetType() {
        return Register.PRESET_TYPE_PROJECT;
    }
}
