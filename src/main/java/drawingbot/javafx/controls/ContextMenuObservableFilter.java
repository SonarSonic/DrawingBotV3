package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.FXHelper;
import drawingbot.image.filters.ObservableImageFilter;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;

public class ContextMenuObservableFilter extends ContextMenu {

    public ContextMenuObservableFilter(TableRow<ObservableImageFilter> row) {
        super();
        MenuItem menuEditFilter = new MenuItem("Edit Settings");
        menuEditFilter.setOnAction(e -> FXHelper.openImageFilterDialog(row.getItem()));
        getItems().add(menuEditFilter);

        FXHelper.addDefaultTableViewContextMenuItems(this, row, DrawingBotV3.INSTANCE.currentFilters, f -> DrawingBotV3.INSTANCE.currentFilters.add(new ObservableImageFilter(f)));
    }
}
