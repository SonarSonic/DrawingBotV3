package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.files.VersionControl;
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
import javafx.css.Styleable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.converter.DefaultStringConverter;
import org.fxmisc.easybind.EasyBind;

import java.util.List;

public class FXVersionControl extends AbstractFXController {

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

        tableViewVersions.itemsProperty().bind(EasyBind.select(versionControl).selectObject(VersionControl::projectVersionsProperty));

        versionThumbColumn.setCellFactory(param -> new TableCellImage<>());
        versionThumbColumn.setCellValueFactory(param -> param.getValue().thumbnailProperty());

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

        buttonAddVersion.setOnAction(e -> saveVersion());
        buttonAddVersion.disableProperty().bind(Bindings.createBooleanBinding(() -> DrawingBotV3.INSTANCE.taskMonitor.isPlotting.get() || DrawingBotV3.INSTANCE.drawingBinding.getValue() == null, DrawingBotV3.INSTANCE.taskMonitor.isPlotting, DrawingBotV3.INSTANCE.drawingBinding));
        buttonAddVersion.setTooltip(new Tooltip("Save Version"));

        buttonDeleteVersion.setOnAction(e -> FXHelper.deleteItem(tableViewVersions.getSelectionModel(), getVersionControl().getProjectVersions()));
        buttonDeleteVersion.setTooltip(new Tooltip("Remove selected version"));
        buttonDeleteVersion.disableProperty().bind(tableViewVersions.getSelectionModel().selectedItemProperty().isNull());

        buttonLoadVersion.setOnAction(e -> tableViewVersions.getSelectionModel().getSelectedItem().loadVersion(DrawingBotV3.context()));
        buttonLoadVersion.setTooltip(new Tooltip("Load the selected version"));
        buttonLoadVersion.disableProperty().bind(tableViewVersions.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveUpVersion.setOnAction(e -> FXHelper.moveItemUp(tableViewVersions.getSelectionModel(), getVersionControl().getProjectVersions()));
        buttonMoveUpVersion.setTooltip(new Tooltip("Move selected version up"));
        buttonMoveUpVersion.disableProperty().bind(tableViewVersions.getSelectionModel().selectedItemProperty().isNull());

        buttonMoveDownVersion.setOnAction(e -> FXHelper.moveItemDown(tableViewVersions.getSelectionModel(), getVersionControl().getProjectVersions()));
        buttonMoveDownVersion.setTooltip(new Tooltip("Move selected version down"));
        buttonMoveDownVersion.disableProperty().bind(tableViewVersions.getSelectionModel().selectedItemProperty().isNull());

        buttonClearVersions.setOnAction(e -> getVersionControl().getProjectVersions().clear());
        buttonClearVersions.setTooltip(new Tooltip("Clear Versions"));
    }

    public void saveVersion(){
        final DBTaskContext context = DrawingBotV3.context();
        final VersionControl control = getVersionControl();
        DrawingBotV3.INSTANCE.backgroundService.submit(() -> {
            GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
            preset.updatePreset(context);
            control.getProjectVersions().add(new ObservableVersion(preset, true));
            NotificationOverlays.INSTANCE.showWithSubtitle("Saved New Version", preset.data.imagePath);
        });
    }

    ////////////////////////////////////////////////////////

    public final SimpleObjectProperty<VersionControl> versionControl = new SimpleObjectProperty<>();

    public VersionControl getVersionControl() {
        return versionControl.get();
    }

    public SimpleObjectProperty<VersionControl> versionControlProperty() {
        return versionControl;
    }

    public void setVersionControl(VersionControl versionControl) {
        this.versionControl.set(versionControl);
    }

    ////////////////////////////////////////////////////////

    @Override
    public List<Styleable> getPersistentNodes(){
        return List.of(versionThumbColumn, versionNameColumn, versionRatingColumn, versionDateColumn, versionNotesColumn, versionPFMColumn, versionFileColumn);
    }

}
