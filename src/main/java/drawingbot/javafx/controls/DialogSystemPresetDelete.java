package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.GenericPreset;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * UI Dialog used when the user attempts to delete a "System Preset"
 */
public class DialogSystemPresetDelete extends Dialog<Boolean> {

    public final ButtonType hidePreset = new ButtonType("Disable Preset", ButtonBar.ButtonData.YES);

    public DialogSystemPresetDelete(GenericPreset<?> preset) {
        super();
        setTitle("System Preset");
        setContentText("The preset '%s' can't be deleted, would you like to disable it instead?".formatted(preset.getPresetName()));
        setResultConverter(button -> button == hidePreset);
        getDialogPane().getButtonTypes().addAll(hidePreset, ButtonType.OK);
        FXApplication.applyTheme((Stage)getDialogPane().getScene().getWindow());
    }

    public static boolean openSystemPresetDeleteDialog(GenericPreset<?> targetPreset) {
        DialogSystemPresetDelete dialog = new DialogSystemPresetDelete(targetPreset);
        dialog.initOwner(FXApplication.primaryStage);
        Optional<Boolean> result = dialog.showAndWait();
        return result.isPresent() && result.get();
    }
}
