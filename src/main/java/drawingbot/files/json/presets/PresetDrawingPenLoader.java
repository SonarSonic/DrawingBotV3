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
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class PresetDrawingPenLoader extends AbstractPresetLoader<DrawingPen> {

    public PresetDrawingPenLoader(PresetType presetType) {
        super(DrawingPen.class, presetType, "user_pen_presets.json");
        setDefaultManager(new PresetDrawingPenManager(this) {
            @Override
            public IDrawingPen getSelectedDrawingPen() {
                //TODO CHANGE ME???
                return DrawingBotV3.INSTANCE.controller.drawingSetsController.getSelectedPen();
            }
        });
    }

    @Override
    public DrawingPen getPresetInstance(GenericPreset<DrawingPen> preset) {
        DrawingPen drawingPen = new DrawingPen();
        drawingPen.preset = preset;
        drawingPen.name = preset.getPresetName();
        drawingPen.type = preset.getPresetSubType();
        preset.data = drawingPen;
        return drawingPen;
    }

    @Override
    public void registerPreset(GenericPreset<DrawingPen> preset) {
        super.registerPreset(preset);
        MasterRegistry.INSTANCE.registerDrawingPen(preset.data);
        preset.data.preset = preset; //set transient binding
    }

    @Override
    public void unregisterPreset(GenericPreset<DrawingPen> preset) {
        super.unregisterPreset(preset);
        MasterRegistry.INSTANCE.unregisterDrawingPen(preset.data);
    }

    @Override
    public void onPresetEdited(GenericPreset<DrawingPen> preset) {
        super.onPresetEdited(preset);
        preset.data.type = preset.getPresetSubType();
        preset.data.name = preset.getPresetName();
    }

    @Override
    public GenericPreset<DrawingPen> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "", "", "", true);
    }

    @Override
    public GenericPreset<DrawingPen> createNewPreset() {
        return createNewPreset(DBConstants.DRAWING_TYPE_USER, "New Preset", true);
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (ObservableList<DrawingPen> list : MasterRegistry.INSTANCE.registeredPens.values()) {
            for (IDrawingPen pen : list) {
                if (pen.isUserCreated() && pen instanceof DrawingPen) {
                    DrawingPen userSet = (DrawingPen) pen;
                    userCreated.add(userSet.preset);
                }
            }
        }
        return userCreated;
    }

    @Override
    public DrawingPen fromJsonElement(Gson gson, GenericPreset<?> preset, JsonElement element) {
        DrawingPen pen = super.fromJsonElement(gson, preset, element);

        //// LEGACY PRESET FIX \\\\
        if(pen.type.equals(DBConstants.PRESET_MISSING_NAME)){
            pen.type = preset.getPresetSubType();
        }
        if(pen.name.equals(DBConstants.PRESET_MISSING_NAME)){
            pen.name = preset.getPresetName();
        }

        return pen;
    }
}
