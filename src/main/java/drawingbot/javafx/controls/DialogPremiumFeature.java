package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.FXHelper;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class DialogPremiumFeature extends Dialog<Boolean> {

    public ButtonType upgrade = new ButtonType("Upgrade", ButtonBar.ButtonData.OK_DONE);

    public DialogPremiumFeature() {
        super();
        setTitle("Premium Feature");

        TextFlow flow = new TextFlow();

        FXHelper.addText(flow, 14, "bold", "This is a Premium Feature");
        FXHelper.addText(flow, 14, "normal", "\n" + "Upgrade to start using this feature!");

        setGraphic(flow);

        getDialogPane().setPrefWidth(500);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().getButtonTypes().add(upgrade);
        setResultConverter(param -> param == upgrade);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());
    }

}