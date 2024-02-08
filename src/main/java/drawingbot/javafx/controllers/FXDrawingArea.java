package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.controls.ControlPresetSelection;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.registry.Register;
import drawingbot.utils.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.NumberStringConverter;

public class FXDrawingArea extends AbstractFXController {

    public final SimpleObjectProperty<ObservableCanvas> drawingArea = new SimpleObjectProperty<>();
    //public final SimpleObjectProperty<GenericPreset<PresetData>> selectedDrawingAreaPreset = new SimpleObjectProperty<>();

    ////////////////////////////////////////////////////////

    public ControlPresetSelection<ObservableCanvas, PresetData> controlDrawingAreaPreset;
    //public ComboBox<GenericPreset<PresetData>> comboBoxDrawingAreaPreset = null;
    //public MenuButton menuButtonDrawingAreaPresets = null;

    /////SIZING OPTIONS
    public CheckBox checkBoxOriginalSizing = null;
    public ChoiceBox<UnitsLength> choiceBoxDrawingUnits = null;
    public Pane paneDrawingAreaCustom = null;
    public TextField textFieldDrawingWidth = null;
    public TextField textFieldDrawingHeight = null;
    public ChoiceBox<EnumOrientation> choiceBoxOrientation = null;
    public TextField textFieldPaddingLeft = null;
    public TextField textFieldPaddingRight = null;
    public TextField textFieldPaddingTop = null;
    public TextField textFieldPaddingBottom = null;
    public CheckBox checkBoxGangPadding = null;

    public ChoiceBox<EnumCroppingMode> choiceBoxCroppingMode = null;

    public ChoiceBox<EnumRescaleMode> choiceBoxRescaleMode = null;
    public TextField textFieldPenWidth = null;

    public ColorPicker colorPickerCanvas = null;
    public ColorPicker colorPickerBackground = null;

    public ChoiceBox<EnumClippingMode> choiceBoxClippingMode = null;

    @FXML
    public void initialize(){
        drawingArea.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                checkBoxOriginalSizing.selectedProperty().unbindBidirectional(oldValue.useOriginalSizing);
                choiceBoxDrawingUnits.valueProperty().unbindBidirectional(oldValue.inputUnits);

                textFieldDrawingWidth.textProperty().unbindBidirectional(oldValue.width);
                textFieldDrawingHeight.textProperty().unbindBidirectional(oldValue.height);

                textFieldPaddingLeft.textProperty().unbindBidirectional(oldValue.drawingAreaPaddingLeft);
                textFieldPaddingRight.textProperty().unbindBidirectional(oldValue.drawingAreaPaddingRight);
                textFieldPaddingTop.textProperty().unbindBidirectional(oldValue.drawingAreaPaddingTop);
                textFieldPaddingBottom.textProperty().unbindBidirectional(oldValue.drawingAreaPaddingBottom);
                checkBoxGangPadding.selectedProperty().unbindBidirectional(oldValue.drawingAreaGangPadding);

                choiceBoxCroppingMode.valueProperty().unbindBidirectional(oldValue.croppingMode);

                choiceBoxRescaleMode.valueProperty().unbindBidirectional(oldValue.rescaleMode);
                textFieldPenWidth.textProperty().unbindBidirectional(oldValue.targetPenWidth);

                colorPickerCanvas.valueProperty().unbindBidirectional(oldValue.canvasColor);
                colorPickerBackground.valueProperty().unbindBidirectional(oldValue.backgroundColor);
                choiceBoxClippingMode.valueProperty().unbindBidirectional(oldValue.clippingMode);

                choiceBoxOrientation.valueProperty().unbindBidirectional(oldValue.orientation);
                choiceBoxOrientation.disableProperty().unbind();
            }

            if(newValue != null){
                checkBoxOriginalSizing.selectedProperty().bindBidirectional(newValue.useOriginalSizing);
                choiceBoxDrawingUnits.valueProperty().bindBidirectional(newValue.inputUnits);

                textFieldDrawingWidth.textProperty().bindBidirectional(newValue.width, new NumberStringConverter(Utils.defaultDF));
                textFieldDrawingHeight.textProperty().bindBidirectional(newValue.height, new NumberStringConverter(Utils.defaultDF));

                textFieldPaddingLeft.textProperty().bindBidirectional(newValue.drawingAreaPaddingLeft, new NumberStringConverter(Utils.defaultDF));
                textFieldPaddingRight.textProperty().bindBidirectional(newValue.drawingAreaPaddingRight, new NumberStringConverter(Utils.defaultDF));
                textFieldPaddingTop.textProperty().bindBidirectional(newValue.drawingAreaPaddingTop, new NumberStringConverter(Utils.defaultDF));
                textFieldPaddingBottom.textProperty().bindBidirectional(newValue.drawingAreaPaddingBottom, new NumberStringConverter(Utils.defaultDF));
                checkBoxGangPadding.selectedProperty().bindBidirectional(newValue.drawingAreaGangPadding);

                choiceBoxCroppingMode.valueProperty().bindBidirectional(newValue.croppingMode);

                choiceBoxRescaleMode.valueProperty().bindBidirectional(newValue.rescaleMode);
                textFieldPenWidth.textProperty().bindBidirectional(newValue.targetPenWidth, new NumberStringConverter(Utils.defaultDF));

                colorPickerCanvas.valueProperty().bindBidirectional(newValue.canvasColor);
                colorPickerBackground.valueProperty().bindBidirectional(newValue.backgroundColor);
                choiceBoxClippingMode.valueProperty().bindBidirectional(newValue.clippingMode);

                choiceBoxOrientation.valueProperty().bindBidirectional(newValue.orientation);
                choiceBoxOrientation.disableProperty().bind(Bindings.createBooleanBinding(() -> newValue.width.get() == newValue.height.get(), newValue.width, newValue.height));
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        colorPickerCanvas.setValue(Color.WHITE);

        controlDrawingAreaPreset.setPresetManager(Register.PRESET_MANAGER_DRAWING_AREA);
        controlDrawingAreaPreset.targetProperty().bind(drawingArea);
        controlDrawingAreaPreset.setAvailablePresets(Register.PRESET_LOADER_DRAWING_AREA.presets);
        controlDrawingAreaPreset.setActivePreset(Register.PRESET_LOADER_DRAWING_AREA.getDefaultPreset());
        controlDrawingAreaPreset.activePresetProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                EnumOrientation orientation = drawingArea.get().orientation.get();

                Register.PRESET_MANAGER_DRAWING_AREA.applyPreset(DrawingBotV3.context(), drawingArea.get(), newValue, true);

                //Restore the original orientation
                if(drawingArea.get().orientation.get() != orientation){
                    drawingArea.get().orientation.set(orientation);
                }
            }
        });

        /////SIZING OPTIONS

        paneDrawingAreaCustom.disableProperty().bind(checkBoxOriginalSizing.selectedProperty());
        choiceBoxDrawingUnits.disableProperty().bind(checkBoxOriginalSizing.selectedProperty());

        choiceBoxDrawingUnits.getItems().addAll(UnitsLength.values());
        choiceBoxDrawingUnits.setValue(UnitsLength.MILLIMETRES);

        textFieldDrawingWidth.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));
        textFieldDrawingHeight.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        choiceBoxOrientation.setItems(FXCollections.observableArrayList(EnumOrientation.values()));

        textFieldPaddingLeft.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));
        textFieldPaddingRight.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));
        textFieldPaddingTop.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));
        textFieldPaddingBottom.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        choiceBoxCroppingMode.getItems().addAll(EnumCroppingMode.values());
        choiceBoxCroppingMode.setValue(EnumCroppingMode.CROP_TO_FIT);

        choiceBoxRescaleMode.getItems().addAll(EnumRescaleMode.values());
        choiceBoxRescaleMode.setValue(EnumRescaleMode.HIGH_QUALITY);

        textFieldPenWidth.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0.3F));
        textFieldPenWidth.disableProperty().bind(choiceBoxRescaleMode.valueProperty().isEqualTo(EnumRescaleMode.OFF));

        choiceBoxClippingMode.getItems().addAll(EnumClippingMode.values());
        choiceBoxClippingMode.setValue(EnumClippingMode.DRAWING);
    }

}