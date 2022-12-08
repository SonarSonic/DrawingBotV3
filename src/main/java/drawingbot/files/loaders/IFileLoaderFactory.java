package drawingbot.files.loaders;

import drawingbot.files.json.projects.DBTaskContext;

import java.io.File;

public interface IFileLoaderFactory {

    String getName();

    /**
     *
     * @param context the task context
     * @param file the file to load
     * @param internal true if the file is located within the DrawingBotV3 jar.
     * @return the file loader instance, or null if this file loader can't load the file
     */
    AbstractFileLoader createLoader(DBTaskContext context, File file, boolean internal);

}
