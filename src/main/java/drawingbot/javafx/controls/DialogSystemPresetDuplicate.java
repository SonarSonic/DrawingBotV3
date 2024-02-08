package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.GenericPreset;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.util.Optional;

public class DialogSystemPresetDuplicate extends Dialog<Boolean> {
    public final ButtonType duplicateButton = new ButtonType("Duplicate Preset", ButtonBar.ButtonData.YES);

    public DialogSystemPresetDuplicate(GenericPreset<?> preset) {
        super();
        setTitle("System Preset");
        setContentText("The preset '%s' can't be edited. \nWould you like to duplicate it instead?".formatted(preset.getPresetName()));
        setResultConverter(button -> button == duplicateButton);
        getDialogPane().getButtonTypes().addAll(duplicateButton, ButtonType.CANCEL);
        FXApplication.applyTheme((Stage)getDialogPane().getScene().getWindow());
    }

    public static boolean openSystemPresetDialog(GenericPreset<?> targetPreset) {
        DialogSystemPresetDuplicate dialog = new DialogSystemPresetDuplicate(targetPreset);
        dialog.initOwner(FXApplication.primaryStage);
        Optional<Boolean> result = dialog.showAndWait();
        return result.isPresent() && result.get();
    }
}
