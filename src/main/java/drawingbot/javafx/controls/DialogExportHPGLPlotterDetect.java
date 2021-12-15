package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.files.exporters.HPGLBuilder;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.FXSerialConnectionController;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class DialogExportHPGLPlotterDetect extends Dialog<Boolean> {

    public ButtonType update = new ButtonType("Apply All", ButtonBar.ButtonData.OK_DONE);
    public float plotterWidthHPGL;
    public float plotterHeightHPGL;

    public DialogExportHPGLPlotterDetect(String id, int[] hardLimits) {
        super();
        setTitle("HPGL - Warning: Hard-Clip Limits");

        TextFlow flow = new TextFlow();

        boolean plotterDetected = !id.isEmpty() || hardLimits != null;

        FXHelper.addText(flow, 14, "bold", "Plotter Detected: ");
        FXHelper.addText(flow, "-fx-stroke:" + (!plotterDetected ? "orange" : "green") + "; -fx-font-size: 14px; -fx-font-weight: normal", (plotterDetected ? "YES" : "NO - Check the plotter is connected and has paper loaded properly"));

        if(!id.isEmpty()){
            FXHelper.addText(flow, 14, "bold", "\n\nPlotter Identification (OI;) ");
            FXHelper.addText(flow, 14, "normal", "\n" + id);
        }

        if(hardLimits != null){
            FXHelper.addText(flow, 14, "bold", "\n\nPlotter Hard-Clip Limits (OH;) ");
            FXHelper.addText(flow, 14, "normal", "\nMin: " + hardLimits[0] + ", " + hardLimits[1] + " Max: " + hardLimits[2] + ", " + hardLimits[3]);

            FXHelper.addText(flow, 14, "normal", "\n\n");

            Button applyHardClip = new Button("Apply Hard-Clip Limits");
            applyHardClip.setOnAction(e -> FXSerialConnectionController.applyHardClipLimits(hardLimits, false));
            flow.getChildren().add(applyHardClip);


            plotterWidthHPGL = HPGLBuilder.fromHPGL(Math.abs(hardLimits[0]) + hardLimits[2]);
            plotterHeightHPGL = HPGLBuilder.fromHPGL(Math.abs(hardLimits[1]) + hardLimits[3]);

            FXHelper.addText(flow, 14, "bold", "\n\nMax Plottable Area:");
            FXHelper.addText(flow, 14, "normal", "\nWidth: " + plotterWidthHPGL + " mm" + "     Height: " + plotterHeightHPGL + " mm");

            FXHelper.addText(flow, 14, "normal", "\n\n");

            Button applyDrawingAreaSize = new Button("Apply Drawing Area Size");
            applyDrawingAreaSize.setOnAction(e -> FXSerialConnectionController.applyDrawingArea(plotterWidthHPGL, plotterHeightHPGL));
            flow.getChildren().add(applyDrawingAreaSize);

        }

        setGraphic(flow);

        getDialogPane().setPrefWidth(600);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        if(plotterDetected){
            getDialogPane().getButtonTypes().add(update);
        }
        setResultConverter(param -> plotterDetected && param == update);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());
    }

}