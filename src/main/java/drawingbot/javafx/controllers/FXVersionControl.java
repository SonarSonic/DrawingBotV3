package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controls.ContextMenuObservableProjectSettings;
import drawingbot.javafx.controls.TableCellImage;
import drawingbot.javafx.controls.TableCellRating;
import drawingbot.javafx.observables.ObservableVersion;
import drawingbot.registry.Register;
import drawingbot.render.overlays.NotificationOverlays;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.converter.DefaultStringConverter;
import org.controlsfx.control.Rating;
import org.fxmisc.easybind.EasyBind;

public class FXVersionControl {

    public final SimpleObjectProperty<ObservableList<ObservableVersion>> projectVersions = new SimpleObjectProperty<>();

    ////////////////////////////////////////////////////////

    public TableView<ObservableVersion> tableViewVersions = null;
    public TableColumn<ObservableVersion, Image> versionThumbColumn = null;
    public TableColumn<ObservableVersion, String> versionNameColumn = null;
    public TableColumn<ObservableVersion, Double> versionRatingColumn = null;
    public TableColumn<ObservableVersion, String> versionDateColumn = null;
    public TableColumn<ObservableVersion, String> versionNotesColumn = null;

    public TableColumn<ObservableVersion, String> versionPFMColumn = null;
    public TableColumn<ObservableVersion, String> versionFileColumn = null;

    public Button buttonAddVersion = null;
    public Button buttonDeleteVersion = null;
    public Button buttonLoadVersion = null;
    public Button buttonMoveUpVersion = null;
    public Button buttonMoveDownVersion = null;
    public Button buttonClearVersions = null;

    @FXML
    public void initialize(){
        tableViewVersions.setRowFactory(param -> {
            TableRow<ObservableVersion> row = new TableRow<>();
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
        versionNameColumn.setCellValueFactory(param -> param.getValue().name);
        versionNameColumn.setEditable(true);

        versionRatingColumn.setCellFactory(param -> new TableCellRating<>(5, false, v -> v.rating));
        versionRatingColumn.setCellValueFactory(param -> param.getValue().rating.asObject());
        versionRatingColumn.setEditable(true);

        versionDateColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        versionDateColumn.setCellValueFactory(param -> param.getValue().date);
        versionDateColumn.setEditable(false);

        versionNotesColumn.setCellFactory(param -> {
            TextFieldTableCell<ObservableVersion, String> cell = new TextFieldTableCell<>(new DefaultStringConverter());
            cell.setWrapText(true);
            return cell;
        });
        versionNotesColumn.setCellValueFactory(param -> param.getValue().notes);
        versionNotesColumn.setEditable(true);

        versionFileColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        versionFileColumn.setCellValueFactory(param -> param.getValue().file);
        versionFileColumn.setEditable(false);

        versionPFMColumn.setVisible(false);
        /*
        versionPFMColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        versionPFMColumn.setCellValueFactory(param -> param.getValue().pfm);
        versionPFMColumn.setEditable(false);
         */

        ;
        buttonAddVersion.setOnAction(e -> saveVersion());
        buttonAddVersion.disableProperty().bind(Bindings.createBooleanBinding(() -> DrawingBotV3.INSTANCE.taskMonitor.isPlotting.get() || DrawingBotV3.INSTANCE.drawingBinding.getValue() == null, DrawingBotV3.INSTANCE.taskMonitor.isPlotting, DrawingBotV3.INSTANCE.drawingBinding));
        buttonAddVersion.setTooltip(new Tooltip("Save Version"));

        buttonDeleteVersion.setOnAction(e -> FXHelper.deleteItem(tableViewVersions.getSelectionModel(), projectVersions.get()));
        buttonDeleteVersion.setTooltip(new Tooltip("Remove selected version"));
        buttonDeleteVersion.disableProperty().bind(tableViewVersions.getSelectionModel().selectedItemProperty().isNull());

        buttonLoadVersion.setOnAction(e -> Register.PRESET_LOADER_PROJECT.getDefaultManager().applyPreset(DrawingBotV3.context(), tableViewVersions.getSelectionModel().getSelectedItem().getPreset()));
        buttonLoadVersion.setTooltip(new Tooltip("Load the selected version"));
        buttonLoadVersion.disableProperty().bind(tableViewVersions.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveUpVersion.setOnAction(e -> FXHelper.moveItemUp(tableViewVersions.getSelectionModel(), projectVersions.get()));
        buttonMoveUpVersion.setTooltip(new Tooltip("Move selected version up"));
        buttonMoveUpVersion.disableProperty().bind(tableViewVersions.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveDownVersion.setOnAction(e -> FXHelper.moveItemDown(tableViewVersions.getSelectionModel(), projectVersions.get()));
        buttonMoveDownVersion.setTooltip(new Tooltip("Move selected version down"));
        buttonMoveDownVersion.disableProperty().bind(tableViewVersions.getSelectionModel().selectedItemProperty().isNull());

        buttonClearVersions.setOnAction(e -> projectVersions.get().clear());
        buttonClearVersions.setTooltip(new Tooltip("Clear Versions"));
    }

    public void saveVersion(){
        final DBTaskContext context = DrawingBotV3.context();
        final ObservableList<ObservableVersion> list = projectVersions.get();
        DrawingBotV3.INSTANCE.backgroundService.submit(() -> {
            GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
            Register.PRESET_LOADER_PROJECT.getDefaultManager().updatePreset(context, preset);
            list.add(new ObservableVersion(preset, true));
            NotificationOverlays.INSTANCE.showWithSubtitle("Saved New Version", preset.data.imagePath);
        });
    }

}
