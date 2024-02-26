package drawingbot.files.json.presets;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.GenericPreset;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import javafx.collections.ObservableList;

import java.util.Comparator;

public class PresetPFMSettingsLoader extends AbstractPresetLoader<PresetData> {

    public PresetPFMSettingsLoader(PresetType presetType) {
        super(PresetData.class, presetType, "user_pfm_presets.json");
    }

    @Override
    protected void onJSONLoaded() {
        super.onJSONLoaded();

        registerMissingDefaultPFMPresets();

        MasterRegistry.INSTANCE.pfmFactories.forEach(pfm -> {
            initSubType(pfm.getRegistryName());
        });

        subTypes.sort(Comparator.comparingInt(k -> {
            PFMFactory<?> factory = MasterRegistry.INSTANCE.getPFMFactory(k);
            if(factory == null){
                return Integer.MAX_VALUE;
            }
            return MasterRegistry.INSTANCE.pfmFactories.indexOf(factory);
        }));
    }


    public void registerMissingDefaultPFMPresets(){
        for(PFMFactory<?> pfm : MasterRegistry.INSTANCE.pfmFactories){
            if(getPresetsForSubType(pfm.getRegistryName()) == null || getPresetsForSubType(pfm.getRegistryName()).stream().noneMatch(preset -> preset.getPresetName().equals("Default"))){
                Register.PRESET_LOADER_PFM.addPreset(Register.PRESET_LOADER_PFM.createNewPreset(pfm.getRegistryName(), "Default", false));

                // Move the default preset to the front of the displayed list
                ObservableList<GenericPreset<PresetData>> presets = Register.PRESET_LOADER_PFM.presetsByType.get(pfm.getRegistryName());
                presets.add(0, presets.remove(presets.size()-1));
            }
        }
    }

    @Override
    public PresetData createDataInstance(GenericPreset<PresetData> preset) {
        return new PresetData();
    }

    @Override
    public GenericPreset<PresetData> getDefaultPreset() {
        return MasterRegistry.INSTANCE.getDefaultPresetWithFallback(this, MasterRegistry.INSTANCE.getDefaultPFM().getRegistryName(), "Default", true);
    }

}