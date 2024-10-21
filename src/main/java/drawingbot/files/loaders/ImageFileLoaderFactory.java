package drawingbot.files.loaders;
import drawingbot.files.json.projects.DBTaskContext;

import java.io.File;
import java.util.Set;

public class ImageFileLoaderFactory implements IFileLoaderFactory{

    @Override
    public String getName() {
        return "Image";
    }

    @Override
    public AbstractFileLoader createLoader(DBTaskContext context, File file, Set<FileLoaderFlags> flags) {
        return new ImageFileLoader(context, file, flags);
    }

}
