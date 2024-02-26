package drawingbot.javafx.controls;

import drawingbot.files.json.IPresetManager;
import drawingbot.javafx.GenericPreset;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Skin;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * UI control for the selection of a {@link GenericPreset} and it's preset sub type, it also provides the "presets" menu button
 * Use the {@link #quickSetup(IPresetManager)} method to configure the preset selector easily
 * @param <TARGET> the type of the instance to apply the preset to
 * @param <DATA> the data type stored in the preset e.g. {@link drawingbot.files.json.PresetData}
 */
public class ControlPresetSelectorCategory<TARGET, DATA> extends ControlPresetSelector<TARGET, DATA> {

    public ControlPresetSelectorCategory() {
        super();
        activePreset.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                if(!newValue.getPresetSubType().equals(getActiveCategory())){
                    setActiveCategory(newValue.getPresetSubType());
                }
            }
        });
        activeCategory.addListener((observable, oldValue, newValue) -> {
            if(newValue != null && isAutoUpdateActivePreset()){
                if(getActivePreset() == null || !getActivePreset().getPresetSubType().equals(newValue)) {
                    GenericPreset<DATA> defaultPreset = getPresetLoader().getDefaultPresetForSubType(newValue);
                    if(defaultPreset == null){
                        defaultPreset = getPresetLoader().getPresetsForSubType(newValue).stream().findFirst().orElse(null);
                    }
                    setActivePreset(defaultPreset);
                }
            }
        });
        presetManager.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                setCategories(newValue.getPresetLoader().getPresetSubTypes());

                if(isAutoUpdateActivePreset()) {
                    GenericPreset<DATA> defaultPreset = newValue.getPresetLoader().getDefaultPreset();
                    if (defaultPreset != null) {
                        setActiveCategory(defaultPreset.getPresetSubType());
                        setActivePreset(defaultPreset);
                    }
                }
            }
        });

        categoryPresetList.bind(Bindings.createObjectBinding(() -> {
            if(activeCategory.get() == null || presetListFactory.get() == null){
                if(presetManager.get() != null){
                    return presetManager.get().getPresetLoader().getPresetsForSubType(activeCategory.get());
                }
                return FXCollections.observableArrayList();
            }
            return presetListFactory.get().apply(activeCategory.get());
        }, activeCategory, presetListFactory, presetManager));
    }

    @Override
    public void quickSetup(IPresetManager<TARGET, DATA> manager){
        setPresetManager(manager);
        setCategories(manager.getPresetLoader().getPresetSubTypes());
        setAvailablePresets(manager.getPresetLoader().getPresets());
        setActivePreset(manager.getPresetLoader().getDefaultPreset());
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ObservableList<String>> categories = new SimpleObjectProperty<>();

    public ObservableList<String> getCategories() {
        return categories.get();
    }

    public ObjectProperty<ObservableList<String>> categoriesProperty() {
        return categories;
    }

    public void setCategories(ObservableList<String> categories) {
        this.categories.set(categories);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<String> activeCategory = new SimpleObjectProperty<>();

    public String getActiveCategory() {
        return activeCategory.get();
    }

    public ObjectProperty<String> activeCategoryProperty() {
        return activeCategory;
    }

    public void setActiveCategory(String activeCategory) {
        this.activeCategory.set(activeCategory);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<Function<String, ObservableList<GenericPreset<DATA>>>> presetListFactory = new SimpleObjectProperty<>();

    public Function<String, ObservableList<GenericPreset<DATA>>> getPresetListFactory() {
        return presetListFactory.get();
    }

    public ObjectProperty<Function<String, ObservableList<GenericPreset<DATA>>>> presetListFactoryProperty() {
        return presetListFactory;
    }

    public void setPresetListFactory(Function<String, ObservableList<GenericPreset<DATA>>> presetListFactory) {
        this.presetListFactory.set(presetListFactory);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ObservableList<GenericPreset<DATA>>> categoryPresetList = new SimpleObjectProperty<>();

    public ObservableList<GenericPreset<DATA>> getCategoryPresetList() {
        return categoryPresetList.get();
    }

    public ObjectProperty<ObservableList<GenericPreset<DATA>>> categoryPresetListProperty() {
        return categoryPresetList;
    }

    public void setCategoryPresetList(ObservableList<GenericPreset<DATA>> categoryPresetList) {
        this.categoryPresetList.set(categoryPresetList);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<Supplier<ComboBox<String>>> categoryComboBoxFactory = new SimpleObjectProperty<>();

    public Supplier<ComboBox<String>> getCategoryComboBoxFactory() {
        return categoryComboBoxFactory.get();
    }

    public ObjectProperty<Supplier<ComboBox<String>>> categoryComboBoxFactoryProperty() {
        return categoryComboBoxFactory;
    }

    public void setCategoryComboBoxFactory(Supplier<ComboBox<String>> categoryComboBoxFactory) {
        this.categoryComboBoxFactory.set(categoryComboBoxFactory);
    }

    ////////////////////////////////////////////////////////

    public BooleanProperty autoUpdateActivePreset = new SimpleBooleanProperty(true);

    public boolean isAutoUpdateActivePreset() {
        return autoUpdateActivePreset.get();
    }

    public BooleanProperty autoUpdateActivePresetProperty() {
        return autoUpdateActivePreset;
    }

    public void setAutoUpdateActivePreset(boolean autoUpdateActivePreset) {
        this.autoUpdateActivePreset.set(autoUpdateActivePreset);
    }

    ////////////////////////////////////////////////////////

    @Override
    public void refresh(){
        if(getSkin() instanceof SkinPresetSelectionCategory<?, ?> skin){
            skin.refresh();
        }
    }

    ////////////////////////////////////////////////////////

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinPresetSelectionCategory<>(this);
    }
}
