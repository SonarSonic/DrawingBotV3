package drawingbot.javafx.controls;

import drawingbot.files.json.IJsonData;
import drawingbot.javafx.GenericPreset;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class ComboCellPreset<D extends IJsonData> extends ComboBoxListCell<GenericPreset<D>> {

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
                box.getChildren().add(new Label("  " + item.getDisplayName()));
                if(item.userCreated){
                    Label userLabel = new Label(" (User)");
                    userLabel.setTextFill(new Color(0/255F, 200/255F, 130/255F, 1.0));
                    box.getChildren().add(userLabel);
                }

                node = box;
            }
            setText(null);
            setGraphic(node);
        }
    }
}
