package drawingbot.files.loaders;
import drawingbot.files.json.projects.DBTaskContext;

import java.io.File;

public class ImageFileLoaderFactory implements IFileLoaderFactory{

    @Override
    public String getName() {
        return "Image";
    }

    @Override
    public AbstractFileLoader createLoader(DBTaskContext context, File file, boolean internal) {
        return new ImageFileLoader(context, file, internal);
    }

}
