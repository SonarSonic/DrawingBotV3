package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;

import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.RangedNumberSetting;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class DialogImageFilter extends Dialog<ObservableImageFilter> {

    public DialogImageFilter(ObservableImageFilter filter) {
        super();
        ObservableImageFilter original = new ObservableImageFilter(filter);

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_LEFT);

        gridPane.setVgap(4);
        gridPane.setHgap(4);

        CheckBox checkBox = new CheckBox("Enabled");
        checkBox.selectedProperty().bindBidirectional(filter.enable);

        gridPane.addRow(0, checkBox);

        int i = 1;
        for(GenericSetting<?, ?> setting : filter.filterSettings){
            Label label = new Label(setting.settingName.getValue() + ": ");
            label.setAlignment(Pos.TOP_LEFT);
            Node node = setting.getJavaFXNode(true);
            node.minWidth(200);
            node.prefHeight(30);

            if(!(setting instanceof RangedNumberSetting)){
                //check boxes don't need a value label.
                gridPane.addRow(i, label, node);
            }else{
                TextField field = setting.getEditableTextField();
                gridPane.addRow(i, label, node, setting.getEditableTextField());
                field.setOnAction(e -> {
                    setting.setValueFromString(field.getText());
                    DrawingBotV3.INSTANCE.onImageFiltersChanged();
                });
            }
            node.setOnMouseReleased(e -> DrawingBotV3.INSTANCE.onImageFiltersChanged()); //change on mouse release, not on value change
            i++;
        }
        setGraphic(gridPane);
        setTitle("Image Filter: " + filter.name.getValue());
        getDialogPane().setPrefWidth(400);
        setResultConverter(param -> param == ButtonType.APPLY ? filter : original);
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());

    }

}
