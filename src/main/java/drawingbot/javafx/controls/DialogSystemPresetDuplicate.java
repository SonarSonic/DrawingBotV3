package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.GenericPreset;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

/**
 * UI Dialog used when the user attempts to edit/update a "System Preset", allowing for either overriding the preset, duplicating it or cancelling the operation
 */
public class DialogSystemPresetDuplicate extends Dialog<DialogSystemPresetDuplicate.Result> {

    public final ButtonType overridePreset = new ButtonType("Override Preset", ButtonBar.ButtonData.YES);
    public final ButtonType duplicatePreset = new ButtonType("Duplicate Preset", ButtonBar.ButtonData.YES);

    public enum Result{
        OVERRIDE,
        DUPLICATE,
        CANCEL;
    }

    public DialogSystemPresetDuplicate(GenericPreset<?> preset) {
        super();
        setTitle("System Preset");
        setContentText("The preset '%s' is a System Preset. \nWould you like to override it?".formatted(preset.getPresetName()));
        setResultConverter(button -> {
            if(button == overridePreset){
                return Result.OVERRIDE;
            }
            if(button == duplicatePreset){
                return Result.DUPLICATE;
            }
            return null;
        });
        getDialogPane().getButtonTypes().addAll(overridePreset, duplicatePreset, ButtonType.CANCEL);
        FXApplication.applyTheme((Stage)getDialogPane().getScene().getWindow());
    }

    public static Result openSystemPresetDialog(GenericPreset<?> targetPreset) {
        DialogSystemPresetDuplicate dialog = new DialogSystemPresetDuplicate(targetPreset);
        dialog.initOwner(FXApplication.primaryStage);
        return dialog.showAndWait().orElse(null);
    }
}
