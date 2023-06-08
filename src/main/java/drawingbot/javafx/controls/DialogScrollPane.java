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
        super();
        setTitle(title);

        scrollPane = new ScrollPane();
        //scrollPane.setFitToWidth(true);
        //HBox.setHgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setContent(content);
        //getDialogPane().setMaxHeight(600);
        getDialogPane().getStylesheets().add(FXHelper.class.getResource("preference-styles.css").toExternalForm());

        getDialogPane().setContent(scrollPane);
        //getDialogPane().setMaxHeight(600);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
        setResultConverter(param -> param == ButtonType.OK);
        FXApplication.applyDBStyle((Stage)getDialogPane().getScene().getWindow());
    }

}