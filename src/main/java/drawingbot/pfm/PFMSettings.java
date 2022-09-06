package drawingbot.pfm;

import drawingbot.api.IProperties;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.CategorySetting;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PFMSettings implements IProperties {

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

    public final SimpleObjectProperty<ObservableList<GenericSetting<?, ?>>> settings = new SimpleObjectProperty<>();

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

    public final ObservableList<Property<?>> observables = PropertyUtil.createPropertiesList(factory, settings, nextDistributionType);

    {
        treeRoot.set(generatePFMSettingTree(settings.get()));
        settings.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                treeRoot.set(null);
                removeBindings(newValue);
            }
            if(newValue != null){
                treeRoot.set(generatePFMSettingTree(newValue));
                addBindings(newValue);
            }
        });
    }

    public static void addBindings(ObservableList<GenericSetting<?, ?>> settings){
        for(GenericSetting<?, ?> setting : settings){
            if(setting.getBindingFactory() != null){
                setting.getBindingFactory().accept(setting, settings);
            }
        }
    }

    public static void removeBindings(ObservableList<GenericSetting<?, ?>> settings){
        for(GenericSetting<?, ?> setting : settings){
            setting.removeBindings();
        }
    }

    public TreeItem<GenericSetting<?, ?>> generatePFMSettingTree(ObservableList<GenericSetting<?, ?>> settings){
        TreeItem<GenericSetting<?, ?>> root = new TreeItem<>(new CategorySetting<>(Object.class, "Root", "Root", true));
        if(settings != null){
            Map<String, List<GenericSetting<?, ?>>> result = settings.stream().collect(Collectors.groupingBy(GenericSetting::getCategory));
            List<String> sortedCategories = new ArrayList<>(result.keySet());
            sortedCategories.sort(Comparator.comparingInt(value -> -MasterRegistry.INSTANCE.getCategoryPriority(value)));

            for(String category : sortedCategories){

                TreeItem<GenericSetting<?, ?>> treeItem = new TreeItem<>(new CategorySetting<>(Object.class, category, category, true));
                treeItem.setExpanded(true);
                for(GenericSetting<?, ?> setting : result.get(category)){
                    treeItem.getChildren().add(new TreeItem<>(setting));
                }
                root.getChildren().add(treeItem);
            }
        }
        return root;
    }

    @Override
    public ObservableList<Property<?>> getProperties() {
        return observables;
    }

    public PFMSettings copy(){
        PFMSettings copy = new PFMSettings();
        copy.setPFMFactory(getPFMFactory());
        copy.setNextDistributionType(getNextDistributionType());
        copy.setSettings(MasterRegistry.INSTANCE.getNewObservableSettingsList(getPFMFactory()));
        GenericSetting.copy(getSettings(), copy.getSettings());
        return copy;
    }
}
