package drawingbot.javafx.controls;

import drawingbot.geom.masking.MaskingSettings;
import drawingbot.image.ImageFilterSettings;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.render.shapes.JFXShape;
import javafx.beans.property.Property;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;

public class ContextMenuMaskingShape extends ContextMenu {

    public ContextMenuMaskingShape(TableRow<JFXShape> row, Property<MaskingSettings> maskingSettingsProperty) {
        super();
        FXHelper.addDefaultTableViewContextMenuItems(this, row, () -> maskingSettingsProperty.getValue().getMasks(), f -> maskingSettingsProperty.getValue().addShape(f.copy()));
    }
}
