package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMSettings;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.binding.Binding;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.cell.ComboBoxListCell;
import org.fxmisc.easybind.EasyBind;

public class ComboCellDistributionType extends ComboBoxListCell<EnumDistributionType> {

    public static Binding<PFMFactory<?>> binding = null;
    public final ChangeListener<PFMFactory<?>> changeListener = (observable, oldValue, newValue) -> updateText();

    public ComboCellDistributionType() {
        super();
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
