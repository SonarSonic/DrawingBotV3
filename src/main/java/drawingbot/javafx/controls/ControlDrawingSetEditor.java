package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.FXMLControl;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.Register;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.css.Styleable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.util.List;

/**
 * UI Control for the editing of {@link drawingbot.drawing.DrawingSets}
 * It can be instanced multiple times and bound to the required {@link drawingbot.drawing.DrawingSets}
 */
@FXMLControl
public class ControlDrawingSetEditor extends VBox {

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

    public ControlDrawingSetEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("drawingseteditor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(c -> this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ObservableDrawingSet> drawingSet = new SimpleObjectProperty<>();

    public ObservableDrawingSet getDrawingSet() {
        return drawingSet.get();
    }

    public ObjectProperty<ObservableDrawingSet> drawingSetProperty() {
        return drawingSet;
    }

    public void setDrawingSet(ObservableDrawingSet drawingSet) {
        this.drawingSet.set(drawingSet);
    }

    ////////////////////////////////////////////////////////

    @FXML
    public void initialize(){

        //VBox
        setSpacing(8);

        ObservableDrawingSet.Listener specialListener = new ObservableDrawingSet.Listener() {

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

        drawingSetProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.removeSpecialListener(specialListener);
            }
            if(newValue != null){
                newValue.addSpecialListener(specialListener);
                controlDrawingPenSelection.refresh();
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        penTableView.itemsProperty().bind(Bindings.createObjectBinding(() -> getDrawingSet() == null ? FXCollections.observableArrayList() : getDrawingSet().pens, drawingSetProperty()));

        penTableView.setRowFactory(param -> {
            TableRow<ObservableDrawingPen> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setContextMenu(new ContextMenuObservablePen(drawingSetProperty(), row));
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
        controlDrawingPenSelection.drawingSetProperty().bind(drawingSetProperty());
        controlDrawingPenSelection.activePresetProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                controlDrawingPenSelection.applyPreset(DrawingBotV3.context());
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        buttonAddPen.setOnAction(e -> FXHelper.addItem(penTableView.getSelectionModel(), getDrawingSet().pens, () -> new ObservableDrawingPen(getDrawingSet().pens.size(), Register.PRESET_LOADER_DRAWING_PENS.unwrapPreset(controlDrawingPenSelection.getActivePreset()))));
        buttonAddPen.setTooltip(new Tooltip("Add Pen"));

        buttonRemovePen.setOnAction(e -> FXHelper.deleteItem(penTableView.getSelectionModel(), getDrawingSet().pens));
        buttonRemovePen.setTooltip(new Tooltip("Remove Selected Pen"));
        buttonRemovePen.disableProperty().bind(penTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonDuplicatePen.setOnAction(e -> FXHelper.duplicateItem(penTableView.getSelectionModel(), getDrawingSet().pens, p -> new ObservableDrawingPen(getDrawingSet().pens.size(), p)));
        buttonDuplicatePen.setTooltip(new Tooltip("Duplicate Selected Pen"));
        buttonDuplicatePen.disableProperty().bind(penTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveUpPen.setOnAction(e -> FXHelper.moveItemUp(penTableView.getSelectionModel(), getDrawingSet().pens));
        buttonMoveUpPen.setTooltip(new Tooltip("Move Selected Pen Up"));
        buttonMoveUpPen.disableProperty().bind(penTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveDownPen.setOnAction(e -> FXHelper.moveItemDown(penTableView.getSelectionModel(), getDrawingSet().pens));
        buttonMoveDownPen.setTooltip(new Tooltip("Move Selected Pen Down"));
        buttonMoveDownPen.disableProperty().bind(penTableView.getSelectionModel().selectedItemProperty().isNull());

        buttonClearDrawingSet.setOnAction(e -> getDrawingSet().getPens().clear());
        buttonClearDrawingSet.setTooltip(new Tooltip("Clear Drawing Pens"));
    }

    public List<Styleable> getPersistentNodes(){
        return List.of(penEnableColumn, penTypeColumn, penNameColumn, penColourColumn, penColourColumn, penStrokeColumn, penPercentageColumn, penWeightColumn, penLinesColumn);
    }

}