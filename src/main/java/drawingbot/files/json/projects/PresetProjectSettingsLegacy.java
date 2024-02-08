package drawingbot.files.json.projects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.drawing.DrawingSet;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.presets.PresetImageFilters;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.EnumRotation;

import java.util.HashMap;
import java.util.List;

/**
 * Legacy Project Data
 */
public class PresetProjectSettingsLegacy extends PresetProjectSettings {

    public GenericPreset<PresetData> drawingArea;
    public GenericPreset<PresetImageFilters> imageFilters;
    public GenericPreset<PresetData> pfmSettings;

    public EnumBlendMode blendMode;
    public EnumRotation imageRotation = EnumRotation.R0;
    public boolean imageFlipHorizontal = false;
    public boolean imageFlipVertical = false;

    public boolean optimiseForPrint;
    public float targetPenWidth;

    public GenericPreset<IDrawingSet> drawingSet; //legacy
    public ColorSeparationHandler colourSplitter;
    public EnumDistributionType distributionType;
    public EnumDistributionOrder distributionOrder;

    public float cyanMultiplier = 1F; //TODO
    public float magentaMultiplier = 1F;
    public float yellowMultiplier = 1F;
    public float keyMultiplier = 0.75F;

    public List<PresetProjectSettingsLegacy> projectVersions; //TODO TEST ME

    public int activeDrawingSlot = 0;
    public List<ObservableDrawingSet> drawingSets;

    public JsonObject drawingState;

    public PresetProjectSettingsLegacy(){
        super();
    }

    public PresetProjectSettingsLegacy(HashMap<String, JsonElement> settingList){
        super(settingList);
    }

}
