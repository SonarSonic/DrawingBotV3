package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.observables.ObservableProjectSettings;
import drawingbot.registry.Register;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableRow;

import java.io.File;

public class ContextMenuObservableProjectSettings extends ContextMenu {

    public ContextMenuObservableProjectSettings(TableRow<ObservableProjectSettings> row) {
        super();

        MenuItem menuLoad = new MenuItem("Load Version");
        menuLoad.setOnAction(e -> Register.PRESET_LOADER_PROJECT.getDefaultManager().applyPreset(row.getItem().preset.get()));
        getItems().add(menuLoad);

        MenuItem menuSave = new MenuItem("Save as project");
        menuSave.setOnAction(e -> FXHelper.exportPreset(row.getItem().preset.get(), new File(row.getItem().preset.get().data.imagePath).getParentFile(), row.getItem().name.get(), false));
        getItems().add(menuSave);


        getItems().add(new SeparatorMenuItem());

        MenuItem menuMoveUp = new MenuItem("Move Up");
        menuMoveUp.setOnAction(e -> FXHelper.moveItemUp(row.getTableView().getSelectionModel(), DrawingBotV3.INSTANCE.projectVersions));
        getItems().add(menuMoveUp);

        MenuItem menuMoveDown = new MenuItem("Move Down");
        menuMoveDown.setOnAction(e -> FXHelper.moveItemDown(row.getTableView().getSelectionModel(), DrawingBotV3.INSTANCE.projectVersions));
        getItems().add(menuMoveDown);

        getItems().add(new SeparatorMenuItem());

        MenuItem menuDelete = new MenuItem("Delete");
        menuDelete.setOnAction(e -> FXHelper.deleteItem(row.getTableView().getSelectionModel(), DrawingBotV3.INSTANCE.projectVersions));
        getItems().add(menuDelete);
    }

}
