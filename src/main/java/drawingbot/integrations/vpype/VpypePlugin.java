package drawingbot.integrations.vpype;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.Hooks;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.json.PresetType;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controls.DialogScrollPane;
import drawingbot.javafx.editors.ControllerNode;
import drawingbot.javafx.editors.TreeNode;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.settings.StringSetting;
import drawingbot.plugins.AbstractPlugin;
import drawingbot.registry.MasterRegistry;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.fxmisc.easybind.EasyBind;

public class VpypePlugin extends AbstractPlugin {

    public static final VpypePlugin INSTANCE = new VpypePlugin();
    public static final String VERSION = "1.0.0";

    public static PresetType PRESET_TYPE_VPYPE_SETTINGS;
    public static PresetVpypeSettingsLoader PRESET_LOADER_VPYPE_SETTINGS;
    public static PresetVpypeSettingsManager PRESET_MANAGER_VPYPE_SETTINGS;
    public static DrawingExportHandler EXPORT_HANDLER_VPYPE;

    public final VpypeSettings vpypeSettings = new VpypeSettings();

    public final SimpleObjectProperty<GenericPreset<PresetVpypeSettings>> selectedVPypePreset = new SimpleObjectProperty<>();
    public final SimpleBooleanProperty dialogResponse = new SimpleBooleanProperty();

    public static TreeNode vpypePage = null;
    private boolean skipNextCommandDialog = false; //if we export the traditional way, don't show the command dialog instantly

    public final StringSetting<?> vpypeExecutable = register(GenericSetting.createStringSetting(DBPreferences.class, "vpype", "vpypeExecutable", ""));

    private VpypePlugin(){
        vpypeExecutable.valueProperty().bindBidirectional(vpypeSettings.vpypeExecutable);
    }


    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getDisplayName() {
        return "vpype Plugin";
    }

    @Override
    public void preInit() {
        super.preInit();

        settings.forEach(setting -> MasterRegistry.INSTANCE.registerApplicationSetting(setting));

        Hooks.addHook(Hooks.FILE_MENU, VpypePlugin::initSpecialExportButton);

        MasterRegistry.INSTANCE.registerPresetType(PRESET_TYPE_VPYPE_SETTINGS = new PresetType("vpype_settings", "VPype Preset"));
        MasterRegistry.INSTANCE.registerPresetLoaders(PRESET_LOADER_VPYPE_SETTINGS = new PresetVpypeSettingsLoader(PRESET_TYPE_VPYPE_SETTINGS));
        MasterRegistry.INSTANCE.registerPresetManager(PRESET_MANAGER_VPYPE_SETTINGS = new PresetVpypeSettingsManager(PRESET_LOADER_VPYPE_SETTINGS));

        MasterRegistry.INSTANCE.registerDrawingExportHandler(EXPORT_HANDLER_VPYPE = new DrawingExportHandler(DrawingExportHandler.Category.SPECIAL, "vpype_export", "Export to vpype", true, (exportTask, saveLocation) -> {

            VpypeHelper.vpypeExport(vpypeSettings, exportTask, saveLocation);

            }, e -> {
            if(skipNextCommandDialog) {
                skipNextCommandDialog = false;
                return null;
            }
            return new DialogScrollPane("Confirm Vpype Settings", vpypePage.getContent(), 550, 400);
        }, FileUtils.FILTER_ALL_FILES){
            @Override
            public void setupExport(ExportTask task) {
                super.setupExport(task);
                task.forceBypassOptimisation = task.forceBypassOptimisation || vpypeSettings.vpypeBypassOptimisation.get();
            }

            @Override
            public boolean requiresSaveLocation(ExportTask.Mode exportMode) {
                //If we're using a special mode, rely on the ExportTask to tell us the destinations and assume we need one (unlikely to batch "show" commands), if not ask for the destination later, when the command has been confirmed
                return exportMode != ExportTask.Mode.PER_DRAWING;
            }
        });

        selectedVPypePreset.setValue(VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.getDefaultPreset());
        selectedVPypePreset.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                PRESET_MANAGER_VPYPE_SETTINGS.applyPreset(DrawingBotV3.context(), vpypeSettings, newValue, false);
            }
        });
    }

    private static Object[] initSpecialExportButton(Object... values) {
        Menu menuFile = (Menu) values[0];

        MenuItem menuExportToVPype = new MenuItem("Export to " + VpypeHelper.VPYPE_NAME);
        menuExportToVPype.setOnAction(e -> {
            if(DrawingBotV3.project().getCurrentDrawing() != null){

                DialogScrollPane vpypePlugin = new DialogScrollPane("Confirm Vpype Settings", vpypePage.getContent(), 550, 400);
                vpypePlugin.initOwner(FXApplication.primaryStage);
                boolean shouldExport = vpypePlugin.showAndWait().orElse(false);

                if(shouldExport){
                    INSTANCE.skipNextCommandDialog = true;
                    FXHelper.exportFile(DrawingBotV3.context(), VpypePlugin.EXPORT_HANDLER_VPYPE, ExportTask.Mode.PER_DRAWING);
                }
            }
        });
        menuExportToVPype.disableProperty().bind(Bindings.isNull(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(project -> project.currentDrawing)));
        menuFile.getItems().add(menuExportToVPype);

        return values;
    }

    @Override
    public void registerPreferencePages() {
        MasterRegistry.INSTANCE.registerPreferencesPage("Export Settings", vpypePage = new ControllerNode<FXVPypeController>("vpype", "/drawingbot/javafx/vpypesettings.fxml"));
    }

}
