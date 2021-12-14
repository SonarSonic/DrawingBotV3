package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.DrawingStyle;
import drawingbot.drawing.DrawingStyleSet;
import drawingbot.javafx.controls.*;
import drawingbot.javafx.observables.ObservableDrawingStyle;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.files.presets.types.PresetPFMSettings;
import drawingbot.javafx.settings.DrawingStylesSetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMMosaicCustom;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;

public class FXStylesController {

    public DrawingStylesSetting master = null;
    public ObservableList<ObservableDrawingStyle> masterStyles = FXCollections.observableArrayList();

    public DrawingStylesSetting editing = null;
    public final ObservableList<ObservableDrawingStyle> editingStyles = FXCollections.observableArrayList();


    public SimpleObjectProperty<ObservableDrawingStyle> editingDrawingStyle = new SimpleObjectProperty<>(null);
    public boolean switchingStyle = false;

    public void initialize(){
            initStylesTable();
            initPFMSettingsControl();

            DrawingBotV3.INSTANCE.controller.mosaicSettingsStage.setOnHidden((e) -> {
                if(editing != null){
                    saveChanges(editing, editingStyles);
                    editing = null;
                }
                if(master != null){
                    saveChanges(master, masterStyles);
                    DrawingStylesSetting reopen = master;
                    master = null;

                    Platform.runLater(() -> openWidget(reopen, (DrawingStyleSet) reopen.getValue()));
                }
            });
    }

    public void openWidget(DrawingStylesSetting stylesSetting, DrawingStyleSet set){
        if(master != null){
            return;
        }
        if(editing != null && editing != stylesSetting){
            master = editing;
            masterStyles.clear();
            masterStyles.addAll(editingStyles);
        }
        boolean isLayeredPFM = DrawingBotV3.INSTANCE.pfmFactory.get().getInstanceClass() == PFMMosaicCustom.class;

        DrawingBotV3.INSTANCE.controller.mosaicSettingsStage.setTitle("Editing Drawing Styles" + (master != null ? ": Editing Slave" : ": Editing Master"));

        styleWeightColumn.setVisible(!isLayeredPFM);
        styleMaskColorColumn.setVisible(isLayeredPFM);

        editingStyles.clear();
        set.styles.forEach(style ->  editingStyles.add(new ObservableDrawingStyle(style)));
        editing = stylesSetting;
        DrawingBotV3.INSTANCE.controller.mosaicSettingsStage.show();
    }

    public void saveChanges(DrawingStylesSetting setting, ObservableList<ObservableDrawingStyle> styles){
        DrawingStyleSet styleSet = new DrawingStyleSet(new ArrayList<>());
        styles.forEach(style -> styleSet.styles.add(new DrawingStyle(style)));
        setting.value.set(styleSet);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////STYLES TABLE
    public TableView<ObservableDrawingStyle> stylesTableView = null;
    public TableColumn<ObservableDrawingStyle, Boolean> styleEnableColumn = null;
    public TableColumn<ObservableDrawingStyle, String> styleNameColumn = null;
    public TableColumn<ObservableDrawingStyle, Integer> styleWeightColumn = null;
    public TableColumn<ObservableDrawingStyle, Color> styleMaskColorColumn = null;

    // public TableColumn<ObservableDrawingStyle, String> styleTypeColumn = null;
    //public TableColumn<ObservableDrawingStyle, String> penPercentageColumn = null;
    //public TableColumn<ObservableDrawingStyle, Integer> penLinesColumn = null;

    public ChoiceBox<PFMFactory<?>> choiceBoxAddPFM = null;

    public Button buttonAddStyle = null;
    public Button buttonRemoveStyle = null;
    public Button buttonDuplicateStyle = null;
    public Button buttonMoveUpStyle = null;
    public Button buttonMoveDownStyle = null;

    public void initStylesTable(){
        stylesTableView.setItems(editingStyles);

        stylesTableView.setRowFactory(param -> {
            TableRow<ObservableDrawingStyle> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            // row.setContextMenu(new ContextMenuObservablePen(row)); //TODO
            return row;
        });

        stylesTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switchingStyle = true;
            editingDrawingStyle.setValue(null);
            if(newValue != null){
                choiceBoxPFM.setValue(newValue.getFactory());
                tableViewAdvancedPFMSettings.setItems(newValue.pfmSettings);
            }else{
                choiceBoxPFM.setValue(null);
                tableViewAdvancedPFMSettings.setItems(FXCollections.emptyObservableList());
            }
            editingDrawingStyle.setValue(newValue);

            switchingStyle = false;
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        styleEnableColumn.setCellFactory(param -> new CheckBoxTableCell<>(index -> styleEnableColumn.getCellObservableValue(index)));
        styleEnableColumn.setCellValueFactory(param -> param.getValue().enable);

        styleNameColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        styleNameColumn.setCellValueFactory(param -> param.getValue().name);

        styleWeightColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        styleWeightColumn.setCellValueFactory(param -> param.getValue().distributionWeight.asObject());

        styleMaskColorColumn.setCellFactory(TableCellColorPicker::new);
        styleMaskColorColumn.setCellValueFactory(param -> param.getValue().maskColor);

        //penPercentageColumn.setCellValueFactory(param -> param.getValue().currentPercentage);
        //penLinesColumn.setCellValueFactory(param -> param.getValue().currentGeometries.asObject());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        choiceBoxAddPFM.setItems(MasterRegistry.INSTANCE.getObservablePFMLoaderList());
        choiceBoxAddPFM.setValue(MasterRegistry.INSTANCE.getDefaultPFM());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        buttonAddStyle.setOnAction(e -> editingStyles.add(new ObservableDrawingStyle(choiceBoxAddPFM.getValue())));
        buttonRemoveStyle.setOnAction(e -> FXHelper.deleteItem(stylesTableView.getSelectionModel().getSelectedItem(), editingStyles));
        buttonDuplicateStyle.setOnAction(e -> {
            ObservableDrawingStyle style = stylesTableView.getSelectionModel().getSelectedItem();
            if(style != null) {
                editingStyles.add(new ObservableDrawingStyle(style));
            }
        });
        buttonMoveUpStyle.setOnAction(e -> FXHelper.moveItemUp(stylesTableView.getSelectionModel().getSelectedItem(), editingStyles));
        buttonMoveDownStyle.setOnAction(e -> FXHelper.moveItemDown(stylesTableView.getSelectionModel().getSelectedItem(), editingStyles));
        buttonMoveDownStyle.setOnAction(e -> FXHelper.moveItemDown(stylesTableView.getSelectionModel().getSelectedItem(), editingStyles));

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PFM SETTINGS

    public AnchorPane anchorPanePFM = null;
    public ChoiceBox<PFMFactory<?>> choiceBoxPFM = null;

    public ComboBox<GenericPreset<PresetPFMSettings>> comboBoxPFMPreset = null;
    public MenuButton menuButtonPFMPresets = null;

    public TableView<GenericSetting<?,?>> tableViewAdvancedPFMSettings = null;
    public TableColumn<GenericSetting<?, ?>, Boolean> tableColumnLock = null;
    public TableColumn<GenericSetting<?, ?>, String> tableColumnSetting = null;
    public TableColumn<GenericSetting<?, ?>, Object> tableColumnValue = null;
    public TableColumn<GenericSetting<?, ?>, Object> tableColumnControl = null;

    public Button buttonPFMSettingReset = null;
    public Button buttonPFMSettingRandom = null;
    public Button buttonPFMSettingHelp = null;

    public void initPFMSettingsControl(){

        anchorPanePFM.disableProperty().bind(editingDrawingStyle.isNull());

        choiceBoxPFM.setItems(MasterRegistry.INSTANCE.getObservablePFMLoaderList());
        choiceBoxPFM.setValue(MasterRegistry.INSTANCE.getDefaultPFM());
        choiceBoxPFM.valueProperty().addListener((observable, oldValue, newValue) -> {
            comboBoxPFMPreset.setItems(MasterRegistry.INSTANCE.getObservablePFMPresetList(newValue));
            comboBoxPFMPreset.setValue(MasterRegistry.INSTANCE.getDefaultPFMPreset(newValue));
            if(editingDrawingStyle.get() != null && editingDrawingStyle.get().pfmFactory.get() != newValue){
                editingDrawingStyle.get().pfmFactory.set(newValue);
                editingDrawingStyle.get().pfmSettings = MasterRegistry.INSTANCE.getNewObservableSettingsList(newValue);
                tableViewAdvancedPFMSettings.setItems(editingDrawingStyle.get().pfmSettings);
                editingDrawingStyle.get().name.set(newValue.getName());
            }
        });


        comboBoxPFMPreset.setItems(MasterRegistry.INSTANCE.getObservablePFMPresetList());
        comboBoxPFMPreset.setValue(MasterRegistry.INSTANCE.getDefaultPFMPreset());
        comboBoxPFMPreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null && !switchingStyle && editingDrawingStyle.get() != null){ //messy but needed to prevent it resetting the style's configuration
               //JsonLoaderManager.PFM.applyPreset(newValue);
                GenericSetting.applySettings(newValue.data.settingList, editingDrawingStyle.get().pfmSettings);
            }
        });



        FXHelper.setupPresetMenuButton(JsonLoaderManager.PFM, menuButtonPFMPresets, false, comboBoxPFMPreset::getValue, (preset) -> {
            comboBoxPFMPreset.setValue(preset);

            ///force update rendering
            comboBoxPFMPreset.setItems(MasterRegistry.INSTANCE.getObservablePFMPresetList());
            comboBoxPFMPreset.setButtonCell(new ComboBoxListCell<>());
        });


        tableViewAdvancedPFMSettings.setItems(FXCollections.emptyObservableList());
        tableViewAdvancedPFMSettings.setRowFactory(param -> {
            TableRow<GenericSetting<?, ?>> row = new TableRow<>();
            row.setContextMenu(new ContextMenuPFMSetting(row));
            return row;
        });

        tableColumnLock.setCellFactory(param -> new CheckBoxTableCell<>(index -> tableColumnLock.getCellObservableValue(index)));
        tableColumnLock.setCellValueFactory(param -> param.getValue().lock);

        tableColumnSetting.setCellValueFactory(param -> param.getValue().settingName);

        tableColumnValue.setCellFactory(param -> {
            TextFieldTableCell<GenericSetting<?, ?>, Object> cell = new TextFieldTableCell<>();
            cell.setConverter(new StringConverterGenericSetting(() -> cell.tableViewProperty().get().getItems().get(cell.getIndex())));
            return cell;
        });
        tableColumnValue.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

        tableColumnControl.setCellFactory(param -> new TableCellSettingControl());
        tableColumnControl.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

        buttonPFMSettingReset.setOnAction(e -> JsonLoaderManager.PFM.applyPreset(comboBoxPFMPreset.getValue()));

        buttonPFMSettingRandom.setOnAction(e -> GenericSetting.randomiseSettings(tableViewAdvancedPFMSettings.getItems()));
        buttonPFMSettingHelp.setOnAction(e -> FXHelper.openURL(DBConstants.URL_READ_THE_DOCS_PFMS));

    }

}
