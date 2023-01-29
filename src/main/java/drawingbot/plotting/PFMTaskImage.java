package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPFMImage;
import drawingbot.api.IPixelData;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.ImageTools;
import drawingbot.image.format.FilteredImageData;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.Register;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.util.List;

public class PFMTaskImage extends PFMTask {

    // PIXEL DATA \\
    public IPixelData pixelDataReference;
    public IPixelData pixelDataPlotting;

    public FilteredImageData imageData;

    @Nullable
    public ImageFilterSettings imgFilterSettings;
    public boolean enableImageFiltering = true;

    public PFMTaskImage(DBTaskContext context, PlottedDrawing drawing, PFMFactory<?> pfmFactory, ObservableDrawingSet drawingPenSet, List<GenericSetting<?, ?>> pfmSettings, @Nullable ImageFilterSettings imgFilterSettings, FilteredImageData imageData){
        super(context, drawing, pfmFactory, drawingPenSet, pfmSettings);
        this.imgFilterSettings = imgFilterSettings;
        this.imageData = imageData;
        this.drawing.setMetadata(Register.INSTANCE.ORIGINAL_FILE, imageData.getSourceFile());
        this.drawing.setMetadata(Register.INSTANCE.ORIGINAL_IMAGE, imageData.getSourceImage());
    }

    public IPFMImage pfm(){
        return (IPFMImage) super.pfm();
    }

    @Override
    public void preProcessImages() {
        super.preProcessImages();

        BufferedImage imgPlotting = null;

        if(enableImageFiltering){
            DrawingBotV3.logger.fine("Applying Cropping");
            updateMessage("Pre-Processing - Cropping");

            if(imgFilterSettings != null){
                if(imageData.isVectorImage()){
                    updateMessage("Pre-Processing - Rasterizing from Vector");
                }

                imgPlotting = imageData.createCroppedImage(drawing.getCanvas(), imgFilterSettings);

                DrawingBotV3.logger.fine("Applying Filters");
                for(ObservableImageFilter filter : imgFilterSettings.currentFilters.get()){
                    if(filter.enable.get()){
                        BufferedImageOp instance = filter.filterFactory.instance();
                        filter.filterSettings.forEach(setting -> setting.applySetting(instance));

                        updateMessage("Pre-Processing - " + filter.name.getValue());
                        imgPlotting = instance.filter(imgPlotting, null);
                    }
                }
            }
        }

        if(imgPlotting == null){
            DrawingBotV3.logger.fine("Copying Original Image");
            imgPlotting = ImageTools.deepCopy(imageData.createPreCroppedImage());
        }

        if(pfm.getPlottingResolution() != 1 && enablePlottingResolution){
            updateMessage("Pre-Processing - Resize");
            imgPlotting = Scalr.resize(imgPlotting, Scalr.Method.ULTRA_QUALITY, (int)(imgPlotting.getWidth() * pfm().getPlottingResolution()), (int)(imgPlotting.getHeight()* pfm().getPlottingResolution()));
        }

        DrawingBotV3.logger.fine("Creating Reference Image");
        pixelDataReference = pfm().createPixelData(imgPlotting.getWidth(), imgPlotting.getHeight());
        pixelDataReference.loadData(imgPlotting);
        this.drawing.setMetadata(Register.INSTANCE.REFERENCE_IMAGE, imgPlotting);

        DrawingBotV3.logger.fine("Creating Plotting Image");
        imgPlotting = pfm().preFilter(imgPlotting);
        pixelDataPlotting = pfm().createPixelData(imgPlotting.getWidth(), imgPlotting.getHeight());
        pixelDataPlotting.setTransparentARGB(pfm().getTransparentARGB());
        pixelDataPlotting.loadData(imgPlotting);
        this.drawing.setMetadata(Register.INSTANCE.PLOTTING_IMAGE, imgPlotting);
    }

    @Override
    public void postProcessImages() {
        super.postProcessImages();
        updateMessage("Post-Processing - Converting Reference Images");
        if(pixelDataPlotting != null){
            this.drawing.setMetadata(Register.INSTANCE.PLOTTING_IMAGE, ImageTools.getBufferedImage(pixelDataPlotting));
        }
    }

    @Override
    public void reset(){
        super.reset();
        imageData = null;

        pixelDataReference = null;
        pixelDataPlotting = null;
    }

    public int getBestPen(int x, int y){
        return 0;
    }

    public int getBestMatchedPen(int argb){
        return 0;
    }

    public File getImageFile(){
        return imageData.getSourceFile();
    }

    public FilteredImageData getImageData(){
        return imageData;
    }

    public IPixelData getPixelData() {
        return pixelDataPlotting;
    }

    public IPixelData getReferencePixelData() {
        return pixelDataReference;
    }

    public BufferedImage getOriginalImage() {
        return drawing.getOriginalImage();
    }

    public BufferedImage getReferenceImage() {
        return drawing.getReferenceImage();
    }

    public BufferedImage getPlottingImage() {
        return drawing.getPlottingImage();
    }

}
