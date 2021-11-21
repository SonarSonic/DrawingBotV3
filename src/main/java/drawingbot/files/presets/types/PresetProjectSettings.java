package drawingbot.files.presets.types;

import com.google.gson.JsonElement;
import drawingbot.DrawingBotV3;
import drawingbot.files.ExportFormats;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.presets.AbstractJsonData;
import drawingbot.files.presets.IJsonData;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.PrintResolution;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.GenericPreset;
import drawingbot.utils.*;
import javafx.application.Platform;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

public class PresetProjectSettings extends AbstractJsonData {

    public String name;
    public String imagePath;
    public String timeStamp;
    public String thumbnailID;

    public GenericPreset<PresetDrawingArea> drawingArea;
    public GenericPreset<PresetImageFilters> imageFilters;
    public GenericPreset<PresetPFMSettings> pfmSettings;
    public GenericPreset<PresetDrawingSet> drawingSet;

    public EnumImageRotate imageRotation = EnumImageRotate.R0;
    public boolean imageFlipHorizontal = false;
    public boolean imageFlipVertical = false;

    public boolean optimiseForPrint;
    public float targetPenWidth;

    public EnumColourSplitter colourSplitter;
    public EnumDistributionType distributionType;
    public EnumDistributionOrder distributionOrder;
    public EnumBlendMode blendMode;

    public float cyanMultiplier = 1F;
    public float magentaMultiplier = 1F;
    public float yellowMultiplier = 1F;
    public float keyMultiplier = 0.75F;

    public PresetProjectSettings(){
        super();
    }

    public PresetProjectSettings(HashMap<String, JsonElement> settingList){
        super(settingList);
    }

    @Override
    public EnumJsonType getJsonType() {
        return EnumJsonType.PROJECT_PRESET;
    }
}
