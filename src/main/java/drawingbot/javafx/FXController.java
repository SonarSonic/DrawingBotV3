package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.*;
import drawingbot.helpers.ImageTools;
import drawingbot.pfm.PFMLoaders;
import drawingbot.tasks.PlottingThread;
import drawingbot.utils.EnumDisplayMode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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

    public MenuItem menuOpenFile = null;
    public MenuItem menuOpenURL = null;
    public MenuItem menuExit = null;

    public MenuItem menuHelpPage = null;

    public ChoiceBox<PFMLoaders> choiceBoxPFM = null;
    public ChoiceBox<EnumDisplayMode> choiceBoxDisplayMode = null;

    public ProgressBar progressBarGeneral = null;
    public Label progressBarLabel = null;

    //VIEWPORT
    public ScrollPane viewportScrollPane = null;
    public StackPane viewportStackPane = null;

    //PEN SETTINGS
    public AnchorPane rightSidePane = null;
    public ComboBox<DrawingSet> comboBoxDrawingSet = null;

    public TableView<ObservableDrawingPen> penTableView = null;
    public TableColumn<ObservableDrawingPen, Boolean> penEnableColumn = null;
    public TableColumn<ObservableDrawingPen, String> penNameColumn = null;
    public TableColumn<ObservableDrawingPen, Color> penColourColumn = null;

    public ComboBox<DrawingPen> comboBoxDrawingPen = null;
    public Button buttonAddPen = null;

    public void initialize(){
        println("Initialize JAVA FX");
        //file menu
        menuOpenFile.setOnAction(e -> openFile());
        menuOpenURL.setOnAction(e -> openURL());
        menuExit.setOnAction(e -> DrawingBotV3.INSTANCE.exit());

        //help menu
        menuHelpPage.setOnAction(e -> openHelpPage());


        //drawing tools
        choiceBoxPFM.setItems(FXCollections.observableArrayList(PFMLoaders.values()));
        choiceBoxPFM.setValue(DrawingBotV3.INSTANCE.pfmLoader);
        choiceBoxPFM.setOnAction(e -> changePathFinderModule(choiceBoxPFM.getSelectionModel().getSelectedItem()));

        choiceBoxDisplayMode.setItems(FXCollections.observableArrayList(EnumDisplayMode.values()));
        choiceBoxDisplayMode.setValue(EnumDisplayMode.DRAWING);
        choiceBoxDisplayMode.setOnAction(e -> changeDisplayMode(choiceBoxDisplayMode.getSelectionModel().getSelectedItem()));

        //pen settings
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
            if(DrawingBotV3.INSTANCE.display_mode == EnumDisplayMode.PEN){
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

        progressBarLabel.setText("");
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

    public void openURL(){
        String url = getClipboardString();
        if (url != null && match(url.toLowerCase(), "^https?:...*(jpg|png)") != null) {
            println("Image URL found on clipboard: " + url);
            PlottingThread.createImagePlottingTask(url);
        }
    }

    public void openFile(){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.setTitle("Select an image file to sketch");
            File file = d.showOpenDialog(null);
            d.setInitialDirectory(new File("/"));
            if(file != null){
                PlottingThread.createImagePlottingTask(file.getAbsolutePath());
            }
        });
    }

    public void openColourPicker(){

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
