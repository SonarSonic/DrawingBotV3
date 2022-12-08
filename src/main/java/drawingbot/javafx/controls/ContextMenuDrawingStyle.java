package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.DrawingSets;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.observables.ObservableDrawingStyle;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableRow;

import java.util.function.Supplier;

public class ContextMenuDrawingStyle extends ContextMenu {

    public ContextMenuDrawingStyle(Supplier<ObservableList<ObservableDrawingStyle>> editingStyles, Supplier<DrawingSets> drawingsSets, TableRow<ObservableDrawingStyle> row) {
        super();

        if(DrawingBotV3.context().project.getPFMSettings().factory.get().isLayeredPFM()){
            MenuItem clearMaskColour = new MenuItem("Clear Mask Colour");
            clearMaskColour.setOnAction(e -> row.getItem().maskColor.set(null));
            getItems().add(clearMaskColour);
        }else{
            MenuItem increaseWeight = new MenuItem("Increase Weight");
            increaseWeight.setOnAction(e -> row.getItem().distributionWeight.set(row.getItem().distributionWeight.get() + 10));
            getItems().add(increaseWeight);

            MenuItem decreaseWeight = new MenuItem("Decrease Weight");
            decreaseWeight.setOnAction(e -> row.getItem().distributionWeight.set(Math.max(0, row.getItem().distributionWeight.get() - 10)));
            getItems().add(decreaseWeight);

            MenuItem resetWeight = new MenuItem("Reset Weight");
            resetWeight.setOnAction(e -> row.getItem().distributionWeight.set(100));
            getItems().add(resetWeight);
        }

        getItems().add(new SeparatorMenuItem());

        FXHelper.addDefaultTableViewContextMenuItems(this, row, editingStyles, style -> new ObservableDrawingStyle(drawingsSets.get(), style));
    }

}
