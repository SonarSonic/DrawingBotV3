package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.DrawingSets;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.observables.ObservableDrawingPen;
import javafx.beans.property.Property;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableRow;

public class ContextMenuObservablePen extends ContextMenu {

    public ContextMenuObservablePen(Property<DrawingSets> drawingSets, TableRow<ObservableDrawingPen> row) {
        super();

        MenuItem increaseWeight = new MenuItem("Increase Weight");
        increaseWeight.setOnAction(e -> row.getItem().distributionWeight.set(row.getItem().distributionWeight.get() + 10));
        getItems().add(increaseWeight);

        MenuItem decreaseWeight = new MenuItem("Decrease Weight");
        decreaseWeight.setOnAction(e -> row.getItem().distributionWeight.set(Math.max(0, row.getItem().distributionWeight.get() - 10)));
        getItems().add(decreaseWeight);

        MenuItem resetWeight = new MenuItem("Reset Weight");
        resetWeight.setOnAction(e -> row.getItem().distributionWeight.set(100));
        getItems().add(resetWeight);

        getItems().add(new SeparatorMenuItem());

        MenuItem pickColour = new MenuItem("Pick Colour");
        pickColour.setOnAction(e -> DrawingBotV3.INSTANCE.controller.startColourPick(row.getItem()));
        getItems().add(pickColour);

        getItems().add(new SeparatorMenuItem());


        FXHelper.addDefaultTableViewContextMenuItems(this, row, () -> drawingSets.getValue().activeDrawingSet.get().pens, p -> new ObservableDrawingPen(drawingSets.getValue().activeDrawingSet.get().getPens().size(), p));
    }

}
