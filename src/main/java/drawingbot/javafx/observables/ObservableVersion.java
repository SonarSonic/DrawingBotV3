package drawingbot.javafx.observables;

import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.image.BufferedImageLoader;
import drawingbot.javafx.GenericPreset;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.util.UUID;

public class ObservableVersion {

    public final SimpleObjectProperty<UUID> uuid = new SimpleObjectProperty<>();
    public final SimpleStringProperty name = new SimpleStringProperty();
    public final SimpleDoubleProperty rating = new SimpleDoubleProperty();
    public final SimpleStringProperty date = new SimpleStringProperty();
    public final SimpleStringProperty file = new SimpleStringProperty();
    public final SimpleStringProperty notes = new SimpleStringProperty();
    public final SimpleStringProperty thumbnailID = new SimpleStringProperty("");
    public final SimpleObjectProperty<GenericPreset<PresetProjectSettings>> preset = new SimpleObjectProperty<>();

    public ObservableVersion(){
        //GSON
    }

    public ObservableVersion(ObservableVersion copy){
        this.uuid.set(UUID.randomUUID());
        this.name.set(copy.name.get());
        this.rating.set(copy.rating.get());
        this.date.set(copy.date.get());
        this.file.set(copy.file.get());
        this.notes.set(copy.notes.get());
        this.thumbnailID.set(copy.thumbnailID.get());
        this.preset.set(new GenericPreset<>(copy.preset.get()));
        preset.get().data.isSubProject = true;//copy.preset.get().data.isSubProject;

        if(copy.thumbnail != null){
            this.thumbnail.set(copy.thumbnail.get());
        }
    }

    public ObservableVersion(GenericPreset<PresetProjectSettings> preset, boolean isSubProject) {
        this.uuid.set(UUID.randomUUID());
        this.name.set(preset.data.name);
        this.rating.set(preset.data.rating);
        this.date.set(preset.data.timeStamp);
        this.thumbnailID.set(preset.data.thumbnailID);
        this.file.set(preset.data.imagePath);
        this.notes.set(preset.data.notes);
        this.preset.set(preset);
        preset.data.isSubProject = true;//isSubProject;

    }

    public GenericPreset<PresetProjectSettings> getPreset(){
        updatePreset();
        return preset.get();
    }

    /////////////////////////////////////////////////////////

    private transient SimpleObjectProperty<Image> thumbnail;

    public Image getThumbnail() {
        return thumbnailProperty().get();
    }

    /**
     * Lazy initialized thumbnail property, so we only load the thumbnail when the UI actually uses it
     */
    public SimpleObjectProperty<Image> thumbnailProperty() {
        if(thumbnail == null){
            thumbnail = new SimpleObjectProperty<>();
            requestThumbnail();
        }
        return thumbnail;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnailProperty().set(thumbnail);
    }

    public void requestThumbnail(){
        if(thumbnailID.get().isEmpty()){
            return;
        }
        BufferedImageLoader loader = new BufferedImageLoader(DrawingBotV3.context(), FileUtils.getUserThumbnailDirectory() + thumbnailID.get() + ".jpg", false);
        DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.backgroundService, loader);
        loader.setOnSucceeded(e -> Platform.runLater(() -> {
            thumbnail.set(SwingFXUtils.toFXImage(loader.getValue(), null));
        }));
    }

    public void loadVersion(DBTaskContext context){
        preset.get().applyPreset(context);
    }


    public void updatePreset(){
        preset.get().data.name = name.get();
        preset.get().data.rating = rating.get();
        preset.get().data.notes = notes.get();
    }

    public ObservableVersion copy() {
        return new ObservableVersion(this);
    }
}
