package drawingbot.files.json.presets;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.DrawingSet;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;

import java.util.ArrayList;

public class PresetDrawingSetLoader extends AbstractPresetLoader<IDrawingSet> {

    public PresetDrawingSetLoader(PresetType presetType) {
        super(IDrawingSet.class, presetType,"user_set_presets.json");
    }

    public void addDrawingSet(IDrawingSet set) {
        addPreset(wrapDrawingPen(set, false));
    }

    public void removeDrawingSet(IDrawingSet set){
        if(set.getLinkedPreset() == null){
            DrawingBotV3.logger.severe("Unable to remove drawing set %s, no linked preset".formatted(set.getCodeName()));
            return;
        }
        removePreset(set.getLinkedPreset());
    }

    @Override
    public void addPreset(GenericPreset<IDrawingSet> preset) {
        super.addPreset(preset);
        preset.data.setLinkedPreset(preset); //set transient binding
    }

    @Override
    public void removePreset(GenericPreset<IDrawingSet> preset) {
        super.removePreset(preset);
        preset.data.setLinkedPreset(null); //set transient binding
    }

    @Override
    public IDrawingSet createDataInstance(GenericPreset<IDrawingSet> preset) {
        return new DrawingSet(preset.getPresetSubType(), preset.getPresetName(), new ArrayList<>());
    }

    @Override
    public GenericPreset<IDrawingSet> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPresetWithFallback(this, "Copic", "Dark Greys", true);
    }

    @Override
    public GenericPreset<IDrawingSet> createNewPreset() {
        return createNewPreset(DBConstants.DRAWING_TYPE_USER, "New Preset", true);
    }

    public IDrawingSet unwrapPreset(GenericPreset<IDrawingSet> preset){
        if(preset != null){
            return preset.data;
        }
        return null;
    }

    public GenericPreset<IDrawingSet> wrapDrawingPen(IDrawingSet set, boolean userCreated){
        if(set == null){
            return null;
        }
        if(set.getLinkedPreset() != null){
            return set.getLinkedPreset();
        }
        GenericPreset<IDrawingSet> preset = createNewPreset();
        preset.setPresetName(set.getName());
        preset.setPresetSubType(set.getType());
        preset.data = new DrawingSet(set);
        preset.data.setLinkedPreset(preset);
        preset.userCreated = userCreated;
        return preset;
    }

    public IDrawingSet findDrawingSet(String subType, String presetName){
        return unwrapPreset(findPreset(subType, presetName));
    }

    public IDrawingSet findDrawingSet(String presetName){
        return unwrapPreset(findPreset(presetName));
    }

    @Override
    public void loadDefaults(DBTaskContext context) {
        //don't load the via default preset method, only User Generated Drawing Sets are "presets"
        context.project().drawingSets.get().getDrawingSetForSlot(0).loadDrawingSet(MasterRegistry.INSTANCE.getDefaultDrawingSet());
    }

}