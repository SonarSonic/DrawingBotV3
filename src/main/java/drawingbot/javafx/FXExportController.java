package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.exporters.GCodeExporter;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.files.presets.types.PresetGCodeSettings;
import drawingbot.utils.EnumDirection;
import drawingbot.utils.Units;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.Pane;
import javafx.util.converter.FloatStringConverter;

public class FXExportController {

    public void initialize(){
        initPathOptimisationPane();
        initSVGSettingsPane();
        initGCodeSettingsPane();

        DrawingBotV3.INSTANCE.controller.exportSettingsStage.setOnHidden(e -> ConfigFileHandler.getApplicationSettings().markDirty());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PATH OPTIMISATION

    public CheckBox checkBoxEnableOptimisation = null;
    public Pane paneOptimisationControls = null;

    public CheckBox checkBoxSimplify = null;
    public TextField textFieldSimplifyTolerance = null;
    public ChoiceBox<Units> choiceBoxSimplifyUnits = null;

    public CheckBox checkBoxMerge = null;
    public TextField textFieldMergeTolerance = null;
    public ChoiceBox<Units> choiceBoxMergeUnits = null;

    public CheckBox checkBoxFilter = null;
    public TextField textFieldFilterTolerance = null;
    public ChoiceBox<Units> choiceBoxFilterUnits = null;

    public CheckBox checkBoxSort = null;
    public TextField textFieldSortTolerance = null;
    public ChoiceBox<Units> choiceBoxSortUnits = null;

    public void initPathOptimisationPane(){
        checkBoxEnableOptimisation.setSelected(ConfigFileHandler.getApplicationSettings().pathOptimisationEnabled);
        checkBoxEnableOptimisation.selectedProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().pathOptimisationEnabled = newValue);

        paneOptimisationControls.disableProperty().bind(checkBoxEnableOptimisation.selectedProperty().not());

        ///simplify

        checkBoxSimplify.setSelected(ConfigFileHandler.getApplicationSettings().lineSimplifyEnabled);
        checkBoxSimplify.selectedProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineSimplifyEnabled = newValue);

        textFieldSimplifyTolerance.setTextFormatter(new TextFormatter<>(new FloatStringConverter(), 0.1F));
        textFieldSimplifyTolerance.setText("" + ConfigFileHandler.getApplicationSettings().lineSimplifyTolerance);
        textFieldSimplifyTolerance.textProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineSimplifyTolerance = Float.parseFloat(newValue));

        choiceBoxSimplifyUnits.setValue(ConfigFileHandler.getApplicationSettings().lineSimplifyUnits);
        choiceBoxSimplifyUnits.setItems(FXCollections.observableArrayList(Units.values()));
        choiceBoxSimplifyUnits.valueProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineSimplifyUnits = newValue);

        ///merge

        checkBoxMerge.setSelected(ConfigFileHandler.getApplicationSettings().lineMergingEnabled);
        checkBoxMerge.selectedProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineMergingEnabled = newValue);

        textFieldMergeTolerance.setTextFormatter(new TextFormatter<>(new FloatStringConverter(), 0.5F));
        textFieldMergeTolerance.setText("" + ConfigFileHandler.getApplicationSettings().lineMergingTolerance);
        textFieldMergeTolerance.textProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineMergingTolerance = Float.parseFloat(newValue));

        choiceBoxMergeUnits.setValue(ConfigFileHandler.getApplicationSettings().lineMergingUnits);
        choiceBoxMergeUnits.setItems(FXCollections.observableArrayList(Units.values()));
        choiceBoxMergeUnits.valueProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineMergingUnits = newValue);

        ///filter

        checkBoxFilter.setSelected(ConfigFileHandler.getApplicationSettings().lineFilteringEnabled);
        checkBoxFilter.selectedProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineFilteringEnabled = newValue);

        textFieldFilterTolerance.setTextFormatter(new TextFormatter<>(new FloatStringConverter(), 0.5F));
        textFieldFilterTolerance.setText("" + ConfigFileHandler.getApplicationSettings().lineFilteringTolerance);
        textFieldFilterTolerance.textProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineFilteringTolerance = Float.parseFloat(newValue));

        choiceBoxFilterUnits.setValue(ConfigFileHandler.getApplicationSettings().lineFilteringUnits);
        choiceBoxFilterUnits.setItems(FXCollections.observableArrayList(Units.values()));
        choiceBoxFilterUnits.valueProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineFilteringUnits = newValue);

        //sort

        checkBoxSort.setSelected(ConfigFileHandler.getApplicationSettings().lineSortingEnabled);
        checkBoxSort.selectedProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineSortingEnabled = newValue);

        textFieldSortTolerance.setTextFormatter(new TextFormatter<>(new FloatStringConverter(), 1F));
        textFieldSortTolerance.setText("" + ConfigFileHandler.getApplicationSettings().lineSortingTolerance);
        textFieldSortTolerance.textProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineSortingTolerance = Float.parseFloat(newValue));

        choiceBoxSortUnits.setValue(ConfigFileHandler.getApplicationSettings().lineSortingUnits);
        choiceBoxSortUnits.setItems(FXCollections.observableArrayList(Units.values()));
        choiceBoxSortUnits.valueProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().lineSortingUnits = newValue);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////SVG SETTINGS

    public CheckBox checkBoxEnableSVGLayerNaming = null;

    public void initSVGSettingsPane(){
        checkBoxEnableSVGLayerNaming.setSelected(ConfigFileHandler.getApplicationSettings().svgLayerRenaming);
        checkBoxEnableSVGLayerNaming.selectedProperty().addListener((observable, oldValue, newValue) -> ConfigFileHandler.getApplicationSettings().svgLayerRenaming = newValue);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////GCODE SETTINGS

    public ComboBox<GenericPreset<PresetGCodeSettings>> comboBoxGCodePreset = null;
    public MenuButton menuButtonGCodePresets = null;

    public TextField textFieldOffsetX = null;
    public TextField textFieldOffsetY = null;
    public TextArea textAreaGCodeStart = null;
    public TextArea textAreaGCodeEnd = null;
    public TextArea textAreaGCodePenDown = null;
    public TextArea textAreaGCodePenUp = null;

    public ChoiceBox<EnumDirection> choiceBoxGCodeXDir = null;
    public ChoiceBox<EnumDirection> choiceBoxGCodeYDir = null;


    public void initGCodeSettingsPane(){

        comboBoxGCodePreset.setItems(JsonLoaderManager.GCODE_SETTINGS.presets);
        comboBoxGCodePreset.setValue(JsonLoaderManager.GCODE_SETTINGS.getDefaultPreset());
        comboBoxGCodePreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                JsonLoaderManager.GCODE_SETTINGS.applyPreset(newValue);
            }
        });

        FXHelper.setupPresetMenuButton(JsonLoaderManager.GCODE_SETTINGS, menuButtonGCodePresets, comboBoxGCodePreset::getValue, (preset) -> {
            comboBoxGCodePreset.setValue(preset);

            ///force update rendering
            comboBoxGCodePreset.setItems(JsonLoaderManager.GCODE_SETTINGS.presets);
            comboBoxGCodePreset.setButtonCell(new ComboBoxListCell<>());
        });

        DrawingBotV3.INSTANCE.gcodeOffsetX.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldOffsetX.textProperty().get()), textFieldOffsetX.textProperty()));
        textFieldOffsetX.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.INSTANCE.gcodeOffsetY.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldOffsetY.textProperty().get()), textFieldOffsetY.textProperty()));
        textFieldOffsetY.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        textAreaGCodeStart.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeStartCode);
        textAreaGCodeStart.setText(GCodeExporter.defaultStartCode);

        textAreaGCodeEnd.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeEndCode);
        textAreaGCodeEnd.setText(GCodeExporter.defaultEndCode);

        textAreaGCodePenDown.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodePenDownCode);
        textAreaGCodePenDown.setText(GCodeExporter.defaultPenDownCode);

        textAreaGCodePenUp.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodePenUpCode);
        textAreaGCodePenUp.setText(GCodeExporter.defaultPenUpCode);

        /*

        choiceBoxGCodeXDir.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeXDirection);
        choiceBoxGCodeXDir.setValue(EnumDirection.POSITIVE);
        choiceBoxGCodeXDir.setItems(FXCollections.observableArrayList(EnumDirection.values()));

        choiceBoxGCodeYDir.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeYDirection);
        choiceBoxGCodeYDir.setValue(EnumDirection.POSITIVE);
        choiceBoxGCodeYDir.setItems(FXCollections.observableArrayList(EnumDirection.values()));
         */

    }

}
