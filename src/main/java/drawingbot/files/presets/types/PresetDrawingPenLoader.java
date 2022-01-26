package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.files.presets.PresetType;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;
import drawingbot.javafx.GenericPreset;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class PresetDrawingPenLoader extends AbstractPresetLoader<PresetDrawingPen> {

    public PresetDrawingPenLoader(PresetType presetType) {
        super(PresetDrawingPen.class, presetType, "user_pen_presets.json");
    }

    @Override
    public PresetDrawingPen getPresetInstance(GenericPreset<PresetDrawingPen> preset) {
        return new PresetDrawingPen(preset);
    }

    @Override
    public void registerPreset(GenericPreset<PresetDrawingPen> preset) {
        super.registerPreset(preset);
        MasterRegistry.INSTANCE.registerDrawingPen(preset.data);
        preset.data.preset = preset; //set transient binding
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetDrawingPen> preset) {
        super.unregisterPreset(preset);
        MasterRegistry.INSTANCE.unregisterDrawingPen(preset.data);
    }

    @Override
    public GenericPreset<PresetDrawingPen> updatePreset(GenericPreset<PresetDrawingPen> preset) {
        IDrawingPen selectedPen = DrawingBotV3.INSTANCE.controller.getSelectedPen();
        if (selectedPen == null) {
            return null; // can't save the preset
        }
        DrawingPen pen = new DrawingPen(selectedPen);
        preset.presetSubType = pen.getType();
        preset.presetName = pen.getName();
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
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "", "", "", true);
    }

    @Override
    public GenericPreset<PresetDrawingPen> createNewPreset() {
        return createNewPreset(DBConstants.DRAWING_TYPE_USER, "New Preset", true);
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (ObservableList<DrawingPen> list : MasterRegistry.INSTANCE.registeredPens.values()) {
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
