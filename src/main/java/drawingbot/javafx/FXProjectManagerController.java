package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.controls.ContextMenuObservableProjectSettings;
import drawingbot.javafx.controls.TableCellImage;
import drawingbot.javafx.observables.ObservableProjectSettings;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.converter.DefaultStringConverter;

//W.I.P
public class FXProjectManagerController {

    public TableView<ObservableProjectSettings> tableViewProjects = null;
    public TableColumn<ObservableProjectSettings, Image> projectThumbColumn = null;
    public TableColumn<ObservableProjectSettings, String> projectNameColumn = null;
    public TableColumn<ObservableProjectSettings, String> projectDateColumn = null;

    public TableColumn<ObservableProjectSettings, String> projectFileColumn = null;
    public TableColumn<ObservableProjectSettings, String> projectPFMColumn = null;

    public ImageView projectPreviewImageView = null;

    public Button removeProject = null;
    public Button importProject = null;
    public Button openProject = null;

    public void initialize(){
        tableViewProjects.setRowFactory(param -> {
            TableRow<ObservableProjectSettings> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setContextMenu(new ContextMenuObservableProjectSettings(row));
            row.setPrefHeight(100);
            return row;
        });
        tableViewProjects.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            projectPreviewImageView.setImage(newValue == null ? null : newValue.thumbnail.get());
        });

        tableViewProjects.setItems(DrawingBotV3.INSTANCE.projectVersions);

        projectThumbColumn.setCellFactory(param -> new TableCellImage<>());
        projectThumbColumn.setCellValueFactory(param -> param.getValue().thumbnail);

        projectNameColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        projectNameColumn.setCellValueFactory(param -> param.getValue().name);

        projectDateColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        projectDateColumn.setCellValueFactory(param -> param.getValue().date);
        projectDateColumn.setEditable(false);

        projectFileColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        projectFileColumn.setCellValueFactory(param -> param.getValue().file);
        projectFileColumn.setEditable(false);

        projectPFMColumn.setVisible(false);
        /*
        projectPFMColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        projectPFMColumn.setCellValueFactory(param -> param.getValue().pfm);
        projectPFMColumn.setEditable(false);

         */
    }
}
