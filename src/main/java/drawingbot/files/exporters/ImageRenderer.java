package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.files.ExportTask;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.blend.BlendComposite;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.plotting.canvas.CanvasUtils;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Produces higher quality images by bypassing the Graphics2D's low quality interpolation and instead uses incremental resizing
 */
public class ImageRenderer {

    public final DBTaskContext context;
    public ICanvas canvas;
    public EnumBlendMode blendMode;
    public boolean isVideo;
    public boolean useAlphaChannel;

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
        this(exportTask.context, exportTask.exportDrawing.getCanvas(), exportTask.context.project().blendMode.get(), isVideo, ImageExporter.useAlphaChannelOnRaster(exportTask));
    }

    public ImageRenderer(DBTaskContext context, ICanvas canvas, EnumBlendMode blendMode, boolean isVideo, boolean useAlphaChannel) {
        this.context = context;
        this.canvas = canvas;
        this.blendMode = blendMode;
        this.isVideo = isVideo;
        this.useAlphaChannel = useAlphaChannel;
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

            activeImage = new BufferedImage(scaledWidth, scaledHeight, useAlphaChannel ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
            graphics = createFreshGraphics2D(context, activeImage, blendMode, isVideo, !useAlphaChannel);

            graphics.scale(linearScale, linearScale);
        } else {
            scaledWidth = rasterWidth;
            scaledHeight = rasterHeight;

            activeImage = new BufferedImage(scaledWidth, scaledHeight, useAlphaChannel ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
            graphics = createFreshGraphics2D(context, activeImage, blendMode, isVideo, !useAlphaChannel);
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

    public BufferedImage createExportImage() {
        if (scaledWidth == rasterWidth && scaledHeight == rasterHeight) {
            return activeImage;
        }
        return Scalr.resize(activeImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, rasterWidth, rasterHeight);
    }


    public static Graphics2D createFreshGraphics2D(DBTaskContext context, BufferedImage image, EnumBlendMode blendMode, boolean isVideo, boolean drawBackground){
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(drawBackground){
            Graphics2DExporter.drawBackground(context, graphics, image.getWidth(), image.getHeight());
        }

        if(blendMode != EnumBlendMode.NORMAL){
            graphics.setComposite(new BlendComposite(blendMode));
        }

        return graphics;
    }

}
