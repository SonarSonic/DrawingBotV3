package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.files.exporters.HPGLBuilder;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.utils.EnumColourSplitter;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.Map;

public class DialogExportHPGLComplete extends Dialog<Boolean> {

    public ButtonType sendToPlotter = new ButtonType("Send to Plotter", ButtonBar.ButtonData.OK_DONE);

    public DialogExportHPGLComplete(HPGLBuilder builder) {
        super();
        setTitle("HPGL File Exported");

        int minX = builder.getHPGLXValue(0, true);
        int maxX = builder.getHPGLXValue(builder.getEffectivePageWidth(), true);

        int minY = builder.getHPGLYValue(0, true);
        int maxY = builder.getHPGLYValue(builder.getEffectivePageHeight(), true);

        TextFlow flow = new TextFlow();

        boolean success = builder.withinHPGLRange(minX, minY, maxX, maxY);

        FXHelper.addText(flow, 14, "bold", "Status: ");
        FXHelper.addText(flow, "-fx-stroke:" + (!success ? "orange" : "green") + "; -fx-font-size: 14px; -fx-font-weight: normal", (success ? "OK" : "WARNING - Exceeds Hard Clip Limits"));

        float plotterWidthHPGL = HPGLBuilder.fromHPGL(Math.abs(DrawingBotV3.INSTANCE.hpglXMin.get()) + DrawingBotV3.INSTANCE.hpglXMax.get());
        float plotterHeightHPGL = HPGLBuilder.fromHPGL(Math.abs(DrawingBotV3.INSTANCE.hpglYMin.get()) + DrawingBotV3.INSTANCE.hpglYMax.get());

        FXHelper.addText(flow, 14, "bold", "\n\nMax Plottable Area:");
        FXHelper.addText(flow, 14, "normal", "\nWidth: " + plotterWidthHPGL + " mm" + "     Height: " + plotterHeightHPGL + " mm");

        FXHelper.addText(flow, 14, "bold", "\n\nDrawing Size:");
        FXHelper.addText(flow, 14, "normal", "\nWidth: " + builder.getEffectivePageWidth() + " mm" + "     Height: " + builder.getEffectivePageHeight() + " mm");

        if(!success){
            FXHelper.addText(flow, 14, "bold", "\n\nPlotter Hard Clip Limits:");
            FXHelper.addText(flow, 14, "normal", "\nMin: " + DrawingBotV3.INSTANCE.hpglXMin.get() + ", " + DrawingBotV3.INSTANCE.hpglYMin.get() + " Max: " + DrawingBotV3.INSTANCE.hpglXMax.get() + ", " + DrawingBotV3.INSTANCE.hpglYMax.get());

            FXHelper.addText(flow, 14, "bold", "\n\nDrawing Bounds:");
            FXHelper.addText(flow, 14, "normal", "\nMin: " + minX + ", " + minY + " Max: " + maxX + ", " + maxY);

            /*
            FXHelper.addText(flow, 14, "bold", "\n\nPen Bounds:");
            FXHelper.addText(flow, 14, "normal", "\nMin: " + ((int)builder.dx.min) + ", " + ((int)builder.dy.min) + " Max: " + ((int)builder.dx.max) + ", " + ((int)builder.dy.max));
             */

            FXHelper.addText(flow, 12, "normal",  "\n\n\n(HPGL Units: 40 units = 1 mm)");

        }
        FXHelper.addText(flow, 14, "bold", "\n\nPen Carousel:\n");
        for(Map.Entry<Integer, ObservableDrawingPen> drawingPenEntry : builder.drawingPenMap.entrySet()){
            flow.getChildren().add(new Rectangle(12, 12, drawingPenEntry.getValue().javaFXColour.getValue()));
            FXHelper.addText(flow, 12, "bold", " Pen " + drawingPenEntry.getKey() + ": ");
            FXHelper.addText(flow, 12, "normal", drawingPenEntry.getValue().getName() + "\n");
        }

        setGraphic(flow);

        getDialogPane().setPrefWidth(500);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().getButtonTypes().add(sendToPlotter);
        setResultConverter(param -> param == sendToPlotter);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());
    }

}