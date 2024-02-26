package drawingbot.integrations.vpype;

import drawingbot.files.json.DefaultPresetEditor;
import drawingbot.files.json.IPresetManager;
import drawingbot.files.json.PresetData;

public class PresetVpypeSettingsEditor extends DefaultPresetEditor<VpypeSettings, PresetData> {

    public PresetVpypeSettingsEditor(IPresetManager<VpypeSettings, PresetData> manager) {
        super(manager);
    }
}
