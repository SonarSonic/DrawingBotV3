package drawingbot.javafx.controllers;

import drawingbot.FXApplication;
import drawingbot.files.json.presets.PresetPFMSettings;
import drawingbot.files.json.presets.PresetPFMSettingsManager;
import drawingbot.javafx.FXController;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controls.ComboCellNamedSetting;
import drawingbot.javafx.controls.ContextMenuPFMSetting;
import drawingbot.javafx.controls.StringConverterGenericSetting;
import drawingbot.javafx.controls.TableCellSettingControl;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMSettings;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.DBConstants;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.control.cell.TextFieldTableCell;

//TODO PRESETS MAY STILL APPLY TO THE MASTER REGISTRY LISTS
public class FXPFMControls {

    public final SimpleObjectProperty<PFMSettings> pfmSettings = new SimpleObjectProperty<>();

    ////////////////////////////////////////////////////////

    public ComboBox<PFMFactory<?>> comboBoxPFM = null;

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

    @FXML
    public void initialize(){

        final ChangeListener<PFMFactory<?>> FACTORY_CHANGE_LISTENER = (observable, oldValue, newValue) -> {
            comboBoxPFMPreset.setItems(MasterRegistry.INSTANCE.getObservablePFMPresetList(newValue));
            comboBoxPFMPreset.setValue(Register.PRESET_LOADER_PFM.getDefaultPresetForSubType(newValue.getName()));
        };

        pfmSettings.addListener((observable, oldValue, newValue) -> {

            if(oldValue != null){
                comboBoxPFM.valueProperty().unbindBidirectional(oldValue.factory);
                tableViewAdvancedPFMSettings.itemsProperty().unbind();
                oldValue.factory.removeListener(FACTORY_CHANGE_LISTENER);
            }

            if(newValue != null){
                comboBoxPFM.valueProperty().bindBidirectional(newValue.factory);
                comboBoxPFMPreset.setItems(MasterRegistry.INSTANCE.getObservablePFMPresetList(newValue.factory.get()));

                tableViewAdvancedPFMSettings.itemsProperty().bind(newValue.settings);
                newValue.factory.addListener(FACTORY_CHANGE_LISTENER);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ////PATH FINDING CONTROLS
        comboBoxPFM.setCellFactory(param -> new ComboCellNamedSetting<>());
        comboBoxPFM.setItems(MasterRegistry.INSTANCE.getObservablePFMLoaderList());
        comboBoxPFM.setValue(MasterRegistry.INSTANCE.getDefaultPFM());
        comboBoxPFM.valueProperty().addListener((observable, oldValue, newValue) -> changePathFinderModule(newValue));


        comboBoxPFMPreset.setValue(Register.PRESET_LOADER_PFM.getDefaultPreset());
        comboBoxPFMPreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                pfmSettingsPresetManager.applyPreset(newValue);
            }
        });

        FXHelper.setupPresetMenuButton(Register.PRESET_LOADER_PFM, () -> pfmSettingsPresetManager, menuButtonPFMPresets, false, comboBoxPFMPreset::getValue, (preset) -> {
            comboBoxPFMPreset.setValue(preset);

            ///force update rendering
            comboBoxPFMPreset.setItems(MasterRegistry.INSTANCE.getObservablePFMPresetList(pfmSettings.getValue().factory.get()));
            comboBoxPFMPreset.setButtonCell(new ComboBoxListCell<>());
        });



        tableViewAdvancedPFMSettings.setRowFactory(param -> {
            TableRow<GenericSetting<?, ?>> row = new TableRow<>();
            row.setContextMenu(new ContextMenuPFMSetting(row));
            return row;
        });

        tableColumnLock.setCellFactory(param -> new CheckBoxTableCell<>(index -> tableColumnLock.getCellObservableValue(index)));
        tableColumnLock.setCellValueFactory(param -> param.getValue().randomiseExclude);

        tableColumnSetting.setCellValueFactory(param -> param.getValue().key);

        tableColumnValue.setCellFactory(param -> {
            TextFieldTableCell<GenericSetting<?, ?>, Object> cell = new TextFieldTableCell<>();
            cell.setConverter(new StringConverterGenericSetting(() -> cell.tableViewProperty().get().getItems().get(cell.getIndex())));
            return cell;
        });
        tableColumnValue.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

        tableColumnControl.setCellFactory(param -> new TableCellSettingControl());
        tableColumnControl.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

        buttonPFMSettingReset.setOnAction(e -> pfmSettingsPresetManager.applyPreset(comboBoxPFMPreset.getValue()));

        buttonPFMSettingRandom.setOnAction(e -> GenericSetting.randomiseSettings(tableViewAdvancedPFMSettings.getItems()));
        buttonPFMSettingHelp.setOnAction(e -> FXHelper.openURL(DBConstants.URL_READ_THE_DOCS_PFMS));
    }

    public void changePathFinderModule(PFMFactory<?> pfm){
        if(pfm.isPremiumFeature() && !FXApplication.isPremiumEnabled){
            comboBoxPFM.setValue(MasterRegistry.INSTANCE.getDefaultPFM());
            FXController.showPremiumFeatureDialog();
        }else{
            pfmSettings.get().factory.set(pfm);
            pfmSettings.get().nextDistributionType.set(pfm.getDistributionType());
        }
    }

    ////////////////////////////////////////////////////////

    private final PresetPFMSettingsManager pfmSettingsPresetManager = new PresetPFMSettingsManager(Register.PRESET_LOADER_PFM) {

        @Override
        public Property<PFMFactory<?>> pfmProperty() {
            return pfmSettings.get().factory;
        }

        @Override
        public Property<ObservableList<GenericSetting<?, ?>>> settingProperty() {
            return pfmSettings.get().settings;
        }
    };

    ////////////////////////////////////////////////////////

}
