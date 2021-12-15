package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.FXHelper;
import drawingbot.utils.EnumColourSplitter;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class DialogColourSeperationMode extends Dialog<Boolean> {

    public DialogColourSeperationMode(EnumColourSplitter splitter) {
        super();

        setTitle("Apply " + splitter.name() + " Colour Seperation - Pen Settings");

        TextFlow flow = new TextFlow();

        FXHelper.addText(flow, 14, "bold", "Would you like to apply the recommended settings?");
        FXHelper.addText(flow, 12, "normal", "\n\n You will need to re-plot to create the CMYK image");
        FXHelper.addText(flow, 12, "normal", "\n Plots will differ from the preview");

        getDialogPane().setPrefWidth(400);

        setGraphic(flow);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        setResultConverter(param -> param == ButtonType.APPLY);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());
    }

}