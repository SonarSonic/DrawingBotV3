package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.File;

public class DialogExportPreset extends Dialog<Boolean> {

    public final ButtonType openFolder = new ButtonType("Open Folder", ButtonBar.ButtonData.OK_DONE);

    public DialogExportPreset(GenericPreset<?> preset, File dest) {
        super();
        setTitle("Exported Preset");

        TextFlow flow = new TextFlow();

        FXHelper.addText(flow, 14, "bold", "Successfully exported preset");
        FXHelper.addText(flow, 14, "normal", "\n" + "Preset Name: " + preset.getPresetName());
        FXHelper.addText(flow, 14, "normal", "\n" + "Preset Sub Type: " + preset.getPresetSubType());
        FXHelper.addText(flow, 14, "normal", "\n" + "Preset Type: " + preset.presetType.id);

        setGraphic(flow);

        getDialogPane().setPrefWidth(500);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().getButtonTypes().add(openFolder);
        setResultConverter(param -> param == openFolder);
        FXApplication.applyTheme((Stage)getDialogPane().getScene().getWindow());
    }

}