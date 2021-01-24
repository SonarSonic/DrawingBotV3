package drawingbot.javafx;

import drawingbot.files.BatchProcessing;
import drawingbot.DrawingBotV3;
import drawingbot.drawing.*;
import drawingbot.files.ExportFormats;
import drawingbot.files.FileUtils;
import drawingbot.helpers.ImageTools;
import drawingbot.pfm.PFMLoaders;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.EnumDisplayMode;
import drawingbot.utils.EnumTaskStage;
import drawingbot.utils.Units;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.converter.DefaultStringConverter;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import static processing.core.PApplet.*;

public class FXController {

    ////MENU
    //file
    public MenuItem menuImport = null;
    public MenuItem menuImportURL = null;
    public MenuItem menuExit = null;
    public Menu menuExport = null;
    public Menu menuExportPerPen = null;
    //help
    public MenuItem menuHelpPage = null;

    ////SETTINGS WINDOW
    public ScrollPane scrollPaneSettings = null;
    public VBox vBoxSettings = null;

    ////VIEWPORT WINDOW
    public VBox vBoxViewportContainer = null;
    public ScrollPane viewportScrollPane = null;
    public StackPane viewportStackPane = null;

    ////VIEWPORT SETTINGS
    public ChoiceBox<EnumDisplayMode> choiceBoxDisplayMode = null;
    public CheckBox checkBoxShowGrid = null;
    public Button buttonZoomIn = null;
    public Button buttonZoomOut = null;
    public Button buttonResetView = null;

    ////DRAWING AREA CONTROLS
    public CheckBox checkBoxOriginalSizing = null;
    public TextField textFieldDrawingWidth = null;
    public TextField textFieldDrawingHeight = null;
    public ChoiceBox<Units> choiceBoxDrawingUnits = null;

    ////PATH FINDING CONTROLS
    public ChoiceBox<PFMLoaders> choiceBoxPFM = null;
    public Label labelElapsedTime = null;
    public Label labelPlottedLines = null;
    public Slider sliderDisplayedLines = null;
    public TextField textFieldDisplayedLines = null;

    ////PEN SETTINGS
    public ComboBox<DrawingSet> comboBoxDrawingSet = null;

    public TableView<ObservableDrawingPen> penTableView = null;
    public TableColumn<ObservableDrawingPen, Boolean> penEnableColumn = null;
    public TableColumn<ObservableDrawingPen, String> penNameColumn = null;
    public TableColumn<ObservableDrawingPen, Color> penColourColumn = null;

    public ComboBox<DrawingPen> comboBoxDrawingPen = null;
    public Button buttonAddPen = null;

    ////BATCH PROCESSING
    public Button buttonSelectInputFolder = null;
    public Button buttonSelectOutputFolder = null;
    public Button buttonStartBatchProcessing = null;
    public Label labelInputFolder = null;
    public Label labelOutputFolder = null;

    ////PROGRESS BAR PANE
    public Pane paneProgressBar = null;
    public ProgressBar progressBarGeneral = null;
    public Label progressBarLabel = null;

    public void initialize(){
        println("Initialize JAVA FX");

        ////MENU

        //file
        menuImport.setOnAction(e -> importFile());
        menuImportURL.setOnAction(e -> importURL());
        menuExit.setOnAction(e -> DrawingBotV3.INSTANCE.exit());
        for(ExportFormats format : ExportFormats.values()){
            MenuItem item = new MenuItem(format.displayName);
            item.setOnAction(e -> exportFile(format, false));
            menuExport.getItems().add(item);
        }

        for(ExportFormats format : ExportFormats.values()){
            MenuItem item = new MenuItem(format.displayName);
            item.setOnAction(e -> exportFile(format, true));
            menuExportPerPen.getItems().add(item);
        }
        //help
        menuHelpPage.setOnAction(e -> openHelpPage());

        ////

        ////SETTINGS WINDOW
        //vBoxSettings.prefWidthProperty().bind(scrollPaneSettings.widthProperty());
        //vBoxSettings.prefHeightProperty().bind(scrollPaneSettings.heightProperty());

        ////

        ////VIEWPORT WINDOW
        //viewportScrollPane.prefWidthProperty().bind(vBoxViewportContainer.widthProperty()); //TODO BIND WITHOUT MESSING UP IMAGE POSITION...
        //viewportScrollPane.prefHeightProperty().bind(vBoxViewportContainer.heightProperty());
        //viewportScrollPane.widthProperty().addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.updateCanvasScaling());
        ////

        ////VIEWPORT SETTINGS
        choiceBoxDisplayMode.getItems().addAll(EnumDisplayMode.values());
        choiceBoxDisplayMode.setValue(EnumDisplayMode.DRAWING);
        choiceBoxDisplayMode.setOnAction(e -> changeDisplayMode(choiceBoxDisplayMode.getSelectionModel().getSelectedItem()));

        DrawingBotV3.displayGrid.bind(checkBoxShowGrid.selectedProperty());
        DrawingBotV3.displayGrid.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.reRender());

        buttonZoomIn.setOnAction(e -> {
            DrawingBotV3.scaleMultiplier.set(DrawingBotV3.scaleMultiplier.getValue() + 0.1);
        });
        buttonZoomOut.setOnAction(e -> {
            if(DrawingBotV3.scaleMultiplier.getValue() > DrawingBotV3.minScale){
                DrawingBotV3.scaleMultiplier.set(DrawingBotV3.scaleMultiplier.getValue() - 0.1);
            }
        });
        DrawingBotV3.scaleMultiplier.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.updateCanvasScaling());

        buttonResetView.setOnAction(e -> {
            viewportScrollPane.setHvalue(0.5);
            viewportScrollPane.setVvalue(0.5);
            DrawingBotV3.scaleMultiplier.set(1.0);
        });

        ////

        ////DRAWING AREA CONTROLS
        DrawingBotV3.useOriginalSizing.bind(checkBoxOriginalSizing.selectedProperty());
        textFieldDrawingWidth.disableProperty().bind(checkBoxOriginalSizing.selectedProperty());
        textFieldDrawingHeight.disableProperty().bind(checkBoxOriginalSizing.selectedProperty());
        choiceBoxDrawingUnits.disableProperty().bind(checkBoxOriginalSizing.selectedProperty());

        textFieldDrawingWidth.textProperty().addListener((observable, oldValue, newValue) -> {
            /*
            if (!newValue.matches("\\d*")) {
                textFieldDrawingWidth.setText(newValue.replaceAll("[^\\d]", ""));
                DrawingBotV3.drawingAreaWidth.setValue(Float.parseFloat(textFieldDrawingWidth.getText()));
            }
             */

        });
        DrawingBotV3.drawingAreaWidth.addListener((observable, oldValue, newValue) -> {
            textFieldDrawingWidth.setText(newValue.toString());
        });

        textFieldDrawingHeight.textProperty().addListener((observable, oldValue, newValue) -> {
            /*
            if (!newValue.matches("\\d*")) {
                textFieldDrawingHeight.setText(newValue.replaceAll("[^\\d]", ""));
                DrawingBotV3.drawingAreaHeight.setValue(Float.parseFloat(textFieldDrawingHeight.getText()));
            }
             */
        });
        DrawingBotV3.drawingAreaHeight.addListener((observable, oldValue, newValue) -> textFieldDrawingHeight.setText(newValue.toString()));

        choiceBoxDrawingUnits.getItems().addAll(Units.values());
        choiceBoxDrawingUnits.setValue(Units.MILLIMETRES);
        DrawingBotV3.drawingAreaUnits.bindBidirectional(choiceBoxDrawingUnits.valueProperty());

        ////

        ////PATH FINDING CONTROLS
        choiceBoxPFM.getItems().addAll(PFMLoaders.values());
        choiceBoxPFM.setValue(DrawingBotV3.INSTANCE.pfmLoader);
        choiceBoxPFM.setOnAction(e -> changePathFinderModule(choiceBoxPFM.getSelectionModel().getSelectedItem()));

        labelElapsedTime.setText("0 s");
        labelPlottedLines.setText("0 lines");

        sliderDisplayedLines.setMax(1);
        sliderDisplayedLines.valueProperty().addListener((observable, oldValue, newValue) -> {
            PlottingTask task = DrawingBotV3.INSTANCE.getActiveTask();
            if(task != null){
                int lines = (int)Utils.mapDouble(newValue.doubleValue(), 0, 1, 0, task.plottedDrawing.getPlottedLineCount());
                task.plottedDrawing.displayedLineCount.setValue(lines);
                textFieldDisplayedLines.setText(String.valueOf(lines));
                if(DrawingBotV3.INSTANCE.renderedLines > lines){
                    DrawingBotV3.INSTANCE.reRender(); //TODO REDISTRIBUTE?
                }
            }
        });

        textFieldDisplayedLines.setOnAction(e -> {
            PlottingTask task = DrawingBotV3.INSTANCE.getActiveTask();
            if(task != null){
                int lines = (int)Math.max(0, Math.min(task.plottedDrawing.getPlottedLineCount(), Double.parseDouble(textFieldDisplayedLines.getText())));
                task.plottedDrawing.displayedLineCount.setValue(lines);
                textFieldDisplayedLines.setText(String.valueOf(lines));
                sliderDisplayedLines.setValue((double)lines / task.plottedDrawing.getPlottedLineCount());
                if(DrawingBotV3.INSTANCE.renderedLines > lines){
                    DrawingBotV3.INSTANCE.reRender();
                }
            }
        });

        ////

        ////PEN SETTINGS
        comboBoxDrawingSet.setItems(FXCollections.observableArrayList(DrawingRegistry.INSTANCE.registeredSets.values()));
        comboBoxDrawingSet.setValue(DrawingRegistry.INSTANCE.getDefaultSet());
        comboBoxDrawingSet.setOnAction(e -> changeDrawingSet(comboBoxDrawingSet.getSelectionModel().getSelectedItem()));
        comboBoxDrawingSet.setCellFactory(param -> new ComboCellDrawingSet());
        comboBoxDrawingSet.setButtonCell(new ComboCellDrawingSet());

        penTableView.setRowFactory(param -> {
            TableRow<ObservableDrawingPen> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setContextMenu(new ObservablePenContextMenu(row));
            return row;
        });

        penTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(DrawingBotV3.INSTANCE.display_mode == EnumDisplayMode.SELECTED_PEN){
                DrawingBotV3.INSTANCE.reRender();
            }
        });
        penNameColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        penNameColumn.setCellValueFactory(param -> param.getValue().name);

        penColourColumn.setCellFactory(ColorTableCell::new);
        penColourColumn.setCellValueFactory(param -> param.getValue().javaFXColour);

        penEnableColumn.setCellFactory(param -> new CheckBoxTableCell<>(index -> penEnableColumn.getCellObservableValue(index)));
        penEnableColumn.setCellValueFactory(param -> param.getValue().enable);

        comboBoxDrawingPen.setItems(FXCollections.observableArrayList(DrawingRegistry.INSTANCE.registeredPens.values()));
        comboBoxDrawingPen.setValue(DrawingRegistry.INSTANCE.getDefaultPen());
        comboBoxDrawingPen.setCellFactory(param -> new ComboCellDrawingPen());
        comboBoxDrawingPen.setButtonCell(new ComboCellDrawingPen());
        buttonAddPen.setOnAction(e -> DrawingBotV3.INSTANCE.observableDrawingSet.pens.add(new ObservableDrawingPen(comboBoxDrawingPen.getValue())));

        ////BATCH PROCESSING

        labelInputFolder.textProperty().bindBidirectional(BatchProcessing.inputFolder);
        labelOutputFolder.textProperty().bindBidirectional(BatchProcessing.outputFolder);
        buttonSelectInputFolder.setOnAction(e -> BatchProcessing.selectFolder(true));
        buttonSelectOutputFolder.setOnAction(e -> BatchProcessing.selectFolder(false));
        buttonStartBatchProcessing.setOnAction(e -> BatchProcessing.startProcessing());

        ////PROGRESS BAR PANE

        progressBarGeneral.prefWidthProperty().bind(paneProgressBar.widthProperty());
        progressBarLabel.setText("");
    }

    public void onTaskStageFinished(PlottingTask task, EnumTaskStage stage){
        switch (stage){
            case QUEUED:
                break;
            case LOADING_IMAGE:
                break;
            case PRE_PROCESSING:
                break;
            case PATH_FINDING:
                sliderDisplayedLines.setValue(1.0F);
                textFieldDisplayedLines.setText(String.valueOf(task.plottedDrawing.getPlottedLineCount()));
                break;
            case POST_PROCESSING:
                break;
            case LOGGING:
                break;
            case FINISHED:
                break;
        }
    }

    public void changePathFinderModule(PFMLoaders pfm){
        DrawingBotV3.INSTANCE.pfmLoader = pfm;

        //TODO MAKE "START DRAW" BUTTON IN GUI
    }

    public void changeDisplayMode(EnumDisplayMode mode){
        DrawingBotV3.INSTANCE.display_mode = mode;
        DrawingBotV3.INSTANCE.reRender();
    }

    public void changeDrawingSet(DrawingSet set){
        DrawingBotV3.INSTANCE.observableDrawingSet.loadDrawingSet(set);
    }

    public void importURL(){
        String url = getClipboardString();
        if (url != null && match(url.toLowerCase(), "^https?:...*(jpg|png)") != null) {
            println("Image URL found on clipboard: " + url);
            DrawingBotV3.INSTANCE.createPlottingTask(url);
        }
    }

    public void importFile(){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().add(FileUtils.IMPORT_IMAGES);
            d.setTitle("Select an image file to sketch");
            d.setInitialDirectory(new File(DrawingBotV3.INSTANCE.savePath("")));
            File file = d.showOpenDialog(null);
            if(file != null){
                DrawingBotV3.INSTANCE.createPlottingTask(file.getAbsolutePath());
            }
        });
    }

    //TODO FINISH PDF EXPORTING
    public void exportFile(ExportFormats format, boolean seperatePens){
        if(DrawingBotV3.INSTANCE.getActiveTask() == null){
            return;
        }
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(format.filters);
            d.setTitle(format.getDialogTitle());
            d.setInitialDirectory(new File(DrawingBotV3.INSTANCE.savePath("")));
            //TODO SET INITIAL FILENAME!!!
            File file = d.showSaveDialog(null);
            if(file != null){
                DrawingBotV3.INSTANCE.createExportTask(format, DrawingBotV3.INSTANCE.getActiveTask(), ExportFormats::defaultFilter, d.getSelectedExtensionFilter().getExtensions().get(0).substring(1), file, seperatePens);
            }
        });
    }

    public void openHelpPage() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create("https://github.com/SonarSonic/Drawbot_image_to_gcode_v3"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClipboardString(){
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)){
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            //
        }
        return null;

    }

    public static class ComboCellDrawingSet extends ComboBoxListCell<DrawingSet> {

        public ComboCellDrawingSet(){
            super();

        }

        @Override
        public void updateItem(DrawingSet item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText("  " + item.toString());
                HBox box = new HBox();
                for(IDrawingPen pen : item.getPens()){
                    box.getChildren().add(new Rectangle(10, 12, ImageTools.getColorFromARGB(pen.getRGBColour())));
                }
                setGraphic(box);
            }
        }
    }

    public static class ComboCellDrawingPen extends ComboBoxListCell<DrawingPen> {

        public final Rectangle colour;

        public ComboCellDrawingPen(){
            super();
            colour = new Rectangle(20, 12, Color.AQUA);
        }

        @Override
        public void updateItem(DrawingPen item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText("  " + item.toString());
                setGraphic(colour);
                colour.setFill(ImageTools.getColorFromARGB(item.getRGBColour()));
            }
        }
    }

    public static class ObservablePenContextMenu extends ContextMenu{

        public ObservablePenContextMenu(TableRow<ObservableDrawingPen> row){
            super();
            MenuItem delete = new MenuItem("Delete");
            delete.setOnAction(e -> DrawingBotV3.INSTANCE.observableDrawingSet.pens.remove(row.getItem()));
            getItems().add(delete);

            MenuItem duplicate = new MenuItem("Duplicate");
            duplicate.setOnAction(e -> DrawingBotV3.INSTANCE.observableDrawingSet.pens.add(row.getItem()));
            getItems().add(duplicate);

        }

    }

}
