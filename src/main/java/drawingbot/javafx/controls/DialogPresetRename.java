package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.GenericPreset;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DialogPresetRename extends Dialog<GenericPreset<?>> {

    public TextField categoryField;
    public TextField nameField;
    public static GenericPreset<?> editingPreset;

    public Label categoryFieldLabel;

    public DialogPresetRename() {
        super();
        VBox vBox = new VBox();
        vBox.setSpacing(8);

        categoryFieldLabel = new Label("Preset Category: ");
        categoryField = new TextField();
        categoryField.textProperty().addListener((observable, oldValue, newValue) -> editingPreset.presetSubType = newValue);
        categoryFieldLabel.setGraphic(categoryField);
        categoryFieldLabel.setContentDisplay(ContentDisplay.RIGHT);
        vBox.getChildren().add(categoryFieldLabel);

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
        FXApplication.applyDBStyle((Stage)getDialogPane().getScene().getWindow());
    }

    public void setEditingPreset(GenericPreset<?> editingPreset, boolean editableCategory) {
        //make sure we set the editing preset first, so text changes don't affect the previous one!
        DialogPresetRename.editingPreset = editingPreset;

        categoryField.setDisable(!editableCategory);
        categoryField.setText(editingPreset.presetSubType);

        nameField.setText(editingPreset.presetName);
    }

}