package drawingbot.files.json;

import drawingbot.javafx.GenericPreset;

public interface IConfigData extends IJsonData {

    GenericPreset<IConfigData> updatePreset(GenericPreset<IConfigData> preset);

    void applyPreset(GenericPreset<IConfigData> preset);

}
