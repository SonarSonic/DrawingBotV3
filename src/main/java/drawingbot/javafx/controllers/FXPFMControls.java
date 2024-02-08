package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.FXController;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controls.*;
import drawingbot.javafx.settings.CategorySetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMSettings;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import org.fxmisc.easybind.EasyBind;

//TODO PRESETS MAY STILL APPLY TO THE MASTER REGISTRY LISTS
public class FXPFMControls extends AbstractFXController {

    public final SimpleObjectProperty<PFMSettings> pfmSettings = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<GenericPreset<PresetData>> selectedPFMPreset = new SimpleObjectProperty<>();
    public final FilteredList<PFMFactory<?>> filteredList = new FilteredList<>(MasterRegistry.INSTANCE.pfmFactories);

    ////////////////////////////////////////////////////////

    public ChoiceBox<String> choiceBoxPFMCategory = null;
    public ComboBox<PFMFactory<?>> comboBoxPFM = null;

    public ControlPresetSelection<PFMSettings, PresetData> controlPFMPreset;

    public TreeTableView<GenericSetting<?, ?>> treeTableViewPFMSettings = null;
    public TreeTableColumn<GenericSetting<?, ?>, Boolean> treeTableColumnLock = null;
    public TreeTableColumn<GenericSetting<?, ?>, String> treeTableColumnSetting = null;
    public TreeTableColumn<GenericSetting<?, ?>, Object> treeTableColumnValue = null;
    public TreeTableColumn<GenericSetting<?, ?>, Object> treeTableColumnControl = null;

    public Button buttonPFMSettingReset = null;
    public Button buttonPFMSettingRandom = null;
    public Button buttonPFMSettingHelp = null;

    @FXML
    public void initialize(){

        final ChangeListener<PFMFactory<?>> FACTORY_CHANGE_LISTENER = (observable, oldValue, newValue) -> {
            if(newValue == null){
                pfmSettings.get().setPFMFactory(comboBoxPFM.getItems().get(0));
            }else{
                controlPFMPreset.setAvailablePresets(MasterRegistry.INSTANCE.getObservablePFMPresetList(newValue));
                controlPFMPreset.setActivePreset(Register.PRESET_LOADER_PFM.getDefaultPresetForSubType(newValue.getRegistryName()));
            }
        };

        pfmSettings.addListener((observable, oldValue, newValue) -> {

            if(oldValue != null){
                comboBoxPFM.valueProperty().unbindBidirectional(oldValue.factory);
                treeTableViewPFMSettings.rootProperty().unbind();
                oldValue.factory.removeListener(FACTORY_CHANGE_LISTENER);
            }

            if(newValue != null){
                comboBoxPFM.valueProperty().bindBidirectional(newValue.factory);
                controlPFMPreset.setAvailablePresets(MasterRegistry.INSTANCE.getObservablePFMPresetList(newValue.factory.get()));

                //tableViewAdvancedPFMSettings.itemsProperty().bind(newValue.settings);
                treeTableViewPFMSettings.rootProperty().bind(newValue.treeRoot);
                newValue.factory.addListener(FACTORY_CHANGE_LISTENER);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ////PATH FINDING CONTROLS
        /*
        if(menuButtonPFM != null){
            menuButtonPFM.getItems().clear();
            ObservableList<PFMFactory<?>> pfmFactories = MasterRegistry.INSTANCE.getObservablePFMLoaderList();
            List<String> categories = new ArrayList<>();
            for(PFMFactory<?> factory : pfmFactories){
                if(!categories.contains(factory.category)){
                    categories.add(factory.category);
                }
            }
            for(String category : categories){
                Menu pfmCategory = new Menu(category);
                for(PFMFactory<?> factory : pfmFactories){
                    if(factory.category.equals(category)){
                        MenuItem pfmItem = new MenuItem();
                        pfmItem.setText(factory.getDisplayName());
                        pfmItem.setOnAction(e -> changePathFinderModule(factory));
                        pfmCategory.getItems().add(pfmItem);
                    }
                }
                menuButtonPFM.getItems().add(pfmCategory);
            }
        }
        */


        choiceBoxPFMCategory.setItems(MasterRegistry.INSTANCE.pfmCategories);
        choiceBoxPFMCategory.setValue("All");

        EasyBind.subscribe(choiceBoxPFMCategory.valueProperty(), s -> {
            filteredList.setPredicate(f -> {
                if(!f.isHidden() || FXApplication.isDeveloperMode){
                    return choiceBoxPFMCategory.getValue().equals("All") || f.category.equals(choiceBoxPFMCategory.getValue());
                }
                return false;
            });
            comboBoxPFM.setValue(filteredList.get(0));
            //comboBoxPFM.setVisibleRowCount(filteredList.size());
            comboBoxPFM.setItems(filteredList);
            comboBoxPFM.setCellFactory(param -> new ComboCellNamedSetting<>());
        });

        if(comboBoxPFM != null){
            comboBoxPFM.setCellFactory(param -> new ComboCellNamedSetting<>());
            comboBoxPFM.setItems(filteredList);
            comboBoxPFM.setValue(MasterRegistry.INSTANCE.getDefaultPFM());
            comboBoxPFM.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue != null){
                    changePathFinderModule(newValue);
                }
            });
            comboBoxPFM.setVisibleRowCount(30);
        }

        selectedPFMPreset.setValue(Register.PRESET_LOADER_PFM.getDefaultPreset());
        selectedPFMPreset.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Register.PRESET_MANAGER_PFM.applyPreset(DrawingBotV3.context(), pfmSettings.get(), newValue, false);
            }
        });

        controlPFMPreset.setPresetManager(Register.PRESET_MANAGER_PFM);
        controlPFMPreset.targetProperty().bind(pfmSettings);
        controlPFMPreset.setAvailablePresets(Register.PRESET_LOADER_PFM.presets);
        controlPFMPreset.activePresetProperty().bindBidirectional(selectedPFMPreset);
        controlPFMPreset.setComboBoxFactory(() -> {
            ComboBox<GenericPreset<PresetData>> comboBox =  new ComboBox<>();
            comboBox.setOnAction(e -> pfmSettings.get().sendListenerEvent(l -> l.onUserChangedPFMPreset(controlPFMPreset.getActivePreset())));
            comboBox.setCellFactory(param -> new ComboCellPreset<>());
            return comboBox;
        });

        //FXHelper.setupPresetMenuButton(menuButtonPFMPresets, Register.PRESET_LOADER_PFM, () -> pfmSettingsPresetManager, false, selectedPFMPreset);

        treeTableViewPFMSettings.setRowFactory(param -> {
            TreeTableRow<GenericSetting<?, ?>> row = new TreeTableRow<>();
            row.setContextMenu(new ContextMenuGenericSetting(row, pfmSettings.get().settings, true));
            row.treeItemProperty().addListener((observable, oldValue, newValue) -> {
                row.disableProperty().unbind();
                if(newValue != null){
                    row.disableProperty().bind(newValue.getValue().disabledProperty());
                }
            });
            row.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue){
                    FXDocumentation.onPFMSettingSelected(row.getItem());
                }
            });
            return row;
        });
        treeTableViewPFMSettings.setShowRoot(false);

        treeTableColumnLock.setCellFactory(param ->
             new CheckBoxTreeTableCell<>(index -> treeTableColumnLock.getCellObservableValue(index))
        );
        treeTableColumnLock.setCellValueFactory(param -> {
            GenericSetting<?, ? > setting = param.getValue().getValue();
            return setting instanceof CategorySetting ? null : setting.randomiseExcludeProperty();
        });

        treeTableColumnSetting.setCellValueFactory(param -> param.getValue().getValue().displayNameProperty());

        treeTableColumnValue.setCellFactory(param -> {
            TextFieldTreeTableCell<GenericSetting<?, ?>, Object> cell = new TextFieldTreeTableCell<>();

            cell.setConverter(new StringConverterGenericSetting(() -> {
                if(cell.getTableRow() != null && cell.getTableRow().getTreeItem() != null){
                    GenericSetting<?, ? > setting = cell.getTableRow().getTreeItem().getValue();
                    return setting instanceof CategorySetting ? null : setting;
                }
                return null;
            }));

            return cell;
        });
        treeTableColumnValue.setCellValueFactory(param -> {
            GenericSetting<?, ? > setting = param.getValue().getValue();
            return setting instanceof CategorySetting ? null : (ObservableValue<Object>) setting.valueProperty();
        });

        treeTableColumnControl.setCellFactory(param -> new TreeTableCellSettingControl());
        treeTableColumnControl.setCellValueFactory(param -> {
            GenericSetting<?, ? > setting = param.getValue().getValue();
            return setting instanceof CategorySetting ? null : (ObservableValue<Object>) setting.valueProperty();
        });

        /*
        tableViewAdvancedPFMSettings.setRowFactory(param -> {
            TableRow<GenericSetting<?, ?>> row = new TableRow<>();
            row.setContextMenu(new ContextMenuPFMSetting(row));
            return row;
        });

        tableColumnLock.setCellFactory(param -> new CheckBoxTableCell<>(index -> tableColumnLock.getCellObservableValue(index)));
        tableColumnLock.setCellValueFactory(param -> param.getValue().randomiseExclude);

        tableColumnSetting.setCellValueFactory(param -> param.getValue().displayNameProperty());

        tableColumnValue.setCellFactory(param -> {
            TextFieldTableCell<GenericSetting<?, ?>, Object> cell = new TextFieldTableCell<>();
            cell.setConverter(new StringConverterGenericSetting(() -> cell.tableViewProperty().get().getItems().get(cell.getIndex())));
            return cell;
        });
        tableColumnValue.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

        tableColumnControl.setCellFactory(param -> new TableCellSettingControl());
        tableColumnControl.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

         */

        buttonPFMSettingReset.setOnAction(e -> {
            if(controlPFMPreset.getActivePreset() == null){
                GenericSetting.resetSettings(pfmSettings.get().settings.get());
            }else{
                controlPFMPreset.applyPreset(DrawingBotV3.context());
            }
            DrawingBotV3.project().onPFMSettingsUserEdited();
        });

        buttonPFMSettingRandom.setOnAction(e -> {
            GenericSetting.randomiseSettings(pfmSettings.get().settings.get());
            DrawingBotV3.project().onPFMSettingsUserEdited();
        });
        buttonPFMSettingHelp.setOnAction(e -> FXDocumentation.openPFMHelp(pfmSettings.get().getPFMFactory()));
    }

    public void changePathFinderModule(PFMFactory<?> pfm){
        if(pfm == null){
            return;
        }
        if(pfm.isPremiumFeature() && !FXApplication.isPremiumEnabled){
            if(comboBoxPFM != null){
                comboBoxPFM.setValue(MasterRegistry.INSTANCE.getDefaultPFM());
            }
            changePathFinderModule(MasterRegistry.INSTANCE.getDefaultPFM());
            FXController.showPremiumFeatureDialog();
        }else{
            pfmSettings.get().factory.set(pfm);
            pfmSettings.get().nextDistributionType.set(pfm.getDistributionType());
        }
    }

}
