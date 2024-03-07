package drawingbot.javafx.controls;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class SkinPresetsButton<TARGET, DATA> extends SkinBase<ControlPresetButton<TARGET, DATA>> {

    protected SkinPresetsButton(ControlPresetButton<TARGET, DATA> control) {
        super(control);

        MenuButton menuButton = createDefaultMenuButton(control);
        getChildren().add(menuButton);
    }


    public static <TARGET, DATA> MenuButton createDefaultMenuButton(ControlPresetButton<TARGET, DATA> control){
        MenuButton menuButton = new MenuButton("Presets");
        menuButton.setMinWidth(70);
        HBox.setHgrow(menuButton, Priority.SOMETIMES);

        MenuItem newPreset = new MenuItem("New Preset");
        newPreset.setOnAction(e -> control.actionNewPreset());

        MenuItem updatePreset = new MenuItem("Update Preset");
        updatePreset.setOnAction(e -> control.actionUpdatePreset());
        updatePreset.disableProperty().bind(control.activePresetProperty().isNull());

        MenuItem renamePreset = new MenuItem("Edit Preset");
        renamePreset.setOnAction(e -> control.actionEditPreset());
        renamePreset.disableProperty().bind(control.activePresetProperty().isNull());

        MenuItem deletePreset = new MenuItem("Delete Preset");
        deletePreset.setOnAction(e -> control.actionDeletePreset());
        deletePreset.disableProperty().bind(control.activePresetProperty().isNull());

        MenuItem importPreset = new MenuItem("Import Preset");
        importPreset.setOnAction(e -> control.actionImportPreset());

        MenuItem exportPreset = new MenuItem("Export Preset");
        exportPreset.setOnAction(e -> control.actionExportPreset());
        exportPreset.disableProperty().bind(control.activePresetProperty().isNull());

        MenuItem setDefault = new MenuItem("Set As Default");
        setDefault.setOnAction(e -> control.actionSetAsDefault());
        setDefault.disableProperty().bind(control.activePresetProperty().isNull());

        MenuItem openPresetManager = new MenuItem("Preset Manager");
        openPresetManager.setOnAction(e -> control.actionShowPresetManager());

        menuButton.getItems().addAll(newPreset, updatePreset, renamePreset, deletePreset, new SeparatorMenuItem(), setDefault, new SeparatorMenuItem(), importPreset, exportPreset, new SeparatorMenuItem(), openPresetManager);

        return menuButton;
    }

}
