package drawingbot.files.loaders;
import java.io.File;

public class ImageFileLoaderFactory implements IFileLoaderFactory{

    @Override
    public String getName() {
        return "Image";
    }

    @Override
    public AbstractFileLoader createLoader(File file, boolean internal) {
        return new ImageFileLoader(file, internal);
    }

}
