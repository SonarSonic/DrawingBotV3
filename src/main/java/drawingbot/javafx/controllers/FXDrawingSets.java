package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.drawing.DrawingSet;
import drawingbot.drawing.DrawingSets;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.controls.*;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import javafx.util.converter.DefaultStringConverter;

import java.util.ArrayList;
import java.util.List;

public class FXDrawingSets extends AbstractFXController {

    public SimpleObjectProperty<DrawingSets> drawingSets = new SimpleObjectProperty<>();

    ////////////////////////////////////////////////////////

    public VBox root;

    public ControlPresetDrawingSet controlDrawingSetPreset;
    public ControlDrawingSetEditor controlDrawingSetEditor;


    public ComboBox<EnumDistributionType> comboBoxDistributionType = null;
    public ComboBox<EnumDistributionOrder> comboBoxDistributionOrder = null;

    public ComboBox<ColorSeparationHandler> comboBoxColourSeperation = null;
    public Button buttonConfigureSplitter = null;

    public TitledPane titledPaneDrawingSetSlots;
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

        root.minHeightProperty().bind(Bindings.createDoubleBinding(() -> titledPaneDrawingSetSlots.isExpanded() ? 900D : 540D, titledPaneDrawingSetSlots.expandedProperty()));

        final ChangeListener<ObservableDrawingSet> activeSetListener = (observable, oldValue, newValue) -> onChangedActiveDrawingSet(oldValue, newValue);

        drawingSets.addListener((observable, oldValue, newValue) -> {

            if(oldValue != null){
                controlDrawingSetPreset.targetProperty().unbind();
                comboBoxDrawingSets.valueProperty().unbindBidirectional(oldValue.activeDrawingSet);
                onChangedActiveDrawingSet(oldValue.activeDrawingSet.get(), null);
                oldValue.activeDrawingSet.removeListener(activeSetListener);

                drawingSetTableView.setItems(FXCollections.observableArrayList());
                comboBoxDrawingSets.setItems(FXCollections.observableArrayList());
            }

            if(newValue != null){
                controlDrawingSetPreset.targetProperty().bind(newValue.activeDrawingSetProperty());
                comboBoxDrawingSets.setItems(newValue.drawingSetSlots);
                drawingSetTableView.setItems(drawingSets.get().drawingSetSlots);

                comboBoxDrawingSets.setValue(newValue.activeDrawingSet.get());
                comboBoxDrawingSets.valueProperty().bindBidirectional(newValue.activeDrawingSet);

                onChangedActiveDrawingSet(null, newValue.activeDrawingSet.get());
                newValue.activeDrawingSet.addListener(activeSetListener);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        controlDrawingSetPreset.activePresetProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                controlDrawingSetPreset.applyPreset(DrawingBotV3.context());
            }
        });

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
            controlDrawingSetEditor.setDrawingSet(null);
        }

        if(newValue == null){
            return; //this is probably a force render update
        }

        controlDrawingSetEditor.setDrawingSet(newValue);
        comboBoxDistributionOrder.valueProperty().bindBidirectional(newValue.distributionOrder);
        comboBoxDistributionType.valueProperty().bindBidirectional(newValue.distributionType);
        comboBoxColourSeperation.valueProperty().bindBidirectional(newValue.colorHandler);

        buttonConfigureSplitter.disableProperty().unbind();
        buttonConfigureSplitter.disableProperty().bind(Bindings.createBooleanBinding(() -> !newValue.colorHandler.get().canUserConfigure(), newValue.colorHandler));

    }


    public ObservableDrawingPen getSelectedPen(){
        return controlDrawingSetEditor.penTableView.getSelectionModel().getSelectedItem();
    }

    ////////////////////////////////////////////////////////


    @Override
    public List<Styleable> getPersistentNodes(){
        List<Styleable> persistentNodes = new ArrayList<>();
        persistentNodes.addAll(List.of(drawingSetNameColumn, drawingSetPensColumn, drawingSetDistributionTypeColumn, drawingSetDistributionOrderColumn, drawingSetColourSeperatorColumn,/*drawingSetShapesColumn,*/drawingSetPercentageColumn));
        persistentNodes.addAll(controlDrawingSetEditor.getPersistentNodes());
        return persistentNodes;
    }

}
