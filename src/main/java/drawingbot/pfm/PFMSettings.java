package drawingbot.pfm;

import drawingbot.api.IProperties;
import drawingbot.files.json.presets.PresetPFMSettings;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.CategorySetting;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.SpecialListenable;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    public final SimpleObjectProperty<TreeItem<GenericSetting<?, ?>>> treeRoot = new SimpleObjectProperty<>();

    public TreeItem<GenericSetting<?, ?>> getTreeRoot() {
        return treeRoot.get();
    }

    public SimpleObjectProperty<TreeItem<GenericSetting<?, ?>>> treeRootProperty() {
        return treeRoot;
    }

    public void setTreeRoot(TreeItem<GenericSetting<?, ?>> treeRoot) {
        this.treeRoot.set(treeRoot);
    }

    ///////////////////////////////////////////////

    {
        PropertyUtil.addSpecialListenerWithSubList(this, settings, (listener, val) -> {}, (listener, val) -> {});

        treeRoot.set(generateGenericSettingsTree(settings.get()));
        settings.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                treeRoot.set(null);
                removeBindings(newValue);
            }
            if(newValue != null){
                treeRoot.set(generateGenericSettingsTree(newValue));
                addBindings(newValue);
            }
        });
        factory.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                settings.set(MasterRegistry.INSTANCE.getObservablePFMSettingsList(newValue));
                sendListenerEvent(l -> l.onPFMChanged(oldValue, newValue));
            }
        });
    }


    public void addBindings(ObservableList<GenericSetting<?, ?>> settings){
        for(GenericSetting<?, ?> setting : settings){
            if(setting.getBindingFactory() != null){
                setting.getBindingFactory().accept(setting, settings);
            }
        }
    }

    public void removeBindings(ObservableList<GenericSetting<?, ?>> settings){
        for(GenericSetting<?, ?> setting : settings){
            setting.removeBindings();
        }
    }

    public static TreeItem<GenericSetting<?, ?>> generateGenericSettingsTree(ObservableList<GenericSetting<?, ?>> settings){
        TreeItem<GenericSetting<?, ?>> root = new TreeItem<>(new CategorySetting<>(Object.class, "Root", "Root", true));
        if(settings != null){
            Map<String, List<GenericSetting<?, ?>>> result = settings.stream().collect(Collectors.groupingBy(GenericSetting::getCategory));
            List<String> categoryNames = new ArrayList<>(result.keySet());
            List<CategorySetting<?>> sortedCategories = new ArrayList<>();
            for(String categoryRegistryName : categoryNames){
                sortedCategories.add(MasterRegistry.INSTANCE.getCategorySettingInstance(categoryRegistryName));
            }
            sortedCategories.sort(Comparator.comparingInt(value -> -value.getPriority()));

            for(CategorySetting<?> categorySetting : sortedCategories){
                TreeItem<GenericSetting<?, ?>> treeItem = new TreeItem<>(categorySetting);
                treeItem.setExpanded(true);
                for(GenericSetting<?, ?> setting : result.get(categorySetting.getKey())){
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

        default void onPFMChanged(PFMFactory<?> oldValue, PFMFactory<?> newValue) {}

        default void onUserChangedPFMPreset(GenericPreset<PresetPFMSettings> pfmPreset) {}

    }
}
