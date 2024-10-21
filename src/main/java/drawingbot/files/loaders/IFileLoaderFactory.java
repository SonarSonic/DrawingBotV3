package drawingbot.files.loaders;

import drawingbot.files.json.projects.DBTaskContext;

import java.io.File;
import java.util.Set;

public interface IFileLoaderFactory {

    String getName();

    /**
     *
     * @param context the task context
     * @param file the file to load
     * @param flags flags to indicate the context of the file loader
     * @return the file loader instance, or null if this file loader can't load the file
     */
    AbstractFileLoader createLoader(DBTaskContext context, File file, Set<FileLoaderFlags> flags);

}
