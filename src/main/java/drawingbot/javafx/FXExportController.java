package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.api.Hooks;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.canvas.CanvasUtils;
import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.json.presets.PresetGCodeSettings;
import drawingbot.registry.Register;
import drawingbot.utils.*;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;
import org.fxmisc.easybind.EasyBind;

public class FXExportController {

    public void initialize(){
        Hooks.runHook(Hooks.FX_EXPORT_CONTROLLER_PRE_INIT, this);

        initPathOptimisationPane();
        initSVGSettingsPane();
        initGCodeSettingsPane();
        initImageSequencePane();
        initHPGLSettingsPane();

        //TODO FIX SAVING OF SETTINGS! DrawingBotV3.INSTANCE.controller.exportSettingsStage.setOnHidden(e -> DrawingBotV3.INSTANCE.getSettings().markDirty());

        DrawingBotV3.INSTANCE.controller.exportSettingsStage.setOnShown(e -> updateImageSequenceStats());


        Hooks.runHook(Hooks.FX_EXPORT_CONTROLLER_POST_INIT, this);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PATH OPTIMISATION

    public CheckBox checkBoxEnableOptimisation = null;
    public Pane paneOptimisationControls = null;

    public CheckBox checkBoxSimplify = null;
    public TextField textFieldSimplifyTolerance = null;
    public ChoiceBox<UnitsLength> choiceBoxSimplifyUnits = null;

    public CheckBox checkBoxMerge = null;
    public TextField textFieldMergeTolerance = null;
    public ChoiceBox<UnitsLength> choiceBoxMergeUnits = null;

    public CheckBox checkBoxFilter = null;
    public TextField textFieldFilterTolerance = null;
    public ChoiceBox<UnitsLength> choiceBoxFilterUnits = null;

    public CheckBox checkBoxSort = null;
    public TextField textFieldSortTolerance = null;
    public ChoiceBox<UnitsLength> choiceBoxSortUnits = null;

    public CheckBox checkBoxMultipass = null;
    public TextField textFieldMultipassCount = null;

    public void initPathOptimisationPane(){
        checkBoxEnableOptimisation.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().pathOptimisationEnabled.valueProperty());

        paneOptimisationControls.disableProperty().bind(checkBoxEnableOptimisation.selectedProperty().not());

        ///simplify

        checkBoxSimplify.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineSimplifyEnabled.valueProperty());

        textFieldSimplifyTolerance.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0.1D));
        textFieldSimplifyTolerance.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineSimplifyTolerance.asDoubleProperty(), new NumberStringConverter(Utils.defaultDF));

        choiceBoxSimplifyUnits.setItems(FXCollections.observableArrayList(UnitsLength.values()));
        choiceBoxSimplifyUnits.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineSimplifyUnits.valueProperty());

        ///merge

        checkBoxMerge.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineMergingEnabled.valueProperty());

        textFieldMergeTolerance.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0.5D));
        textFieldMergeTolerance.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineMergingTolerance.asDoubleProperty(), new NumberStringConverter(Utils.defaultDF));

        choiceBoxMergeUnits.setItems(FXCollections.observableArrayList(UnitsLength.values()));
        choiceBoxMergeUnits.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineMergingUnits.valueProperty());

        ///filter

        checkBoxFilter.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineFilteringEnabled.valueProperty());

        textFieldFilterTolerance.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0.5D));
        textFieldFilterTolerance.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineFilteringTolerance.asDoubleProperty(), new NumberStringConverter(Utils.defaultDF));

        choiceBoxFilterUnits.setItems(FXCollections.observableArrayList(UnitsLength.values()));
        choiceBoxFilterUnits.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineFilteringUnits.valueProperty());

        ///sort

        checkBoxSort.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineSortingEnabled.valueProperty());

        textFieldSortTolerance.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 1D));
        textFieldSortTolerance.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineSortingTolerance.asDoubleProperty(), new NumberStringConverter(Utils.defaultDF));

        choiceBoxSortUnits.setItems(FXCollections.observableArrayList(UnitsLength.values()));
        choiceBoxSortUnits.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().lineSortingUnits.valueProperty());

        ///multipass

        checkBoxMultipass.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().multipassEnabled.valueProperty());

        textFieldMultipassCount.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 1));
        textFieldMultipassCount.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().multipassCount.asIntegerProperty(), new NumberStringConverter(Utils.defaultDF));


    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////SVG SETTINGS

    public ComboBox<String> comboBoxLayerNamingPattern = null;
    public CheckBox checkBoxExportBackgroundLayer = null;

    public void initSVGSettingsPane(){
        comboBoxLayerNamingPattern.setEditable(true);
        comboBoxLayerNamingPattern.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().svgLayerNaming.valueProperty());
        comboBoxLayerNamingPattern.getItems().addAll("%NAME%", "%INDEX% - %NAME%", "Pen%INDEX%");

        checkBoxExportBackgroundLayer.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().exportSVGBackground.valueProperty());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////GCODE SETTINGS
    public final SimpleObjectProperty<GenericPreset<PresetGCodeSettings>> selectedGCodePreset = new SimpleObjectProperty<>();

    public AnchorPane anchorPaneGCodeSettings = null;

    public ComboBox<GenericPreset<PresetGCodeSettings>> comboBoxGCodePreset = null;
    public MenuButton menuButtonGCodePresets = null;

    public TextField textFieldOffsetX = null;
    public TextField textFieldOffsetY = null;
    public ChoiceBox<UnitsLength> choiceBoxGCodeUnits = null;
    public TextField textFieldGCodeCurveFlatness = null;
    public CheckBox checkBoxGCodeEnableFlattening = null;
    public CheckBox checkBoxGCodeCenterZeroPoint = null;
    public ChoiceBox<GCodeBuilder.CommentType> choiceBoxCommentTypes = null;

    public TextArea textAreaGCodeStart = null;
    public TextArea textAreaGCodeEnd = null;
    public TextArea textAreaGCodePenDown = null;
    public TextArea textAreaGCodePenUp = null;
    public TextArea textAreaGCodeStartLayer = null;
    public TextArea textAreaGCodeEndLayer = null;

    public void initGCodeSettingsPane(){
        selectedGCodePreset.setValue(Register.PRESET_LOADER_GCODE_SETTINGS.getDefaultPreset());
        selectedGCodePreset.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Register.PRESET_LOADER_GCODE_SETTINGS.getDefaultManager().tryApplyPreset(DrawingBotV3.context(), newValue);
            }
        });

        comboBoxGCodePreset.setItems(Register.PRESET_LOADER_GCODE_SETTINGS.presets);
        comboBoxGCodePreset.valueProperty().bindBidirectional(selectedGCodePreset);

        //TODO FIX RENAMING!!!!!
        FXHelper.setupPresetMenuButton(menuButtonGCodePresets, Register.PRESET_LOADER_GCODE_SETTINGS, Register.PRESET_LOADER_GCODE_SETTINGS::getDefaultManager, false, selectedGCodePreset);

        textFieldOffsetX.textFormatterProperty().setValue(new TextFormatter<>(new DoubleStringConverter(), 0D));
        textFieldOffsetX.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeOffsetX, new NumberStringConverter(Utils.defaultDF));

        textFieldOffsetY.textFormatterProperty().setValue(new TextFormatter<>(new DoubleStringConverter(), 0D));
        textFieldOffsetY.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeOffsetY, new NumberStringConverter(Utils.defaultDF));

        choiceBoxGCodeUnits.getItems().addAll(UnitsLength.values());
        choiceBoxGCodeUnits.setValue(UnitsLength.MILLIMETRES);
        choiceBoxGCodeUnits.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeUnits);

        textFieldGCodeCurveFlatness.textFormatterProperty().setValue(new TextFormatter<>(new DoubleStringConverter(), 0.1D));
        textFieldGCodeCurveFlatness.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeCurveFlatness, new NumberStringConverter(Utils.defaultDF));
        textFieldGCodeCurveFlatness.disableProperty().bind(checkBoxGCodeEnableFlattening.selectedProperty().not());

        checkBoxGCodeEnableFlattening.setSelected(DrawingBotV3.INSTANCE.gcodeSettings.gcodeEnableFlattening.getValue());
        checkBoxGCodeEnableFlattening.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeEnableFlattening);

        checkBoxGCodeCenterZeroPoint.setSelected(DrawingBotV3.INSTANCE.gcodeSettings.gcodeCenterZeroPoint.getValue());
        checkBoxGCodeCenterZeroPoint.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeCenterZeroPoint);

        checkBoxGCodeCenterZeroPoint.setSelected(DrawingBotV3.INSTANCE.gcodeSettings.gcodeCenterZeroPoint.getValue());
        checkBoxGCodeCenterZeroPoint.selectedProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeCenterZeroPoint);


        choiceBoxCommentTypes.getItems().addAll(GCodeBuilder.CommentType.values());
        choiceBoxCommentTypes.setValue(GCodeBuilder.CommentType.BRACKETS);
        choiceBoxCommentTypes.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeCommentType);

        textAreaGCodeStart.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeStartCode);

        textAreaGCodeEnd.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeEndCode);

        textAreaGCodePenDown.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodePenDownCode);

        textAreaGCodePenUp.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodePenUpCode);

        textAreaGCodeStartLayer.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeStartLayerCode);

        textAreaGCodeEndLayer.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeSettings.gcodeEndLayerCode);

        /*

        choiceBoxGCodeXDir.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeXDirection);
        choiceBoxGCodeXDir.setValue(EnumDirection.POSITIVE);
        choiceBoxGCodeXDir.setItems(FXCollections.observableArrayList(EnumDirection.values()));

        choiceBoxGCodeYDir.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.gcodeYDirection);
        choiceBoxGCodeYDir.setValue(EnumDirection.POSITIVE);
        choiceBoxGCodeYDir.setItems(FXCollections.observableArrayList(EnumDirection.values()));
         */

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////IMAGE SEQUENCE SETTINGS
    public AnchorPane anchorPaneImgSeqSettings = null;

    public TextField textFieldDPI = null;
    public TextField textFieldFPS = null;
    public TextField textFieldDuration = null;
    public TextField textFieldHoldStart = null;
    public TextField textFieldHoldEnd = null;
    public ChoiceBox<UnitsTime> choiceBoxTimeUnits = null;

    
    public Label labelExportSize = null;
    public Label labelFrameCount = null;
    public Label labelGeometriesPFrame = null;
    public Label labelVerticesPFrame = null;


    public void initImageSequencePane(){

        InvalidationListener updateListener = observable -> updateImageSequenceStats();

        textFieldDPI.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 300D));
        textFieldDPI.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().exportDPI.asDoubleProperty(), new NumberStringConverter(Utils.defaultDF));
        textFieldDPI.textProperty().addListener(updateListener);
        textFieldDPI.disableProperty().bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).select(p -> p.drawingArea).selectObject(d -> d.useOriginalSizing));
        
        textFieldFPS.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 25D));
        textFieldFPS.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().framesPerSecond.asIntegerProperty(), new NumberStringConverter(Utils.defaultDF));
        textFieldFPS.textProperty().addListener(updateListener);

        textFieldDuration.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 5));
        textFieldDuration.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().duration.asIntegerProperty(), new NumberStringConverter(Utils.defaultDF));
        textFieldDuration.textProperty().addListener(updateListener);

        textFieldHoldStart.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 1));
        textFieldHoldStart.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().frameHoldStart.asIntegerProperty(), new NumberStringConverter(Utils.defaultDF));
        textFieldHoldStart.textProperty().addListener(updateListener);

        textFieldHoldEnd.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 1));
        textFieldHoldEnd.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().frameHoldEnd.asIntegerProperty(), new NumberStringConverter(Utils.defaultDF));
        textFieldHoldEnd.textProperty().addListener(updateListener);

        choiceBoxTimeUnits.setItems(FXCollections.observableArrayList(UnitsTime.values()));
        choiceBoxTimeUnits.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.getPreferences().durationUnits.valueProperty());
        choiceBoxTimeUnits.valueProperty().addListener(updateListener);

        updateImageSequenceStats();

    }

    public void updateImageSequenceStats(){
        PlottedDrawing drawing = DrawingBotV3.taskManager().getCurrentDrawing();

        int frameCount = DrawingBotV3.INSTANCE.getPreferences().getFrameCount();
        int geometriesPerFrame = drawing == null ? 0 : DrawingBotV3.INSTANCE.getPreferences().getGeometriesPerFrame(drawing.getGeometryCount());
        long verticesPerFrame = drawing == null ? 0 : DrawingBotV3.INSTANCE.getPreferences().getVerticesPerFrame(drawing.getVertexCount());


        if(verticesPerFrame == 1 && frameCount > verticesPerFrame){
            frameCount = (int) drawing.getVertexCount();
        }
        
        if(DrawingBotV3.project().openImage.get() != null){
            int exportWidth = CanvasUtils.getRasterExportWidth(DrawingBotV3.project().openImage.get().getTargetCanvas(), DrawingBotV3.INSTANCE.getPreferences().exportDPI.get(), false);
            int exportHeight = CanvasUtils.getRasterExportHeight(DrawingBotV3.project().openImage.get().getTargetCanvas(), DrawingBotV3.INSTANCE.getPreferences().exportDPI.get(), false);
            labelExportSize.setText(exportWidth + " x " + exportHeight);
        }else{
            labelExportSize.setText("0 x 0");
        }
        
        labelFrameCount.setText("" + Utils.defaultNF.format(frameCount));
        labelGeometriesPFrame.setText("" + Utils.defaultNF.format(geometriesPerFrame));
        labelVerticesPFrame.setText("" + Utils.defaultNF.format(verticesPerFrame));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////HPGL SETTINGS
    public Tab tabHPGLSettings = null;
    public AnchorPane anchorPaneHPGLSettings = null;

    public ComboBox<String> comboBoxHPGLPresetCategory = null;
    public ComboBox<GenericPreset<?>> comboBoxHPGLPreset = null;
    public MenuButton menuButtonHPGLPresets = null;

    public TextField textFieldHPGLXMin = null;
    public TextField textFieldHPGLXMax = null;

    public TextField textFieldHPGLYMin = null;
    public TextField textFieldHPGLYMax = null;

    public CheckBox checkBoxHPGLMirrorX = null;
    public CheckBox checkBoxHPGLMirrorY = null;

    public ChoiceBox<EnumAlignment> choiceBoxHPGLAlignX = null;
    public ChoiceBox<EnumAlignment> choiceBoxHPGLAlignY = null;

    public ChoiceBox<EnumRotation> choiceBoxHPGLRotation = null;
    public TextField textFieldHPGLCurveFlatness = null;

    public TextField textFieldHPGLPenSpeed = null;
    public TextField textFieldHPGLPenNumber = null;
    public TextField textFieldHPGLPenForce = null;


    public void initHPGLSettingsPane(){

    }

}
