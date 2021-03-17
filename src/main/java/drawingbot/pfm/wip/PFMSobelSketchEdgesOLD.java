package drawingbot.pfm.wip;

import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.GrayscaleFilter;
import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GLine;
import drawingbot.image.filters.SplitEdgeFilter;
import drawingbot.pfm.AbstractSketchPFM;
import drawingbot.utils.Utils;
import org.joml.Vector2d;

import java.awt.image.BufferedImage;
import java.util.List;

public class PFMSobelSketchEdgesOLD extends AbstractSketchPFM {

    public boolean enableShading;
    public int squigglesTillShading;
    public int startAngleMin;
    public int startAngleMax;
    public float drawingDeltaAngle;
    public float shadingDeltaAngle;

    public BufferedImage imageSobelH;
    public BufferedImage imageSobelV;

    @Override
    public BufferedImage preFilter(BufferedImage image) {
        BufferedImage filtered = new GrayscaleFilter().filter(image, null);
        SplitEdgeFilter sobelFilter = new SplitEdgeFilter();
        sobelFilter.setEdgeMatrix(EdgeFilter.SOBEL_H);
        imageSobelH = sobelFilter.filter(filtered, null);
        sobelFilter.setEdgeMatrix(EdgeFilter.SOBEL_V);
        imageSobelV = sobelFilter.filter(filtered, null);
        return image;
    }
    @Override
    public void init(IPlottingTask task) {
        super.init(task);
        if(startAngleMax < startAngleMin){
            int value = startAngleMin;
            startAngleMin = startAngleMax;
            startAngleMax = value;
        }
    }

    @Override
    public void doProcess(IPlottingTask task) {

        List<int[]> darkestPixels = findDarkestPixels(task.getPixelData());

        for(int[] pixel : darkestPixels){
            x = pixel[0];
            y = pixel[1];
            double hSobel = ((imageSobelH.getRGB(x, y)>>16)&0xff);//TODO GET AVERAGE ANGLE INSTEAD?
            double vSobel = ((imageSobelV.getRGB(x, y)>>16)&0xff);
            double alpha = Math.atan2(hSobel, vSobel);
            double grad_mag = (hSobel) + (vSobel);

            Vector2d vertX = new Vector2d(-0.5, 0.5);
            Vector2d vertY = new Vector2d(0, 0);

            vertY.add(randomSeed(-0.01F, 0.01F), randomSeed(-0.01F,0.01F));

            double lineLength = Math.max(grad_mag, 1);
            vertX.mul(lineLength);
            ///add randomness

            //rotate line

            Vector2d p1  = rotate_origin(vertX, alpha);
            Vector2d p2  = rotate_origin(vertY, alpha);

            vertX = new Vector2d(p1.x(), p2.x()).add(x, x);
            vertY = new Vector2d(p1.y(), p2.y()).add(y, y);



            int x1 = Utils.clamp((int)vertX.x(), 0, task.getPixelData().getWidth()-1);
            int y1 = Utils.clamp((int)vertY.x(), 0, task.getPixelData().getHeight()-1);
            int x2 = Utils.clamp((int)vertX.y(), 0, task.getPixelData().getWidth()-1);
            int y2 = Utils.clamp((int)vertY.y(), 0, task.getPixelData().getHeight()-1);

            int rgba = adjustLuminanceLine(task, task.getPixelData(), x1, y1, x2, y2, adjustbrightness);
            task.addGeometry(new GLine(x1, y1, x2, y2), null, rgba);

            if(updateProgress(task) || task.isFinished()){
                task.finishProcess();
            }
        }
    }

    public Vector2d rotate_origin(Vector2d xy, double radians){
        double rotateX = (xy.x() * Math.cos(radians)) + (xy.y() * Math.sin(radians));
        double rotateY = -(xy.x() * Math.sin(radians)) + (xy.y() * Math.cos(radians));
        return new Vector2d(rotateX, rotateY);
    }

    @Override
    public void findDarkestNeighbour(IPixelData pixels, int start_x, int start_y) {
        resetLuminanceTest();
        float delta_angle;
        float start_angle = 0;

            //delta_angle = drawingDeltaAngle / (float)tests;

        //for (int d = 0; d < tests; d ++) {
        //}
    }
}
