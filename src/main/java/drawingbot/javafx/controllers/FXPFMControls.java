package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.FXController;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controls.ComboCellNamedSetting;
import drawingbot.javafx.controls.ComboCellPreset;
import drawingbot.javafx.controls.ControlPFMSettingsEditor;
import drawingbot.javafx.controls.ControlPresetSelector;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMSettings;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import org.fxmisc.easybind.EasyBind;

//TODO PRESETS MAY STILL APPLY TO THE MASTER REGISTRY LISTS
public class FXPFMControls extends AbstractFXController {

    public final ObjectProperty<PFMSettings> pfmSettings = new SimpleObjectProperty<>();
    public final FilteredList<PFMFactory<?>> filteredList = new FilteredList<>(MasterRegistry.INSTANCE.pfmFactories);

    ////////////////////////////////////////////////////////

    public ChoiceBox<String> choiceBoxPFMCategory = null;
    public ComboBox<PFMFactory<?>> comboBoxPFM = null;

    public ControlPresetSelector<PFMSettings, PresetData> controlPFMPreset;
    public ControlPFMSettingsEditor controlPFMSettingsEditor;

    @FXML
    public void initialize(){

        controlPFMSettingsEditor.pfmSettingsProperty().bind(pfmSettings);
        controlPFMSettingsEditor.activePresetProperty().bind(controlPFMPreset.activePresetProperty());
        controlPFMSettingsEditor.addSpecialListener(new ControlPFMSettingsEditor.Listener() {
            @Override
            public void onPFMSettingHighlighted(GenericSetting<?, ?> setting) {
                FXDocumentation.onPFMSettingSelected(setting);
            }

            @Override
            public void onPFMSettingsUserEdited() {
                DrawingBotV3.project().onPFMSettingsUserEdited();
            }
        });

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
                controlPFMPreset.activePresetProperty().unbindBidirectional(oldValue.selectedPresetProperty());
                oldValue.factory.removeListener(FACTORY_CHANGE_LISTENER);
            }

            if(newValue != null){
                if(newValue.selectedPreset.get() == null){
                    newValue.setSelectedPreset(Register.PRESET_LOADER_PFM.getDefaultPresetForSubType(newValue.getPFMFactory().getRegistryName()));
                }
                comboBoxPFM.valueProperty().bindBidirectional(newValue.factory);
                controlPFMPreset.setAvailablePresets(MasterRegistry.INSTANCE.getObservablePFMPresetList(newValue.factory.get()));
                controlPFMPreset.activePresetProperty().bindBidirectional(newValue.selectedPresetProperty());

                //tableViewAdvancedPFMSettings.itemsProperty().bind(newValue.settings);
                newValue.factory.addListener(FACTORY_CHANGE_LISTENER);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        ////PATH FINDING CONTROLS

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

        controlPFMPreset.quickSetup(Register.PRESET_MANAGER_PFM);
        controlPFMPreset.targetProperty().bind(pfmSettings);
        controlPFMPreset.setComboBoxFactory(() -> {
            ComboBox<GenericPreset<PresetData>> comboBox =  new ComboBox<>();
            comboBox.setOnAction(e -> pfmSettings.get().sendListenerEvent(l -> l.onUserChangedPFMPreset(controlPFMPreset.getActivePreset())));
            comboBox.setCellFactory(param -> new ComboCellPreset<>());
            return comboBox;
        });

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
