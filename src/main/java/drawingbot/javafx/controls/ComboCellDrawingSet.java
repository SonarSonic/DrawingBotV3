package drawingbot.javafx.controls;

import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.image.ImageTools;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ComboCellDrawingSet extends ComboBoxListCell<IDrawingSet<IDrawingPen>> {

    public ComboCellDrawingSet() {
        super();

    }

    @Override
    public void updateItem(IDrawingSet<IDrawingPen> item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("  " + item.toString());
            HBox box = new HBox();
            int minColourWidth = 2;
            int maxColourWidth = 20;
            int penTotal = item.getPens().size();
            int fullRenderWidth = Math.min(60, penTotal * maxColourWidth);
            int renderTotal = Math.min(penTotal, fullRenderWidth / minColourWidth);
            int renderWidth = fullRenderWidth / renderTotal;
            for (int i = 0; i < renderTotal; i++) {
                IDrawingPen pen = item.getPens().get(i);
                box.getChildren().add(new Rectangle(renderWidth, 12, ImageTools.getColorFromARGB(pen.getARGB())));
            }
            int remainder = fullRenderWidth - (renderWidth * renderTotal);
            if (remainder != 0) { //add spacer
                box.getChildren().add(new Rectangle(remainder, 12, Color.TRANSPARENT));
            }
            setGraphic(box);
        }
    }
}
