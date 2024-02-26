package drawingbot.javafx.controls;

import drawingbot.FXApplication;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UI Dialog used by the {@link drawingbot.javafx.controllers.FXPresetManager} when a single / multiple presets have been selected for deletion
 */
public class DialogPresetMultiDelete extends Dialog<Boolean> {

    public final ButtonType deletePresets = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);

    public DialogPresetMultiDelete(List<GenericPreset<?>> presets) {
        super();
        setTitle("Delete %s Presets".formatted(presets.size()));

        TextFlow flow = new TextFlow();

        List<GenericPreset<?>> systemPresets = presets.stream().filter(GenericPreset::isSystemPreset).collect(Collectors.toList());
        List<GenericPreset<?>> userPresets = presets.stream().filter(GenericPreset::isUserPreset).collect(Collectors.toList());
        if(!systemPresets.isEmpty()){
            FXHelper.addText(flow, 14, "bold", "The following %s system presets can't be deleted".formatted(systemPresets.size()));
            systemPresets.forEach(p -> {
                FXHelper.addText(flow, 12, "normal", "\n- %s (%s)".formatted(p.getDisplayName(), p.getPresetSubType()));
            });
            FXHelper.addText(flow, 12, "normal", "\n");
        }

        if(!userPresets.isEmpty()){
            FXHelper.addText(flow, 14, "bold", "\nThe following %s presets will be deleted".formatted(userPresets.size()));
            userPresets.forEach(p -> {
                FXHelper.addText(flow, 12, "normal", "\n- %s (%s)".formatted(p.getDisplayName(), p.getPresetSubType()));
            });
            FXHelper.addText(flow, 12, "normal", "\n");
        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(flow);

        getDialogPane().setContent(scrollPane);
        getDialogPane().setPrefWidth(-1);
        getDialogPane().setMaxHeight(600);
        setResultConverter(button -> button == deletePresets);

        if(userPresets.isEmpty()){
            getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        }else{
            getDialogPane().getButtonTypes().addAll(deletePresets, ButtonType.CANCEL);
        }
        FXApplication.applyTheme((Stage)getDialogPane().getScene().getWindow());
    }

    public static boolean openSystemPresetMultiDeleteDialog(List<GenericPreset<?>> presets) {
        DialogPresetMultiDelete dialog = new DialogPresetMultiDelete(presets);
        dialog.initOwner(FXApplication.primaryStage);
        Optional<Boolean> result = dialog.showAndWait();
        return result.isPresent() && result.get();
    }
}
