package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.observables.ObservableDrawingPen;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.util.List;

public class DialogExportNPens extends Dialog<Integer> {

    public final List<ObservableDrawingPen> activePens;
    private final SimpleIntegerProperty nPens = new SimpleIntegerProperty(6);
    private final TextFlow flow;

    public DialogExportNPens(List<ObservableDrawingPen> activePens){
        this.activePens = activePens;

        setTitle("Choose N Pens to Export");

        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(nPens, new NumberStringConverter());

        Label label = new Label("Max Pens per/file: ", textField);
        label.setContentDisplay(ContentDisplay.RIGHT);

        flow = new TextFlow();

        updatePenGroups();
        nPens.addListener((observable, oldValue, newValue) -> updatePenGroups());

        VBox vBox = new VBox();
        vBox.getChildren().addAll(label, flow);
        setGraphic(vBox);

        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        setResultConverter(param -> {
            textField.commitValue();
            return param == ButtonType.APPLY ? nPens.get() : -1;
        });
        FXApplication.applyDBStyle((Stage)getDialogPane().getScene().getWindow());
    }

    public void updatePenGroups(){
        flow.getChildren().clear();
        if(nPens.get() > 0) {
            int file = 1;
            for (int i = 0; i < activePens.size(); i += nPens.get()) {
                FXHelper.addText(flow, 6, "normal", "\n");
                FXHelper.addText(flow, 14, "bold", "File: " + file);
                FXHelper.addText(flow, 6, "normal", "\n");
                for (int j = 0; j < nPens.get(); j++) {
                    int index = i + j;
                    if (index < activePens.size()) {
                        ObservableDrawingPen pen = activePens.get(index);
                        flow.getChildren().add(new Rectangle(12, 12, pen.javaFXColour.getValue()));
                        FXHelper.addText(flow, 12, "normal", " " + pen.getName() + "\n");
                    }
                }
                file++;
            }
        }
        getDialogPane().getScene().getWindow().sizeToScene();
    }

}
