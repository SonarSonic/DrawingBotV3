package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.FXHelper;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class DialogScrollPane extends Dialog<Boolean> {

    public ScrollPane scrollPane;

    public DialogScrollPane(String title, Node content) {
        this(title, content, -1, -1);
    }

    public DialogScrollPane(String title, Node content, double prefWidth, double prefHeight) {
        super();
        setTitle(title);

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(content);
        getDialogPane().getStylesheets().add(FXHelper.class.getResource("preference-styles.css").toExternalForm());

        getDialogPane().setContent(scrollPane);
        getDialogPane().setPrefWidth(prefWidth);
        getDialogPane().setPrefHeight(prefHeight);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setResultConverter(param -> param == ButtonType.OK);
        FXApplication.applyTheme((Stage)getDialogPane().getScene().getWindow());
    }

}