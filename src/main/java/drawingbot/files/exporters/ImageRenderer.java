package drawingbot.files.exporters;

import drawingbot.api.ICanvas;
import drawingbot.files.ExportTask;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.ImageTools;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.plotting.canvas.CanvasUtils;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Produces higher quality images by bypassing the Graphics2D's low quality interpolation and instead uses incremental resizing
 * TODO WHEN USING ORIGINAL SIZING THE EXPORT ARE POOR QUALITY
 */
public class ImageRenderer {

    public final DBTaskContext context;
    public ICanvas canvas;
    public EnumBlendMode blendMode;
    public boolean isVideo;

    public boolean drawBackground;
    public int outputBufferedImageType;

    private Graphics2D graphics;
    private BufferedImage activeImage;

    //the dimensions of the final image
    private int rasterWidth;
    private int rasterHeight;

    //the dimensions of the scaled image which will be rendered onto
    private int scaledWidth;
    private int scaledHeight;
    private double linearScale;

    public ImageRenderer(ExportTask exportTask, boolean isVideo) {
        this(exportTask.context, exportTask.exportDrawing.getCanvas(), exportTask.context.project().blendMode.get(), isVideo, ImageExporter.getOutputBufferedImageType(exportTask), ImageExporter.drawBackgroundOnRaster(exportTask));
    }

    public ImageRenderer(DBTaskContext context, ICanvas canvas, EnumBlendMode blendMode, boolean isVideo, int outputBufferedImageType, boolean drawBackground) {
        this.context = context;
        this.canvas = canvas;
        this.blendMode = blendMode;
        this.isVideo = isVideo;
        this.outputBufferedImageType = outputBufferedImageType;
        this.drawBackground = drawBackground;
    }

    private void setup() {
        rasterWidth = CanvasUtils.getRasterExportWidth(canvas, DBPreferences.INSTANCE.exportDPI.get(), isVideo);
        rasterHeight = CanvasUtils.getRasterExportHeight(canvas, DBPreferences.INSTANCE.exportDPI.get(), isVideo);

        //apply the scaling caused by the new DPI.
        double scale = (double) rasterWidth / canvas.getScaledWidth();
        linearScale = (int) Math.ceil(scale);

        if (linearScale != scale) {
            scaledWidth = (int) (canvas.getScaledWidth() * linearScale);
            scaledHeight = (int) (canvas.getScaledHeight() * linearScale);

            activeImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            graphics = createFreshGraphics2D(context, activeImage, blendMode, isVideo, drawBackground);

            graphics.scale(linearScale, linearScale);
        } else {
            scaledWidth = rasterWidth;
            scaledHeight = rasterHeight;

            activeImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            graphics = createFreshGraphics2D(context, activeImage, blendMode, isVideo, drawBackground);

            graphics.scale(scale, scale);
        }
    }

    public Graphics2D refreshGraphics(){
        if(graphics == null){
            ///write the background then dispose
            setup();
            graphics.dispose();
        }
        graphics = createFreshGraphics2D(context, activeImage, blendMode, isVideo, false);
        graphics.scale(linearScale, linearScale);
        return graphics;
    }

    public void dispose() {
        graphics.dispose();
    }

    public Graphics2D getGraphics() {
        if (graphics == null) {
            setup();
        }
        return graphics;
    }

    public BufferedImage getActiveImage() {
        return activeImage;
    }

    private BufferedImage getScaledImage() {
        if (scaledWidth == rasterWidth && scaledHeight == rasterHeight) {
            return activeImage;
        }
        return Scalr.resize(activeImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, rasterWidth, rasterHeight);
    }

    public BufferedImage createExportImage() {
        BufferedImage exportImage = getScaledImage();
        if(exportImage.getType() == outputBufferedImageType){
            return exportImage;
        }
        BufferedImage convertedImage = new BufferedImage(exportImage.getWidth(), exportImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        ImageTools.drawImage(exportImage, convertedImage);
        return convertedImage;
    }

    public static Graphics2D createFreshGraphics2D(DBTaskContext context, BufferedImage image, EnumBlendMode blendMode, boolean isVideo, boolean drawBackground){
        Graphics2D graphics = image.createGraphics();

        graphics.setBackground(Color.WHITE);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        if(drawBackground){
            Graphics2DExporter.drawBackground(context, graphics, image.getWidth(), image.getHeight());
        }

        graphics.setComposite(blendMode.awtComposite);

        return graphics;
    }

}
