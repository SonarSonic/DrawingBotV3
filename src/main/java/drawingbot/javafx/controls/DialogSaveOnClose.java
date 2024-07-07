package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

public class DialogSaveOnClose extends Dialog<DialogSaveOnClose.ExitResponse> {

    public enum ExitResponse {
        SAVE,
        DONT_SAVE,
        CANCEL_CLOSE;

        public boolean shouldCancel(){
            return this == CANCEL_CLOSE;
        }

        public boolean shouldSave(){
            return this == SAVE;
        }
    }

    public DialogSaveOnClose(String projectName) {
        super();
        setTitle("Save Project ");
        setContentText("Would you like to save changes to '%s'?".formatted(projectName));

        ButtonType saveButton = new ButtonType("Save Project", ButtonBar.ButtonData.YES);
        ButtonType dontSaveButton = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
        setResultConverter(button -> {
            if(button == saveButton){
                return ExitResponse.SAVE;
            }
            if(button == ButtonType.CANCEL){
                return ExitResponse.CANCEL_CLOSE;
            }
            return ExitResponse.DONT_SAVE;
        });
        getDialogPane().getButtonTypes().addAll(saveButton, dontSaveButton, ButtonType.CANCEL);
        FXApplication.applyTheme((Stage)getDialogPane().getScene().getWindow());
    }
}
