package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.files.json.projects.ObservableProject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.ArrayList;

public class RecentProjectHandler {

    public static final ObservableList<File> recentProjects = FXCollections.observableArrayList();
    public static final int maxRecentProjects = 12;
    public static boolean recentProjectsChanged = false;

    public static void tick(){
        if(recentProjectsChanged){
            recentProjectsChanged = false;
            DrawingBotV3.INSTANCE.backgroundService.submit(RecentProjectHandler::saveRecentProjectData);
        }
    }

    public static File getRecentProjectDataFile(){
        return new File(FileUtils.getUserDataDirectory(), "user_recent_projects.json");
    }

    public static void saveRecentProjectData(){
        File file = getRecentProjectDataFile();
        RecentProjectData projectData = new RecentProjectData(new ArrayList<>(recentProjects));
        JsonLoaderManager.exportJsonFile(file, RecentProjectData.class, projectData);
    }

    public static void loadRecentProjectData(){
        File file = getRecentProjectDataFile();
        if(file.exists()){
            RecentProjectData projectData = JsonLoaderManager.importJsonFile(file, RecentProjectData.class);
            recentProjects.setAll(projectData.recentFiles);
        }
    }

    public static void addRecentProject(ObservableProject project){
        File file = project.file.get();

        if(file == null || !file.exists()){
            return;
        }

        //Remove from existing position
        recentProjects.remove(project.file.get());

        //Add at the head of the list
        recentProjects.add(0, project.file.get());

        //Remove old entries
        if(recentProjects.size() > maxRecentProjects){
            recentProjects.remove(maxRecentProjects, recentProjects.size());
        }

        recentProjectsChanged = true;
    }

}
