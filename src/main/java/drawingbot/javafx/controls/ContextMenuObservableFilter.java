package drawingbot.javafx.controls;

import drawingbot.image.ImageFilterRegistry;
import drawingbot.javafx.FXController;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableRow;

public class ContextMenuObservableFilter extends ContextMenu {

    public ContextMenuObservableFilter(TableRow<ImageFilterRegistry.ObservableImageFilter> row) {
        super();

        FXController.addDefaultTableViewContextMenuItems(this, row, ImageFilterRegistry.currentFilters, f -> ImageFilterRegistry.currentFilters.add(new ImageFilterRegistry.ObservableImageFilter(f)));
    }
}
