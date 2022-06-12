package drawingbot.files.loaders;

import drawingbot.files.loaders.AbstractFileLoader;

import java.io.File;

public interface IFileLoaderFactory {

    String getName();

    /**
     * @param file the file to load
     * @param internal true if the file is located within the DrawingBotV3 jar.
     * @return the file loader instance, or null if this file loader can't load the file
     */
    AbstractFileLoader createLoader(File file, boolean internal);

}
