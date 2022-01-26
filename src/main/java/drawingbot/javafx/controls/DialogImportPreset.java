package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class DialogImportPreset extends Dialog<Boolean> {

    public DialogImportPreset(GenericPreset<?> preset) {
        super();
        setTitle("Imported Preset");

        TextFlow flow = new TextFlow();

        FXHelper.addText(flow, 14, "bold", "Successfully imported preset");
        FXHelper.addText(flow, 14, "normal", "\n" + "Preset Name: " + preset.presetName);
        FXHelper.addText(flow, 14, "normal", "\n" + "Preset Sub Type: " + preset.presetSubType);
        FXHelper.addText(flow, 14, "normal", "\n" + "Preset Type: " + preset.presetType.id);

        setGraphic(flow);

        getDialogPane().setPrefWidth(500);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setResultConverter(param -> param == ButtonType.OK);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());
    }

}