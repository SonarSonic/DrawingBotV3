package drawingbot.javafx.controls;

import drawingbot.javafx.FXController;
import drawingbot.utils.GenericPreset;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

//TODO CHANGE TYPES??
public class DialogPresetRename extends Dialog<GenericPreset> {

    public Label labelPresetSubType;
    public Label labelPresetType;
    public TextField nameField;

    public DialogPresetRename() {
        super();
        VBox vBox = new VBox();

        //labelPresetSubType = new Label("Preset Subtype: "); //TODO REMOVE???
        //vBox.getChildren().add(labelTargetPFM);

        //labelPresetType = new Label("Preset Type: ");
        //vBox.getChildren().add(labelTotalSettings);

        Label nameFieldLabel = new Label("Preset Name: ");
        nameField = new TextField();
        nameField.textProperty().addListener((observable, oldValue, newValue) -> FXController.editingPreset.presetName = newValue);
        nameFieldLabel.setGraphic(nameField);
        nameFieldLabel.setContentDisplay(ContentDisplay.RIGHT);
        vBox.getChildren().add(nameFieldLabel);

        setGraphic(vBox);
        setResultConverter(param -> param == ButtonType.APPLY ? FXController.editingPreset : null);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);
    }

    public void updateDialog() {
        //labelPresetSubType.setText("Preset Subtype: " + FXController.editingPreset.presetSubType);
        nameField.setText(FXController.editingPreset.presetName);
    }


}
