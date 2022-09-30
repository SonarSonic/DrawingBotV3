package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.api.Hooks;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.ColourSeperationHandler;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.drawing.DrawingSets;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.presets.PresetDrawingPen;
import drawingbot.files.json.presets.PresetDrawingSet;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.controls.*;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.Optional;

public class FXDrawingSets {

    public SimpleObjectProperty<DrawingSets> drawingSets = new SimpleObjectProperty<>();

    ////////////////////////////////////////////////////////

    public ComboBox<String> comboBoxSetType = null;
    public ComboBox<IDrawingSet<IDrawingPen>> comboBoxDrawingSet = null;
    public MenuButton menuButtonDrawingSetPresets = null;

    public TableView<ObservableDrawingPen> penTableView = null;
    public TableColumn<ObservableDrawingPen, Boolean> penEnableColumn = null;
    public TableColumn<ObservableDrawingPen, String> penTypeColumn = null;
    public TableColumn<ObservableDrawingPen, String> penNameColumn = null;
    public TableColumn<ObservableDrawingPen, Color> penColourColumn = null;
    public TableColumn<ObservableDrawingPen, Float> penStrokeColumn = null;
    public TableColumn<ObservableDrawingPen, String> penPercentageColumn = null;
    public TableColumn<ObservableDrawingPen, Integer> penWeightColumn = null;
    public TableColumn<ObservableDrawingPen, Integer> penLinesColumn = null;

    public ComboBox<String> comboBoxPenType = null;
    public ComboBox<DrawingPen> comboBoxDrawingPen = null;
    public ComboBoxListViewSkin<DrawingPen> comboBoxDrawingPenSkin = null;
    public MenuButton menuButtonDrawingPenPresets = null;

    public Button buttonAddPen = null;
    public Button buttonRemovePen = null;
    public Button buttonDuplicatePen = null;
    public Button buttonMoveUpPen = null;
    public Button buttonMoveDownPen = null;
    public Button buttonClearDrawingSet = null;

    public ComboBox<EnumDistributionType> comboBoxDistributionType = null;
    public ComboBox<EnumDistributionOrder> comboBoxDistributionOrder = null;

    public ComboBox<ColourSeperationHandler> comboBoxColourSeperation = null;
    public Button buttonConfigureSplitter = null;

    public ComboBox<ObservableDrawingSet> comboBoxDrawingSets = null;
    //public Button buttonAddDrawingSet = null;
    //public Button buttonDeleteDrawingSet = null;

    public TableView<ObservableDrawingSet> drawingSetTableView = null;
    public TableColumn<ObservableDrawingSet, String> drawingSetNameColumn = null;
    public TableColumn<ObservableDrawingSet, ObservableList<ObservableDrawingPen>> drawingSetPensColumn = null;
    public TableColumn<ObservableDrawingSet, EnumDistributionType> drawingSetDistributionTypeColumn = null;
    public TableColumn<ObservableDrawingSet, EnumDistributionOrder> drawingSetDistributionOrderColumn = null;
    public TableColumn<ObservableDrawingSet, ColourSeperationHandler> drawingSetColourSeperatorColumn = null;
    public TableColumn<ObservableDrawingSet, Integer> drawingSetShapesColumn = null;
    public TableColumn<ObservableDrawingSet, Integer> drawingSetPercentageColumn = null;


    public Button buttonAddDrawingSetSlot = null;
    public Button buttonRemoveDrawingSetSlot = null;
    public Button buttonDuplicateDrawingSetSlot = null;
    public Button buttonMoveUpDrawingSetSlot = null;
    public Button buttonMoveDownDrawingSetSlot = null;
    public Button buttonClearDrawingSets = null;

    @FXML
    public void initialize(){

        final ChangeListener<ObservableDrawingSet> activeSetListener = (observable, oldValue, newValue) -> onChangedActiveDrawingSet(oldValue, newValue);
        drawingSets.addListener((observable, oldValue, newValue) -> {

            if(oldValue != null){
                comboBoxDrawingSets.itemsProperty().unbind();
                comboBoxDrawingSets.valueProperty().unbindBidirectional(oldValue.activeDrawingSet);

                onChangedActiveDrawingSet(oldValue.activeDrawingSet.get(), null);
                oldValue.activeDrawingSet.removeListener(activeSetListener);

                drawingSetTableView.itemsProperty().unbind();
            }

            if(newValue != null){
                comboBoxDrawingSets.itemsProperty().bind(newValue.drawingSetSlots);
                comboBoxDrawingSets.setValue(newValue.activeDrawingSet.get());
                comboBoxDrawingSets.valueProperty().bindBidirectional(newValue.activeDrawingSet);

                onChangedActiveDrawingSet(null, newValue.activeDrawingSet.get());
                newValue.activeDrawingSet.addListener(activeSetListener);

                drawingSetTableView.itemsProperty().bind(drawingSets.get().drawingSetSlots);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        comboBoxSetType.setItems(FXCollections.observableArrayList(MasterRegistry.INSTANCE.registeredSets.keySet()));
        comboBoxSetType.setValue(MasterRegistry.INSTANCE.getDefaultDrawingSet().getType());
        comboBoxSetType.valueProperty().addListener((observable, oldValue, newValue) -> {
            comboBoxDrawingSet.setItems(MasterRegistry.INSTANCE.registeredSets.get(newValue));
            comboBoxDrawingSet.setValue(null);
        });

        comboBoxDrawingSet.setItems(MasterRegistry.INSTANCE.registeredSets.get(comboBoxSetType.getValue()));
        comboBoxDrawingSet.setValue(MasterRegistry.INSTANCE.getDefaultDrawingSet());
        comboBoxDrawingSet.valueProperty().addListener((observable, oldValue, newValue) -> drawingSets.get().changeDrawingSet(newValue));
        comboBoxDrawingSet.setCellFactory(param -> new ComboCellDrawingSet<>());
        comboBoxDrawingSet.setButtonCell(new ComboCellDrawingSet<>());
        comboBoxDrawingSet.setPromptText("Select a Drawing Set");

        FXHelper.setupPresetMenuButton(Register.PRESET_LOADER_DRAWING_SET, this::getDrawingSetPresetManager, menuButtonDrawingSetPresets, true,
                () -> {
                    if(comboBoxDrawingSet.getValue() instanceof PresetDrawingSet){
                        PresetDrawingSet set = (PresetDrawingSet) comboBoxDrawingSet.getValue();
                        return set.preset;
                    }
                    return null;
                }, (preset) -> {
                    //force update rendering
                    comboBoxSetType.setItems(FXCollections.observableArrayList(MasterRegistry.INSTANCE.registeredSets.keySet()));
                    comboBoxDrawingSet.setItems(MasterRegistry.INSTANCE.registeredSets.get(comboBoxSetType.getValue()));
                    comboBoxDrawingSet.setButtonCell(new ComboCellDrawingSet<>());
                    if(preset != null){
                        comboBoxSetType.setValue(preset.presetSubType);
                        comboBoxDrawingSet.setValue(preset.data);
                    }
            /*
            else{
                //don't set to avoid overwriting the users configured pens
                //comboBoxSetType.setValue(DrawingRegistry.INSTANCE.getDefaultSetType());
                //comboBoxDrawingSet.setValue(DrawingRegistry.INSTANCE.getDefaultSet(comboBoxSetType.getValue()));
            }
             */
                });

        Optional<MenuItem> setAsDefaultSet = menuButtonDrawingSetPresets.getItems().stream().filter(menuItem -> menuItem.getText() != null && menuItem.getText().equals("Set As Default")).findFirst();
        setAsDefaultSet.ifPresent(menuItem -> menuItem.setOnAction(e -> {
            if (comboBoxDrawingSet.getValue() != null) {
                DrawingBotV3.INSTANCE.getProgramSettings().defaultPresets.put(Register.PRESET_TYPE_DRAWING_SET.id, comboBoxDrawingSet.getValue().getCodeName());
            }
        }));

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        penTableView.setRowFactory(param -> {
            TableRow<ObservableDrawingPen> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setContextMenu(new ContextMenuObservablePen(drawingSets, row));
            return row;
        });


        penTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(DrawingBotV3.project().displayMode.get() == Register.INSTANCE.DISPLAY_MODE_SELECTED_PEN){
                DrawingBotV3.project().reRender();
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        penNameColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        penNameColumn.setCellValueFactory(param -> param.getValue().name);

        penTypeColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        penTypeColumn.setCellValueFactory(param -> param.getValue().type);

        penColourColumn.setCellFactory(TableCellColorPicker::new);
        penColourColumn.setCellValueFactory(param -> param.getValue().javaFXColour);

        penStrokeColumn.setCellFactory(param -> new TextFieldTableCell<>(new FloatStringConverter()));
        penStrokeColumn.setCellValueFactory(param -> param.getValue().strokeSize.asObject());

        penEnableColumn.setCellFactory(param -> new CheckBoxTableCell<>(index -> penEnableColumn.getCellObservableValue(index)));
        penEnableColumn.setCellValueFactory(param -> param.getValue().enable);

        penPercentageColumn.setCellValueFactory(param -> param.getValue().currentPercentage);

        penWeightColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        penWeightColumn.setCellValueFactory(param -> param.getValue().distributionWeight.asObject());

        penLinesColumn.setCellValueFactory(param -> param.getValue().currentGeometries.asObject());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        comboBoxPenType.setItems(FXCollections.observableArrayList(MasterRegistry.INSTANCE.registeredPens.keySet()));
        comboBoxPenType.setValue(MasterRegistry.INSTANCE.getDefaultDrawingPen().getType());

        comboBoxPenType.valueProperty().addListener((observable, oldValue, newValue) -> {
            comboBoxDrawingPen.setItems(MasterRegistry.INSTANCE.registeredPens.get(newValue));
            comboBoxDrawingPen.setValue(MasterRegistry.INSTANCE.getDefaultPen(newValue));
        });

        comboBoxDrawingPenSkin = new ComboBoxListViewSkin<>(comboBoxDrawingPen);
        comboBoxDrawingPenSkin.hideOnClickProperty().set(false);
        comboBoxDrawingPen.setSkin(comboBoxDrawingPenSkin);

        comboBoxDrawingPen.setItems(MasterRegistry.INSTANCE.registeredPens.get(comboBoxPenType.getValue()));
        comboBoxDrawingPen.setValue(MasterRegistry.INSTANCE.getDefaultDrawingPen());
        comboBoxDrawingPen.setCellFactory(param -> new ComboCellDrawingPen(drawingSets, true));
        comboBoxDrawingPen.setButtonCell(new ComboCellDrawingPen(drawingSets,false));

        FXHelper.setupPresetMenuButton(Register.PRESET_LOADER_DRAWING_PENS, this::getDrawingPenPresetManager, menuButtonDrawingPenPresets, true,
                () -> {
                    if(comboBoxDrawingPen.getValue() instanceof PresetDrawingPen){
                        PresetDrawingPen set = (PresetDrawingPen) comboBoxDrawingPen.getValue();
                        return set.preset;
                    }
                    return null;
                }, (preset) -> {
                    //force update rendering
                    comboBoxPenType.setItems(FXCollections.observableArrayList(MasterRegistry.INSTANCE.registeredPens.keySet()));
                    comboBoxDrawingPen.setItems(MasterRegistry.INSTANCE.registeredPens.get(comboBoxPenType.getValue()));
                    comboBoxDrawingPen.setButtonCell(new ComboCellDrawingPen(drawingSets,false));

                    if(preset != null){
                        comboBoxPenType.setValue(preset.presetSubType);
                        comboBoxDrawingPen.setValue(preset.data);
                    }else{
                        comboBoxPenType.setValue(MasterRegistry.INSTANCE.getDefaultDrawingPen().getType());
                        comboBoxDrawingPen.setValue(MasterRegistry.INSTANCE.getDefaultDrawingPen());
                    }
                });

        Optional<MenuItem> setAsDefaultPen = menuButtonDrawingPenPresets.getItems().stream().filter(menuItem -> menuItem.getText() != null && menuItem.getText().equals("Set As Default")).findFirst();
        setAsDefaultPen.ifPresent(menuItem -> menuItem.setOnAction(e -> {
            if (comboBoxDrawingPen.getValue() != null) {
                DrawingBotV3.INSTANCE.getProgramSettings().defaultPresets.put(Register.PRESET_TYPE_DRAWING_PENS.id, comboBoxDrawingPen.getValue().getCodeName());
            }
        }));

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        buttonAddPen.setOnAction(e -> FXHelper.addItem(penTableView.getSelectionModel(), drawingSets.get().activeDrawingSet.get().pens, () -> new ObservableDrawingPen(drawingSets.get().activeDrawingSet.get().pens.size(), comboBoxDrawingPen.getValue())));
        buttonAddPen.setTooltip(new Tooltip("Add Pen"));

        buttonRemovePen.setOnAction(e -> FXHelper.deleteItem(penTableView.getSelectionModel(), drawingSets.get().activeDrawingSet.get().pens));
        buttonRemovePen.setTooltip(new Tooltip("Remove Selected Pen"));
        buttonRemovePen.disableProperty().bind(penTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonDuplicatePen.setOnAction(e -> FXHelper.duplicateItem(penTableView.getSelectionModel(), drawingSets.get().activeDrawingSet.get().pens, p -> new ObservableDrawingPen(drawingSets.get().activeDrawingSet.get().pens.size(), p)));
        buttonDuplicatePen.setTooltip(new Tooltip("Duplicate Selected Pen"));
        buttonDuplicatePen.disableProperty().bind(penTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveUpPen.setOnAction(e -> FXHelper.moveItemUp(penTableView.getSelectionModel(), drawingSets.get().activeDrawingSet.get().pens));
        buttonMoveUpPen.setTooltip(new Tooltip("Move Selected Pen Up"));
        buttonMoveUpPen.disableProperty().bind(penTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveDownPen.setOnAction(e -> FXHelper.moveItemDown(penTableView.getSelectionModel(), drawingSets.get().activeDrawingSet.get().pens));
        buttonMoveDownPen.setTooltip(new Tooltip("Move Selected Pen Down"));
        buttonMoveDownPen.disableProperty().bind(penTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonClearDrawingSet.setOnAction(e -> drawingSets.get().activeDrawingSet.get().getPens().clear());
        buttonClearDrawingSet.setTooltip(new Tooltip("Clear Drawing Pens"));

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        comboBoxDistributionOrder.setItems(FXCollections.observableArrayList(EnumDistributionOrder.values()));

        comboBoxDistributionType.setItems(FXCollections.observableArrayList(EnumDistributionType.values()));
        comboBoxDistributionType.setButtonCell(new ComboCellDistributionType());
        comboBoxDistributionType.setCellFactory(param -> new ComboCellDistributionType());

        comboBoxColourSeperation.setItems(MasterRegistry.INSTANCE.colourSplitterHandlers);
        comboBoxColourSeperation.setCellFactory(param -> new ComboCellNamedSetting<>());

        buttonConfigureSplitter.setOnAction(e -> drawingSets.get().activeDrawingSet.get().colourSeperator.get().onUserConfigure());

        comboBoxDrawingSets.setCellFactory(param -> new ComboCellDrawingSet<>());
        comboBoxDrawingSets.setButtonCell(new ComboCellDrawingSet<>());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        drawingSetTableView.setRowFactory(param -> {
            TableRow<ObservableDrawingSet> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            //row.setContextMenu(new ContextMenuObservablePen(row));
            return row;
        });

        drawingSetTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(DrawingBotV3.project().displayMode.get() == Register.INSTANCE.DISPLAY_MODE_SELECTED_PEN){
                DrawingBotV3.project().reRender();
            }
            drawingSets.get().activeDrawingSet.set(newValue);
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        drawingSetNameColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        drawingSetNameColumn.setCellValueFactory(param -> param.getValue().name);

        drawingSetPensColumn.setCellFactory(param -> new TableCellNode<>(value -> new ControlPenPalette(value, drawingSetPensColumn.widthProperty())));
        drawingSetPensColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().pens));

        drawingSetDistributionTypeColumn.setCellFactory(param -> new ComboBoxTableCell<>(FXCollections.observableArrayList(EnumDistributionType.values())));
        drawingSetDistributionTypeColumn.setCellValueFactory(param -> param.getValue().distributionType);

        drawingSetDistributionOrderColumn.setCellFactory(param -> new ComboBoxTableCell<>(FXCollections.observableArrayList(EnumDistributionOrder.values())));
        drawingSetDistributionOrderColumn.setCellValueFactory(param -> param.getValue().distributionOrder);

        drawingSetColourSeperatorColumn.setCellFactory(param -> new ComboBoxTableCell<>(MasterRegistry.INSTANCE.colourSplitterHandlers));
        drawingSetColourSeperatorColumn.setCellValueFactory(param -> param.getValue().colourSeperator);


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        buttonAddDrawingSetSlot.setOnAction(e -> FXHelper.addItem(drawingSetTableView.getSelectionModel(), drawingSets.get().drawingSetSlots.get(), () -> new ObservableDrawingSet(new DrawingSet("User", "Empty", new ArrayList<>()))));
        buttonAddDrawingSetSlot.setTooltip(new Tooltip("Add Drawing Set"));

        buttonRemoveDrawingSetSlot.setOnAction(e -> FXHelper.deleteItem(drawingSetTableView.getSelectionModel(), drawingSets.get().drawingSetSlots.get()));
        buttonRemoveDrawingSetSlot.setTooltip(new Tooltip("Remove selected Drawing Set"));
        buttonRemoveDrawingSetSlot.disableProperty().bind(drawingSetTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonDuplicateDrawingSetSlot.setOnAction(e -> FXHelper.duplicateItem(drawingSetTableView.getSelectionModel(), drawingSets.get().drawingSetSlots.get(), ObservableDrawingSet::new));
        buttonDuplicateDrawingSetSlot.setTooltip(new Tooltip("Duplicate selected Drawing Set"));
        buttonDuplicateDrawingSetSlot.disableProperty().bind(drawingSetTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveUpDrawingSetSlot.setOnAction(e -> FXHelper.moveItemUp(drawingSetTableView.getSelectionModel(), drawingSets.get().drawingSetSlots.get()));
        buttonMoveUpDrawingSetSlot.setTooltip(new Tooltip("Move selected Drawing Set up"));
        buttonMoveUpDrawingSetSlot.disableProperty().bind(drawingSetTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveDownDrawingSetSlot.setOnAction(e -> FXHelper.moveItemDown(drawingSetTableView.getSelectionModel(), drawingSets.get().drawingSetSlots.get()));
        buttonMoveDownDrawingSetSlot.setTooltip(new Tooltip("Move selected Drawing Set down"));
        buttonMoveDownDrawingSetSlot.disableProperty().bind(drawingSetTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonClearDrawingSets.setOnAction(e -> drawingSets.get().drawingSetSlots.get().clear());
        buttonClearDrawingSets.setTooltip(new Tooltip("Clear Drawing Sets"));
    }

    //// DRAWING SET LISTENERS \\\\

    private ListChangeListener<ObservableDrawingPen> penListener = null;
    private ChangeListener<EnumDistributionOrder> distributionOrderListener = null;
    private ChangeListener<EnumDistributionType> distributionTypeListener = null;
    private ChangeListener<ColourSeperationHandler> colourSeperatorListener = null;

    private void initListeners(){
        penListener = c -> DrawingBotV3.INSTANCE.onDrawingSetChanged();
        distributionOrderListener = (observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingSetChanged();
        distributionTypeListener = (observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingSetChanged();
        colourSeperatorListener = (observable, oldValue, newValue) -> changeColourSplitter(DrawingBotV3.context(), oldValue, newValue, drawingSets.get());
    }

    private void addListeners(ObservableDrawingSet newValue){
        if(penListener == null){
            initListeners();
        }
        newValue.pens.addListener(penListener);
        newValue.distributionOrder.addListener(distributionOrderListener);
        newValue.distributionType.addListener(distributionTypeListener);
        newValue.colourSeperator.addListener(colourSeperatorListener);
    }

    private void removeListeners(ObservableDrawingSet oldValue){
        if(penListener == null){
            return;
        }
        oldValue.pens.removeListener(penListener);
        oldValue.distributionOrder.removeListener(distributionOrderListener);
        oldValue.distributionType.removeListener(distributionTypeListener);
        oldValue.colourSeperator.removeListener(colourSeperatorListener);
    }

    public void onChangedActiveDrawingSet(ObservableDrawingSet oldValue, ObservableDrawingSet newValue){
        if(oldValue != null){
            removeListeners(oldValue);
            comboBoxDistributionOrder.valueProperty().unbindBidirectional(oldValue.distributionOrder);
            comboBoxDistributionType.valueProperty().unbindBidirectional(oldValue.distributionType);
            comboBoxColourSeperation.valueProperty().unbindBidirectional(oldValue.colourSeperator);
        }

        if(newValue == null){
            return; //this is probably a force render update
        }

        addListeners(newValue);
        penTableView.setItems(newValue.pens);
        comboBoxDistributionOrder.valueProperty().bindBidirectional(newValue.distributionOrder);
        comboBoxDistributionType.valueProperty().bindBidirectional(newValue.distributionType);
        comboBoxColourSeperation.valueProperty().bindBidirectional(newValue.colourSeperator);

        buttonConfigureSplitter.disableProperty().unbind();
        buttonConfigureSplitter.disableProperty().bind(Bindings.createBooleanBinding(() -> !newValue.colourSeperator.get().canUserConfigure(), newValue.colourSeperator));

    }

    public void changeColourSplitter(DBTaskContext context, ColourSeperationHandler oldValue, ColourSeperationHandler newValue, DrawingSets drawingSets){
        if(oldValue == newValue){
            return;
        }
        if(oldValue.wasApplied()){
            oldValue.resetSettings(context, drawingSets);
            oldValue.setApplied(false);
        }

        if(newValue.onUserSelected()){
            newValue.applySettings(context, drawingSets);
            newValue.setApplied(true);
        }
    }

    public ObservableDrawingPen getSelectedPen(){
        return penTableView.getSelectionModel().getSelectedItem();
    }

    ////////////////////////////////////////////////////////

    public AbstractPresetManager<PresetDrawingPen> drawingPenPresetManager;

    public void setDrawingPenPresetManager(AbstractPresetManager<PresetDrawingPen> presetManager){
        this.drawingPenPresetManager = presetManager;
    }

    public AbstractPresetManager<PresetDrawingPen> getDrawingPenPresetManager(){
        if(drawingPenPresetManager == null){
            return Register.PRESET_LOADER_DRAWING_PENS.getDefaultManager();
        }
        return drawingPenPresetManager;
    }


    ////////////////////////////////////////////////////////

    public AbstractPresetManager<PresetDrawingSet> drawingSetPresetManager;

    public void setDrawingSetPresetManager(AbstractPresetManager<PresetDrawingSet> presetManager){
        this.drawingSetPresetManager = presetManager;
    }

    public AbstractPresetManager<PresetDrawingSet> getDrawingSetPresetManager(){
        if(drawingSetPresetManager == null){
            return Register.PRESET_LOADER_DRAWING_SET.getDefaultManager();
        }
        return drawingSetPresetManager;
    }


    ////////////////////////////////////////////////////////

}
