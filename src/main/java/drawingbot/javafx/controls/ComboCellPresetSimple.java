package drawingbot.javafx.controls;

import drawingbot.javafx.GenericPreset;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class ComboCellPresetSimple<D> extends ComboBoxListCell<GenericPreset<D>> {

    public ComboCellPresetSimple() {
        super();
    }

    @Override
    public void updateItem(GenericPreset<D> item, boolean empty) {
        // Remove the old binding
        textProperty().unbind();

        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            textProperty().bind(item.presetNameProperty());
            setGraphic(null);
        }
    }
}
