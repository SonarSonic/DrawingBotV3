package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.pfm.PFMLoaders;
import drawingbot.tasks.PlottingThread;
import drawingbot.utils.EnumDisplayMode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;

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

}
