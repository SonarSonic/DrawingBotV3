package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.GenericPreset;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.util.Optional;

public class DialogSystemPresetDelete extends Dialog<Boolean> {

    public DialogSystemPresetDelete(GenericPreset<?> preset) {
        super();
        setTitle("System Preset");
        setContentText("The preset '%s' can't be deleted".formatted(preset.getPresetName()));
        setResultConverter(button -> false);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        FXApplication.applyTheme((Stage)getDialogPane().getScene().getWindow());
    }

    public static boolean openSystemPresetDeleteDialog(GenericPreset<?> targetPreset) {
        DialogSystemPresetDelete dialog = new DialogSystemPresetDelete(targetPreset);
        dialog.initOwner(FXApplication.primaryStage);
        Optional<Boolean> result = dialog.showAndWait();
        return result.isPresent() && result.get();
    }
}
