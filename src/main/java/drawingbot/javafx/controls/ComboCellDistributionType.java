package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.pfm.PFMFactory;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.cell.ComboBoxListCell;

public class ComboCellDistributionType extends ComboBoxListCell<EnumDistributionType> {

    public final ChangeListener<PFMFactory<?>> changeListener = (observable, oldValue, newValue) -> updateText();

    public ComboCellDistributionType() {
        super();
    }

    @Override
    public void updateItem(EnumDistributionType item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            DrawingBotV3.INSTANCE.pfmSettings.factory.removeListener(changeListener);

            setText(null);
            setGraphic(null);
        } else {
            DrawingBotV3.INSTANCE.pfmSettings.factory.addListener(changeListener);

            updateText();
            setGraphic(null);
        }
    }

    public void updateText(){
        if(getItem() == EnumDistributionType.getRecommendedType()){
            setText(getItem().toString() + " (Recommended)");
            return;
        }
        setText(getItem().toString());
    }
}
