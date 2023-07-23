package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.presets.PresetImageFilters;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controllers.AbstractFXController;
import drawingbot.registry.Register;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;

import java.io.File;
import java.nio.file.Files;

public class FXVPypeController extends AbstractFXController {


    public void initialize(){
        initVPypeSettingsPane();
    }

    public final SimpleObjectProperty<GenericPreset<PresetVpypeSettings>> selectedVPypePreset = new SimpleObjectProperty<>();

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////VPYPE OPTIMISATION

    public ComboBox<GenericPreset<PresetVpypeSettings>> comboBoxVPypePreset = null;
    public MenuButton menuButtonVPypePresets = null;
    public TextArea textAreaVPypeCommand = null;
    public CheckBox checkBoxBypassPathOptimisation = null;
    public TextField textBoxVPypeExecutablePath = null;
    public Button buttonVPypeExecutablePath = null;

    public Label labelWildcard = null;
    public Button buttonCancel = null;
    public Button buttonSendCommand = null;


    public void initVPypeSettingsPane(){
        selectedVPypePreset.setValue(Register.PRESET_LOADER_VPYPE_SETTINGS.getDefaultPreset());
        selectedVPypePreset.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Register.PRESET_LOADER_VPYPE_SETTINGS.getDefaultManager().applyPreset(DrawingBotV3.context(), newValue);
            }
        });

        comboBoxVPypePreset.setItems(Register.PRESET_LOADER_VPYPE_SETTINGS.presets);
        comboBoxVPypePreset.valueProperty().bindBidirectional(selectedVPypePreset);

        FXHelper.setupPresetMenuButton(menuButtonVPypePresets, Register.PRESET_LOADER_VPYPE_SETTINGS, Register.PRESET_LOADER_VPYPE_SETTINGS::getDefaultManager, false, selectedVPypePreset);

        textAreaVPypeCommand.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.vpypeSettings.vPypeCommand);

        labelWildcard.setText(VpypeHelper.OUTPUT_FILE_WILDCARD);

        checkBoxBypassPathOptimisation.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.vpypeSettings.vPypeBypassOptimisation);

        textBoxVPypeExecutablePath.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.vpypeSettings.vPypeExecutable);
        buttonVPypeExecutablePath.setOnAction(e -> VpypeHelper.choosePathToExecutable(DrawingBotV3.INSTANCE.vpypeSettings));

        buttonCancel.setOnAction(e -> DrawingBotV3.INSTANCE.controller.vpypeSettingsStage.hide());
        buttonSendCommand.setOnAction(e -> {
            if(DrawingBotV3.INSTANCE.vpypeSettings.vPypeExecutable.getValue().isEmpty() || Files.notExists(new File(DrawingBotV3.INSTANCE.vpypeSettings.vPypeExecutable.getValue()).toPath())){
                textBoxVPypeExecutablePath.requestFocus();
            }else{
                DrawingBotV3.INSTANCE.controller.vpypeSettingsStage.hide();
                VpypeHelper.exportToVpype(DrawingBotV3.INSTANCE.vpypeSettings);
            }
        });
    }
}
