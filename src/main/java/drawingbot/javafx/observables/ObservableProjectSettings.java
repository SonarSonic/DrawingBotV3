package drawingbot.javafx.observables;

import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.image.BufferedImageLoader;
import drawingbot.javafx.GenericPreset;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.util.UUID;

public class ObservableProjectSettings {

    public final SimpleObjectProperty<UUID> uuid = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<Image> thumbnail = new SimpleObjectProperty<>();
    public final SimpleStringProperty name = new SimpleStringProperty();
    public final SimpleStringProperty date = new SimpleStringProperty();
    public final SimpleStringProperty file = new SimpleStringProperty();
    public final SimpleStringProperty thumbnailID = new SimpleStringProperty("");
    public SimpleObjectProperty<GenericPreset<PresetProjectSettings>> preset = new SimpleObjectProperty<>();

    public ObservableProjectSettings(GenericPreset<PresetProjectSettings> preset, boolean isSubProject) {
        this.thumbnail.set(null);
        this.name.set(preset.data.name);
        this.date.set(preset.data.timeStamp);
        this.thumbnailID.set(preset.data.thumbnailID);
        this.file.set(preset.data.imagePath);
        this.preset.set(preset);
        preset.data.isSubProject = isSubProject;
        loadThumbnail();
    }

    private void loadThumbnail(){
        if(!thumbnailID.get().isEmpty()){
            BufferedImageLoader loader = new BufferedImageLoader(FileUtils.getUserThumbnailDirectory() + thumbnailID.get() + ".jpg", false);
            DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.backgroundService, loader);
            loader.setOnSucceeded(e -> thumbnail.set(SwingFXUtils.toFXImage(loader.getValue(), null)));
        }
    }

}
