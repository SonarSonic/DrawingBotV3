package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.utils.EnumColourSplitter;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DialogColourSeperationMode extends Dialog<Boolean> {

    public DialogColourSeperationMode(EnumColourSplitter splitter) {
        super();
        setTitle("Apply " + splitter.name() + " Colour Seperation - Pen Settings");
        String contentText = "Would you like to apply the recommended settings?";
        if(splitter != EnumColourSplitter.DEFAULT){
            contentText += "\n\n You will need to re-plot to create the CMYK image";
            contentText += "\n Plots will differ from the preview";
        }
        setContentText(contentText);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        setResultConverter(param -> param == ButtonType.APPLY);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());
    }

}