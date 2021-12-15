package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.files.exporters.HPGLBuilder;
import drawingbot.javafx.FXHelper;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class DialogExportHPGLHardLimitWarning extends Dialog<Boolean> {

    public ButtonType update = new ButtonType("Update Hard-Clip Limits", ButtonBar.ButtonData.OK_DONE);

    public DialogExportHPGLHardLimitWarning(int[] hardLimits) {
        super();
        setTitle("HPGL - Warning: Hard-Clip Limits");

        TextFlow flow = new TextFlow();

        FXHelper.addText(flow, 14, "bold", "Status: ");
        FXHelper.addText(flow, "-fx-stroke:" + "orange" + "; -fx-font-size: 14px; -fx-font-weight: normal", "WARNING - Hard-Clip Limits Don't Match");

        FXHelper.addText(flow, 14, "bold", "\n\nPlotter Hard-Clip Limits (Detected via OH;):");
        FXHelper.addText(flow, 14, "normal", "\nMin: " + hardLimits[0] + ", " + hardLimits[1] + " Max: " + hardLimits[2] + ", " + hardLimits[3]);

        FXHelper.addText(flow, 14, "bold", "\n\nSelected Hard-Clip Limits:");
        FXHelper.addText(flow, 14, "normal", "\nMin: " + DrawingBotV3.INSTANCE.hpglXMin.get() + ", " + DrawingBotV3.INSTANCE.hpglYMin.get() + " Max: " + DrawingBotV3.INSTANCE.hpglXMax.get() + ", " + DrawingBotV3.INSTANCE.hpglYMax.get());

        FXHelper.addText(flow, 14, "bold", "\n\nIf you choose to update the limits you will be prompted to re-export the HPGL");
        setGraphic(flow);

        getDialogPane().setPrefWidth(500);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().getButtonTypes().add(update);
        setResultConverter(param -> param == update);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());
    }

}