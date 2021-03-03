package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingRegistry;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.utils.EnumJsonType;
import drawingbot.javafx.GenericPreset;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class PresetDrawingPenLoader extends AbstractPresetLoader<PresetDrawingPen> {

    public PresetDrawingPenLoader() {
        super(PresetDrawingPen.class, EnumJsonType.DRAWING_PEN, "user_pen_presets.json");
    }

    @Override
    public PresetDrawingPen getPresetInstance(GenericPreset<PresetDrawingPen> preset) {
        return new PresetDrawingPen(preset);
    }

    @Override
    public void registerPreset(GenericPreset<PresetDrawingPen> preset) {
        DrawingRegistry.INSTANCE.registerDrawingPen(preset.data);
        preset.data.preset = preset; //set transient binding
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetDrawingPen> preset) {
        DrawingRegistry.INSTANCE.unregisterDrawingPen(preset.data);
    }

    @Override
    public GenericPreset<PresetDrawingPen> updatePreset(GenericPreset<PresetDrawingPen> preset) {
        IDrawingPen selectedPen = DrawingBotV3.INSTANCE.controller.getSelectedPen();
        if (selectedPen == null) {
            return null; // can't save the preset
        }
        DrawingPen pen = new DrawingPen(selectedPen);
        preset.presetName = pen.getDisplayName();
        //preset.presetSubType = DrawingRegistry.userType;
        preset.data.update(pen);
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetDrawingPen> preset) {
        ///nothing to apply
    }

    @Override
    public void onPresetEdited(GenericPreset<PresetDrawingPen> preset) {
        super.onPresetEdited(preset);
        preset.data.type = preset.presetSubType;
        preset.data.name = preset.presetName;
    }

    @Override
    public GenericPreset<PresetDrawingPen> getDefaultPreset() {
        return null;
    }

    @Override
    public GenericPreset<PresetDrawingPen> createNewPreset() {
        return createNewPreset(DrawingRegistry.userType, "New Preset", true);
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (ObservableList<DrawingPen> list : DrawingRegistry.INSTANCE.registeredPens.values()) {
            for (IDrawingPen pen : list) {
                if (pen instanceof PresetDrawingPen) {
                    PresetDrawingPen userSet = (PresetDrawingPen) pen;
                    userCreated.add(userSet.preset);
                }
            }
        }
        return userCreated;
    }
}
