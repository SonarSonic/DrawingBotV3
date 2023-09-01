package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.DrawingSet;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;

import java.util.ArrayList;

public class PresetDrawingSetLoader extends AbstractPresetLoader<DrawingSet> {

    public PresetDrawingSetLoader(PresetType presetType) {
        super(DrawingSet.class, presetType,"user_set_presets.json");
        setDefaultManager(new PresetDrawingSetManager(this) {
            @Override
            public ObservableDrawingSet getSelectedDrawingSet(DBTaskContext context) {
                return context.project().getDrawingSets().getActiveDrawingSet();
            }
        });
    }

    @Override
    public DrawingSet getPresetInstance(GenericPreset<DrawingSet> preset) {
        return new DrawingSet(preset.getPresetSubType(), preset.getPresetName(), new ArrayList<>());
    }

    @Override
    public void registerPreset(GenericPreset<DrawingSet> preset) {
        super.registerPreset(preset);
        MasterRegistry.INSTANCE.registerDrawingSet(preset.data);
        preset.data.preset = preset; //set transient binding
    }

    @Override
    public void unregisterPreset(GenericPreset<DrawingSet> preset) {
        super.unregisterPreset(preset);
        MasterRegistry.INSTANCE.unregisterDrawingSet(preset.data);
    }

    @Override
    public void onPresetEdited(GenericPreset<DrawingSet> preset) {
        super.onPresetEdited(preset);
        preset.data.type = preset.getPresetSubType();
        preset.data.name = preset.getPresetName();
    }

    @Override
    public GenericPreset<DrawingSet> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "", "", "", true);
    }

    @Override
    public GenericPreset<DrawingSet> createNewPreset() {
        return createNewPreset(DBConstants.DRAWING_TYPE_USER, "New Preset", true);
    }

    @Override
    public void loadDefaults() {
        //don't load the via default preset method, only User Generated Drawing Sets are "presets"
        DrawingBotV3.project().drawingSets.get().getDrawingSetForSlot(0).loadDrawingSet(MasterRegistry.INSTANCE.getDefaultDrawingSet());
    }

}