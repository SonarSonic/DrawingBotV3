package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Supplier;

public class DialogGenericRename extends Dialog<String> {

    public TextField textField;

    public DialogGenericRename(Supplier<String> valueSupplier) {
        super();
        final String value = valueSupplier.get();
        VBox vBox = new VBox();
        vBox.setSpacing(8);

        Label nameFieldLabel = new Label("Rename: ");
        textField = new TextField();
        textField.setText(value);
        nameFieldLabel.setGraphic(textField);
        nameFieldLabel.setContentDisplay(ContentDisplay.RIGHT);
        vBox.getChildren().add(nameFieldLabel);

        setGraphic(vBox);
        setResultConverter(param -> param == ButtonType.APPLY ? textField.getText() : value);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        FXApplication.applyDBStyle((Stage)getDialogPane().getScene().getWindow());
    }


}