package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.GenericPreset;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DialogPresetRename extends Dialog<GenericPreset<?>> {

    public TextField nameField;
    public static GenericPreset<?> editingPreset;

    public DialogPresetRename() {
        super();
        VBox vBox = new VBox();

        //labelPresetSubType = new Label("Preset Subtype: "); //TODO REMOVE???
        //vBox.getChildren().add(labelTargetPFM);

        //labelPresetType = new Label("Preset Type: ");
        //vBox.getChildren().add(labelTotalSettings);

        Label nameFieldLabel = new Label("Preset Name: ");
        nameField = new TextField();
        nameField.textProperty().addListener((observable, oldValue, newValue) -> editingPreset.presetName = newValue);
        nameFieldLabel.setGraphic(nameField);
        nameFieldLabel.setContentDisplay(ContentDisplay.RIGHT);
        vBox.getChildren().add(nameFieldLabel);

        setGraphic(vBox);
        setResultConverter(param -> param == ButtonType.APPLY ? editingPreset : null);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());
    }

    public void setEditingPreset(GenericPreset<?> editingPreset) {
        this.editingPreset = editingPreset;
        nameField.setText(editingPreset.presetName);
    }

}