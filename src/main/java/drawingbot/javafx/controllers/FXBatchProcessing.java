package drawingbot.javafx.controllers;

import javafx.css.Styleable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class FXBatchProcessing extends AbstractFXController {

    public VBox vboxBatchProcessing = null;

    public Label labelInputFolder = null;
    public Label labelOutputFolder = null;

    public Button buttonSelectInputFolder = null;
    public Button buttonSelectOutputFolder = null;
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
