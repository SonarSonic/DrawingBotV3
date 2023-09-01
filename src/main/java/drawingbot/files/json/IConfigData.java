package drawingbot.files.json;

import drawingbot.javafx.GenericPreset;

public interface IConfigData {

    GenericPreset<IConfigData> updatePreset(GenericPreset<IConfigData> preset);

    void applyPreset(GenericPreset<IConfigData> preset);

}
