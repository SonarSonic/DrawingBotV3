package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.pfm.PFMLoaders;
import drawingbot.utils.EnumDisplayMode;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

import static processing.core.PApplet.*;

public class FXController {

    public MenuItem menuFileOpen = null;

    public MenuItem menuHelpPage = null;

    public ChoiceBox<PFMLoaders> choiceBoxPFM = null;
    public ChoiceBox<EnumDisplayMode> choiceBoxDisplayMode = null;


    public void initialize(){
        println("Initialize JAVA FX");
        //file menu
        menuFileOpen.setOnAction(e -> DrawingBotV3.INSTANCE.action_open());

        //help menu
        menuHelpPage.setOnAction(e -> openHelpPage());

        //drawing tools
        choiceBoxPFM.setItems(FXCollections.observableArrayList(PFMLoaders.values()));
        choiceBoxPFM.setOnAction(e -> DrawingBotV3.INSTANCE.action_changePFM(choiceBoxPFM.getSelectionModel().getSelectedItem()));
        choiceBoxPFM.setValue(PFMLoaders.ORIGINAL);

        choiceBoxDisplayMode.setItems(FXCollections.observableArrayList(EnumDisplayMode.values()));
        choiceBoxDisplayMode.setOnAction(e -> DrawingBotV3.INSTANCE.changeDisplayMode(choiceBoxDisplayMode.getSelectionModel().getSelectedItem()));
        choiceBoxDisplayMode.setValue(EnumDisplayMode.DRAWING);
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

}
