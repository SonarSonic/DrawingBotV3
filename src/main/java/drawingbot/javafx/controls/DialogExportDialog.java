package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.FXHelper;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class DialogExportDialog extends Dialog<Boolean> {

    public ScrollPane scrollPane;

    public DialogExportDialog(String title, Node content) {
        super();
        setTitle(title);

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(content);
        getDialogPane().getStylesheets().add(FXHelper.class.getResource("preference-styles.css").toExternalForm());

        getDialogPane().setContent(scrollPane);
        getDialogPane().setPrefWidth(600);
        getDialogPane().setPrefHeight(500);

        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setResultConverter(param -> param == ButtonType.OK);
        FXApplication.applyDBStyle((Stage)getDialogPane().getScene().getWindow());
    }

}