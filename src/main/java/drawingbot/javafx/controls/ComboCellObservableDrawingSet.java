package drawingbot.javafx.controls;

import drawingbot.javafx.observables.ObservableDrawingSet;
import javafx.scene.control.cell.ComboBoxListCell;

public class ComboCellObservableDrawingSet extends ComboBoxListCell<ObservableDrawingSet> {

    public ComboCellObservableDrawingSet() {
        super();
    }

    @Override
    public void updateItem(ObservableDrawingSet item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("  " + item.toString());
            setGraphic(new ControlPenPalette(item.pens));
        }
    }

}
