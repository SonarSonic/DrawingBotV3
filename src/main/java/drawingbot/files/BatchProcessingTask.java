package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.pfm.PFMLoaders;
import drawingbot.plotting.PlottingTask;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;

public class BatchProcessingTask extends Task<Boolean> {

    public String inputFolder;
    public String outputFolder;
    public PFMLoaders loader;
    public ObservableDrawingSet drawingPenSet;

    private int imageCount;
    private int imagesDone;

    public BatchProcessingTask(String inputFolder, String outputFolder){
        this.inputFolder = inputFolder;
        this.outputFolder = outputFolder;
        this.loader = DrawingBotV3.INSTANCE.pfmLoader;
        this.drawingPenSet = new ObservableDrawingSet(DrawingBotV3.INSTANCE.observableDrawingSet);
    }

    private void onInternalTaskTitleChanged(ObservableValue<? extends String> observableValue, String oldTitle, String newTitle) {
        updateTitle("Batch Processing " + (imagesDone+1) + " / " + imageCount + "  " + newTitle);
        DrawingBotV3.INSTANCE.updateLocalMessage(getTitle() + " : " + getMessage());
    }

    private void onInternalTaskMessageChanged(ObservableValue<? extends String> observableValue, String oldMessage, String newMessage) {
        updateMessage(newMessage);
        DrawingBotV3.INSTANCE.updateLocalMessage(getTitle() + " : " + getMessage());
    }

    private void onInternalTaskProgressChanged(ObservableValue<? extends Number> observableValue, Number oldProgress, Number newProgress) {
        updateProgress(imagesDone + newProgress.doubleValue(), imageCount);
        DrawingBotV3.INSTANCE.updateLocalProgress(getProgress());
    }

    @Override
    protected Boolean call() throws Exception {
        try{

        PathMatcher imageMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{tif, tga, png, jpg, gif, bmp, jpeg}");
        List<Path> files = Files.walk(new File(inputFolder).toPath()).filter(Files::isRegularFile).filter(Files::isReadable).collect(Collectors.toList());
        //TODO FIX CHECKING FOR PATH
        System.out.println("Batch Processing: Found " + files.size());


        if(!files.isEmpty()){
            imageCount = files.size();
            imagesDone = 0;
            loop: for(Path path : files){
                updateTitle("Batch Processing " + (imagesDone+1) + " / " + imageCount + "  ");
                File newPath = new File(outputFolder + "\\" + path.getFileName());
                if(!Files.exists(newPath.toPath())){
                    PlottingTask internalTask = new PlottingTask(loader, drawingPenSet, path.toString());
                    internalTask.titleProperty().addListener(this::onInternalTaskTitleChanged); //listeners could cause memory leaks???
                    internalTask.messageProperty().addListener(this::onInternalTaskMessageChanged);
                    internalTask.progressProperty().addListener(this::onInternalTaskProgressChanged);
                    DrawingBotV3.INSTANCE.setActivePlottingTask(internalTask);
                    while(!internalTask.isTaskFinished() && !isCancelled()){
                        if(!internalTask.doTask()){
                            cancel();
                            continue loop; //prevents the export task
                        }
                    }
                    DrawingBotV3.INSTANCE.setActivePlottingTask(null); //prevents rendering and avoids memory leak.
                    if(!isCancelled()){
                        ExportTask exportTask = new ExportTask(ExportFormats.EXPORT_IMAGE, internalTask, ExportFormats::defaultFilter, ".png", newPath, false);
                        exportTask.call();
                    }
                }
                imagesDone++;
            }
        }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
