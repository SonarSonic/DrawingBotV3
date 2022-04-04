package drawingbot.javafx.controllers;

import drawingbot.files.json.presets.PresetImageFilters;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controls.ContextMenuObservableFilter;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.EnumFilterTypes;
import drawingbot.utils.EnumRotation;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.converter.DefaultStringConverter;

import java.awt.image.BufferedImageOp;

public class FXImageFilters {

    public final SimpleObjectProperty<ObservableList<ObservableImageFilter>> currentFilters = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<EnumRotation> imageRotation = new SimpleObjectProperty<>(EnumRotation.R0);
    public final SimpleBooleanProperty imageFlipHorizontal = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty imageFlipVertical = new SimpleBooleanProperty(false);

    public ComboBox<GenericPreset<PresetImageFilters>> comboBoxImageFilterPreset = null;

    public MenuButton menuButtonFilterPresets = null;

    public TableView<ObservableImageFilter> tableViewImageFilters = null;
    public TableColumn<ObservableImageFilter, Boolean> columnEnableImageFilter = null;
    public TableColumn<ObservableImageFilter, String> columnImageFilterType = null;
    public TableColumn<ObservableImageFilter, String> columnImageFilterSettings = null;

    public ComboBox<EnumFilterTypes> comboBoxFilterType = null;
    public ComboBox<GenericFactory<BufferedImageOp>> comboBoxImageFilter = null;
    public Button buttonAddFilter = null;

    public ChoiceBox<EnumRotation> choiceBoxRotation = null;
    public CheckBox checkBoxFlipX = null;
    public CheckBox checkBoxFlipY = null;

    @FXML
    public void initialize(){

        comboBoxImageFilterPreset.setItems(Register.PRESET_LOADER_FILTERS.presets);
        comboBoxImageFilterPreset.setValue(Register.PRESET_LOADER_FILTERS.getDefaultPreset());
        comboBoxImageFilterPreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Register.PRESET_LOADER_FILTERS.applyPreset(newValue);
            }
        });

        FXHelper.setupPresetMenuButton(Register.PRESET_LOADER_FILTERS, menuButtonFilterPresets, false, comboBoxImageFilterPreset::getValue, (preset) -> {
            comboBoxImageFilterPreset.setValue(preset);

            ///force update rendering
            comboBoxImageFilterPreset.setItems(Register.PRESET_LOADER_FILTERS.presets);
            comboBoxImageFilterPreset.setButtonCell(new ComboBoxListCell<>());
        });

        tableViewImageFilters.itemsProperty().bind(currentFilters);
        tableViewImageFilters.setRowFactory(param -> {
            TableRow<ObservableImageFilter> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setOnMouseClicked(e -> {
                if(e.getClickCount() > 1){
                    FXHelper.openImageFilterDialog(row.getItem());
                }
            });
            row.setContextMenu(new ContextMenuObservableFilter(row));
            row.setPrefHeight(30);
            return row;
        });

        columnEnableImageFilter.setCellFactory(param -> new CheckBoxTableCell<>(index -> columnEnableImageFilter.getCellObservableValue(index)));
        columnEnableImageFilter.setCellValueFactory(param -> param.getValue().enable);

        columnImageFilterType.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        columnImageFilterType.setCellValueFactory(param -> param.getValue().name);


        columnImageFilterSettings.setCellValueFactory(param -> param.getValue().settingsString);

        comboBoxFilterType.setItems(FXCollections.observableArrayList(MasterRegistry.INSTANCE.imgFilterFactories.keySet()));
        comboBoxFilterType.setValue(MasterRegistry.INSTANCE.getDefaultImageFilterType());
        comboBoxFilterType.valueProperty().addListener((observable, oldValue, newValue) -> {
            comboBoxImageFilter.setItems(MasterRegistry.INSTANCE.imgFilterFactories.get(newValue));
            comboBoxImageFilter.setValue(MasterRegistry.INSTANCE.getDefaultImageFilter(newValue));
        });

        comboBoxImageFilter.setItems(MasterRegistry.INSTANCE.imgFilterFactories.get(MasterRegistry.INSTANCE.getDefaultImageFilterType()));
        comboBoxImageFilter.setValue(MasterRegistry.INSTANCE.getDefaultImageFilter(MasterRegistry.INSTANCE.getDefaultImageFilterType()));
        buttonAddFilter.setOnAction(e -> {
            if(comboBoxImageFilter.getValue() != null){
                FXHelper.addImageFilter(comboBoxImageFilter.getValue());
            }
        });

        choiceBoxRotation.setItems(FXCollections.observableArrayList(EnumRotation.DEFAULTS));
        choiceBoxRotation.setValue(EnumRotation.R0);
        choiceBoxRotation.valueProperty().bindBidirectional(imageRotation);

        checkBoxFlipX.setSelected(false);
        checkBoxFlipX.selectedProperty().bindBidirectional(imageFlipHorizontal);

        checkBoxFlipY.setSelected(false);
        checkBoxFlipY.selectedProperty().bindBidirectional(imageFlipVertical);

    }

}
