package drawingbot.files.loaders;

import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.image.format.FilteredImageData;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.Register;
import drawingbot.render.overlays.NotificationOverlays;
import javafx.application.Platform;

import java.io.File;

public class ProjectFileLoader extends AbstractFileLoader{
    
    public ProjectFileLoader(DBTaskContext context, File file, boolean internal, boolean isSubTask) {
        super(context, file, internal, isSubTask);
    }

    @Override
    public boolean hasImageData() {
        return false;
    }

    @Override
    protected FilteredImageData call() throws Exception {
        updateTitle("Loading Project File");
        Platform.runLater(() -> {
            GenericPreset<PresetProjectSettings> preset = FXHelper.loadPresetFile(context, Register.PRESET_LOADER_PROJECT, file, false);
            if(preset != null){
                ObservableProject project = new ObservableProject(FileUtils.removeExtension(file.getName()), file);

                Register.PRESET_LOADER_PROJECT.getDefaultManager().applyPreset(project.context, preset, false);

                DrawingBotV3.INSTANCE.activeProjects.add(project);
                DrawingBotV3.INSTANCE.activeProject.set(project);
            }
        });
        updateProgress(1, 1);
        return null;
    }

    @Override
    public String getFileTypeDisplayName() {
        return "Project File";
    }

    @Override
    public void onFileLoaded(){
        NotificationOverlays.INSTANCE.showWithSubtitle("Loaded Project", file.toString());
    }
}
