package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.controllers.AbstractFXController;
import drawingbot.javafx.controls.ControlPresetSelector;
import javafx.scene.control.*;

public class FXVPypeController extends AbstractFXController {

    public void initialize(){
        initVPypeSettingsPane();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public ControlPresetSelector<VpypeSettings, PresetData> controlVpypePreset;
    public TextArea textAreaVPypeCommand = null;
    public CheckBox checkBoxBypassPathOptimisation = null;
    public TextField textBoxVPypeExecutablePath = null;
    public Button buttonAutoDetectPath = null;
    public Button buttonVPypeExecutablePath = null;

    public Label labelWildcard = null;

    public void initVPypeSettingsPane(){
        controlVpypePreset.quickSetup(VpypePlugin.PRESET_MANAGER_VPYPE_SETTINGS);
        controlVpypePreset.setTarget(VpypePlugin.INSTANCE.vpypeSettings);
        controlVpypePreset.activePresetProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                controlVpypePreset.applyPreset(DrawingBotV3.context());
            }
        });

        textAreaVPypeCommand.textProperty().bindBidirectional(VpypePlugin.INSTANCE.vpypeSettings.vpypeCommand);

        labelWildcard.setText(VpypeHelper.OUTPUT_FILE_WILDCARD);

        checkBoxBypassPathOptimisation.selectedProperty().bindBidirectional(VpypePlugin.INSTANCE.vpypeSettings.vpypeBypassOptimisation);

        textBoxVPypeExecutablePath.textProperty().bindBidirectional(VpypePlugin.INSTANCE.vpypeSettings.vpypeExecutable);
        buttonAutoDetectPath.setOnAction(e -> VpypeHelper.autoDetectVpype(VpypePlugin.INSTANCE.vpypeSettings));
        buttonVPypeExecutablePath.setOnAction(e -> VpypeHelper.choosePathToExecutable(VpypePlugin.INSTANCE.vpypeSettings));
    }
}
