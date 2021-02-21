package drawingbot.javafx.controls;

import drawingbot.utils.GenericSetting;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;

import java.util.concurrent.ThreadLocalRandom;

public class ContextMenuPFMSetting extends ContextMenu {

    public ContextMenuPFMSetting(TableRow<GenericSetting<?, ?>> row) {
        super();

        MenuItem menuDelete = new MenuItem("Randomise");
        menuDelete.setOnAction(e -> row.getItem().randomiseSetting(ThreadLocalRandom.current()));
        getItems().add(menuDelete);

        MenuItem menuDuplicate = new MenuItem("Reset");
        menuDuplicate.setOnAction(e -> row.getItem().resetSetting());
        getItems().add(menuDuplicate);
    }
}
