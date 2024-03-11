package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.presets.PresetImageFilters;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.format.ImageData;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controls.ControlImageFiltersEditor;
import drawingbot.javafx.controls.ControlPresetSelector;
import drawingbot.registry.Register;
import drawingbot.utils.EnumRotation;
import drawingbot.utils.Utils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.css.Styleable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.NumberStringConverter;
import org.fxmisc.easybind.EasyBind;

import java.util.List;

public class FXImageProcessing extends AbstractFXController {

    public final SimpleObjectProperty<ImageFilterSettings> settings = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<ImageData> image = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<GenericPreset<PresetImageFilters>> selectedImagePreset = new SimpleObjectProperty<>();

    ////////////////////////////////////////////////////////

    public ControlPresetSelector<ImageFilterSettings, PresetImageFilters> controlImageFilterPreset;

    public ControlImageFiltersEditor imageFiltersControl;

    public TextField textFieldCropStartX = null;
    public TextField textFieldCropStartY = null;
    public TextField textFieldCropWidth = null;
    public TextField textFieldCropHeight = null;
    public ToggleButton buttonEditCrop = null;
    public Button buttonResetCrop;

    public ChoiceBox<EnumRotation> choiceBoxRotation = null;
    public CheckBox checkBoxFlipX = null;
    public CheckBox checkBoxFlipY = null;

    @FXML
    public void initialize(){

        imageFiltersControl.settings.bind(settings);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        selectedImagePreset.setValue(Register.PRESET_LOADER_FILTERS.getDefaultPreset());
        selectedImagePreset.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Register.PRESET_MANAGER_FILTERS.applyPreset(DrawingBotV3.context(), settings.get(), newValue, false);
            }
        });

        controlImageFilterPreset.setPresetManager(Register.PRESET_MANAGER_FILTERS);
        controlImageFilterPreset.targetProperty().bind(settings);
        controlImageFilterPreset.setAvailablePresets(Register.PRESET_LOADER_FILTERS.getPresets());
        controlImageFilterPreset.activePresetProperty().bindBidirectional(selectedImagePreset);

        image.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                textFieldCropStartX.textProperty().unbindBidirectional(oldValue.getImageCropping().cropStartX);
                textFieldCropStartY.textProperty().unbindBidirectional(oldValue.getImageCropping().cropStartY);
                textFieldCropWidth.textProperty().unbindBidirectional(oldValue.getImageCropping().cropWidth);
                textFieldCropHeight.textProperty().unbindBidirectional(oldValue.getImageCropping().cropHeight);
                choiceBoxRotation.valueProperty().unbindBidirectional(oldValue.getImageCropping().imageRotation);
                checkBoxFlipX.selectedProperty().unbindBidirectional(oldValue.getImageCropping().imageFlipHorizontal);
                checkBoxFlipY.selectedProperty().unbindBidirectional(oldValue.getImageCropping().imageFlipVertical);
            }
            if(newValue != null){
                textFieldCropStartX.textProperty().bindBidirectional(newValue.getImageCropping().cropStartX, new NumberStringConverter(Utils.oneDecimal));
                textFieldCropStartY.textProperty().bindBidirectional(newValue.getImageCropping().cropStartY, new NumberStringConverter(Utils.oneDecimal));
                textFieldCropWidth.textProperty().bindBidirectional(newValue.getImageCropping().cropWidth, new NumberStringConverter(Utils.oneDecimal));
                textFieldCropHeight.textProperty().bindBidirectional(newValue.getImageCropping().cropHeight, new NumberStringConverter(Utils.oneDecimal));
                choiceBoxRotation.valueProperty().bindBidirectional(newValue.getImageCropping().imageRotation);
                checkBoxFlipX.selectedProperty().bindBidirectional(newValue.getImageCropping().imageFlipHorizontal);
                checkBoxFlipY.selectedProperty().bindBidirectional(newValue.getImageCropping().imageFlipVertical);
            }
        });

        textFieldCropStartX.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));
        textFieldCropStartY.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));
        textFieldCropWidth.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));
        textFieldCropHeight.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(ObservableProject::displayModeProperty).addListener((observable, oldValue, newValue) -> {
            buttonEditCrop.setSelected(newValue == Register.INSTANCE.DISPLAY_MODE_IMAGE_CROPPING);
        });

        buttonEditCrop.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                DrawingBotV3.project().displayMode.set(Register.INSTANCE.DISPLAY_MODE_IMAGE_CROPPING);
            }else if(DrawingBotV3.project().displayMode.get() == Register.INSTANCE.DISPLAY_MODE_IMAGE_CROPPING){
                DrawingBotV3.project().displayMode.set(Register.INSTANCE.DISPLAY_MODE_IMAGE);
            }
        });

        buttonResetCrop.setOnAction(e -> {
            if(DrawingBotV3.project().openImage.get() != null){
                DrawingBotV3.project().openImage.get().resetCrop();
            }
        });

        choiceBoxRotation.setItems(FXCollections.observableArrayList(EnumRotation.DEFAULTS));
        choiceBoxRotation.setValue(EnumRotation.R0);

        checkBoxFlipX.setSelected(false);

        checkBoxFlipY.setSelected(false);

    }

    @Override
    public List<Styleable> getPersistentNodes() {
        return imageFiltersControl.getPersistentNodes();
    }
}
