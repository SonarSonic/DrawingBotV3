package drawingbot.javafx.controls;

import javafx.scene.control.ComboBox;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class SkinPresetSelectionCategory<TARGET, DATA> extends SkinBase<ControlPresetSelectorCategory<TARGET, DATA>> {

    protected HBox hBox;
    protected ComboBox<String> comboBox;
    protected ControlPresetSelector<TARGET, DATA> internalPresetSelection;

    protected SkinPresetSelectionCategory(ControlPresetSelectorCategory<TARGET, DATA> control) {
        super(control);

        comboBox = createComboBox(control);

        internalPresetSelection = new ControlPresetSelector<>();
        internalPresetSelection.presetManagerProperty().bind(control.presetManagerProperty());
        internalPresetSelection.activePresetProperty().bindBidirectional(control.activePresetProperty()); //bi-directional to allow selection
        internalPresetSelection.availablePresetsProperty().bind(control.categoryPresetListProperty());
        internalPresetSelection.presetFilterProperty().bind(control.presetFilterProperty());
        internalPresetSelection.targetProperty().bind(control.targetProperty());
        internalPresetSelection.comboBoxFactoryProperty().bind(control.comboBoxFactoryProperty());
        internalPresetSelection.disablePresetMenuProperty().bind(control.disablePresetMenuProperty());

        hBox = new HBox(4, comboBox, internalPresetSelection);
        HBox.setHgrow(hBox, Priority.ALWAYS);
        getChildren().add(hBox);

        //Update the combo box if the factory is altered
        control.categoryComboBoxFactoryProperty().addListener((observable, oldValue, newValue) -> {
            refreshComboBox();
        });

    }

    private void refreshComboBox(){
        ControlPresetSelectorCategory<TARGET, DATA> control = getSkinnable();
        ComboBox<String> oldComboBox = comboBox;
        ComboBox<String> newComboBox = createComboBox(control);

        destroyComboBox(control, oldComboBox);
        hBox.getChildren().set(0, newComboBox);
        comboBox = newComboBox;
    }

    private ComboBox<String> createComboBox(ControlPresetSelectorCategory<TARGET, DATA> control){
        ComboBox<String> categoryComboBox = control.getCategoryComboBoxFactory() != null ? control.getCategoryComboBoxFactory().get() : new ComboBox<>();
        if(categoryComboBox.getPromptText() == null){
            categoryComboBox.setPromptText("Category");
        }
        categoryComboBox.valueProperty().bindBidirectional(control.activeCategoryProperty());
        categoryComboBox.itemsProperty().bind(control.categoriesProperty());
        categoryComboBox.setPrefWidth(300);

        HBox.setHgrow(categoryComboBox, Priority.ALWAYS);
        return categoryComboBox;
    }

    private void destroyComboBox(ControlPresetSelectorCategory<TARGET, DATA> control, ComboBox<String> comboBox){
        comboBox.valueProperty().unbindBidirectional(control.activeCategoryProperty());
        comboBox.itemsProperty().unbind();
    }

    public void refresh(){
        internalPresetSelection.refresh();
        refreshComboBox();
    }
}
