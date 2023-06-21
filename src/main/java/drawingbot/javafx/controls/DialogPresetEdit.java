package drawingbot.javafx.controls;

import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.IJsonData;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.editors.Editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DialogPresetEdit<O extends IJsonData> extends DialogScrollPane {

    private DialogPresetEdit(GenericPreset<O> preset, List<Consumer<GenericPreset<O>>> callbacks) {
        super("Edit " + preset.presetType.displayName, Editors.page("settings", builder -> {
            AbstractPresetManager<O> presetManager =  preset.presetLoader.getDefaultManager();
            presetManager.addEditDialogElements(preset, builder, callbacks);
        }).getContent());

    }

    public static <O extends IJsonData> boolean openPresetEditDialog(GenericPreset<O> preset){
        GenericPreset<O> copy = new GenericPreset<>(preset);
        List<Consumer<GenericPreset<O>>> callbacks = new ArrayList<>();
        DialogPresetEdit<O> dialog = new DialogPresetEdit<>(copy, callbacks);
        Optional<Boolean> result = dialog.showAndWait();
        if(result.isPresent() && !result.get()){
            return false;
        }

        //Copy any changes too the settings to the copy of the preset TODO should we do this for name / sub type for consistency.
        callbacks.forEach(consumer -> consumer.accept(copy));

        //Save the changes to the actual preset
        preset.copyData(copy);
        return true;
    }

}
