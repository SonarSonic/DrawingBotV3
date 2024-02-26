package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.function.Consumer;

public class ControlDirectoryPicker extends Control {

    public ControlDirectoryPicker(){}

    public void showPickerDialog(){
        Platform.runLater(() -> {
            DirectoryChooser d = new DirectoryChooser();
            d.titleProperty().bind(windowTitleProperty());
            d.setInitialDirectory(getInitialDirectory());
            if(getValue() != null){
                File current = new File(getValue());
                if(current.exists()){
                    d.setInitialDirectory(current);
                }
            }
            File file = d.showDialog(FXApplication.primaryStage);
            if(file != null){
                setValue(file.toString());
                doEdit();
            }
        });
    }

    public void doEdit(){
        if(getOnEdit() != null){
            getOnEdit().accept(getValue());
        }
    }

    ////////////////////////////////////////////////////////

    public StringProperty value = new SimpleStringProperty("");

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<File> initialDirectory = new SimpleObjectProperty<>(null);

    public File getInitialDirectory() {
        return initialDirectory.get();
    }

    public ObjectProperty<File> initialDirectoryProperty() {
        return initialDirectory;
    }

    public void setInitialDirectory(File initialDirectory) {
        this.initialDirectory.set(initialDirectory);
    }

    ////////////////////////////////////////////////////////

    public StringProperty windowTitle = new SimpleStringProperty("Select Directory");

    public String getWindowTitle() {
        return windowTitle.get();
    }

    public StringProperty windowTitleProperty() {
        return windowTitle;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle.set(windowTitle);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<Consumer<String>> onEdit = new SimpleObjectProperty<>();

    public Consumer<String> getOnEdit() {
        return onEdit.get();
    }

    public ObjectProperty<Consumer<String>> onEditProperty() {
        return onEdit;
    }

    public void setOnEdit(Consumer<String> onEdit) {
        this.onEdit.set(onEdit);
    }

    ////////////////////////////////////////////////////////

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinDirectoryPicker(this);
    }
}
