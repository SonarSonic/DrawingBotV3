package drawingbot.files.json.projects;

import drawingbot.plotting.ITaskManager;

public class DBTaskContext {

    public final ObservableProject project;
    public final ITaskManager taskManager;

    public DBTaskContext(ObservableProject project, ITaskManager taskManager) {
        this.project = project;
        this.taskManager = taskManager;
    }

    public ObservableProject project(){
        return project;
    }

    public ITaskManager taskManager(){
        return taskManager;
    }
}

