package drawingbot.files.json.presets;

import drawingbot.files.json.*;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMSettings;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;

import java.util.HashMap;
import java.util.List;

public class PresetPFMSettingsManager extends AbstractPresetManager<PFMSettings, PresetData> {

    public PresetPFMSettingsManager(IPresetLoader<PresetData> presetLoader) {
        super(presetLoader, PFMSettings.class);
    }

    public static String getPFMPresetJson(PFMFactory<?> pfmFactory, List<GenericSetting<?,?>> pfmSettings) {
        GenericPreset<PresetData> preset = Register.PRESET_LOADER_PFM.createNewPreset();
        preset.setPresetSubType(pfmFactory.getRegistryName());
        preset.data.settings = GenericSetting.toJsonMap(pfmSettings, new HashMap<>(), false);
        return JsonLoaderManager.createDefaultGson().toJson(preset);
    }

    @Override
    public PFMSettings getTargetFromContext(DBTaskContext context) {
        return context.project().getPFMSettings();
    }

    @Override
    public void updatePreset(DBTaskContext context, PFMSettings target, GenericPreset<PresetData> preset, boolean changesOnly) {
        if(target == null){
            return;
        }
        preset.setPresetSubType(target.getPFMFactory().getRegistryName());
        preset.data.settings = GenericSetting.toJsonMap(target.getSettings(), new HashMap<>(), false);
    }

    @Override
    public void applyPreset(DBTaskContext context, PFMSettings target, GenericPreset<PresetData> preset, boolean changesOnly) {
        if(target == null){
            return;
        }
        target.setPFMFactory(MasterRegistry.INSTANCE.getPFMFactory(preset.getPresetSubType()));
        GenericSetting.applySettings(preset.data.settings, target.getSettings());
    }

    @Override
    public DefaultPresetEditor<PFMSettings, PresetData> createPresetEditor() {
        return new PresetPFMSettingsEditor(this);
    }
}