package drawingbot.files.loaders;

import drawingbot.files.FileUtils;
import drawingbot.files.json.projects.DBTaskContext;

import java.io.File;

public class ProjectFileLoaderFactory implements IFileLoaderFactory{

    @Override
    public String getName() {
        return "Project";
    }

    @Override
    public AbstractFileLoader createLoader(DBTaskContext context, File file, boolean internal, boolean isSubTask) {
        String extension = FileUtils.getExtension(file.toString());
        if(extension.equalsIgnoreCase(".drawingbotv3")) {
            return new ProjectFileLoader(context, file, internal, isSubTask);
        }
        return null;
    }
}
