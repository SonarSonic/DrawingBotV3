package drawingbot.javafx;

import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class ColorTableCell<T> extends TableCell<T, Color> {
    private final ColorPicker colorPicker;

    public ColorTableCell(TableColumn<T, Color> column) {
        this.colorPicker = new ColorPicker();
        this.colorPicker.editableProperty().bind(column.editableProperty());
        this.colorPicker.disableProperty().bind(column.editableProperty().not());
        this.colorPicker.setOnShowing(event -> {
            final TableView<T> tableView = getTableView();
            tableView.getSelectionModel().select(getTableRow().getIndex());
            tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);       
        });
        this.colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(isEditing()) {
                commitEdit(newValue);
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