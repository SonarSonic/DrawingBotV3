package drawingbot.pfm;

import drawingbot.geom.shapes.GLine;
import drawingbot.geom.shapes.GRectangle;
import drawingbot.geom.shapes.GShape;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

public class PFMTest extends AbstractPFM {

    @Override
    public void doProcess() {
        task.addGeometry(new GRectangle(0.5F, 0.5F, 10, 10));
        task.addGeometry(new GRectangle(-0.5F + task.getPixelData().getWidth()-12, 0.5F, 10, 10));
        task.addGeometry(new GRectangle(0.5F, -0.5F + task.getPixelData().getHeight()-12, 10, 10));
        task.addGeometry(new GRectangle(-0.5F + task.getPixelData().getWidth()-12, -0.5F + task.getPixelData().getHeight()-12, 10, 10));

        task.addGeometry(new GLine(-10 + task.getPixelData().getWidth()/2F, task.getPixelData().getHeight()/2F, 10 + task.getPixelData().getWidth()/2F, task.getPixelData().getHeight()/2F));

        task.addGeometry(new GLine(task.getPixelData().getWidth()/2F, -10 + task.getPixelData().getHeight()/2F, task.getPixelData().getWidth()/2F, 10 + task.getPixelData().getHeight()/2F));

        Graphics2D graphics2D = task.imgPlotting.createGraphics();

        drawTextAsGeometry("TL", 1+16, 1 + 64, 64, graphics2D);
        drawTextAsGeometry("TR", task.getPixelData().getWidth() - 104, 1 + 64, 64, graphics2D);
        drawTextAsGeometry("BL", 1+16, task.getPixelData().getHeight() - 32, 64, graphics2D);
        drawTextAsGeometry("BR", task.getPixelData().getWidth() - 104, task.getPixelData().getHeight() - 32, 64, graphics2D);

        task.finishProcess();
    }

    public void drawTextAsGeometry(String text, int x, int y, float size, Graphics2D graphics2D){
        Font baseFont = graphics2D.getFont().deriveFont(size);
        GlyphVector top = baseFont.createGlyphVector(graphics2D.getFontRenderContext(), text);
        top.setGlyphTransform(0, AffineTransform.getTranslateInstance(x, y));
        task.addGeometry(new GShape(top.getOutline()));
    }
}
