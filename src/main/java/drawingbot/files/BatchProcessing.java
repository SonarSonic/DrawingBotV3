package drawingbot.files;

import drawingbot.DrawingBotV3;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class BatchProcessing {

    public static SimpleStringProperty inputFolder = new SimpleStringProperty(null);
    public static SimpleStringProperty outputFolder = new SimpleStringProperty(null);

    public static void selectFolder(boolean input){
        Platform.runLater(() -> {
            DirectoryChooser d = new DirectoryChooser();
            d.setTitle("Select " + (input ? "Input Folder" : "Output Folder"));
            d.setInitialDirectory(new File(DrawingBotV3.INSTANCE.savePath("")));
            File file = d.showDialog(null);
            if(file != null){
                if(input){
                    inputFolder.set(file.getPath());
                }else{
                    outputFolder.set(file.getPath());
                }
            }
        });
    }

    public static void startProcessing(){
        if(inputFolder.get() == null || outputFolder.get() == null){
            return;
        }
        DrawingBotV3.INSTANCE.executorService.submit(new BatchProcessingTask(inputFolder.get(), outputFolder.get()));
    }


}
