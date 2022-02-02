package drawingbot.pfm;

import drawingbot.drawing.DrawingStyle;
import drawingbot.drawing.DrawingStyleSet;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.GenericSetting;
import drawingbot.plotting.PlottingTask;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumDistributionType;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.util.*;

public class PFMLayers extends AbstractPFM{
    public DrawingStyleSet drawingStyles;

    protected int layerCount;
    protected double[] subTaskProgress;
    protected PlottingTask currentTask;

    public ObservableDrawingSet evenlyDistributedDrawingSet;

    protected List<DrawingStyle> activeStyles = null;
    protected List<ObservableList<GenericSetting<?, ?>>> settingsPerStyle = null;

    protected DrawingStyle currentDrawingStyle = null;
    protected ObservableList<GenericSetting<?, ?>> currentStyleSettings = null;

    public void nextDrawingStyle(){
        int styleCount = activeStyles.size();
        int styleIndex = 0;
        currentDrawingStyle = activeStyles.get(styleIndex);
        currentStyleSettings = settingsPerStyle.get(styleIndex);

        if(styleCount > 1){
            activeStyles.remove(styleIndex);
            settingsPerStyle.remove(styleIndex);
        }
    }

    @Override
    public void preProcess() {

        evenlyDistributedDrawingSet = new ObservableDrawingSet(task.getDrawingSet());
        evenlyDistributedDrawingSet.distributionType.set(EnumDistributionType.EVEN_WEIGHTED);

        activeStyles = new ArrayList<>();
        settingsPerStyle = new ArrayList<>();

    }

    @Override
    public void doProcess() {

        for(DrawingStyle style : drawingStyles.styles){
            if(style.isEnabled() && style.getDistributionWeight() != 0) activeStyles.add(style);
        }

        for(DrawingStyle style : activeStyles){
            ObservableList<GenericSetting<?, ?>> settings = MasterRegistry.INSTANCE.getNewObservableSettingsList(style.getFactory());
            GenericSetting.applySettings(style.settings, settings);
            settingsPerStyle.add(settings);
        }
        
        layerCount = activeStyles.size();
        this.subTaskProgress = new double[layerCount];
        
        BufferedImage plotImage = task.imgPlotting;

        for(int layerIndex = 0; layerIndex < layerCount; layerIndex ++){
            final int taskIndex = layerIndex;

            nextDrawingStyle();

            currentTask = new PlottingTask(task.drawingArea, currentDrawingStyle.getFactory(), currentStyleSettings, evenlyDistributedDrawingSet, plotImage, task.originalFile);
            currentTask.progressProperty().addListener((observable, oldValue, newValue) -> updateSubTaskProgress(taskIndex, newValue.doubleValue(), 1D));
            currentTask.plottedDrawing = task.plottedDrawing;
            currentTask.isSubTask = true;

            task.groupID = taskIndex;
            task.plottedDrawing.addGroupPFMType(taskIndex, currentDrawingStyle.getFactory());

            while(!currentTask.isTaskFinished() && !task.plottingFinished && !task.isFinished()){
                currentTask.doTask();
            }

            if(!currentTask.plottedDrawing.ignoreWeightedDistribution){
                currentTask.pfmFactory.distributionType.distribute.accept(currentTask.plottedDrawing);
            }
            
            plotImage = currentTask.getPlottingImage();
        }

        task.finishProcess();        
        
    }

    public void updateSubTaskProgress(int currentIndex, double workDone, double max){
        subTaskProgress[currentIndex] = workDone / max;
        double totalProgress = 0;
        for(double progress : subTaskProgress){
            totalProgress += progress / layerCount;
        }
        task.updateProgress(totalProgress, 1);
    }

    @Override
    public void onStopped() {
        currentTask.stopElegantly();
    }

    @Override
    public float getPlottingResolution() {
        return 1.0F;
    }

}
