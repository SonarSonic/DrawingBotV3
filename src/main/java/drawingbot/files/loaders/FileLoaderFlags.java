package drawingbot.files.loaders;

public enum FileLoaderFlags {

    /**
     * The file loader is a sub task of another task, e.g. a project reloading task, or batch processing task
     */
    SUB_TASK,
    /**
     * The file should be loaded from inside the applications resources directory
     */
    INTERNAL_FILE,

    /**
     * The file is being loaded during a project reload, and should not alter the state of the project
     */
    PROJECT_LOADING;

}
