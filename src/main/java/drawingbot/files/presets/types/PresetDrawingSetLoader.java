package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingRegistry;
import drawingbot.files.presets.AbstractJsonLoader;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.utils.EnumJsonType;
import drawingbot.utils.GenericPreset;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class PresetDrawingSetLoader extends AbstractPresetLoader<PresetDrawingSet> {

    public PresetDrawingSetLoader() {
        super(PresetDrawingSet.class, EnumJsonType.DRAWING_SET,"user_set_presets.json");
    }

    @Override
    public PresetDrawingSet getPresetInstance(GenericPreset<PresetDrawingSet> preset) {
        return new PresetDrawingSet(preset.presetSubType, preset.presetName, new ArrayList<>(), preset);
    }

    @Override
    public void registerPreset(GenericPreset<PresetDrawingSet> preset) {
        DrawingRegistry.INSTANCE.registerDrawingSet(preset.data);
        preset.data.preset = preset; //set transient binding
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetDrawingSet> preset) {
        DrawingRegistry.INSTANCE.unregisterDrawingSet(preset.data);
    }

    @Override
    public GenericPreset<PresetDrawingSet> updatePreset(GenericPreset<PresetDrawingSet> preset) {
        preset.data.pens.clear();
        DrawingBotV3.observableDrawingSet.getPens().forEach(p -> preset.data.pens.add(new DrawingPen(p)));
        return preset;
    }

    @Override
    public void applyPreset(GenericPreset<PresetDrawingSet> preset) {
        //nothing to apply
    }

    @Override
    public void onPresetEdited(GenericPreset<PresetDrawingSet> preset) {
        super.onPresetEdited(preset);
        preset.data.type = preset.presetSubType;
        preset.data.name = preset.presetName;
    }

    @Override
    public GenericPreset<PresetDrawingSet> getDefaultPreset() {
        return null;
    }

    @Override
    public GenericPreset<PresetDrawingSet> createNewPreset() {
        return createNewPreset(DrawingRegistry.userType, "New Preset", true);
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (ObservableList<IDrawingSet<IDrawingPen>> list : DrawingRegistry.INSTANCE.registeredSets.values()) {
            for (IDrawingSet<IDrawingPen> set : list) {
                if (set instanceof PresetDrawingSet) {
                    PresetDrawingSet userSet = (PresetDrawingSet) set;
                    userCreated.add(userSet.preset);
                }
            }
        }
        return userCreated;
    }

}
