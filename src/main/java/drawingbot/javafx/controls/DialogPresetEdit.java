package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.files.json.IPresetManager;
import drawingbot.javafx.GenericPreset;

import java.util.Optional;

/**
 * UI Dialog for the on-the-fly editing of presets, uses a {@link ControlPresetEditor} to provide an instance of a {@link drawingbot.files.json.IPresetEditor}
 */
public class DialogPresetEdit<TARGET, DATA> extends DialogScrollPane {

    public ControlPresetEditor control;

    private DialogPresetEdit(String message, ControlPresetEditor control, GenericPreset<DATA> preset) {
        super(message, control, -1, 500);
        this.control = control;
        this.scrollPane.setMaxHeight(500);
        this.getDialogPane().setMaxHeight(500);
    }

    public static <DATA, TARGET> GenericPreset<DATA> openPresetEditDialog(IPresetManager<TARGET, DATA> manager, GenericPreset<DATA> selectedPreset, boolean isInspector) {
        return openPresetEditDialog("Edit " + selectedPreset.getPresetName(), manager, selectedPreset, isInspector, false);
    }

    public static <DATA, TARGET> GenericPreset<DATA> openPresetNewDialog(IPresetManager<TARGET, DATA> manager, GenericPreset<DATA> selectedPreset, boolean isInspector) {
        return openPresetEditDialog("New " + selectedPreset.getPresetType().getDisplayName(), manager, selectedPreset, isInspector, true);
    }

    public static <DATA, TARGET> GenericPreset<DATA> openPresetEditDialog(String message, IPresetManager<TARGET, DATA> manager, GenericPreset<DATA> selectedPreset, boolean isInspector, boolean isNewPreset) {
        ControlPresetEditor control = new ControlPresetEditor();
        control.setDetailed(isInspector);
        control.setSelectedPreset(selectedPreset);
        DialogPresetEdit<TARGET, DATA> dialog = new DialogPresetEdit<>(message, control, selectedPreset);
        dialog.initOwner(FXApplication.primaryStage);
        Optional<Boolean> result = dialog.showAndWait();
        if(result.isPresent() && !result.get()){
            return null;
        }

        GenericPreset<?> resultPreset = control.confirmEdit(isNewPreset);

        //Ensure the the editor / ui components are disposed
        control.setSelectedPreset(null);

        return (GenericPreset<DATA>) resultPreset;
    }
}
