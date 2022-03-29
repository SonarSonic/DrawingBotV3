package drawingbot.javafx.controls;

import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.image.ImageTools;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class ComboCellDrawingSet<S extends IDrawingSet<?>> extends ComboBoxListCell<S> {

    public ComboCellDrawingSet() {
        super();

    }

    @Override
    public void updateItem(S item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("  " + item.toString());
            setGraphic(createPenPalette(item.getPens()));
        }
    }

    public static HBox createPenPalette(List<? extends IDrawingPen> drawingPens){
        HBox box = new HBox();
        int minColourWidth = 2;
        int maxColourWidth = 20;
        int penTotal = drawingPens.size();
        int fullRenderWidth = Math.min(60, penTotal * maxColourWidth);
        int renderTotal = Math.min(penTotal, fullRenderWidth / minColourWidth);
        int renderWidth = fullRenderWidth / Math.max(1, renderTotal);
        for (int i = 0; i < renderTotal; i++) {
            IDrawingPen pen = drawingPens.get(i);
            box.getChildren().add(new Rectangle(renderWidth, 12, ImageTools.getColorFromARGB(pen.getARGB())));
        }
        int remainder = fullRenderWidth - (renderWidth * renderTotal);
        if (remainder != 0) { //add spacer
            box.getChildren().add(new Rectangle(remainder, 12, Color.TRANSPARENT));
        }
        return box;
    }
}
