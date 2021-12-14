package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

public class DialogExportSequenceBegin extends Dialog<Boolean> {

    public DialogExportSequenceBegin() {
        super();
        setTitle("Confirm Image Sequence Settings");

        getDialogPane().setContent(DrawingBotV3.INSTANCE.controller.exportController.anchorPaneImgSeqSettings);
        getDialogPane().setPrefWidth(420);
        getDialogPane().setPrefHeight(360);

        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setResultConverter(param -> param == ButtonType.OK);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());
    }

}