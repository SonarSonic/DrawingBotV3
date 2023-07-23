package drawingbot.files.json.presets;

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

public class PresetDrawingPenLoader extends AbstractPresetLoader<PresetDrawingPen> {

    public PresetDrawingPenLoader(PresetType presetType) {
        super(PresetDrawingPen.class, presetType, "user_pen_presets.json");
        setDefaultManager(new PresetDrawingPenManager(this) {
            @Override
            public IDrawingPen getSelectedDrawingPen() {
                //TODO CHANGE ME???
                return DrawingBotV3.INSTANCE.controller.drawingSetsController.getSelectedPen();
            }
        });
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
    public void onPresetEdited(GenericPreset<PresetDrawingPen> preset) {
        super.onPresetEdited(preset);
        preset.data.type = preset.getPresetSubType();
        preset.data.name = preset.getPresetName();
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
