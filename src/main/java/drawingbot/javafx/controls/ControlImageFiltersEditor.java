package drawingbot.javafx.controls;

import drawingbot.javafx.FXMLControl;
import drawingbot.image.ImageFilterSettings;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumFilterTypes;
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
import javafx.util.converter.DefaultStringConverter;

import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.util.List;

/**
 * UI Control for the editing of {@link ImageFilterSettings}
 * It can be instanced multiple times and bound to the required {@link ImageFilterSettings}
 */
@FXMLControl
public class ControlImageFiltersEditor extends VBox {

    public TableView<ObservableImageFilter> tableViewImageFilters = null;
    public TableColumn<ObservableImageFilter, Boolean> columnEnableImageFilter = null;
    public TableColumn<ObservableImageFilter, String> columnImageFilterType = null;
    public TableColumn<ObservableImageFilter, String> columnImageFilterSettings = null;

    public ComboBox<EnumFilterTypes> comboBoxFilterType = null;
    public ComboBox<GenericFactory<BufferedImageOp>> comboBoxImageFilter = null;
    public Button buttonAddFilter = null;
    public Button buttonRemoveFilter = null;
    public Button buttonDuplicateFilter = null;
    public Button buttonMoveUpFilter = null;
    public Button buttonMoveDownFilter = null;
    public Button buttonClearFilters = null;

    public ControlImageFiltersEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("imagefilterseditor.fxml"));
        fxmlLoader.setClassLoader(getClass().getClassLoader());
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(c -> this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    ////////////////////////////////////////////////////////

    public final SimpleObjectProperty<ImageFilterSettings> settings = new SimpleObjectProperty<>();

    public ImageFilterSettings getSettings() {
        return settings.get();
    }

    public SimpleObjectProperty<ImageFilterSettings> settingsProperty() {
        return settings;
    }

    public void setSettings(ImageFilterSettings settings) {
        this.settings.set(settings);
    }

    ////////////////////////////////////////////////////////

    @FXML
    public void initialize(){
        //VBox
        setSpacing(8);

        settings.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                tableViewImageFilters.itemsProperty().unbind();
            }
            if(newValue != null){
                tableViewImageFilters.itemsProperty().bind(newValue.currentFilters);
            }
        });


        tableViewImageFilters.setRowFactory(param -> {
            TableRow<ObservableImageFilter> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setOnMouseClicked(e -> {
                if(e.getClickCount() > 1){
                    FXHelper.openImageFilterDialog(row.getItem());
                }
            });
            row.setContextMenu(new ContextMenuObservableFilter(row, settings));
            row.setPrefHeight(30);
            return row;
        });

        columnEnableImageFilter.setCellFactory(param -> new CheckBoxTableCell<>(index -> columnEnableImageFilter.getCellObservableValue(index)));
        columnEnableImageFilter.setCellValueFactory(param -> param.getValue().enable);

        columnImageFilterType.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        columnImageFilterType.setCellValueFactory(param -> param.getValue().name);


        columnImageFilterSettings.setCellValueFactory(param -> param.getValue().settingsString);

        comboBoxFilterType.setItems(FXCollections.observableArrayList(MasterRegistry.INSTANCE.imgFilterFactories.keySet()));
        comboBoxFilterType.setValue(MasterRegistry.INSTANCE.getDefaultImageFilterType());
        comboBoxFilterType.valueProperty().addListener((observable, oldValue, newValue) -> {
            comboBoxImageFilter.setItems(MasterRegistry.INSTANCE.imgFilterFactories.get(newValue));
            comboBoxImageFilter.setValue(MasterRegistry.INSTANCE.getDefaultImageFilter(newValue));
        });

        comboBoxImageFilter.setItems(MasterRegistry.INSTANCE.imgFilterFactories.get(MasterRegistry.INSTANCE.getDefaultImageFilterType()));
        comboBoxImageFilter.setValue(MasterRegistry.INSTANCE.getDefaultImageFilter(MasterRegistry.INSTANCE.getDefaultImageFilterType()));
        buttonAddFilter.setOnAction(e -> {
            if(comboBoxImageFilter.getValue() != null){
                FXHelper.addImageFilter(comboBoxImageFilter.getValue(), settings.get());
            }
        });
        buttonRemoveFilter.setOnAction(e -> FXHelper.deleteItem(tableViewImageFilters.getSelectionModel(), settings.get().currentFilters.get()));
        buttonRemoveFilter.setTooltip(new Tooltip("Remove Selected Filter"));
        buttonRemoveFilter.disableProperty().bind(tableViewImageFilters.getSelectionModel().selectedItemProperty().isNull());

        buttonDuplicateFilter.setOnAction(e -> FXHelper.duplicateItem(tableViewImageFilters.getSelectionModel(), settings.get().currentFilters.get(), ObservableImageFilter::new));
        buttonDuplicateFilter.setTooltip(new Tooltip("Duplicate Selected Filter"));
        buttonDuplicateFilter.disableProperty().bind(tableViewImageFilters.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveUpFilter.setOnAction(e -> FXHelper.moveItemUp(tableViewImageFilters.getSelectionModel(), settings.get().currentFilters.get()));
        buttonMoveUpFilter.setTooltip(new Tooltip("Move Selected Filter Up"));
        buttonMoveUpFilter.disableProperty().bind(tableViewImageFilters.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveDownFilter.setOnAction(e -> FXHelper.moveItemDown(tableViewImageFilters.getSelectionModel(), settings.get().currentFilters.get()));
        buttonMoveDownFilter.setTooltip(new Tooltip("Move Selected Filter Down"));
        buttonMoveDownFilter.disableProperty().bind(tableViewImageFilters.getSelectionModel().selectedItemProperty().isNull());

        buttonClearFilters.setOnAction(e -> settings.get().currentFilters.get().clear());
        buttonClearFilters.setTooltip(new Tooltip("Clear Filters"));

    }

    public List<Styleable> getPersistentNodes(){
        return List.of(tableViewImageFilters, columnEnableImageFilter, columnImageFilterType, columnImageFilterSettings);
    }

}
