package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.files.FileUtils;
import drawingbot.files.json.*;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controls.ControlPresetEditor;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.overlays.NotificationOverlays;
import drawingbot.utils.Utils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.converter.DefaultStringConverter;
import org.controlsfx.control.action.Action;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//TODO DRAWING AREA - THE A4 PRESETS ACTUALLY EXCLUDE SOME SETTINGS, SO THEY SHOULD BE NOT VISIBLE!!
//ONLY COMMIT DIRTY CHANGES ONCE CLOSED
public class FXPresetManager extends AbstractFXController{

    public ObjectProperty<ObservableList<GenericPreset<?>>> presets = new SimpleObjectProperty<>();
    public ObjectProperty<ObservableList<GenericPreset<?>>> filteredPresets = new SimpleObjectProperty<>();
    public StringProperty search = new SimpleStringProperty("");
    public ObjectProperty<PresetType> type = new SimpleObjectProperty<>();
    public StringProperty category = new SimpleStringProperty();
    public ObjectProperty<FilterState> state = new SimpleObjectProperty<>(FilterState.ALL);
    public ObjectProperty<GenericPreset<?>> selectedPreset = new SimpleObjectProperty<>();

    public TextField textFieldSearch;
    public ComboBox<PresetType> comboBoxFilterType;
    public ComboBox<String> comboBoxFilterCategory;
    public ComboBox<FilterState> comboBoxFilterState;
    public TableView<GenericPreset<?>> tableViewPresets;
    public Button buttonAddPreset;
    public Button buttonRemovePreset;
    public Button buttonDuplicatePreset;
    public Button buttonMoveUpPreset;
    public Button buttonMoveDownPreset;
    public TableColumn<GenericPreset<?>, Boolean> tableColumnEnabled;
    public TableColumn<GenericPreset<?>, String> tableColumnCategory;
    public TableColumn<GenericPreset<?>, String> tableColumnName;

    public ScrollPane scrollBoxPresetInspector;
    public ControlPresetEditor controlPresetEditor;
    public Button buttonResetChanges;
    public Button buttonSaveChanges;
    public Button buttonImportPresets;

    public MenuItem menuButtonExportSelectedPresets;
    public Menu menuExportPerType;
    public MenuItem menuButtonExportAll;

    public enum FilterState{
        ALL(filter -> true),
        SYSTEM_PRESETS(filter -> !filter.isUserPreset()),
        USER_PRESETS(filter -> filter.isUserPreset()),
        HIDDEN_ONLY(filter -> !filter.isEnabled());

        public Predicate<GenericPreset<?>> filter;

        FilterState(Predicate<GenericPreset<?>> filter){
            this.filter = filter;
        }


        @Override
        public String toString() {
            return Utils.capitalize(name());
        }
    }

    @FXML
    public void initialize(){
        presets.bind(Bindings.createObjectBinding(this::getPresetList, type, category));
        filteredPresets.bind(Bindings.createObjectBinding(() -> new FilteredList<>(presets.get(), this::filterPreset), presets));
        type.set(Register.PRESET_TYPE_PFM);
        type.addListener((observable, oldValue, newValue) -> category.set(null));

        textFieldSearch.textProperty().bindBidirectional(search);
        textFieldSearch.textProperty().addListener((observable, oldValue, newValue) -> refreshPresetList());

        comboBoxFilterType.valueProperty().bindBidirectional(type);
        comboBoxFilterType.setItems(new FilteredList<>(FXCollections.observableArrayList(MasterRegistry.INSTANCE.presetTypes), type -> !type.hidden));

        comboBoxFilterCategory.valueProperty().bindBidirectional(category);
        comboBoxFilterCategory.itemsProperty().bind(Bindings.createObjectBinding(this::getTypeCategories, type));

        comboBoxFilterState.valueProperty().bindBidirectional(state);
        comboBoxFilterState.itemsProperty().set(FXCollections.observableArrayList(FilterState.values()));
        comboBoxFilterState.valueProperty().addListener((observable, oldValue, newValue) -> refreshPresetList());

        tableViewPresets.itemsProperty().bind(filteredPresets);
        tableViewPresets.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem export = new MenuItem("Export");
            export.setOnAction(e -> actionExportSelectedPresets());

            MenuItem add = new MenuItem("Add");
            add.setOnAction(e -> actionAdd());
            add.disableProperty().bind(buttonAddPreset.disableProperty());

            MenuItem delete = new MenuItem("Delete");
            delete.setOnAction(e -> actionRemove());
            delete.disableProperty().bind(buttonRemovePreset.disableProperty());

            MenuItem duplicate = new MenuItem("Duplicate");
            duplicate.setOnAction(e -> actionDuplicate());
            duplicate.disableProperty().bind(buttonDuplicatePreset.disableProperty());

            MenuItem moveUp = new MenuItem("Move Up");
            moveUp.setOnAction(e -> actionMoveUp());
            moveUp.disableProperty().bind(buttonMoveUpPreset.disableProperty());

            MenuItem moveDown = new MenuItem("Move Down");
            moveDown.setOnAction(e -> actionMoveDown());
            moveDown.disableProperty().bind(buttonMoveDownPreset.disableProperty());

            contextMenu.getItems().addAll(export, add, delete, duplicate, moveUp, moveDown);
            tableViewPresets.setContextMenu(contextMenu);
        }

        selectedPreset.bind(tableViewPresets.getSelectionModel().selectedItemProperty());

        tableColumnEnabled.setCellFactory(param -> new CheckBoxTableCell<>());
        tableColumnEnabled.setCellValueFactory(param -> param.getValue().enabledProperty());

        tableColumnCategory.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        tableColumnCategory.setCellValueFactory(param -> param.getValue().presetSubTypeProperty());
        tableColumnCategory.setEditable(false);

        tableColumnName.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        tableColumnName.setCellValueFactory(param -> param.getValue().presetNameProperty());
        tableColumnCategory.setEditable(false);

        controlPresetEditor = new ControlPresetEditor();
        controlPresetEditor.setDetailed(true);
        controlPresetEditor.selectedPreset.bind(selectedPreset);
        VBox.setVgrow(controlPresetEditor, Priority.ALWAYS);
        HBox.setHgrow(controlPresetEditor, Priority.ALWAYS);
        scrollBoxPresetInspector.setContent(controlPresetEditor);

        buttonSaveChanges.setOnAction(e -> {
            GenericPreset<?> selected = controlPresetEditor.getSelectedPreset();
            GenericPreset<?> preset = controlPresetEditor.confirmEdit();
            if(preset == null || selected == preset){
                return;
            }
            if(filterPreset(preset)){
                selectPreset(preset);
            }else{
                selectPreset(null);
            }
        });
        buttonResetChanges.setOnAction(e -> controlPresetEditor.resetEdit());

        buttonAddPreset.setOnAction(e -> actionAdd());
        buttonAddPreset.setTooltip(new Tooltip("Create New Preset"));

        buttonRemovePreset.setOnAction(e -> actionRemove());
        buttonRemovePreset.setTooltip(new Tooltip("Delete Selected Presets"));
        buttonRemovePreset.disableProperty().bind(Bindings.isEmpty(tableViewPresets.getSelectionModel().getSelectedItems()));

        buttonDuplicatePreset.setOnAction(e -> actionDuplicate());
        buttonDuplicatePreset.disableProperty().bind(Bindings.createBooleanBinding(() -> tableViewPresets.getSelectionModel().getSelectedItems().size() != 1, tableViewPresets.getSelectionModel().selectedItemProperty(), tableViewPresets.getSelectionModel().getSelectedItems()));

        buttonMoveUpPreset.setOnAction(e -> actionMoveUp());
        buttonMoveUpPreset.disableProperty().bind(Bindings.createBooleanBinding(() -> tableViewPresets.getSelectionModel().getSelectedItems().size() != 1, tableViewPresets.getSelectionModel().selectedItemProperty(), tableViewPresets.getSelectionModel().getSelectedItems()));

        buttonMoveDownPreset.setOnAction(e -> actionMoveDown());
        buttonMoveDownPreset.disableProperty().bind(Bindings.createBooleanBinding(() -> tableViewPresets.getSelectionModel().getSelectedItems().size() != 1, tableViewPresets.getSelectionModel().selectedItemProperty(), tableViewPresets.getSelectionModel().getSelectedItems()));

        buttonImportPresets.setOnAction(e -> actionImportPresets());

        menuButtonExportSelectedPresets.setOnAction(e -> actionExportSelectedPresets());
        menuButtonExportSelectedPresets.disableProperty().bind(Bindings.isEmpty(tableViewPresets.getSelectionModel().getSelectedItems()));

        for(PresetType type : MasterRegistry.INSTANCE.presetTypes.stream().filter(type -> !type.isHidden()).collect(Collectors.toSet())){
            MenuItem menuItem = new MenuItem(type.getDisplayName());
            IPresetLoader<?> presetLoader = MasterRegistry.INSTANCE.getPresetLoader(type);
            menuItem.setOnAction(e -> actionExportPresets((List<GenericPreset<?>>) (Object)presetLoader.getSaveablePresets()));
            menuExportPerType.getItems().add(menuItem);
        }

        menuButtonExportAll.setOnAction(e -> {
            Set<PresetType> types = MasterRegistry.INSTANCE.presetTypes.stream().filter(type -> !type.isHidden()).collect(Collectors.toSet());
            List<GenericPreset<?>> presets = types.stream().mapMulti((BiConsumer<PresetType, ? super Consumer<GenericPreset<?>>>)(type, consumer) -> MasterRegistry.INSTANCE.getPresetLoader(type).getSaveablePresets().forEach(consumer)).collect(Collectors.toList());
            actionExportPresets(presets);
        });

    }

    private FileChooser importFileChooser = null;

    public void actionImportPresets(){
        if(importFileChooser == null){
            importFileChooser = FXHelper.createFileChooser("Import Presets", DrawingBotV3.project().getImportDirectory(), FileUtils.FILTER_JSON);
        }
        List<File> files = importFileChooser.showOpenMultipleDialog(FXApplication.primaryStage);

        if(files != null){
            //Collect all presets from provided files
            List<GenericPreset<?>> presets = new ArrayList<>();
            files.forEach(file -> presets.addAll(JsonLoaderManager.importPresetContainerFile(file, false)));

            //Find all the preset types
            Set<PresetType> types = presets.stream().map(GenericPreset::getPresetType).collect(Collectors.toSet());

            //Notify the user
            NotificationOverlays.INSTANCE.showWithSubtitle("Imported %s Presets".formatted(presets.size()), "Types: %s".formatted(types.toString()));

            //Select the imported presets in the table
            selectPresets(presets);
        }
    }

    public void actionExportSelectedPresets(){
        actionExportPresets(new ArrayList<>(tableViewPresets.getSelectionModel().getSelectedItems()));
    }

    public void actionExportPresets(List<GenericPreset<?>> presets){
        FileChooser fileChooser = FXHelper.createFileChooser("Export Presets", DrawingBotV3.project().getExportDirectory(), FileUtils.FILTER_JSON);
        File file = fileChooser.showSaveDialog(FXApplication.primaryStage);

        if(file != null){
            JsonLoaderManager.exportPresetContainerFile(file, new PresetContainerJsonFile(presets));
            Set<PresetType> types = presets.stream().map(GenericPreset::getPresetType).collect(Collectors.toSet());
            NotificationOverlays.INSTANCE.showWithSubtitle("Exported %s Presets".formatted(presets.size()), "Types: %s".formatted(types.toString()), new Action("Open Folder", event -> FXHelper.openFolder(file.getParentFile())));
        }
    }

    public void actionAdd(){
        if(type.get() != null){
            IPresetManager<?, ?> manager = MasterRegistry.INSTANCE.getDefaultPresetManager(type.get());
            GenericPreset<?> preset = FXHelper.actionNewPreset(manager, null, true);
            selectPreset(preset);
        }
    }

    public void actionRemove(){
        if(!tableViewPresets.getSelectionModel().getSelectedItems().isEmpty()){
            FXHelper.actionDeletePresets(new ArrayList<>(tableViewPresets.getSelectionModel().getSelectedItems()));
        }
    }


    public void actionDuplicate(){
        if(tableViewPresets.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        GenericPreset<?> preset = tableViewPresets.getSelectionModel().getSelectedItem();
        GenericPreset<?> duplicate = FXHelper.actionDuplicatePreset(preset);
        selectPreset(duplicate);
    }

    public void actionMoveUp(){
        move(-1);
    }

    public void actionMoveDown(){
        move(1);
    }

    public void move(int shift){
        if(tableViewPresets.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        IPresetLoader<?> loader = MasterRegistry.INSTANCE.getPresetLoader(type.get());
        GenericPreset<?> preset = tableViewPresets.getSelectionModel().getSelectedItem();
        if(loader.reorderPreset(preset, tableViewPresets.getItems(), shift)){
            selectPreset(preset);
        }
    }

    public void selectPreset(GenericPreset<?> preset){
        tableViewPresets.getSelectionModel().clearSelection();

        if(preset != null){
            int index = tableViewPresets.getItems().indexOf(preset);
            if(index != -1){
                tableViewPresets.scrollTo(index);
                tableViewPresets.getSelectionModel().select(index);
            }
        }
    }

    public void selectPresets(List<GenericPreset<?>> presets){
        tableViewPresets.getSelectionModel().clearSelection();

        for(GenericPreset<?> preset : presets){
            int index = tableViewPresets.getItems().indexOf(preset);
            if(index != -1){
                tableViewPresets.scrollTo(index);
                tableViewPresets.getSelectionModel().select(index);
            }
        }
    }


    public void refreshPresetList(){
        ObservableList<GenericPreset<?>> list = filteredPresets.get();
        if(list instanceof FilteredList<GenericPreset<?>> fList){
            fList.setPredicate(this::filterPreset);
        }
    }

    public ObservableList<String> getTypeCategories(){
        IPresetLoader<?> loader = MasterRegistry.INSTANCE.getPresetLoader(type.get());
        if(loader == null){
            return FXCollections.observableArrayList();
        }
        return loader.getPresetSubTypes();
    }

    public ObservableList<GenericPreset<?>> getPresetList(){
        if(type.get() != null){
            IPresetLoader loader = MasterRegistry.INSTANCE.getPresetLoader(type.get());
            if(loader != null){
                if(category.get() != null){
                    return loader.getPresetsForSubType(category.get());
                }
                return loader.getPresets();
            }
        }
        return FXCollections.observableArrayList();
    }

    public boolean filterPreset(GenericPreset<?> preset){
        if(!state.get().filter.test(preset)){
            return false;
        }
        if(!search.get().isEmpty() && !preset.getPresetName().toLowerCase().contains(search.get().toLowerCase())){
            return false;
        }
        return true;
    }
}
