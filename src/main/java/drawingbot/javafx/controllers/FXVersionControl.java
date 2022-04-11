package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.presets.PresetProjectSettings;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controls.ContextMenuObservableProjectSettings;
import drawingbot.javafx.controls.TableCellImage;
import drawingbot.javafx.observables.ObservableProjectSettings;
import drawingbot.registry.Register;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.converter.DefaultStringConverter;

public class FXVersionControl {

    public final SimpleObjectProperty<ObservableList<ObservableProjectSettings>> projectVersions = new SimpleObjectProperty<>();

    ////////////////////////////////////////////////////////

    public TableView<ObservableProjectSettings> tableViewVersions = null;
    public TableColumn<ObservableProjectSettings, Image> versionThumbColumn = null;
    public TableColumn<ObservableProjectSettings, String> versionNameColumn = null;
    public TableColumn<ObservableProjectSettings, String> versionDateColumn = null;

    public TableColumn<ObservableProjectSettings, String> versionPFMColumn = null;
    public TableColumn<ObservableProjectSettings, String> versionFileColumn = null;

    public Button buttonAddVersion = null;
    public Button buttonDeleteVersion = null;
    public Button buttonMoveUpVersion = null;
    public Button buttonMoveDownVersion = null;

    @FXML
    public void initialize(){
        tableViewVersions.setRowFactory(param -> {
            TableRow<ObservableProjectSettings> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setContextMenu(new ContextMenuObservableProjectSettings(row));
            return row;
        });

        tableViewVersions.itemsProperty().bind(projectVersions);

        versionThumbColumn.setCellFactory(param -> new TableCellImage<>());
        versionThumbColumn.setCellValueFactory(param -> param.getValue().thumbnail);

        versionNameColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        versionNameColumn.setCellValueFactory(param -> param.getValue().userDefinedName);

        versionDateColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        versionDateColumn.setCellValueFactory(param -> param.getValue().date);
        versionDateColumn.setEditable(false);

        versionFileColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        versionFileColumn.setCellValueFactory(param -> param.getValue().file);
        versionFileColumn.setEditable(false);

        versionPFMColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        versionPFMColumn.setCellValueFactory(param -> param.getValue().pfm);
        versionPFMColumn.setEditable(false);

        buttonAddVersion.setOnAction(e -> saveVersion());
        buttonDeleteVersion.setOnAction(e -> FXHelper.deleteItem(tableViewVersions.getSelectionModel().getSelectedItem(), projectVersions.get()));
        buttonMoveUpVersion.setOnAction(e -> FXHelper.moveItemUp(tableViewVersions.getSelectionModel().getSelectedItem(), projectVersions.get()));
        buttonMoveDownVersion.setOnAction(e -> FXHelper.moveItemDown(tableViewVersions.getSelectionModel().getSelectedItem(), projectVersions.get()));
    }

    public void saveVersion(){
        final ObservableList<ObservableProjectSettings> list = projectVersions.get();
        DrawingBotV3.INSTANCE.backgroundService.submit(() -> {
            GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
            Register.PRESET_LOADER_PROJECT.getDefaultManager().updatePreset(preset);
            list.add(new ObservableProjectSettings(preset, true));
        });
    }

}
