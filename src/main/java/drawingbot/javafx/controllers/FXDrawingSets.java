package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.drawing.DrawingSet;
import drawingbot.drawing.DrawingSets;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controls.*;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
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
import java.util.List;

public class FXDrawingSets extends AbstractFXController {

    public SimpleObjectProperty<DrawingSets> drawingSets = new SimpleObjectProperty<>();

    ////////////////////////////////////////////////////////

    public ControlPresetDrawingSet controlDrawingSetSelection;

    public TableView<ObservableDrawingPen> penTableView = null;
    public TableColumn<ObservableDrawingPen, Boolean> penEnableColumn = null;
    public TableColumn<ObservableDrawingPen, String> penTypeColumn = null;
    public TableColumn<ObservableDrawingPen, String> penNameColumn = null;
    public TableColumn<ObservableDrawingPen, Color> penColourColumn = null;
    public TableColumn<ObservableDrawingPen, Float> penStrokeColumn = null;
    public TableColumn<ObservableDrawingPen, String> penPercentageColumn = null;
    public TableColumn<ObservableDrawingPen, Integer> penWeightColumn = null;
    public TableColumn<ObservableDrawingPen, Integer> penLinesColumn = null;

    public ControlPresetDrawingPen controlDrawingPenSelection;

    public Button buttonAddPen = null;
    public Button buttonRemovePen = null;
    public Button buttonDuplicatePen = null;
    public Button buttonMoveUpPen = null;
    public Button buttonMoveDownPen = null;
    public Button buttonClearDrawingSet = null;

    public ComboBox<EnumDistributionType> comboBoxDistributionType = null;
    public ComboBox<EnumDistributionOrder> comboBoxDistributionOrder = null;

    public ComboBox<ColorSeparationHandler> comboBoxColourSeperation = null;
    public Button buttonConfigureSplitter = null;

    public ComboBox<ObservableDrawingSet> comboBoxDrawingSets = null;

    public TableView<ObservableDrawingSet> drawingSetTableView = null;
    public TableColumn<ObservableDrawingSet, String> drawingSetNameColumn = null;
    public TableColumn<ObservableDrawingSet, ObservableList<ObservableDrawingPen>> drawingSetPensColumn = null;
    public TableColumn<ObservableDrawingSet, EnumDistributionType> drawingSetDistributionTypeColumn = null;
    public TableColumn<ObservableDrawingSet, EnumDistributionOrder> drawingSetDistributionOrderColumn = null;
    public TableColumn<ObservableDrawingSet, ColorSeparationHandler> drawingSetColourSeperatorColumn = null;
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
        DrawingSets.Listener specialListener = new DrawingSets.Listener() {

            @Override
            public void onActiveSlotChanged(ObservableDrawingSet activeSet) {
                controlDrawingPenSelection.refresh();
            }

            @Override
            public void onDrawingPenPropertyChanged(ObservableDrawingPen pen, Observable property) {
                if(property == pen.name || property == pen.type){
                    controlDrawingPenSelection.refresh();
                }
            }

            @Override
            public void onDrawingPenAdded(ObservableDrawingPen pen) {
                controlDrawingPenSelection.refresh();
            }

            @Override
            public void onDrawingPenRemoved(ObservableDrawingPen pen) {
                controlDrawingPenSelection.refresh();
            }
        };
        drawingSets.addListener((observable, oldValue, newValue) -> {

            if(oldValue != null){
                comboBoxDrawingSets.valueProperty().unbindBidirectional(oldValue.activeDrawingSet);
                onChangedActiveDrawingSet(oldValue.activeDrawingSet.get(), null);
                oldValue.activeDrawingSet.removeListener(activeSetListener);

                drawingSetTableView.setItems(FXCollections.observableArrayList());
                comboBoxDrawingSets.setItems(FXCollections.observableArrayList());
                oldValue.removeSpecialListener(specialListener);

            }

            if(newValue != null){
                comboBoxDrawingSets.setItems(newValue.drawingSetSlots);
                drawingSetTableView.setItems(drawingSets.get().drawingSetSlots);

                comboBoxDrawingSets.setValue(newValue.activeDrawingSet.get());
                comboBoxDrawingSets.valueProperty().bindBidirectional(newValue.activeDrawingSet);

                onChangedActiveDrawingSet(null, newValue.activeDrawingSet.get());
                newValue.activeDrawingSet.addListener(activeSetListener);
                newValue.addSpecialListener(specialListener);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        controlDrawingSetSelection.targetProperty().bind(Bindings.select(drawingSets, "activeDrawingSet"));
        controlDrawingSetSelection.activePresetProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                controlDrawingSetSelection.applyPreset(DrawingBotV3.context());
            }
        });

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

        controlDrawingPenSelection.targetProperty().bind(penTableView.getSelectionModel().selectedItemProperty());
        controlDrawingPenSelection.drawingSetsProperty().bind(drawingSets);
        controlDrawingPenSelection.activePresetProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                controlDrawingPenSelection.applyPreset(DrawingBotV3.context());
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        buttonAddPen.setOnAction(e -> FXHelper.addItem(penTableView.getSelectionModel(), drawingSets.get().activeDrawingSet.get().pens, () -> new ObservableDrawingPen(drawingSets.get().activeDrawingSet.get().pens.size(), Register.PRESET_LOADER_DRAWING_PENS.unwrapPreset(controlDrawingPenSelection.getActivePreset()))));
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
        comboBoxDistributionType.setButtonCell(new ComboCellDistributionType(true));
        comboBoxDistributionType.setCellFactory(param -> new ComboCellDistributionType(false));

        comboBoxColourSeperation.setItems(MasterRegistry.INSTANCE.colourSplitterHandlers);
        comboBoxColourSeperation.setCellFactory(param -> new ComboCellNamedSetting<>());
        comboBoxColourSeperation.setVisibleRowCount(10);

        buttonConfigureSplitter.setOnAction(e -> {
            ObservableDrawingSet drawingSet = drawingSets.get().activeDrawingSet.get();
            drawingSet.colorHandler.get().onUserConfigure(drawingSet);
        });

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

        drawingSetPensColumn.setCellFactory(param -> new TableCellNode<>((set, pens) -> new ControlPenPalette(pens, drawingSetPensColumn.widthProperty())));
        drawingSetPensColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().pens));

        drawingSetDistributionTypeColumn.setCellFactory(param -> new ComboBoxTableCell<>(FXCollections.observableArrayList(EnumDistributionType.values())));
        drawingSetDistributionTypeColumn.setCellValueFactory(param -> param.getValue().distributionType);

        drawingSetDistributionOrderColumn.setCellFactory(param -> new ComboBoxTableCell<>(FXCollections.observableArrayList(EnumDistributionOrder.values())));
        drawingSetDistributionOrderColumn.setCellValueFactory(param -> param.getValue().distributionOrder);

        drawingSetColourSeperatorColumn.setCellFactory(param -> new ComboBoxTableCell<>(MasterRegistry.INSTANCE.colourSplitterHandlers));
        drawingSetColourSeperatorColumn.setCellValueFactory(param -> param.getValue().colorHandler);


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        buttonAddDrawingSetSlot.setOnAction(e -> FXHelper.addItem(drawingSetTableView.getSelectionModel(), drawingSets.get().drawingSetSlots, () -> new ObservableDrawingSet(new DrawingSet("User", "Empty", new ArrayList<>()))));
        buttonAddDrawingSetSlot.setTooltip(new Tooltip("Add Drawing Set"));

        buttonRemoveDrawingSetSlot.setOnAction(e -> FXHelper.deleteItem(drawingSetTableView.getSelectionModel(), drawingSets.get().drawingSetSlots));
        buttonRemoveDrawingSetSlot.setTooltip(new Tooltip("Remove selected Drawing Set"));
        buttonRemoveDrawingSetSlot.disableProperty().bind(drawingSetTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonDuplicateDrawingSetSlot.setOnAction(e -> FXHelper.duplicateItem(drawingSetTableView.getSelectionModel(), drawingSets.get().drawingSetSlots, ObservableDrawingSet::new));
        buttonDuplicateDrawingSetSlot.setTooltip(new Tooltip("Duplicate selected Drawing Set"));
        buttonDuplicateDrawingSetSlot.disableProperty().bind(drawingSetTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveUpDrawingSetSlot.setOnAction(e -> FXHelper.moveItemUp(drawingSetTableView.getSelectionModel(), drawingSets.get().drawingSetSlots));
        buttonMoveUpDrawingSetSlot.setTooltip(new Tooltip("Move selected Drawing Set up"));
        buttonMoveUpDrawingSetSlot.disableProperty().bind(drawingSetTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveDownDrawingSetSlot.setOnAction(e -> FXHelper.moveItemDown(drawingSetTableView.getSelectionModel(), drawingSets.get().drawingSetSlots));
        buttonMoveDownDrawingSetSlot.setTooltip(new Tooltip("Move selected Drawing Set down"));
        buttonMoveDownDrawingSetSlot.disableProperty().bind(drawingSetTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonClearDrawingSets.setOnAction(e -> drawingSets.get().drawingSetSlots.clear());
        buttonClearDrawingSets.setTooltip(new Tooltip("Clear Drawing Sets"));

    }

    //// DRAWING SET LISTENERS \\\\

    public void onChangedActiveDrawingSet(ObservableDrawingSet oldValue, ObservableDrawingSet newValue){
        if(oldValue != null){
            comboBoxDistributionOrder.valueProperty().unbindBidirectional(oldValue.distributionOrder);
            comboBoxDistributionType.valueProperty().unbindBidirectional(oldValue.distributionType);
            comboBoxColourSeperation.valueProperty().unbindBidirectional(oldValue.colorHandler);
        }

        if(newValue == null){
            return; //this is probably a force render update
        }

        penTableView.setItems(newValue.pens);
        comboBoxDistributionOrder.valueProperty().bindBidirectional(newValue.distributionOrder);
        comboBoxDistributionType.valueProperty().bindBidirectional(newValue.distributionType);
        comboBoxColourSeperation.valueProperty().bindBidirectional(newValue.colorHandler);

        buttonConfigureSplitter.disableProperty().unbind();
        buttonConfigureSplitter.disableProperty().bind(Bindings.createBooleanBinding(() -> !newValue.colorHandler.get().canUserConfigure(), newValue.colorHandler));

    }


    public ObservableDrawingPen getSelectedPen(){
        return penTableView.getSelectionModel().getSelectedItem();
    }

    ////////////////////////////////////////////////////////


    @Override
    public List<Styleable> getPersistentNodes(){
        return List.of(penEnableColumn, penTypeColumn, penNameColumn, penColourColumn, penColourColumn, penStrokeColumn, penPercentageColumn, penWeightColumn, penLinesColumn, drawingSetNameColumn, drawingSetPensColumn, drawingSetDistributionTypeColumn, drawingSetDistributionOrderColumn, drawingSetColourSeperatorColumn,/*drawingSetShapesColumn,*/drawingSetPercentageColumn);
    }

}
