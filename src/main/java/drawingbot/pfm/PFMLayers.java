package drawingbot.pfm;

import drawingbot.drawing.DrawingStyle;
import drawingbot.drawing.DrawingStyleSet;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.geom.basic.IGeometry;
import drawingbot.javafx.GenericSetting;
import drawingbot.plotting.PlottingTask;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumDistributionType;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.util.*;

public class PFMLayers extends AbstractPFM{
    public DrawingStyleSet drawingStyles;

    protected List<LayerTask> layerTasks;
    protected double[] subTaskProgress;

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

        layerTasks = new ArrayList<>();
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

        final Map<DrawingStyle, List<IGeometry>> geometriesPerPFM = new HashMap<>();        
        
        int layerCount = activeStyles.size();
        
        BufferedImage plotImage = task.imgPlotting;

        for(int layerIndex = 0; layerIndex < layerCount; layerIndex ++){
            nextDrawingStyle();
            PlottingTask plottingTask = new PlottingTask(currentDrawingStyle.getFactory(), currentStyleSettings, evenlyDistributedDrawingSet, plotImage, task.originalFile);
            plottingTask.isSubTask = true;
            plottingTask.enableImageFiltering = true;
            layerTasks.add(new PFMLayers.LayerTask(currentDrawingStyle, plottingTask));
            
            PFMLayers.LayerTask layerTask = layerTasks.get(layerIndex);
            final int taskIndex = layerIndex;
            layerTask.task.progressProperty().addListener((observable, oldValue, newValue) -> updateSubTaskProgress(taskIndex, newValue.doubleValue(), 1D));
            layerTask.task.groupID = layerIndex;

            task.plottedDrawing.addGroupPFMType(layerIndex, layerTask.task.pfmFactory);
            layerTask.task.plottedDrawing.addGroupPFMType(layerIndex, layerTask.task.pfmFactory);

            while(!layerTask.task.isTaskFinished() && !task.plottingFinished && !task.isFinished()){
                layerTask.task.doTask();
            }

            if(!layerTask.task.plottedDrawing.ignoreWeightedDistribution){
                layerTask.task.pfmFactory.distributionType.distribute.accept(layerTask.task.plottedDrawing);
            }

            geometriesPerPFM.putIfAbsent(layerTask.style, new ArrayList<>());
            List<IGeometry> pfmGeometryList = geometriesPerPFM.get(layerTask.style);
            for(IGeometry geometry : layerTask.task.plottedDrawing.geometries){
                task.plottedDrawing.addGeometry(geometry);
                pfmGeometryList.add(geometry);
            }
            
            plotImage = layerTask.task.getPlottingImage();
            layerTask.task.reset();
        }       
        
        layerTasks.clear();
        task.finishProcess();        
        
    }

    public void updateSubTaskProgress(int currentIndex, double workDone, double max){
        subTaskProgress[currentIndex] = workDone / max;
        double totalProgress = 0;
        for(double progress : subTaskProgress){
            totalProgress += progress / layerTasks.size();
        }
        task.updateProgress(totalProgress, 1);
    }

    @Override
    public void onStopped() {
        if(layerTasks != null && !layerTasks.isEmpty()){
            layerTasks.forEach(task -> task.task.stopElegantly());
        }
    }

    @Override
    public float getPlottingResolution() {
        return 1.0F;
    }
    
    public static class LayerTask{

        public DrawingStyle style;
        public PlottingTask task;

        public LayerTask(DrawingStyle style, PlottingTask task) {
            this.task = task;
            this.style = style;
        }

    }

}
