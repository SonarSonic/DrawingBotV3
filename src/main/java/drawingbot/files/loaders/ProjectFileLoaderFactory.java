package drawingbot.files.loaders;

import drawingbot.files.FileUtils;

import java.io.File;

public class ProjectFileLoaderFactory implements IFileLoaderFactory{

    @Override
    public String getName() {
        return "Project";
    }

    @Override
    public AbstractFileLoader createLoader(File file, boolean internal) {
        String extension = FileUtils.getExtension(file.toString());
        if(extension.equalsIgnoreCase(".drawingbotv3")) {
            return new ProjectFileLoader(file, internal);
        }
        return null;
    }
}
