package drawingbot.javafx.controls;

import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.EditorFactoryCache;
import javafx.scene.control.TableCell;

public class TableCellSettingControl extends TableCell<GenericSetting<?, ?>, Object> {

    public EditorFactoryCache editorCache;

    public TableCellSettingControl(EditorFactoryCache editorCache){
        this.editorCache = editorCache;
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("");
            if(getTableRow() != null && getTableRow().getItem() != null){
                setGraphic(editorCache.getOrCreateEditor(getTableRow().getItem()).getNode());
            }else{
                setGraphic(null);
            }
        }
    }

}
