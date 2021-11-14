package drawingbot.pfm;

import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.PointFilter;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.ImageTools;
import drawingbot.image.blend.BlendComposite;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.plotting.PlottingTask;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PFMMosaicCustom extends AbstractMosaicPFM {

    public String maskImagePath;
    public BufferedImage maskImage;
    public float featherRadius;

    @Override
    public void createMosaicTasks() {

        try {
            maskImage = BufferedImageLoader.loadImage(maskImagePath, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage scaledMaskImage =  maskImage != null ? Scalr.resize(maskImage, Scalr.Method.SPEED, task.imgPlotting.getWidth(),  task.imgPlotting.getHeight()) : null;

        int tileCount = activeStyles.size();

        for(int i = 0; i < tileCount; i ++){
            nextDrawingStyle();

            BufferedImage tileImage;

            if(maskImage == null || currentDrawingStyle.getMaskColor() == null || currentDrawingStyle.getMaskColor().getOpacity() == 0){
                tileImage = task.imgPlotting;
            }else{
                tileImage = new BufferedImage(task.imgPlotting.getWidth(), task.imgPlotting.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics2D = tileImage.createGraphics();

                graphics2D.drawImage(task.imgPlotting, null, 0, 0);
                graphics2D.setComposite(BlendComposite.getInstance(EnumBlendMode.LIGHTEN));

                int maskRGB = ImageTools.getARGBFromColor(currentDrawingStyle.maskColor);
                int white = ImageTools.getARGB(255, 255, 255, 255);
                int black = ImageTools.getARGB(255, 0, 0, 0);

                BufferedImage tileMask = new PointFilter() {
                    @Override
                    public int filterRGB(int x, int y, int rgb) {
                        return rgb == maskRGB ? black : white;
                    }
                }.filter(scaledMaskImage, null);

                graphics2D.drawImage(tileMask, new GaussianFilter(featherRadius), 0, 0);

                graphics2D.dispose();
            }

            PlottingTask tileTask = new PlottingTask(currentDrawingStyle.getFactory(), currentStyleSettings, evenlyDistributedDrawingSet, tileImage, task.originalFile);
            tileTask.isSubTask = true;
            tileTask.enableImageFiltering = false;
            mosaicTasks.add(new AbstractMosaicPFM.MosaicTask(currentDrawingStyle, tileTask, AffineTransform.getTranslateInstance(0, 0)));
        }
    }

    @Override
    public int calculateTileCount() {
        return activeStyles.size();
    }
}
