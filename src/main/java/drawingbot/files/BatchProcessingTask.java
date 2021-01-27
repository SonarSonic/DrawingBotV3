package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.pfm.PFMLoaders;
import drawingbot.plotting.PlottingTask;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    @Override
    protected void setException(Throwable t) {
        super.setException(t);
        BatchProcessing.finishProcessing();
    }

    @Override
    protected Boolean call() throws Exception {
        PathMatcher imageMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{tif,tga,png,jpg,gif,bmp,jpeg}");
        List<Path> files = Files.walk(new File(inputFolder).toPath()).filter(Files::isRegularFile).filter(Files::isReadable).filter(p -> imageMatcher.matches(p.getFileName())).collect(Collectors.toList());
        DrawingBotV3.println("Batch Processing: Found " + files.size());

        if(!files.isEmpty()){

            ExecutorService service = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "DrawingBotV3 - Batch Processing Thread");
                t.setDaemon(true);
                return t ;
            });

            imageCount = files.size();
            imagesDone = 0;
            loop: for(Path path : files){
                updateTitle("Batch Processing " + (imagesDone+1) + " / " + imageCount);
                String simpleFileName = FileUtils.removeExtension(path.getFileName().toString());
                if(BatchProcessing.overwriteExistingFiles.get() || BatchProcessing.exportTasks.stream().anyMatch(b -> b.hasMissingFiles(outputFolder, simpleFileName, drawingPenSet))){
                    PlottingTask internalTask = new PlottingTask(loader, drawingPenSet, path.toString());
                    Future<?> futurePlottingTask = service.submit(internalTask);
                    while(!futurePlottingTask.isDone()){
                        ///wait
                        if(isCancelled()){
                            futurePlottingTask.cancel(true);
                            service.shutdown();
                            break loop;
                        }
                    }
                    if(!isCancelled()){
                        List<ExportTask> tasks = new ArrayList<>();
                        for(BatchProcessing.BatchExportTask task : BatchProcessing.exportTasks){
                            if(BatchProcessing.overwriteExistingFiles.get() || task.hasMissingFiles(outputFolder, simpleFileName, drawingPenSet)){
                                File saveLocation = new File(outputFolder + "\\" + simpleFileName + task.getCleanExtension());
                                if(task.enablePerDrawing.get()){
                                    tasks.add(new ExportTask(task.format, internalTask, ExportFormats::defaultFilter, task.getCleanExtension(), saveLocation, false, BatchProcessing.overwriteExistingFiles.get()));

                                }
                                if(task.enablePerPen.get()){
                                    tasks.add(new ExportTask(task.format, internalTask, ExportFormats::defaultFilter, task.getCleanExtension(), saveLocation, true, BatchProcessing.overwriteExistingFiles.get()));
                                }
                            }
                        }
                        for(ExportTask task : tasks){
                            Future<?> futureExportTask = service.submit(task);
                            while(!futureExportTask.isDone()){
                                ///wait
                                if(isCancelled()){
                                    futureExportTask.cancel(true);
                                    service.shutdown();
                                    break loop;
                                }
                            }
                        }
                    }
                }
                imagesDone++;
            }
            service.shutdown();
        }
        BatchProcessing.finishProcessing();

        return null;
    }

}
