package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class DialogExportSequenceBegin extends Dialog<Boolean> {

    public DialogExportSequenceBegin() {
        super();
        setTitle("Confirm Image Sequence Settings");

        AnchorPane pane = DrawingBotV3.INSTANCE.controller.exportController.anchorPaneImgSeqSettings;
        Parent oldParent = pane.getParent();
        getDialogPane().setContent(pane);
        getDialogPane().setPrefWidth(420);
        getDialogPane().setPrefHeight(360);

        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setResultConverter(param -> param == ButtonType.OK);
        FXApplication.applyDBStyle((Stage)getDialogPane().getScene().getWindow());
        setOnCloseRequest(e -> {
            if(oldParent instanceof Pane){
                ((Pane)oldParent).getChildren().add(pane);
            }
        });
    }

}