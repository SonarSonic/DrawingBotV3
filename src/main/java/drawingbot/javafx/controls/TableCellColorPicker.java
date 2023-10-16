package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

/**embeds a usable color picker into a TableCell, used for pen colour selection*/
public class TableCellColorPicker<T> extends TableCell<T, Color> {
    private final ColorPicker colorPicker;

    public TableCellColorPicker(TableColumn<T, Color> column) {
        this.colorPicker = new ColorPicker();
        this.colorPicker.editableProperty().bind(column.editableProperty());
        this.colorPicker.disableProperty().bind(column.editableProperty().not());
        this.colorPicker.setOnShowing(event -> {
            final TableView<T> tableView = getTableView();
            tableView.getSelectionModel().select(getTableRow().getIndex());
            tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);       
        });
        this.colorPicker.setOnHidden(event -> {
            if (isEditing()) {
                commitEdit(this.colorPicker.getValue());
            }
        });
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(Color item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);  
        if(empty) {     
            setGraphic(null);
        } else {        
            this.colorPicker.setValue(item);
            this.setGraphic(this.colorPicker);
        } 
    }
}