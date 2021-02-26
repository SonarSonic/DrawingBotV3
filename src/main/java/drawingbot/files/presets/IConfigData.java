package drawingbot.files.presets;

import drawingbot.javafx.GenericPreset;

public interface IConfigData extends IJsonData {

    GenericPreset<IConfigData> updatePreset(GenericPreset<IConfigData> preset);

    void applyPreset(GenericPreset<IConfigData> preset);

}
