package drawingbot.files;

import drawingbot.files.json.JsonData;

import java.io.File;
import java.util.ArrayList;

@JsonData
public class RecentProjectData {

    public ArrayList<File> recentFiles = new ArrayList<>();

    public RecentProjectData() {
        //for GSON
    }

    public RecentProjectData(ArrayList<File> recentFiles) {
        this.recentFiles = recentFiles;
    }
}
