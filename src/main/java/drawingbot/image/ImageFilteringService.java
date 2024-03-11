package drawingbot.image;

import drawingbot.api.ICanvas;
import drawingbot.api.IProgressCallback;
import drawingbot.image.format.ImageData;
import drawingbot.image.kernels.IKernelFactory;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageFilteringService implements ImageFilterSettings.Listener, ObservableCanvas.Listener {

    /**
     * Flags for the state of the cache, these are the valid flags: <br>
     * {@link Flags#FORCE_REDRAW}} <br>
     * {@link Flags#OPEN_IMAGE_UPDATED}} <br>
     * {@link Flags#CROPPING_CHANGED} <br>
     * {@link Flags#CANVAS_CHANGED} <br>
     * {@link Flags#IMAGE_FILTERS_PARTIAL_UPDATE} <br>
     * {@link Flags#IMAGE_FILTERS_FULL_UPDATE}
     */
    private final FlagStates liveState = new FlagStates(Flags.RENDER_CATEGORY);
    private final FlagStates runningState = new FlagStates(Flags.RENDER_CATEGORY);
    private final Map<ObservableImageFilter, ImageStateCache> filterCache = new LinkedHashMap<>();
    private final InvalidationListener croppingListener;

    private transient BufferedImage cropped;

    public ImageFilteringService(){
        targetCanvasProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                if(oldValue instanceof ObservableCanvas observableCanvas){
                    observableCanvas.removeSpecialListener(this);
                }
            }
            if(newValue != null){
                if(newValue instanceof ObservableCanvas observableCanvas){
                    observableCanvas.addSpecialListener(this);
                }
            }
            liveState.setFlag(Flags.CANVAS_CHANGED, true);
        });

        imageSettingsProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.removeSpecialListener(this);
                dispose();
            }
            if(newValue != null){
                newValue.addSpecialListener(this);
                newValue.currentFilters.get().forEach(this::onImageFilterAdded);
            }
            liveState.setFlag(Flags.IMAGE_FILTERS_FULL_UPDATE, true);
        });

        croppingListener = observable -> {
            liveState.setFlag(Flags.CROPPING_CHANGED, true);
        };

        imageDataProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.getImageCropping().removeListener(croppingListener);
            }
            if(newValue != null){
                newValue.getImageCropping().addListener(croppingListener);
                markDirty();
            }
            liveState.setFlag(Flags.OPEN_IMAGE_UPDATED, true);
        });

        enabledProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                liveState.setFlag(Flags.FORCE_REDRAW, true);
            }
        });
    }

    public void update(){
        if(!isEnabled()){
            return;
        }
        if(liveState.anyMatch(Flags.FORCE_REDRAW, Flags.OPEN_IMAGE_UPDATED, Flags.CROPPING_CHANGED, Flags.CANVAS_CHANGED, Flags.IMAGE_FILTERS_PARTIAL_UPDATE, Flags.IMAGE_FILTERS_FULL_UPDATE)){
            runningState.loadState(liveState);
            getImageFilteringService().restart();
            liveState.clear();
        }
    }

    private BufferedImage doUpdate(ICanvas targetCanvas, IProgressCallback callback){
        boolean updateDownstream = runningState.anyMatch(Flags.FORCE_REDRAW);

        if(runningState.anyMatch(Flags.OPEN_IMAGE_UPDATED)){
            updateDownstream = true;
        }

        if(updateDownstream || cropped == null || runningState.anyMatch(Flags.CROPPING_CHANGED, Flags.CANVAS_CHANGED)){
            cropped = getImageData().createCroppedImage(targetCanvas);
            updateDownstream = true;
        }

        if(!updateDownstream && runningState.anyMatch(Flags.IMAGE_FILTERS_FULL_UPDATE)){
            updateDownstream = true;
        }

        BufferedImage filteredImage = cropped;
        int filterCount = 0;
        for(ObservableImageFilter filter : getImageSettings().currentFilters.get()){
            ImageStateCache stateCache = this.filterCache.get(filter);
            if(filter.enable.get()){
                if(updateDownstream || stateCache.isDirty || stateCache.bufferedImage == null){

                    BufferedImageOp imageOp = filter.getBufferedImageOp();

                    IKernelFactory kernelFactory = MasterRegistry.INSTANCE.getImageFilterKernel(imageOp);
                    if(kernelFactory != null){
                        BufferedImage dstImage = new BufferedImage(filteredImage.getWidth(), filteredImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        filteredImage = kernelFactory.doProcess(imageOp, filteredImage, dstImage);
                    }else if(stateCache.bufferedImage != null && isCachedImageCompatible(filteredImage, stateCache.bufferedImage)){
                        filteredImage = imageOp.filter(filteredImage, stateCache.bufferedImage);
                    }else{
                        filteredImage = imageOp.filter(filteredImage, null);
                    }

                    updateDownstream = true; //one of the filters has changed, so all the ones after this need to be updated
                }else{
                    filteredImage = stateCache.bufferedImage;
                }
                stateCache.bufferedImage = filteredImage;
                stateCache.isDirty = false;
            }else if(stateCache.isDirty){
                //the filter has just been disabled, so update downstream filters
                updateDownstream = true;
                stateCache.isDirty = false;
                stateCache.bufferedImage = null;
            }

            filterCount++;
            callback.updateProgress(filterCount, getImageSettings().currentFilters.get().size());
        }

        callback.updateProgress(1, 1);
        return filteredImage;
    }

    public void dispose(){
        filterCache.values().forEach(ImageStateCache::destroy);
        filterCache.clear();
    }

    ////////////////////////////////////////////////////////

    // Image Filter Listener

    @Override
    public void onImageFilterAdded(ObservableImageFilter filter) {
        filterCache.put(filter, new ImageStateCache(filter));
        liveState.setFlag(Flags.IMAGE_FILTERS_PARTIAL_UPDATE, true);
    }

    @Override
    public void onImageFilterRemoved(ObservableImageFilter filter) {
        filterCache.remove(filter).destroy();
        liveState.setFlag(Flags.IMAGE_FILTERS_FULL_UPDATE, true);
    }

    @Override
    public void onImageFilterPropertyChanged(ObservableImageFilter filter, Observable property) {
        filterCache.get(filter).isDirty = true;
        liveState.setFlag(Flags.IMAGE_FILTERS_PARTIAL_UPDATE, true);
    }


    ////////////////////////////////////////////////////////

    // Canvas Listener

    @Override
    public void onCanvasPropertyChanged(ObservableCanvas canvas, Observable property) {
        liveState.setFlag(Flags.CANVAS_CHANGED, true);
    }


    ////////////////////////////////////////////////////////

    private void markDirty(){
        filterCache.values().forEach(ImageStateCache::markDirty);
    }

    private boolean isCachedImageCompatible(BufferedImage src, BufferedImage dst){
        return src.getWidth() == dst.getWidth() && src.getHeight() == dst.getHeight() && src.getType() == dst.getType();
    }

    private static class ImageStateCache {
        public BufferedImage bufferedImage = null;
        public boolean isDirty;

        public ImageStateCache(ObservableImageFilter filter){
            this.isDirty = filter.enable.get(); //if the filter is enabled mark it dirty, if not we don't need to mark updates
        }

        public void destroy(){
            bufferedImage = null;
            isDirty = false;
        }

        public void markDirty(){
            isDirty = true;
        }
    }

    ////////////////////////////////////////////////////////

    private final BooleanProperty enabled = new SimpleBooleanProperty(false);

    public boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ImageFilterSettings> imageSettings = new SimpleObjectProperty<>();

    public ImageFilterSettings getImageSettings() {
        return imageSettings.get();
    }

    public ObjectProperty<ImageFilterSettings> imageSettingsProperty() {
        return imageSettings;
    }

    public void setImageSettings(ImageFilterSettings imageSettings) {
        this.imageSettings.set(imageSettings);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ImageData> imageData = new SimpleObjectProperty<>();

    public ImageData getImageData() {
        return imageData.get();
    }

    public ObjectProperty<ImageData> imageDataProperty() {
        return imageData;
    }

    public void setImageData(ImageData imageData) {
        this.imageData.set(imageData);
    }

    ///////////////////////////////////////

    public ObjectProperty<ICanvas> targetCanvas = new SimpleObjectProperty<>();

    public ICanvas getTargetCanvas() {
        return targetCanvas.get();
    }

    public ObjectProperty<ICanvas> targetCanvasProperty() {
        return targetCanvas;
    }

    public void setTargetCanvas(ICanvas targetCanvas) {
        this.targetCanvas.set(targetCanvas);
    }

    ///////////////////////////////////////

    public ObjectProperty<BufferedImage> filteredImage = new SimpleObjectProperty<>();

    public BufferedImage getFilteredImage() {
        return filteredImage.get();
    }

    public ReadOnlyObjectProperty<BufferedImage> filteredImageProperty() {
        return filteredImage;
    }

    public void setFilteredImage(BufferedImage filteredImage) {
        this.filteredImage.set(filteredImage);
    }

    ///////////////////////////////////////

    public ObjectProperty<ICanvas> filteredCanvas = new SimpleObjectProperty<>();

    public ICanvas getFilteredCanvas() {
        return filteredCanvas.get();
    }

    public ObjectProperty<ICanvas> filteredCanvasProperty() {
        return filteredCanvas;
    }

    public void setFilteredCanvas(ICanvas filteredCanvas) {
        this.filteredCanvas.set(filteredCanvas);
    }

    ///////////////////////////////////////

    public Service<BufferedImage> imageFilteringService;

    public Service<BufferedImage> getImageFilteringService(){
        if(imageFilteringService == null){
            imageFilteringService = new Service<>() {
                @Override
                protected Task<BufferedImage> createTask() {
                    return new Task<>() {
                        @Override
                        protected BufferedImage call() {
                            setFilteredImage(null);
                            setFilteredCanvas(null);
                            if(getImageData() == null){
                                return null;
                            }
                            SimpleCanvas canvas = new SimpleCanvas(getTargetCanvas());
                            BufferedImage result = doUpdate(canvas, IProgressCallback.NULL);
                            setFilteredCanvas(getImageData().createImageTargetCanvas(canvas));
                            setFilteredImage(result);
                            return result;
                        }
                    };
                }
            };
        }
        return imageFilteringService;
    }

}
