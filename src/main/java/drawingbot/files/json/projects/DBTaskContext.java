package drawingbot.files.json.projects;

import drawingbot.plotting.ITaskManager;
import drawingbot.utils.Metadata;
import drawingbot.utils.MetadataMap;

public class DBTaskContext {

    public final ObservableProject project;
    public final ITaskManager taskManager;
    private MetadataMap metadata; //optional, lazy initialized when used

    public DBTaskContext(DBTaskContext context) {
        this.project = context.project;
        this.taskManager = context.taskManager;
        this.metadata = context.metadata != null ? context.metadata.copy() : null;
    }

    public DBTaskContext(ObservableProject project, ITaskManager taskManager) {
        this.project = project;
        this.taskManager = taskManager;
    }

    public ObservableProject project() {
        return project;
    }

    public ITaskManager taskManager() {
        return taskManager;
    }

    public <T> DBTaskContext setMetadata(Metadata<T> metadata, T value) {
        if (metadata == null) {
            this.metadata = new MetadataMap();
        }
        this.metadata.setMetadata(metadata, value);
        return this;
    }

    public <T> T getMetadata(Metadata<T> metadata) {
        if (metadata == null) {
            return null;
        }
        return this.metadata.getMetadata(metadata);
    }

}