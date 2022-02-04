package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.pfm.PFMFactory;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;

public class ComboCellPFM extends ComboBoxListCell<PFMFactory<?>> {

    public ComboCellPFM() {
        super();
    }

    @Override
    public void updateItem(PFMFactory<?> item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if(item.isPremium() && !FXApplication.isPremiumEnabled){
                HBox box = new HBox();
                Label premiumLabel = new Label(" (Premium)");
                box.getChildren().add(new Label("  " + item.toString()));
                box.getChildren().add(premiumLabel);
                setText(null);
                setGraphic(box);
            }else if(item.isBeta()){
                HBox box = new HBox();
                Label betaLabel = new Label(" (Beta)");
                betaLabel.setStyle("-fx-font-weight: bold");
                box.getChildren().add(new Label("  " + item.toString()));
                box.getChildren().add(betaLabel);
                setText(null);
                setGraphic(box);
            }else{
                setText("  " + item.toString());
                setGraphic(null);
            }

        }
    }
}
