package drawingbot.pfm;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.DrawingStyle;
import drawingbot.drawing.DrawingStyleSet;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.ImageTools;
import drawingbot.javafx.GenericSetting;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.plotting.PlottingTask;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumDistributionType;
import javafx.collections.ObservableList;

import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractMosaicPFM extends AbstractPFM{

    public DrawingStyleSet drawingStyles;

    protected List<MosaicTask> mosaicTasks;
    protected double[] subTaskProgress;

    public ObservableDrawingSet evenlyDistributedDrawingSet;

    protected List<DrawingStyle> activeStyles = null;
    protected List<Integer> tilesPerStyle = null;
    protected List<ObservableList<GenericSetting<?, ?>>> settingsPerStyle = null;

    protected DrawingStyle currentDrawingStyle = null;
    protected ObservableList<GenericSetting<?, ?>> currentStyleSettings = null;

    /**
     * Should call nextDrawingStyle before each tile is created
     */
    public abstract void createMosaicTasks();

    public abstract int calculateTileCount();

    public void nextDrawingStyle(){
        int styleIndex = randomSeed(0, activeStyles.size());
        int styleCount = tilesPerStyle.get(styleIndex);
        currentDrawingStyle = activeStyles.get(styleIndex);
        currentStyleSettings = settingsPerStyle.get(styleIndex);

        if(styleCount <= 1){
            activeStyles.remove(styleIndex);
            tilesPerStyle.remove(styleIndex);
            settingsPerStyle.remove(styleIndex);
        }else{
            tilesPerStyle.set(styleIndex, styleCount + 1);
        }
    }


    @Override
    public void preProcess() {

        evenlyDistributedDrawingSet = new ObservableDrawingSet(task.getDrawingSet());
        evenlyDistributedDrawingSet.distributionType.set(EnumDistributionType.EVEN_WEIGHTED);

        mosaicTasks = new ArrayList<>();
        activeStyles = new ArrayList<>();
        tilesPerStyle = new ArrayList<>();
        settingsPerStyle = new ArrayList<>();

    }

    @Override
    public void doProcess() {

        int totalWeight = 0;
        for(DrawingStyle style : drawingStyles.styles){
            if(style.isEnabled() && style.getDistributionWeight() != 0){
                totalWeight += style.getDistributionWeight();
                activeStyles.add(style);
            }
        }

        int tileCount = calculateTileCount();
        int styleIndex = 0;
        int activeCount = 0;
        int highestCount = 0;
        int dominantIndex = -1;

        for(DrawingStyle style : activeStyles){
            float percentage = (float)style.getDistributionWeight() / totalWeight;
            int tilesToPlot = Math.max(1, (int)(percentage * tileCount));
            if(tilesToPlot > highestCount){
                dominantIndex = styleIndex;
                activeCount += tilesToPlot;
            }
            ObservableList<GenericSetting<?, ?>> settings = MasterRegistry.INSTANCE.getNewObservableSettingsList(style.getFactory());
            GenericSetting.applySettings(style.settings, settings);
            settingsPerStyle.add(settings);
            tilesPerStyle.add(tilesToPlot);
            styleIndex++;
        }

        if(dominantIndex == -1){
            task.finishProcess();
            return;
        }

        if(activeCount < tileCount){
            ///add any tiles which are not accounted for due to rounding error to the most dominant style or the first style if they are all the same weight
            tilesPerStyle.set(dominantIndex, tilesPerStyle.get(dominantIndex) + (tileCount-activeCount));
        }

        //////

        createMosaicTasks();

        //////


        this.subTaskProgress = new double[mosaicTasks.size()];

        ///start multi-threading
        task.updateMessage("Processing - Start Multi-Threading");
        ExecutorService service = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "DrawingBotV3 - Mosaic Tile Task");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(DrawingBotV3.INSTANCE.exceptionHandler);
            return t ;
        });

        final CountDownLatch latch = new CountDownLatch(mosaicTasks.size());

        final Map<DrawingStyle, List<IGeometry>> geometriesPerPFM = new HashMap<>();

        //create the threads
        int index = 0;
        for(AbstractMosaicPFM.MosaicTask mosaicTask : mosaicTasks){
            final int subTaskIndex = index;
            mosaicTask.task.progressProperty().addListener((observable, oldValue, newValue) -> updateSubTaskProgress(subTaskIndex, newValue.doubleValue(), 1D));
            service.submit(() -> {

                //very important for allowing the groups to be considered together when optimising
                mosaicTask.task.groupID = subTaskIndex;

                //mark the group type in the MAIN drawing, not the mosaic drawing, there it doesn't really matter since we're not optimising, but we'll set it anyway...
                task.plottedDrawing.addGroupPFMType(subTaskIndex, mosaicTask.task.pfmFactory);
                mosaicTask.task.plottedDrawing.addGroupPFMType(subTaskIndex, mosaicTask.task.pfmFactory);

                while(!mosaicTask.task.isTaskFinished() && !task.plottingFinished && !task.isFinished()){
                    mosaicTask.task.doTask();
                }

                if(!mosaicTask.task.plottedDrawing.ignoreWeightedDistribution){
                    mosaicTask.task.pfmFactory.distributionType.distribute.accept(mosaicTask.task.plottedDrawing);
                }

                geometriesPerPFM.putIfAbsent(mosaicTask.style, new ArrayList<>());
                List<IGeometry> pfmGeometryList = geometriesPerPFM.get(mosaicTask.style);
                for(IGeometry geometry : mosaicTask.task.plottedDrawing.geometries){
                    geometry.transform(mosaicTask.transform);
                    task.plottedDrawing.addGeometry(geometry);
                    pfmGeometryList.add(geometry);
                }
                mosaicTask.task.reset();
                latch.countDown();
            });
            index++;
        }

        task.updateMessage("Processing - Plotting Mosaic");
        try {
            latch.await();
        } catch (InterruptedException e) {
            task.finishProcess();
        } finally {
            task.updateMessage("Processing - Stop Multi-Threading");
            service.shutdown();
        }

        //a cheeky method of obtaining a even distribution across the entire image, relies on the PFM implementing CustomRGBA note.
        //note even though we copy to another drawing, however are still affecting the original drawings geometries, so the path order is actually unaffected!
        for(Map.Entry<DrawingStyle, List<IGeometry>> entry : geometriesPerPFM.entrySet()){
            if(entry.getValue() != null){
                PlottedDrawing drawing = new PlottedDrawing(evenlyDistributedDrawingSet);
                EnumDistributionType distributionType = entry.getKey().getFactory().getDistributionType();
                if(AbstractMosaicPFM.class.isAssignableFrom(entry.getKey().getFactory().getInstanceClass())){
                    distributionType = EnumDistributionType.EVEN_WEIGHTED;
                }
                evenlyDistributedDrawingSet.distributionType.set(distributionType);
                entry.getValue().sort(Comparator.comparingInt(o -> ImageTools.getPerceivedLuminanceFromRGB(o.getCustomRGBA() == null ? 0 : o.getCustomRGBA())));
                drawing.addGeometry(entry.getValue());
                drawing.updatePenDistribution();
            }
        }


        mosaicTasks.clear();

        task.finishProcess();
    }

    public void updateSubTaskProgress(int currentIndex, double workDone, double max){
        subTaskProgress[currentIndex] = workDone / max;
        double totalProgress = 0;
        for(double progress : subTaskProgress){
            totalProgress += progress / mosaicTasks.size();
        }
        task.updateProgress(totalProgress, 1);
    }

    @Override
    public void onStopped() {
        if(mosaicTasks != null && !mosaicTasks.isEmpty()){
            mosaicTasks.forEach(task -> task.task.stopElegantly());
        }
    }

    public static class MosaicTask{

        public DrawingStyle style;
        public PlottingTask task;
        public AffineTransform transform;

        public MosaicTask(DrawingStyle style, PlottingTask task, AffineTransform transform) {
            this.task = task;
            this.style = style;
            this.transform = transform;
        }

    }

}
