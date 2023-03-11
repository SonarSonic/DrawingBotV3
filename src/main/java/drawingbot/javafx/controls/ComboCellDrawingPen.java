package drawingbot.javafx.controls;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSets;
import drawingbot.image.ImageTools;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

public class ComboCellDrawingPen extends ComboBoxListCell<DrawingPen> {

    public final HBox hbox;
    public final CheckBox checkBox;
    public final Rectangle colour;
    public Property<DrawingSets> drawingSets;
    private BooleanProperty property;

    public ComboCellDrawingPen(Property<DrawingSets> drawingSets, boolean useCheckBox) {
        super();
        this.drawingSets = drawingSets;
        hbox = new HBox();

        if (useCheckBox) {
            property = new SimpleBooleanProperty(false);
            checkBox = new CheckBox();
            checkBox.selectedProperty().bindBidirectional(property);
            checkBox.setOnAction(event -> {
                if(checkBox.isSelected()){
                    drawingSets.getValue().activeDrawingSet.get().addNewPen(getItem(), true);
                }else{
                    drawingSets.getValue().activeDrawingSet.get().pens.removeIf((p) -> p.getCodeName().equals(getItem().getCodeName()));
                }
            });
            hbox.setSpacing(10);
            hbox.getChildren().add(checkBox);
            hbox.setAlignment(Pos.CENTER_LEFT);
        } else {
            checkBox = null;
        }
        colour = new Rectangle(20, 12, Color.AQUA);
        hbox.getChildren().add(colour);
    }

    @Override
    public void updateItem(DrawingPen item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("  " + item.toString());
            setGraphic(hbox);
            if (checkBox != null) {
                property.set(drawingSets.getValue().activeDrawingSet.get().containsPen(item));
            }
            colour.setFill(ImageTools.getColorFromARGB(item.getARGB()));
        }
    }
}
