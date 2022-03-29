package drawingbot.plotting;

import drawingbot.DrawingBotV3;
import drawingbot.api.IPFMImage;
import drawingbot.api.IPixelData;
import drawingbot.geom.GeometryUtils;
import drawingbot.api.ICanvas;
import drawingbot.plotting.canvas.ImageCanvas;
import drawingbot.image.FilteredBufferedImage;
import drawingbot.image.ImageTools;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.canvas.SimpleCanvas;
import drawingbot.registry.Register;
import org.imgscalr.Scalr;
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

    public boolean enableImageFiltering = true;

    public PFMTaskImage(ICanvas drawingArea, PFMFactory<?> pfmFactory, List<GenericSetting<?, ?>> pfmSettings, ObservableDrawingSet drawingPenSet, BufferedImage image, File originalImageFile){
        this(drawingArea, pfmFactory, pfmSettings, drawingPenSet, image, DrawingBotV3.INSTANCE.imageRotation.get().flipAxis, originalImageFile);
    }

    public PFMTaskImage(ICanvas drawingArea, PFMFactory<?> pfmFactory, List<GenericSetting<?, ?>> pfmSettings, ObservableDrawingSet drawingPenSet, BufferedImage image, boolean flipAxis, File originalImageFile){
        super(new ImageCanvas(drawingArea, image, flipAxis), pfmFactory, pfmSettings, drawingPenSet);
        this.originalImageFile = originalImageFile;
        this.drawing.setMetadata(Register.INSTANCE.ORIGINAL_FILE, originalImageFile);
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


            imgPlotting = FilteredBufferedImage.applyCropping(imgPlotting, drawing.getCanvas());

            DrawingBotV3.logger.fine("Applying Filters");
            for(ObservableImageFilter filter : DrawingBotV3.INSTANCE.currentFilters){
                if(filter.enable.get()){
                    BufferedImageOp instance = filter.filterFactory.instance();
                    filter.filterSettings.forEach(setting -> setting.applySetting(instance));

                    updateMessage("Pre-Processing - " + filter.name.getValue());
                    imgPlotting = instance.filter(imgPlotting, null);
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
