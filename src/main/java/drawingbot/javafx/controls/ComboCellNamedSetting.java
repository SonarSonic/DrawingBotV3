package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.utils.INamedSetting;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class ComboCellNamedSetting<S extends INamedSetting> extends ComboBoxListCell<S> {

    public ComboCellNamedSetting() {
        super();
    }

    @Override
    public void updateItem(S item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            Node node = item.getDisplayNode();
            if(node == null){
                HBox box = new HBox();
                box.getChildren().add(new Label("  " + item.getDisplayName()));
                if(item.isPremiumFeature() && !FXApplication.isPremiumEnabled){
                    Label premiumLabel = new Label(" (Premium)");
                    box.getChildren().add(premiumLabel);
                }else if(!item.getReleaseState().isRelease()){
                    Label stateLabel = new Label(" (" + item.getReleaseState().getDisplayName() + ")");
                    stateLabel.setStyle("-fx-font-weight: bold");
                    stateLabel.setTextFill(item.getReleaseState().color);
                    box.getChildren().add(stateLabel);
                }
                if(item.isNewFeature()){
                    Label stateLabel = new Label(" (" + "New" + ")");
                    stateLabel.setStyle("-fx-font-weight: bold;");
                    stateLabel.setTextFill(new Color(0/255F, 147/255F, 255/255F, 1.0));
                    box.getChildren().add(stateLabel);
                }

                node = box;
            }
            setText(null);
            setGraphic(node);
        }
    }
}
