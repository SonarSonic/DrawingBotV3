package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.GenericSetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.utils.EnumColourSplitter;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * a task which triggers additional tasks for each colour seperation output
 */
public class SplitPlottingTask extends PlottingTask{

    public EnumColourSplitter splitter;
    public List<PlottingTask> subTasks;
    public double[] subTaskProgress;
    public int[] renderedLines;

    public SplitPlottingTask(PFMFactory<?> pfmFactory, List<GenericSetting<?, ?>> pfmSettings, ObservableDrawingSet drawingPenSet, BufferedImage image, File originalFile, EnumColourSplitter splitter) {
        super(pfmFactory, pfmSettings, drawingPenSet, image, originalFile);
        this.splitter = splitter;
        this.subTaskProgress = new double[splitter.getSplitCount()];
        this.renderedLines = new int[splitter.getSplitCount()];

        DrawingSet set = splitter.getDrawingSet();
        float alpha = (pfmFactory.getTransparentCMYK() ? 30 : 255) / 255F;
        for(ObservableDrawingPen pen : drawingPenSet.getPens()){
            if(pen.source instanceof DrawingPen && set.pens.contains(pen.source)){
                Color color = pen.javaFXColour.get();
                if(color.getOpacity() != alpha){
                    Platform.runLater(() -> pen.javaFXColour.set(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha)));
                }
            }
        }
    }

    public boolean doTask(){
        switch (stage){
            case PRE_PROCESSING:
                subTasks = null;
                break;
            case DO_PROCESS:

                //generate split images
                updateMessage("Processing - Splitting Images");
                List<BufferedImage> subImages = splitter.splitFunction.apply(imgPlotting);

                //generate sub tasks
                subTasks = new ArrayList<>();
                for(BufferedImage img : subImages){
                    PlottingTask subTask = new PlottingTask(pfmFactory, pfmSettings, plottedDrawing.drawingPenSet, img, originalFile);
                    subTask.enableImageFiltering = false; //prevent image filtering we will do it here and pass the correct image
                    subTask.isSubTask = true; //prevents some calls
                    subTask.plottedDrawing.ignoreWeightedDistribution = true; // very important, the sub task will call weighted distribution without this.
                    subTasks.add(subTask);
                }

                ///start multi-threading
                updateMessage("Processing - Start Multi-Threading");
                ExecutorService service = Executors.newFixedThreadPool(splitter.getSplitCount(), r -> {
                    Thread t = new Thread(r, "DrawingBotV3 - Split Plotting Task");
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler(DrawingBotV3.INSTANCE.exceptionHandler);
                    return t ;
                });

                final CountDownLatch latch = new CountDownLatch(splitter.getSplitCount());

                //create the threads
                int index = 0;
                for(PlottingTask subTask : subTasks){
                    int subTaskIndex = index;
                    subTask.progressProperty().addListener((observable, oldValue, newValue) -> updateSubTaskProgress(subTaskIndex, newValue.doubleValue(), 1D));
                    service.submit(() -> {
                        while(!subTask.isTaskFinished() && !plottingFinished && !isFinished()){
                            subTask.defaultPen = subTaskIndex; //make sure it's on the right colour seperation pen.
                            subTask.doTask();
                        }
                        subTask.plottedDrawing.geometries.forEach(g -> g.setPenIndex(subTaskIndex));
                        latch.countDown();
                    });
                    index++;
                }

                updateMessage("Processing - Plotting CMYK");
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    return false;
                } finally {
                    updateMessage("Processing - Stop Multi-Threading");
                    service.shutdown();
                }

                updateMessage("Processing - Merge Drawings");
                for(PlottingTask subTask : subTasks){
                    plottedDrawing.addGeometry(subTask.plottedDrawing);
                }

                subTasks.clear();
                plottedDrawing.displayedShapeMax.set(-1); //sometimes needed
                finishStage();
                return true; ///return to override the defaults
            case POST_PROCESSING:
                DrawingBotV3.INSTANCE.activeTask.set(this);
                //DrawingBotV3.INSTANCE.reRender(); //with darken enabled the render is already accurate
                finishStage();
                return true;
        }
        return super.doTask();
    }

    public int getCurrentGeometryCount(){
        if(subTasks == null || subTasks.isEmpty()){
            return plottedDrawing.getGeometryCount();
        }
        int count = 0;
        for(PlottingTask task : subTasks){
            count += task.plottedDrawing.getGeometryCount();
        }
        return count;
    }

    public long getCurrentVertexCount(){
        if(subTasks == null || subTasks.isEmpty()){
            return plottedDrawing.getVertexCount();
        }
        long count = 0;
        for(PlottingTask task : subTasks){
            count += task.plottedDrawing.getVertexCount();
        }
        return count;
    }

    public void updateSubTaskProgress(int currentIndex, double workDone, double max){
        subTaskProgress[currentIndex] = workDone / max;
        double totalProgress = 0;
        for(double progress : subTaskProgress){
            totalProgress += progress / splitter.getSplitCount();
        }
        updateProgress(totalProgress, 1);
    }

}
