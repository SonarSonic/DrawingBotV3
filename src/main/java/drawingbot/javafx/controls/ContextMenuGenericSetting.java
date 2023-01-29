package drawingbot.javafx.controls;

import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.CategorySetting;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;

import java.util.concurrent.ThreadLocalRandom;

public class ContextMenuGenericSetting extends ContextMenu {

    public ContextMenuGenericSetting(IndexedCell<GenericSetting<?, ?>> row, ObjectProperty<ObservableList<GenericSetting<?, ?>>> settingsProp) {
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
            }else{
                setting.randomise(ThreadLocalRandom.current());
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
                    }
                }
            }else {
                setting.resetSetting();
            }
        });
        getItems().add(menuDuplicate);
    }
}
