package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class PresetDrawingSetLoader extends AbstractPresetLoader<PresetDrawingSet> {

    public PresetDrawingSetLoader(PresetType presetType) {
        super(PresetDrawingSet.class, presetType,"user_set_presets.json");
        setDefaultManager(new PresetDrawingSetManager(this) {
            @Override
            public ObservableDrawingSet getSelectedDrawingSet(DBTaskContext context) {
                return context.project().getDrawingSets().getActiveDrawingSet();
            }
        });
    }

    @Override
    public PresetDrawingSet getPresetInstance(GenericPreset<PresetDrawingSet> preset) {
        return new PresetDrawingSet(preset.presetSubType, preset.presetName, new ArrayList<>(), preset);
    }

    @Override
    public void registerPreset(GenericPreset<PresetDrawingSet> preset) {
        super.registerPreset(preset);
        MasterRegistry.INSTANCE.registerDrawingSet(preset.data);
        preset.data.preset = preset; //set transient binding
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetDrawingSet> preset) {
        super.unregisterPreset(preset);
        MasterRegistry.INSTANCE.unregisterDrawingSet(preset.data);
    }

    @Override
    public void onPresetEdited(GenericPreset<PresetDrawingSet> preset) {
        super.onPresetEdited(preset);
        preset.data.type = preset.presetSubType;
        preset.data.name = preset.presetName;
    }

    @Override
    public GenericPreset<PresetDrawingSet> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPreset(this, "", "", "", true);
    }

    @Override
    public GenericPreset<PresetDrawingSet> createNewPreset() {
        return createNewPreset(DBConstants.DRAWING_TYPE_USER, "New Preset", true);
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (ObservableList<IDrawingSet<IDrawingPen>> list : MasterRegistry.INSTANCE.registeredSets.values()) {
            for (IDrawingSet<IDrawingPen> set : list) {
                if (set instanceof PresetDrawingSet) {
                    PresetDrawingSet userSet = (PresetDrawingSet) set;
                    userCreated.add(userSet.preset);
                }
            }
        }
        return userCreated;
    }

    @Override
    public void loadDefaults() {
        //don't load the via default preset method, only User Generated Drawing Sets are "presets"
        DrawingBotV3.project().drawingSets.get().getDrawingSetForSlot(0).loadDrawingSet(MasterRegistry.INSTANCE.getDefaultDrawingSet());
    }

}