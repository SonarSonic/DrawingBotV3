package drawingbot.files.json.presets;

import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.exporters.GCodeExporter;
import drawingbot.files.exporters.GCodeSettings;
import drawingbot.files.json.DefaultPresetManager;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.Editors;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.utils.UnitsLength;
import javafx.collections.FXCollections;

public class PresetGCodeSettingsManager extends DefaultPresetManager<GCodeSettings, PresetData> {

    public GenericSetting<GCodeSettings, Float> gcodeOffsetX = registerSetting(GenericSetting.createFloatSetting(GCodeSettings.class, "gcodeOffsetX", 0F, i -> i.gcodeOffsetX));
    public GenericSetting<GCodeSettings, Float> gcodeOffsetY = registerSetting(GenericSetting.createFloatSetting(GCodeSettings.class, "gcodeOffsetY", 0F, i -> i.gcodeOffsetY));
    public GenericSetting<GCodeSettings, UnitsLength> gcodeUnits = registerSetting(GenericSetting.createOptionSetting(GCodeSettings.class, UnitsLength.class, "gcodeUnits", FXCollections.observableArrayList(UnitsLength.values()), UnitsLength.MILLIMETRES, i -> i.gcodeUnits));
    public GenericSetting<GCodeSettings, Float> gcodeCurveFlatness = registerSetting(GenericSetting.createFloatSetting(GCodeSettings.class, "gcodeCurveFlatness", 0.1F, i -> i.gcodeCurveFlatness).setValidator(Math::abs).createDisableBinding("gcodeEnableFlattening", false));
    public GenericSetting<GCodeSettings, Boolean> gcodeEnableFlattening = registerSetting(GenericSetting.createBooleanSetting(GCodeSettings.class, "gcodeEnableFlattening", true, i -> i.gcodeEnableFlattening));
    public GenericSetting<GCodeSettings, Boolean> gcodeCenterZeroPoint = registerSetting(GenericSetting.createBooleanSetting(GCodeSettings.class, "gcodeCenterZeroPoint", false, i -> i.gcodeCenterZeroPoint));
    public GenericSetting<GCodeSettings, GCodeBuilder.CommentType> gcodeCommentType = registerSetting(GenericSetting.createOptionSetting(GCodeSettings.class, GCodeBuilder.CommentType.class, "gcodeCommentType", FXCollections.observableArrayList(GCodeBuilder.CommentType.values()), GCodeBuilder.CommentType.BRACKETS, i -> i.gcodeCommentType));
    public GenericSetting<GCodeSettings, String> gcodeStartCode = registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodeStartCode", GCodeExporter.defaultStartCode, i -> i.gcodeStartCode).setEditorFactory(Editors::createGenericTextArea));
    public GenericSetting<GCodeSettings, String> gcodeEndCode = registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodeEndCode", GCodeExporter.defaultEndCode, i -> i.gcodeEndCode).setEditorFactory(Editors::createGenericTextArea));
    public GenericSetting<GCodeSettings, String> gcodePenDownCode = registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodePenDownCode", GCodeExporter.defaultPenDownCode, i -> i.gcodePenDownCode).setEditorFactory(Editors::createGenericTextArea));
    public GenericSetting<GCodeSettings, String> gcodePenUpCode = registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodePenUpCode", GCodeExporter.defaultPenUpCode, i -> i.gcodePenUpCode).setEditorFactory(Editors::createGenericTextArea));
    public GenericSetting<GCodeSettings, String> gcodeStartLayerCode = registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodeStartLayerCode", GCodeExporter.defaultStartLayerCode, i -> i.gcodeStartLayerCode).setEditorFactory(Editors::createGenericTextArea));
    public GenericSetting<GCodeSettings, String> gcodeEndLayerCode = registerSetting(GenericSetting.createStringSetting(GCodeSettings.class, "gcodeEndLayerCode", GCodeExporter.defaultEndLayerCode, i -> i.gcodeEndLayerCode).setEditorFactory(Editors::createGenericTextArea));

    public PresetGCodeSettingsManager(PresetGCodeSettingsLoader presetLoader) {
        super(presetLoader, GCodeSettings.class);
    }

    @Override
    public GCodeSettings getTargetFromContext(DBTaskContext context) {
        return DBPreferences.INSTANCE.gcodeSettings;
    }

    @Override
    public PresetGCodeSettingsEditor createPresetEditor() {
        return new PresetGCodeSettingsEditor(this);
    }
}
