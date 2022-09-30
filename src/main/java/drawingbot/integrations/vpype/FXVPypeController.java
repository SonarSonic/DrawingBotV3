package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.files.ConfigFileHandler;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.Register;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;

import java.io.File;
import java.nio.file.Files;

public class FXVPypeController {


    public void initialize(){
        initVPypeSettingsPane();
    }


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

        comboBoxVPypePreset.setItems(Register.PRESET_LOADER_VPYPE_SETTINGS.presets);
        comboBoxVPypePreset.setValue(Register.PRESET_LOADER_VPYPE_SETTINGS.getDefaultPreset());
        comboBoxVPypePreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Register.PRESET_LOADER_VPYPE_SETTINGS.getDefaultManager().applyPreset(DrawingBotV3.context(), newValue);
            }
        });

        FXHelper.setupPresetMenuButton(Register.PRESET_LOADER_VPYPE_SETTINGS, Register.PRESET_LOADER_VPYPE_SETTINGS::getDefaultManager, menuButtonVPypePresets, false, comboBoxVPypePreset::getValue, (preset) -> {
            comboBoxVPypePreset.setValue(preset);

            ///force update rendering
            comboBoxVPypePreset.setItems(Register.PRESET_LOADER_VPYPE_SETTINGS.presets);
            comboBoxVPypePreset.setButtonCell(new ComboBoxListCell<>());
        });

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
