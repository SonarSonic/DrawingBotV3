package drawingbot.javafx.controls;

import drawingbot.image.ImageFilterSettings;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.observables.ObservableImageFilter;
import javafx.beans.property.Property;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;

public class ContextMenuObservableFilter extends ContextMenu {

    public ContextMenuObservableFilter(TableRow<ObservableImageFilter> row, Property<ImageFilterSettings> imgFilterProperty) {
        super();
        MenuItem menuEditFilter = new MenuItem("Edit Settings");
        menuEditFilter.setOnAction(e -> FXHelper.openImageFilterDialog(row.getItem()));
        getItems().add(menuEditFilter);

        FXHelper.addDefaultTableViewContextMenuItems(this, row, () -> imgFilterProperty.getValue().currentFilters.get(), ObservableImageFilter::new);
    }
}
