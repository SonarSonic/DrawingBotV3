package drawingbot.javafx.observables;

import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

import java.util.UUID;

public class ObservableVersion {

    public final SimpleObjectProperty<UUID> uuid = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<Image> thumbnail = new SimpleObjectProperty<>();
    public final SimpleStringProperty name = new SimpleStringProperty();
    public final SimpleDoubleProperty rating = new SimpleDoubleProperty();
    public final SimpleStringProperty date = new SimpleStringProperty();
    public final SimpleStringProperty file = new SimpleStringProperty();
    public final SimpleStringProperty notes = new SimpleStringProperty();
    public final SimpleStringProperty thumbnailID = new SimpleStringProperty("");
    private final SimpleObjectProperty<GenericPreset<PresetProjectSettings>> preset = new SimpleObjectProperty<>();

    public ObservableVersion(ObservableVersion copy){
        this.uuid.set(UUID.randomUUID());
        this.thumbnail.set(copy.thumbnail.get());
        this.name.set(copy.name.get());
        this.rating.set(copy.rating.get());
        this.date.set(copy.date.get());
        this.file.set(copy.file.get());
        this.notes.set(copy.notes.get());
        this.thumbnailID.set(copy.thumbnailID.get());
        this.preset.set(FXHelper.copyPreset(copy.preset.get()));
        preset.get().data.isSubProject = copy.preset.get().data.isSubProject;
    }

    public ObservableVersion(GenericPreset<PresetProjectSettings> preset, boolean isSubProject) {
        this.uuid.set(UUID.randomUUID());
        this.thumbnail.set(null);
        this.name.set(preset.data.name);
        this.rating.set(preset.data.rating);
        this.date.set(preset.data.timeStamp);
        this.thumbnailID.set(preset.data.thumbnailID);
        this.file.set(preset.data.imagePath);
        this.notes.set(preset.data.notes);
        this.preset.set(preset);
        preset.data.isSubProject = isSubProject;
    }

    public GenericPreset<PresetProjectSettings> getPreset(){
        updatePreset();
        return preset.get();
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
