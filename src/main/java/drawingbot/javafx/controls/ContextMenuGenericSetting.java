package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.CategorySetting;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.MenuItem;

import java.util.concurrent.ThreadLocalRandom;

public class ContextMenuGenericSetting extends ContextMenu {

    public ContextMenuGenericSetting(IndexedCell<GenericSetting<?, ?>> row, ObjectProperty<ObservableList<GenericSetting<?, ?>>> settingsProp, boolean pfmSettings) {
        super();

        MenuItem menuDelete = new MenuItem("Randomise");
        menuDelete.setOnAction(e -> {
            GenericSetting<?, ?> setting = row.getItem();
            if(setting instanceof CategorySetting){
                CategorySetting<?> categorySetting = (CategorySetting<?>) setting;
                for(GenericSetting<?, ?> other : settingsProp.get()){
                    if(other.getCategory().equals(categorySetting.getCategory())){
                        other.randomise(ThreadLocalRandom.current());
                    }
                }
                if(pfmSettings)
                    DrawingBotV3.project().onPFMSettingsUserEdited();
            }else{
                setting.randomise(ThreadLocalRandom.current());
                if(pfmSettings)
                    DrawingBotV3.project().onPFMSettingsUserEdited();
            }
        });
        getItems().add(menuDelete);

        MenuItem menuDuplicate = new MenuItem("Reset");
        menuDuplicate.setOnAction(e -> {
            GenericSetting<?, ?> setting = row.getItem();
            if(setting instanceof CategorySetting){
                CategorySetting<?> categorySetting = (CategorySetting<?>) setting;
                for(GenericSetting<?, ?> other : settingsProp.get()){
                    if(other.getCategory().equals(categorySetting.getCategory())){
                        other.resetSetting();
                        if(pfmSettings)
                            DrawingBotV3.project().onPFMSettingsUserEdited();
                    }
                }
            }else {
                setting.resetSetting();
                if(pfmSettings)
                    DrawingBotV3.project().onPFMSettingsUserEdited();
            }
        });
        getItems().add(menuDuplicate);
    }
}
