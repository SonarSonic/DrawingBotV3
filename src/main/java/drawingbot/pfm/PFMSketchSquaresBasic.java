package drawingbot.pfm;

import drawingbot.api.IPixelData;

public class PFMSketchSquaresBasic extends AbstractSketchPFM {

    public float startAngle;

    @Override
    public void nextPathFindingResult(PathFindingContext context, IPixelData pixels) {
        float angle = startAngle + (float)Math.toDegrees((Math.sin(Math.toRadians((context.getX()/9D))) + Math.cos(Math.toRadians((context.getY()/9D)+26D))));
        int nextLineLength = tools.randomInt(minLineLength, maxLineLength);
        float avgDarkness = tools.findDarkestLine(pixels, context.getX(), context.getY(), minLineLength, nextLineLength, lineTests, angle, 360.0F, false, context.getDstPosition());
        context.setResult(context.getDstPosition(), avgDarkness);
    }
}