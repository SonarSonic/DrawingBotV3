package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.api.IProperties;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.CategorySetting;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.SpecialListenable;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PFMSettings extends SpecialListenable<PFMSettings.Listener> implements IProperties {

    ///////////////////////////////////////////////

    public final SimpleObjectProperty<PFMFactory<?>> factory = new SimpleObjectProperty<>();

    public PFMFactory<?> getPFMFactory() {
        return factory.get();
    }

    public SimpleObjectProperty<PFMFactory<?>> factoryProperty() {
        return factory;
    }

    public void setPFMFactory(PFMFactory<?> factory) {
        this.factory.set(factory);
    }

    ///////////////////////////////////////////////

    public final ObjectProperty<GenericPreset<PresetData>> selectedPreset = new SimpleObjectProperty<>();

    public GenericPreset<PresetData> getSelectedPreset() {
        return selectedPreset.get();
    }

    public ObjectProperty<GenericPreset<PresetData>> selectedPresetProperty() {
        return selectedPreset;
    }

    public void setSelectedPreset(GenericPreset<PresetData> selectedPreset) {
        this.selectedPreset.set(selectedPreset);
    }

    ///////////////////////////////////////////////

    public final SimpleObjectProperty<ObservableList<GenericSetting<?, ?>>> settings = new SimpleObjectProperty<>(FXCollections.observableArrayList());

    public ObservableList<GenericSetting<?, ?>> getSettings() {
        return settings.get();
    }

    public SimpleObjectProperty<ObservableList<GenericSetting<?, ?>>> settingsProperty() {
        return settings;
    }

    public void setSettings(ObservableList<GenericSetting<?, ?>> settings) {
        this.settings.set(settings);
    }

    ///////////////////////////////////////////////

    public final SimpleObjectProperty<EnumDistributionType> nextDistributionType = new SimpleObjectProperty<>();

    public EnumDistributionType getNextDistributionType() {
        return nextDistributionType.get();
    }

    public SimpleObjectProperty<EnumDistributionType> nextDistributionTypeProperty() {
        return nextDistributionType;
    }

    public void setNextDistributionType(EnumDistributionType nextDistributionType) {
        this.nextDistributionType.set(nextDistributionType);
    }

    ///////////////////////////////////////////////


    private SimpleObjectProperty<TreeItem<GenericSetting<?, ?>>> treeRoot;

    public TreeItem<GenericSetting<?, ?>> getTreeRoot() {
        return treeRoot == null ? null : treeRoot.get();
    }

    public SimpleObjectProperty<TreeItem<GenericSetting<?, ?>>> treeRootProperty() {
        if(treeRoot == null){
            treeRoot = new SimpleObjectProperty<>();
            treeRoot.set(generateGenericSettingsTree(settings.get()));
            settings.addListener((observable, oldValue, newValue) -> {
                if(oldValue != null){
                    treeRoot.set(null);
                }
                if(newValue != null) {
                    treeRoot.set(generateGenericSettingsTree(newValue));
                }
            });
        }
        return treeRoot;
    }

    public void setTreeRoot(TreeItem<GenericSetting<?, ?>> treeRoot) {
        if(this.treeRoot == null && treeRoot == null){
            return;
        }
        this.treeRootProperty().set(treeRoot);
    }

    ///////////////////////////////////////////////

    {
        PropertyUtil.addSpecialListenerWithSubList(this, settings, (listener, val) -> {}, (listener, val) -> {});

        settings.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                GenericSetting.removeBindings(newValue);
            }
            if(newValue != null){
                GenericSetting.addBindings(newValue);
            }
        });
        factory.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                settings.set(MasterRegistry.INSTANCE.getNewObservableSettingsList(newValue));
                sendListenerEvent(l -> l.onUserChangedPFM(oldValue, newValue));
            }
        });
        selectedPreset.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Register.PRESET_MANAGER_PFM.applyPreset(DrawingBotV3.context(), this, newValue, false);
            }
        });
    }

    public static TreeItem<GenericSetting<?, ?>> generateGenericSettingsTree(Collection<GenericSetting<?, ?>> settings){
        TreeItem<GenericSetting<?, ?>> root = new TreeItem<>(new CategorySetting<>(Object.class, "Root", "Root", true));
        if(settings != null){

            List<CategorySetting<?>> sortedCategories = MasterRegistry.INSTANCE.getSortedCategorySettings(settings);
            Map<String, List<GenericSetting<?, ?>>> groupedSettings = MasterRegistry.INSTANCE.getSettingsByCategory(settings);

            for(CategorySetting<?> categorySetting : sortedCategories){
                TreeItem<GenericSetting<?, ?>> treeItem = new TreeItem<>(categorySetting);
                treeItem.setExpanded(true);
                for(GenericSetting<?, ?> setting : groupedSettings.get(categorySetting.getKey())){
                    treeItem.getChildren().add(new TreeItem<>(setting));
                }
                root.getChildren().add(treeItem);
            }
        }
        return root;
    }

    public PFMSettings copy(){
        PFMSettings copy = new PFMSettings();
        copy.setPFMFactory(getPFMFactory());
        copy.setNextDistributionType(getNextDistributionType());
        copy.setSettings(MasterRegistry.INSTANCE.getNewObservableSettingsList(getPFMFactory()));
        GenericSetting.copy(getSettings(), copy.getSettings());
        return copy;
    }

    ///////////////////////////

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(factory, settings, nextDistributionType);
        }
        return propertyList;
    }

    ///////////////////////////

    public interface Listener extends GenericSetting.Listener {

        default void onUserChangedPFM(PFMFactory<?> oldValue, PFMFactory<?> newValue) {}

        default void onUserChangedPFMPreset(GenericPreset<PresetData> pfmPreset) {}

    }
}
