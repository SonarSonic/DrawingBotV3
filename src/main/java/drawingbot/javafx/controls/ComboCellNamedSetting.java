package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.utils.INamedSetting;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;

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
            HBox box = new HBox();
            box.getChildren().add(new Label("  " + item.getDisplayName()));
            if(item.isPremiumFeature() && !FXApplication.isPremiumEnabled){
                Label premiumLabel = new Label(" (Premium)");
                box.getChildren().add(premiumLabel);
            }else if(!item.getReleaseState().isRelease()){
                Label stateLabel = new Label(" (" + item.getReleaseState().getDisplayName() + ")");
                stateLabel.setStyle("-fx-font-weight: bold");
                box.getChildren().add(stateLabel);
            }
            setText(null);
            setGraphic(box);
        }
    }
}
