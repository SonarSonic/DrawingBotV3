package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.files.json.IPresetManager;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.editors.Editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DialogPresetEdit<TARGET, DATA> extends DialogScrollPane {

    private DialogPresetEdit(IPresetManager<TARGET, DATA> manager, GenericPreset<DATA> preset, List<Consumer<GenericPreset<DATA>>> callbacks) {
        super("Edit " + preset.presetType.displayName, Editors.page("settings", builder -> {
            manager.addEditDialogElements(preset, builder, callbacks);
        }).getContent());

    }

    public static <DATA, TARGET> boolean openPresetEditDialog(IPresetManager<TARGET, DATA> manager, GenericPreset<DATA> editingPreset) {
        GenericPreset<DATA> copy = new GenericPreset<>(editingPreset);
        List<Consumer<GenericPreset<DATA>>> callbacks = new ArrayList<>();
        DialogPresetEdit<TARGET, DATA> dialog = new DialogPresetEdit<>(manager, copy, callbacks);
        dialog.initOwner(FXApplication.primaryStage);
        Optional<Boolean> result = dialog.showAndWait();
        if(result.isPresent() && !result.get()){
            return false;
        }

        //Copy any changes too the settings to the copy of the preset TODO should we do this for name / sub type for consistency.
        callbacks.forEach(consumer -> consumer.accept(copy));

        //Save the changes to the actual preset
        editingPreset.copyData(copy);
        return true;
    }
}
