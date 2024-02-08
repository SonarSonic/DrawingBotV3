package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controllers.AbstractFXController;
import drawingbot.javafx.controls.ComboCellPreset;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;

public class FXVPypeController extends AbstractFXController {

    public void initialize(){
        initVPypeSettingsPane();
    }

    public final SimpleObjectProperty<GenericPreset<PresetVpypeSettings>> selectedVPypePreset = new SimpleObjectProperty<>();

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public ComboBox<GenericPreset<PresetVpypeSettings>> comboBoxVPypePreset = null;
    public MenuButton menuButtonVPypePresets = null;
    public TextArea textAreaVPypeCommand = null;
    public CheckBox checkBoxBypassPathOptimisation = null;
    public TextField textBoxVPypeExecutablePath = null;
    public Button buttonAutoDetectPath = null;
    public Button buttonVPypeExecutablePath = null;

    public Label labelWildcard = null;

    public void initVPypeSettingsPane(){
        selectedVPypePreset.setValue(VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.getDefaultPreset());
        selectedVPypePreset.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                VpypePlugin.PRESET_MANAGER_VPYPE_SETTINGS.applyPreset(DrawingBotV3.context(), VpypePlugin.INSTANCE.vpypeSettings, newValue, false);
            }
        });

        comboBoxVPypePreset.setItems(VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.presets);
        comboBoxVPypePreset.valueProperty().bindBidirectional(selectedVPypePreset);
        comboBoxVPypePreset.setCellFactory(f -> new ComboCellPreset<>());

        FXHelper.setupPresetMenuButton(menuButtonVPypePresets, VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS, VpypePlugin.PRESET_MANAGER_VPYPE_SETTINGS, false, selectedVPypePreset);

        textAreaVPypeCommand.textProperty().bindBidirectional(VpypePlugin.INSTANCE.vpypeSettings.vpypeCommand);

        labelWildcard.setText(VpypeHelper.OUTPUT_FILE_WILDCARD);

        checkBoxBypassPathOptimisation.selectedProperty().bindBidirectional(VpypePlugin.INSTANCE.vpypeSettings.vpypeBypassOptimisation);

        textBoxVPypeExecutablePath.textProperty().bindBidirectional(VpypePlugin.INSTANCE.vpypeSettings.vpypeExecutable);
        buttonAutoDetectPath.setOnAction(e -> VpypeHelper.autoDetectVpype(VpypePlugin.INSTANCE.vpypeSettings));
        buttonVPypeExecutablePath.setOnAction(e -> VpypeHelper.choosePathToExecutable(VpypePlugin.INSTANCE.vpypeSettings));
    }
}
