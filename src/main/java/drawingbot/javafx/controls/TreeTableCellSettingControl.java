package drawingbot.javafx.controls;

import drawingbot.javafx.GenericSetting;
import javafx.scene.control.TreeTableCell;

public class TreeTableCellSettingControl extends TreeTableCell<GenericSetting<?, ?>, Object> {

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("");
            if(getTableRow() != null && getTableRow().getItem() != null){
                setGraphic(getTableRow().getItem().getJavaFXEditor(false));
            }else{
                setGraphic(null);
            }
        }
    }

}
