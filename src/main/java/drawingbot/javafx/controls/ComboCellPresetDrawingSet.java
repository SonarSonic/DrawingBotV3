package drawingbot.javafx.controls;

import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.preferences.DBPreferences;
import javafx.beans.InvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class ComboCellPresetDrawingSet extends ComboBoxListCell<GenericPreset<IDrawingSet>> {

    private ObservableDrawingSet currentDrawingSet = null;
    private final InvalidationListener graphicListener = observable -> setGraphic(ComboCellDrawingSet.createStaticPenPalette(currentDrawingSet.getPens()));
    private final InvalidationListener textListener = observable -> setText("  " + currentDrawingSet.getName());

    public ComboCellPresetDrawingSet() {
        super();
    }

    @Override
    public void updateItem(GenericPreset<IDrawingSet> item, boolean empty) {
        super.updateItem(item, empty);

        if(currentDrawingSet != null){
            currentDrawingSet.pens.removeListener(graphicListener);
            currentDrawingSet.name.removeListener(textListener);
            currentDrawingSet = null;
        }
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            setText("");
            hBox.getChildren().add(ComboCellDrawingSet.createStaticPenPalette(item.data.getPens()));
            Label displayNameLabel = new Label("  " + item.getPresetName());
            displayNameLabel.setPrefHeight(12);
            displayNameLabel.textFillProperty().bind(DBPreferences.INSTANCE.defaultThemeColor);

            hBox.getChildren().add(displayNameLabel);
            if(item.userCreated){
                hBox.getChildren().add(ComboCellPreset.createUserLabel());
            }
            setGraphic(hBox);

        }
    }
}
