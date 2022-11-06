package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.image.PixelDataComposite;
import drawingbot.image.PixelDataGraphicsComposite;

import java.awt.*;

/**
 * A basic class used for the rendering of geometries when erasing, having it as a seperate class allows for different implementations independent of the PFM.
 */
public class PFMRenderPipe {

    private BasicStroke defaultStroke = null;
    private Color defaultColor = null;
    public BresenhamHelper bresenhamHelper = null;

    public BasicStroke getDefaultStroke(float lineWidth){
        if(defaultStroke == null || defaultStroke.getLineWidth() != lineWidth){
            defaultStroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        return defaultStroke;
    }

    public Color getDefaultEraseColor(int adjust){
        if(defaultColor == null || defaultColor.getAlpha() != adjust){
            defaultColor = new Color(255, 255, 255, adjust);
        }
        return defaultColor;
    }


    public int eraseGeometry(IPixelData pixelData, IGeometry geometry, int adjust, float lineWidth){
        return eraseGeometry(pixelData, geometry, adjust, lineWidth, this::defaultEraseFunction); //FIX COLOUR SAMPLES
    }

    public int eraseGeometry(IPixelData pixelData, IGeometry geometry, int adjust, float lineWidth, PixelDataComposite.ICompositeFunction function){
        if(pixelData instanceof PixelDataGraphicsComposite data){
            Graphics2D graphics2D = data.getGraphics2D();
            data.enableBlending(function);
            graphics2D.setStroke(getDefaultStroke(lineWidth));
            graphics2D.setColor(getDefaultEraseColor(adjust));
            graphics2D.draw(geometry.getAWTShape());
            data.disableBlending();
        }
        return -1; //TODO FIX COLOUR SAMPLES
    }


    public void defaultEraseFunction(int[] foreground, int[] background, int[] result){
        int offset = (int) (foreground[0]*1F); //1.2F offset approximately makes images match previous versions more closely

        result[0] = Math.min(255, background[0] + offset);
        result[1] = Math.min(255, background[1] + offset);
        result[2] = Math.min(255, background[2] + offset);
        result[3] = Math.min(255, background[3] + offset);
    }


    public void luminanceEraseFunction(int[] foreground, int[] background, int[] result){
        int offset = (int) (foreground[0]*1F); //1.2F offset approximately makes images match previous versions more closely

        result[0] = background[0];
        result[1] = Math.min(255, background[1] + offset);
        result[2] = Math.min(255, background[2] + offset);
        result[3] = Math.min(255, background[3] + offset);
    }


    public void alphaEraseFunction(int[] foreground, int[] background, int[] result){
        int offset = (int) (foreground[0]*1F); //1.2F offset approximately makes images match previous versions more closely
        result[0] = Math.max(0, background[0] - offset);
        result[1] = background[1];
        result[2] = background[2];
        result[3] = background[3];

    }

}
