package drawingbot.javafx.controllers;

import drawingbot.javafx.controls.ControlDirectoryPicker;
import javafx.css.Styleable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.List;

public class FXBatchProcessing extends AbstractFXController {

    public VBox vboxBatchProcessing = null;

    public ControlDirectoryPicker inputFolderPicker = null;
    public ControlDirectoryPicker outputFolderPicker = null;
    public Button buttonStartBatchProcessing = null;
    public Button buttonStopBatchProcessing = null;

    public CheckBox checkBoxOverwrite = null;

    public TableView<?> tableViewBatchExport = null;
    public TableColumn<?, String> tableColumnFileFormat = null;
    public TableColumn<?, Boolean> tableColumnPerDrawing = null;
    public TableColumn<?, Boolean> tableColumnPerPen = null;
    public TableColumn<?, Boolean> tableColumnPerGroup = null;

    @FXML
    public void initialize(){
        ///NOP
    }

    @Override
    public List<Styleable> getPersistentNodes() {
        return List.of(tableColumnFileFormat, tableColumnPerDrawing, tableColumnPerPen, tableColumnPerGroup);
    }
}
