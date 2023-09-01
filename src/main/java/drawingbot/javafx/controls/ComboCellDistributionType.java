package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMSettings;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.binding.Binding;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.fxmisc.easybind.EasyBind;

public class ComboCellDistributionType extends ComboBoxListCell<EnumDistributionType> {

    public static Binding<PFMFactory<?>> binding = null;
    public final ChangeListener<PFMFactory<?>> changeListener = (observable, oldValue, newValue) -> updateText();
    public final boolean isButtonCell;

    public ComboCellDistributionType(boolean isButtonCell) {
        super();
        this.isButtonCell = isButtonCell;
    }

    @Override
    public void updateItem(EnumDistributionType item, boolean empty) {
        super.updateItem(item, empty);

        if(binding == null){
            binding = EasyBind.select(DrawingBotV3.INSTANCE.activeProject).select(ObservableProject::pfmSettingsProperty).selectObject(PFMSettings::factoryProperty);
        }

        if (empty || item == null) {
            binding.removeListener(changeListener);

            setText(null);
            setGraphic(null);
        } else {
            binding.addListener(changeListener);

            setGraphic(null);
            setText(null);
            updateText();
        }
    }

    public void updateText(){
        if(isButtonCell){
            setText(getItem().displayName);
            setGraphic(null);
            return;
        }

        HBox box = new HBox();
        Label displayName = new Label(getItem().displayName);
        box.getChildren().add(displayName);

        if(getItem() == EnumDistributionType.getRecommendedType()){
            Label stateLabel = new Label(" (" + "Recommended" + ")");
            stateLabel.setStyle("-fx-font-weight: bold");
            stateLabel.setTextFill(new Color(0/255F, 200/255F, 120/255F, 1.0));
            box.getChildren().add(stateLabel);
        }

        setText(null);
        setGraphic(box);
    }
}
