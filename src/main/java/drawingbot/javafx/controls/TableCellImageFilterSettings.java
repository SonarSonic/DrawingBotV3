package drawingbot.javafx.controls;

import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.GenericSetting;
import javafx.collections.ObservableList;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class TableCellImageFilterSettings extends TableCell<ObservableImageFilter, ObservableList<GenericSetting<?, ?>>> {

    public TableCellImageFilterSettings() {
        super();
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    protected void updateItem(ObservableList<GenericSetting<?, ?>> item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.toString());
            if (item.isEmpty()) {
                setGraphic(null);
            } else {
                HBox hBox = new HBox();
                hBox.maxWidth(getWidth());
                hBox.maxHeight(getHeight());
                for (GenericSetting<?, ?> setting : item) {
                    TextField field = new TextField();
                    StringConverterGenericSetting<?> stringConverter = new StringConverterGenericSetting<>(() -> setting);
                    field.setText(setting.getValueAsString());
                    field.setOnAction(e -> {
                        Object obj = stringConverter.fromString(field.getText());
                        setting.setValue(obj);
                        if (!field.getText().equals(setting.getValueAsString())) {
                            field.setText(setting.getValueAsString());
                        }
                    });
                    field.setMaxWidth(80);
                    field.maxHeight(getHeight());
                    Label label = new Label(setting.key.getValue() + ": ");
                    label.setGraphic(field);
                    label.setContentDisplay(ContentDisplay.RIGHT);
                    hBox.getChildren().add(label);
                }
                setGraphic(hBox);
            }

        }
    }

}
