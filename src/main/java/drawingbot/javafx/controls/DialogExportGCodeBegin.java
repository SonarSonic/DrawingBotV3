package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class DialogExportGCodeBegin extends Dialog<Boolean> {

    public DialogExportGCodeBegin() {
        super();
        setTitle("Confirm GCode Settings");

        AnchorPane pane = DrawingBotV3.INSTANCE.controller.exportController.anchorPaneGCodeSettings;
        Parent oldParent = pane.getParent();
        getDialogPane().setContent(pane);
        getDialogPane().setPrefWidth(420);
        getDialogPane().setPrefHeight(360);

        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setResultConverter(param -> param == ButtonType.OK);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());
        setOnCloseRequest(e -> {
            if(oldParent instanceof Pane){
                ((Pane)oldParent).getChildren().add(pane);
            }
        });
    }

}