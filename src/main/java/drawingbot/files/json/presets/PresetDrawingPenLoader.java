package drawingbot.files.json.presets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;

public class PresetDrawingPenLoader extends AbstractPresetLoader<IDrawingPen> {

    public PresetDrawingPenLoader(PresetType presetType) {
        super(IDrawingPen.class, presetType, "user_pen_presets.json");
    }

    public void addDrawingPen(IDrawingPen pen) {
        addPreset(wrapDrawingPen(pen, false));
    }

    public void removeDrawingPen(IDrawingPen pen){
        if(pen.getLinkedPreset() == null){
            DrawingBotV3.logger.severe("Unable to remove drawing pen %s, no linked preset".formatted(pen.getCodeName()));
            return;
        }
        removePreset(pen.getLinkedPreset());
    }

    @Override
    public void addPreset(GenericPreset<IDrawingPen> preset) {
        super.addPreset(preset);
        preset.data.setLinkedPreset(preset); //set transient binding
    }

    @Override
    public void removePreset(GenericPreset<IDrawingPen> preset) {
        super.removePreset(preset);
        preset.data.setLinkedPreset(null); //set transient binding
    }

    @Override
    public DrawingPen createDataInstance(GenericPreset<IDrawingPen> preset) {
        DrawingPen drawingPen = new DrawingPen(preset.getPresetSubType(), preset.getPresetName(), ImageTools.getARGBFromColor(Color.BLACK));
        drawingPen.setLinkedPreset(preset);
        preset.data = drawingPen;
        return drawingPen;
    }

    @Override
    public IDrawingPen duplicateData(Gson gson, GenericPreset<IDrawingPen> preset) {
        //Important: the JSON adapter actually returns instance from the current preset so we must provide a new instance instead
        return new DrawingPen(preset.getData());
    }

    @Override
    public GenericPreset<IDrawingPen> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPresetWithFallback(this, "Copic Original", "100 Black", true);
    }

    @Override
    public GenericPreset<IDrawingPen> getDefaultPresetForSubType(String subType) {
        return super.getDefaultPresetForSubType(subType);
    }

    @Override
    public GenericPreset<IDrawingPen> createNewPreset() {
        return createNewPreset(DBConstants.DRAWING_TYPE_USER, "New Preset", true);
    }

    public IDrawingPen unwrapPreset(GenericPreset<IDrawingPen> preset){
        if(preset != null){
            return preset.data;
        }
        return null;
    }

    public GenericPreset<IDrawingPen> wrapDrawingPen(IDrawingPen pen, boolean userCreated){
        if(pen == null){
            return null;
        }
        if(pen.getLinkedPreset() != null){
            return pen.getLinkedPreset();
        }
        GenericPreset<IDrawingPen> preset = createNewPreset();
        preset.setPresetName(pen.getName());
        preset.setPresetSubType(pen.getType());
        preset.data = new DrawingPen(pen);
        preset.data.setLinkedPreset(preset);
        preset.userCreated = userCreated;
        return preset;
    }

    public IDrawingPen findDrawingPen(String subType, String presetName){
        return unwrapPreset(findPreset(subType, presetName));
    }

    public IDrawingPen findDrawingPen(String presetName){
        return unwrapPreset(findPreset(presetName));
    }

    @Override
    public IDrawingPen fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement element) {
        IDrawingPen pen = super.fromJsonElement(gson, preset, element);

        if(pen instanceof DrawingPen drawingPen){
            //// LEGACY PRESET FIX \\\\
            if(drawingPen.type.equals(DBConstants.PRESET_MISSING_NAME)){
                drawingPen.type = preset.getPresetSubType();
            }
            if(drawingPen.name.equals(DBConstants.PRESET_MISSING_NAME)){
                drawingPen.name = preset.getPresetName();
            }
        }

        return pen;
    }
}
