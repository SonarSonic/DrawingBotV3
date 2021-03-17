package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPathFindingModule;
import drawingbot.drawing.ObservableDrawingSet;
import drawingbot.javafx.GenericFactory;
import drawingbot.utils.EnumColourSplitter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * a task which triggers additional tasks for each colour seperation output
 */
public class SplitPlottingTask extends PlottingTask{

    public EnumColourSplitter splitter;
    public List<PlottingTask> subTasks;
    public int currentIndex;

    public SplitPlottingTask(GenericFactory<IPathFindingModule> pfmFactory, ObservableDrawingSet drawingPenSet, BufferedImage image, File originalFile, EnumColourSplitter splitter) {
        super(pfmFactory, drawingPenSet, image, originalFile);
        this.splitter = splitter;
        this.plottedDrawing.ignoreWeightedDistribution = true;
    }

    public boolean doTask(){
        switch (stage){
            case PRE_PROCESSING:
                subTasks = null;
                break;
            case DO_PROCESS:
                ///generate the sub task
                if(subTasks == null){
                    List<BufferedImage> subImages = splitter.splitFunction.apply(img_plotting);
                    subTasks = new ArrayList<>();
                    for(BufferedImage img : subImages){
                        PlottingTask subTask = new PlottingTask(pfmFactory, plottedDrawing.drawingPenSet, img, originalFile);
                        subTask.enableImageFiltering = false; //prevent image filtering we will do it here and pass the correct image
                        subTask.isSubTask = true; //prevent some calls
                        subTask.plottedDrawing.ignoreWeightedDistribution = true; // very important, the sub task will call weighted distribution without this.
                        subTasks.add(subTask);
                    }
                }
                currentIndex = 0;
                for(PlottingTask subTask : subTasks){
                    updateMessage("Plotting Image: " + pfmFactory.getName() + " Layer: " + splitter.outputNames.get(currentIndex));
                    subTask.progressProperty().addListener((observable, oldValue, newValue) -> updateProgress((newValue.doubleValue() / subTasks.size()) + ((1D/subTasks.size()) * currentIndex), 1D));
                    DrawingBotV3.INSTANCE.renderedTask = subTask;
                    DrawingBotV3.INSTANCE.reRender();
                    while(!subTask.isTaskFinished() && !plottingFinished && !isFinished()){
                        subTask.defaultPen = currentIndex;//make sure it's on the right colour seperation pen.
                        subTask.doTask();
                    }
                    DrawingBotV3.INSTANCE.renderedTask = null;
                    plottedDrawing.addGeometry(subTask.plottedDrawing);
                    currentIndex++;
                }
                plottedDrawing.displayedLineCount.set(-1); //sometimes needed
                finishStage();
                return true; ///return to override the defaults
            case POST_PROCESSING:
                DrawingBotV3.INSTANCE.activeTask = this;
                DrawingBotV3.INSTANCE.reRender();
                finishStage();
                return true;
        }
        return super.doTask();
    }

}
