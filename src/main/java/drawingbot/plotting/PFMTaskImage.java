package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPFMImage;
import drawingbot.api.IPixelData;
import drawingbot.geom.GeometryUtils;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.FilteredBufferedImage;
import drawingbot.image.ImageTools;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.Register;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.awt.ShapeReader;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.util.List;

public class PFMTaskImage extends PFMTask {

    public File originalImageFile;

    // PIXEL DATA \\
    public IPixelData pixelDataReference;
    public IPixelData pixelDataPlotting;

    @Nullable
    public ImageFilterSettings imgFilterSettings;
    public boolean enableImageFiltering = true;

    public PFMTaskImage(IDrawingManager taskManager, PlottedDrawing drawing, PFMFactory<?> pfmFactory, ObservableDrawingSet drawingPenSet, List<GenericSetting<?, ?>> pfmSettings, @Nullable ImageFilterSettings imgFilterSettings, BufferedImage image, @Nullable File originalImageFile){
        super(taskManager, drawing, pfmFactory, drawingPenSet, pfmSettings);
        this.originalImageFile = originalImageFile;
        if(originalImageFile != null){
            this.drawing.setMetadata(Register.INSTANCE.ORIGINAL_FILE, originalImageFile);
        }
        this.imgFilterSettings = imgFilterSettings;
        this.drawing.setMetadata(Register.INSTANCE.ORIGINAL_IMAGE, image);
    }

    public IPFMImage pfm(){
        return (IPFMImage) super.pfm();
    }

    @Override
    public void preProcessImages() {
        super.preProcessImages();

        BufferedImage imgPlotting;

        DrawingBotV3.logger.fine("Copying Original Image");
        imgPlotting = ImageTools.deepCopy(drawing.getOriginalImage());

        if(enableImageFiltering){
            DrawingBotV3.logger.fine("Applying Cropping");
            updateMessage("Pre-Processing - Cropping");

            if(imgFilterSettings != null){
                imgPlotting = FilteredBufferedImage.applyCropping(imgPlotting, drawing.getCanvas(), imgFilterSettings);

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

            updateMessage("Pre-Processing - Resize");
            imgPlotting = Scalr.resize(imgPlotting, Scalr.Method.ULTRA_QUALITY, (int)(imgPlotting.getWidth() * pfm().getPlottingResolution()), (int)(imgPlotting.getHeight()* pfm().getPlottingResolution()));
        }
        imgPlotting = pfm().preFilter(imgPlotting);

        DrawingBotV3.logger.fine("Creating Pixel Data");
        pixelDataReference = ImageTools.newPixelData(imgPlotting.getWidth(), imgPlotting.getHeight(), pfm().getColourMode());
        pixelDataPlotting = ImageTools.newPixelData(imgPlotting.getWidth(), imgPlotting.getHeight(), pfm().getColourMode());
        pixelDataPlotting.setTransparentARGB(pfm().getTransparentARGB());

        DrawingBotV3.logger.fine("Setting Pixel Data");
        ImageTools.copyToPixelData(imgPlotting, pixelDataReference);
        ImageTools.copyToPixelData(imgPlotting, pixelDataPlotting);

        tools.setClippingShape(tools.getClippingShape() != null ? tools.getClippingShape() : ShapeReader.read(new Rectangle2D.Double(0, -imgPlotting.getHeight(), imgPlotting.getWidth(), imgPlotting.getHeight()), 6F, GeometryUtils.factory));

        this.drawing.setMetadata(Register.INSTANCE.REFERENCE_IMAGE, imgPlotting);
        this.drawing.setMetadata(Register.INSTANCE.PLOTTING_IMAGE, imgPlotting);
    }

    @Override
    public void postProcessImages() {
        super.postProcessImages();
        updateMessage("Post-Processing - Converting Reference Images");
        this.drawing.setMetadata(Register.INSTANCE.PLOTTING_IMAGE, ImageTools.getBufferedImage(pixelDataPlotting));
    }

    @Override
    public void reset(){
        super.reset();
        originalImageFile = null;

        pixelDataReference = null;
        pixelDataPlotting = null;
    }

    public File getImageFile(){
        return originalImageFile;
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