package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;

import java.io.File;

public class ControlFilePicker extends ControlDirectoryPicker {

    public ControlFilePicker() {}

    @Override
    public void showPickerDialog() {
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.titleProperty().bind(windowTitleProperty());
            File current = new File(getValue());
            d.setInitialFileName(current.exists() ? current.getName() : getInitialFileName());
            d.setInitialDirectory(current.exists() ? current.getParentFile() : getInitialDirectory());

            d.getExtensionFilters().addAll(getExtensionFilters());
            if(getSelectedExtensionFilter() == null && !getExtensionFilters().isEmpty()){
                setSelectedExtensionFilter(getExtensionFilters().get(0));
            }
            d.selectedExtensionFilterProperty().bindBidirectional(selectedExtensionFilterProperty());

            File file = d.showOpenDialog(FXApplication.primaryStage);
            if(file != null){
                setValue(file.toString());
                if(getOnEdit() != null){
                    getOnEdit().accept(file.toString());
                }
            }
        });
    }

    @Override
    public void doEdit(){
        if(getOnEdit() != null){
            getOnEdit().accept(getValue());
        }
    }

    ////////////////////////////////////////////////////////

    public StringProperty initialFileName = new SimpleStringProperty("");

    public String getInitialFileName() {
        return initialFileName.get();
    }

    public StringProperty initialFileNameProperty() {
        return initialFileName;
    }

    public void setInitialFileName(String initialFileName) {
        this.initialFileName.set(initialFileName);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<FileChooser.ExtensionFilter> selectedExtensionFilter = new SimpleObjectProperty<>(null);

    public FileChooser.ExtensionFilter getSelectedExtensionFilter() {
        return selectedExtensionFilter.get();
    }

    public ObjectProperty<FileChooser.ExtensionFilter> selectedExtensionFilterProperty() {
        return selectedExtensionFilter;
    }

    public void setSelectedExtensionFilter(FileChooser.ExtensionFilter selectedExtensionFilter) {
        this.selectedExtensionFilter.set(selectedExtensionFilter);
    }

    ////////////////////////////////////////////////////////

    public final ObservableList<FileChooser.ExtensionFilter> extensionFilters = FXCollections.observableArrayList();

    public ObservableList<FileChooser.ExtensionFilter> getExtensionFilters() {
        return extensionFilters;
    }
}
