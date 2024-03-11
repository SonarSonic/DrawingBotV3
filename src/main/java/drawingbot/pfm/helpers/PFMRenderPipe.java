package drawingbot.pfm.helpers;

import drawingbot.api.IPixelData;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.image.PixelDataAdditiveComposite;
import drawingbot.image.PixelDataComposite;
import drawingbot.image.PixelDataGraphicsComposite;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.utils.EnumRescaleMode;
import drawingbot.utils.Utils;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * A basic class used for the rendering of geometries when erasing, having it as a seperate class allows for different implementations independent of the PFM.
 */
public class PFMRenderPipe {

    private BasicStroke defaultStroke = null;
    private Color defaultColor = null;
    public BresenhamHelper bresenhamHelper = new BresenhamHelper();
    public EnumRescaleMode rescaleMode = DBPreferences.INSTANCE.defaultRescalingMode.get();
    public RenderPipeSampleTest sampleTest = new RenderPipeSampleTest();

    public void setRescaleMode(EnumRescaleMode rescaleMode) {
        this.rescaleMode = rescaleMode;
    }

    public BasicStroke getDefaultStroke(float lineWidth){
        lineWidth = Math.abs(lineWidth);
        if(defaultStroke == null || defaultStroke.getLineWidth() != lineWidth){
            defaultStroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        return defaultStroke;
    }

    public Color getDefaultEraseColor(int adjust){
        if(defaultColor == null || defaultColor.getRed() != adjust){
            defaultColor = new Color(adjust, adjust, adjust, 255);
        }
        return defaultColor;
    }

    public Color getDefaultEraseColorAlpha(int adjust){
        if(defaultColor == null || defaultColor.getAlpha() != adjust){
            defaultColor = new Color(255, 255, 255, adjust);
        }
        return defaultColor;
    }


    public int eraseGeometry(IPixelData pixelData, IPixelData reference, IGeometry geometry, int adjust, float lineWidth){
        return eraseGeometry(pixelData, reference, geometry, adjust, lineWidth, this::defaultEraseFunction); //FIX COLOUR SAMPLES
    }

    public int eraseGeometry(IPixelData pixelData, IPixelData reference, IGeometry geometry, int adjust, float lineWidth, PixelDataComposite.ICompositeFunction function){
        int colourSamples = -1;
        if(pixelData instanceof PixelDataGraphicsComposite data){
            //HQ method: using Graphics2D implementation, slower but supports anti aliased lines and lineWidth.
            Graphics2D graphics2D = data.getGraphics2D();
            data.enableBlending(function);
            graphics2D.setStroke(getDefaultStroke(lineWidth));
            graphics2D.setColor(getDefaultEraseColorAlpha(adjust));
            graphics2D.draw(geometry.getAWTShape());
            data.disableBlending();

            // Add Colour Samples to the Geometry
            sampleTest.resetColourSamples(0); //make sure we don't alter the pixel data twice
            sampleTest.setPixelDataTargets(reference, null);
            bresenhamHelper.plotShape(geometry.getAWTShape(), sampleTest);//Erase the geometry and gather colour samples
            colourSamples = sampleTest.getCurrentAverage();
        }else if(pixelData instanceof PixelDataAdditiveComposite data) {
            // Setup color samples, with this method, we perform both simultaneously erasing/sampling together
            sampleTest.resetColourSamples(0); //make sure we don't alter the pixel data twice
            sampleTest.setPixelDataTargets(reference, null);

            data.preDraw();
            data.getCacheGraphics().setStroke(getDefaultStroke(lineWidth));
            data.getCacheGraphics().setColor(getDefaultEraseColor(adjust));
            geometry.renderAWT(data.getCacheGraphics());
            data.postDraw(sampleTest);

            colourSamples = sampleTest.getCurrentAverage();
        }else{
            //original method: using bresenham, fast but with non-anti aliased lines and no support for lineWidth.
            sampleTest.resetColourSamples(adjust);
            sampleTest.setPixelDataTargets(reference, pixelData);
            geometry.renderBresenham(bresenhamHelper, sampleTest);//Erase the geometry and gather colour samples
            colourSamples = sampleTest.getCurrentAverage();
        }
        return colourSamples;
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

    /**
     * Simple colour sample test which gets colour samples from the reference data and erases the pixel data
     */
    public static class RenderPipeSampleTest extends ColourSampleTest implements BresenhamHelper.IPixelSetter {
        private IPixelData referenceData;
        private IPixelData pixelData;

        public void setPixelDataTargets(IPixelData referenceData, IPixelData pixelData){
            this.referenceData = referenceData;
            this.pixelData = pixelData;
        }

        @Override
        public void setPixel(int x, int y) {
            if(isPixelInvalid(referenceData, x, y)){
                return;
            }
            sum_alpha += referenceData.getAlpha(x, y);
            sum_red += referenceData.getRed(x, y);
            sum_green += referenceData.getGreen(x, y);
            sum_blue += referenceData.getBlue(x, y);
            if(adjustLum != 0 && pixelData != null){
                int red = Utils.clamp(pixelData.getRed(x, y)+adjustLum, 0, 255);
                int green = Utils.clamp(pixelData.getGreen(x, y)+adjustLum, 0, 255);
                int blue = Utils.clamp(pixelData.getBlue(x, y)+adjustLum, 0, 255);
                int alpha = pixelData.getAlpha(x, y);
                pixelData.setARGB(x, y, alpha, red, green, blue);
            }
            total_pixels++;
        }

        public void destroy(){

        }
    }
}
