package drawingbot.javafx.controls;

import drawingbot.javafx.GenericPreset;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class ComboCellPreset<D> extends ComboBoxListCell<GenericPreset<D>> {

    public ComboCellPreset() {
        super();
    }

    @Override
    public void updateItem(GenericPreset<D> item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            Node node = item.getDisplayNode();
            if(node == null){
                HBox box = new HBox();
                Label nameLabel = new Label();
                nameLabel.textProperty().bind(item.presetNameProperty());
                box.getChildren().addAll(new Label("  "), nameLabel);
                if(item.overridesSystemPreset){
                    box.getChildren().add(createOverridesLabel());
                } else if(item.userCreated){
                    box.getChildren().add(createUserLabel());
                }
                node = box;
            }
            setText(null);
            setGraphic(node);
        }
    }

    public static Label createUserLabel(){
        Label userLabel = new Label(" (User)");
        userLabel.setTextFill(new Color(0/255F, 200/255F, 130/255F, 1.0));
        return userLabel;
    }

    public static Label createOverridesLabel(){
        Label userLabel = new Label(" (Overrides)");
        userLabel.setTextFill(new Color(200/255F, 0/255F, 130/255F, 1.0));
        return userLabel;
    }
}
