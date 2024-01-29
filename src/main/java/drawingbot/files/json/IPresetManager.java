package drawingbot.files.json;

import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;

public interface IPresetManager<O> {

    /**
     * updates the presets settings with the ones currently configured
     * @return the preset or null if the settings couldn't be saved
     */
    GenericPreset<O> updatePreset(DBTaskContext context, GenericPreset<O> preset, boolean loadingProject);

    /**
     * applies the presets settings
     */
    void applyPreset(DBTaskContext context, GenericPreset<O> preset, boolean changesOnly, boolean loadingProject);
}
