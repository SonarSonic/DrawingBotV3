package drawingbot.javafx.controls;

import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class SkinDirectoryPicker extends SkinBase<ControlDirectoryPicker> {

    protected SkinDirectoryPicker(ControlDirectoryPicker control) {
        super(control);

        boolean isFile = control instanceof ControlFilePicker;

        TextField textField = new TextField();
        textField.promptTextProperty().bind(control.windowTitleProperty());
        textField.setOnMouseClicked(e -> {
            if(e.isPrimaryButtonDown() && e.getClickCount() >= 2 && textField.getText().isEmpty()){
                getSkinnable().showPickerDialog();
            }
        });
        textField.textProperty().bindBidirectional(control.valueProperty());
        textField.setOnAction(e -> getSkinnable().doEdit());

        Button button = new Button("", new HBox(8, new Glyph("FontAwesome", (isFile ? FontAwesome.Glyph.FILE : FontAwesome.Glyph.FOLDER).getChar())));
        button.setMaxWidth(25);
        button.setOnAction(e -> {
            getSkinnable().showPickerDialog();
        });

        HBox hBox = new HBox(4, textField, button);
        HBox.setHgrow(hBox, Priority.ALWAYS);
        HBox.setHgrow(textField, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.NEVER);
        hBox.setPrefWidth(-1);

        getChildren().add(hBox);

    }


}
