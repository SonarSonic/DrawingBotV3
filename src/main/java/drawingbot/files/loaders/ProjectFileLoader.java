package drawingbot.files.loaders;

import drawingbot.files.FileUtils;
import drawingbot.image.format.FilteredImageData;
import drawingbot.javafx.FXHelper;
import drawingbot.registry.Register;

import java.io.File;

public class ProjectFileLoader extends AbstractFileLoader{
    
    public ProjectFileLoader(File file, boolean internal) {
        super(file, internal);
    }

    @Override
    public boolean hasImageData() {
        return false;
    }

    @Override
    protected FilteredImageData call() throws Exception {
        FXHelper.loadPresetFile(Register.PRESET_TYPE_PROJECT, file, true);
        return null;
    }
}
