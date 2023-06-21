package drawingbot.pfm;

import drawingbot.api.IPlottingTools;
import drawingbot.geom.shapes.GLine;
import drawingbot.geom.shapes.GRectangle;
import drawingbot.geom.shapes.GShape;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class PFMTest extends AbstractPFMImage {

    @Override
    public void run() {
        tools.addGeometry(new GRectangle(0.5F, 0.5F, 10, 10));
        tools.addGeometry(new GRectangle(-0.5F + tools.getPlottingWidth()-12, 0.5F, 10, 10));
        tools.addGeometry(new GRectangle(0.5F, -0.5F + tools.getPlottingHeight()-12, 10, 10));
        tools.addGeometry(new GRectangle(-0.5F + tools.getPlottingWidth()-12, -0.5F + tools.getPlottingHeight()-12, 10, 10));

        tools.addGeometry(new GLine(-10 + tools.getPlottingWidth()/2F, tools.getPlottingHeight()/2F, 10 + tools.getPlottingWidth()/2F, tools.getPlottingHeight()/2F));

        tools.addGeometry(new GLine(tools.getPlottingWidth()/2F, -10 + tools.getPlottingHeight()/2F, tools.getPlottingWidth()/2F, 10 + tools.getPlottingHeight()/2F));

        Graphics2D graphics2D = new BufferedImage(tools.getPlottingWidth(), tools.getPlottingHeight(), BufferedImage.TYPE_INT_ARGB).createGraphics();

        drawTextAsGeometry(tools,"TL", 1+16, 1 + 64, 64, graphics2D);
        drawTextAsGeometry(tools,"TR", tools.getPlottingWidth() - 104, 1 + 64, 64, graphics2D);
        drawTextAsGeometry(tools,"BL", 1+16, tools.getPlottingHeight() - 32, 64, graphics2D);
        drawTextAsGeometry(tools,"BR", tools.getPlottingWidth() - 104, tools.getPlottingHeight() - 32, 64, graphics2D);

    }

    public static void drawTextAsGeometry(IPlottingTools tools, String text, float x, float y, float size, Graphics2D graphics2D){
        Font baseFont = graphics2D.getFont().deriveFont(size);
        GlyphVector top = baseFont.createGlyphVector(graphics2D.getFontRenderContext(), text);
        top.setGlyphTransform(0, AffineTransform.getTranslateInstance(x, y));
        tools.addGeometry(new GShape(top.getOutline()));
    }
}
