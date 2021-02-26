package drawingbot.javafx.controls;

import drawingbot.image.ImageFilterRegistry;
import drawingbot.javafx.FXController;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;

public class ContextMenuObservableFilter extends ContextMenu {

    public ContextMenuObservableFilter(TableRow<ImageFilterRegistry.ObservableImageFilter> row) {
        super();
        MenuItem menuEditFilter = new MenuItem("Edit Settings");
        menuEditFilter.setOnAction(e -> FXController.openImageFilterDialog(row.getItem()));
        getItems().add(menuEditFilter);

        FXController.addDefaultTableViewContextMenuItems(this, row, ImageFilterRegistry.currentFilters, f -> ImageFilterRegistry.currentFilters.add(new ImageFilterRegistry.ObservableImageFilter(f)));
    }
}
