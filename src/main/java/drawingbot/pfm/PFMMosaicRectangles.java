package drawingbot.pfm;

import drawingbot.plotting.PlottingTask;
import org.imgscalr.Scalr;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class PFMMosaicRectangles extends AbstractMosaicPFM{

    public int columns;
    public int rows;
    public int columnPadding;
    public int rowPadding;

    @Override
    public void createMosaicTasks() {
        int tileWidth = task.getPixelData().getWidth() / columns;
        int tileHeight = task.getPixelData().getHeight() / rows;

        int tilePaddingX = (columnPadding*tileWidth)/100;
        int tilePaddingY = (rowPadding*tileHeight)/100;

        for(int y = 0; y < rows; y ++){
            for(int x = 0; x < columns; x ++){
                int tileXPos = (x * tileWidth) + tilePaddingX;
                int tileYPos = (y * tileHeight) + tilePaddingY;

                nextDrawingStyle();

                BufferedImage tileImage = Scalr.crop(task.img_plotting, tileXPos, tileYPos, tileWidth - tilePaddingX *2, tileHeight - tilePaddingY *2);
                PlottingTask tileTask = new PlottingTask(currentDrawingStyle.getFactory(), currentStyleSettings, evenlyDistributedDrawingSet, tileImage, task.originalFile);
                tileTask.isSubTask = true;
                tileTask.enableImageFiltering = false;
                mosaicTasks.add(new AbstractMosaicPFM.MosaicTask(currentDrawingStyle, tileTask, AffineTransform.getTranslateInstance(tileXPos, tileYPos)));
            }
        }
    }

    @Override
    public int calculateTileCount() {
        return rows * columns;
    }
}
