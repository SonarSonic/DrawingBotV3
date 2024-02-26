package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;

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
        newPreset.setOnAction(e -> {
            GenericPreset<DATA> result = FXHelper.actionNewPreset(control.getPresetManager(), control.getTarget(), false);
            if(result != null){
                control.refresh();
                control.setActivePreset(result);
                control.setActivePreset(result); //FORCE: if new sub types are created FIXME?
            }
        });

        MenuItem updatePreset = new MenuItem("Update Preset");
        updatePreset.setOnAction(e -> {
            GenericPreset<DATA>  result = FXHelper.actionUpdatePreset(control.getPresetManager(), control.getActivePreset(), control.getTarget());
            if(result != null){
                control.refresh();
                control.setActivePreset(result);
                control.setActivePreset(result); //FORCE: if new sub types are created FIXME?
            }
        });
        updatePreset.disableProperty().bind(control.activePresetProperty().isNull());

        MenuItem renamePreset = new MenuItem("Edit Preset");
        renamePreset.setOnAction(e -> {
            GenericPreset<DATA>  result = FXHelper.actionEditPreset(control.getPresetManager(), control.getActivePreset(), control.getTarget(), false);
            if(result != null){
                control.refresh();
                control.setActivePreset(result);
                control.setActivePreset(result); //FORCE: if new sub types are created FIXME?
            }
        });
        renamePreset.disableProperty().bind(control.activePresetProperty().isNull());

        MenuItem deletePreset = new MenuItem("Delete Preset");
        deletePreset.setOnAction(e -> {
            GenericPreset<DATA>  preset = control.getActivePreset();
            if(preset == null){
                return;
            }

            String subType = preset.getPresetSubType();
            int oldIndex = control.getPresetType().getSubTypeBehaviour().isIgnored() ? control.getPresetLoader().getPresets().indexOf(preset) : control.getPresetLoader().getPresetsForSubType(subType).indexOf(preset);

            GenericPreset<DATA>  result = FXHelper.actionDeletePreset(control.getPresetLoader(), control.getActivePreset());
            if(result == null){
                control.refresh();

                //1. Try to set the preset to the next one down in the list in the sub type / preset list
                int nextIndex = Math.max(0, oldIndex-1);
                List<GenericPreset<DATA>> targetList = control.getPresetType().getSubTypeBehaviour().isIgnored() ? control.getPresetLoader().getPresets() : control.getPresetLoader().getPresetsForSubType(subType);
                if(!targetList.isEmpty() && nextIndex < targetList.size()){
                    GenericPreset<DATA> nextPreset = targetList.get(nextIndex);
                    if(nextPreset != null){
                        control.setActivePreset(nextPreset);
                        return;
                    }
                }
                //2. If we failed to find it use the default for the sub type
                GenericPreset<DATA> defaultPreset = control.getPresetLoader().getDefaultPresetForSubType(subType);
                if(defaultPreset != null){
                    control.setActivePreset(defaultPreset);
                    return;
                }
                //3. If we still found nothing, switch to the generic default preset
                control.setActivePreset(control.getPresetLoader().getDefaultPreset());
            }
        });
        deletePreset.disableProperty().bind(control.activePresetProperty().isNull());

        MenuItem importPreset = new MenuItem("Import Preset");
        importPreset.setOnAction(e -> {
            FXHelper.importPreset(DrawingBotV3.context(), control.getPresetType(), false, true);
        });

        MenuItem exportPreset = new MenuItem("Export Preset");
        exportPreset.setOnAction(e -> {
            GenericPreset<DATA> current = control.getActivePreset();
            if(current != null){
                FXHelper.exportPreset(DrawingBotV3.context(), current, DrawingBotV3.project().getExportDirectory(), current.getPresetName(), true);
            }
        });
        exportPreset.disableProperty().bind(control.activePresetProperty().isNull());

        MenuItem setDefault = new MenuItem("Set As Default");
        setDefault.setOnAction(e -> {
            GenericPreset<DATA> current = control.getActivePreset();
            if(current != null){
                FXHelper.actionSetDefaultPreset(control.getPresetLoader(), current);
                control.refresh();
            }
        });
        setDefault.disableProperty().bind(control.activePresetProperty().isNull());

        MenuItem openPresetManager = new MenuItem("Preset Manager");
        openPresetManager.setOnAction(e -> {

            GenericPreset<DATA> current = control.getActivePreset();
            if(current != null){
                DrawingBotV3.INSTANCE.controller.presetManagerController.type.set(current.getPresetType());
                if(!current.getPresetType().getSubTypeBehaviour().isIgnored()){
                    DrawingBotV3.INSTANCE.controller.presetManagerController.category.set(current.getPresetSubType());
                }
                DrawingBotV3.INSTANCE.controller.presetManagerController.selectPreset(current);
            }
            DrawingBotV3.INSTANCE.controller.presetManagerStage.show();
        });

        menuButton.getItems().addAll(newPreset, updatePreset, renamePreset, deletePreset, new SeparatorMenuItem(), setDefault, new SeparatorMenuItem(), importPreset, exportPreset, new SeparatorMenuItem(), openPresetManager);

        return menuButton;
    }

}
