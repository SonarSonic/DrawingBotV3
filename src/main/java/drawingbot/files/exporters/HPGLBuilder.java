package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.EnumRotation;
import drawingbot.utils.Limit;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/// W.I.P
public class HPGLBuilder {

    public final PlottingTask task;
    private final PrintWriter output;
    public final EnumRotation hpglRotation;

    public boolean isPenDown;
    public float xOffset, yOffset;

    ///tallies
    public float distanceMoved;
    public float distanceDown;
    public float distanceUp;
    public int pointsDrawn;
    public int penLifts;
    public int penDrops;

    public float lastX = 0, lastY = 0;
    public float lastMoveX = 0, lastMoveY = 0;
    public Limit dx = new Limit(), dy = new Limit();

    public HPGLBuilder(PlottingTask task, PrintWriter output, EnumRotation hpglRotation) {
        this.task = task;
        this.output = output;
        this.hpglRotation = hpglRotation;
    }

    public float getEffectivePageWidth(){
        return hpglRotation.flipAxis ? task.resolution.getPrintPageHeight() : task.resolution.getPrintPageWidth();
    }

    public float getEffectivePageHeight(){
        return hpglRotation.flipAxis ? task.resolution.getPrintPageWidth() : task.resolution.getPrintPageHeight();
    }

    public void open() {

        int plotterWidthHPGL = Math.abs(DrawingBotV3.INSTANCE.hpglXMin.get()) + DrawingBotV3.INSTANCE.hpglXMax.get();
        int plotterHeightHPGL = Math.abs(DrawingBotV3.INSTANCE.hpglYMin.get()) + DrawingBotV3.INSTANCE.hpglYMax.get();

        xOffset = 0;
        yOffset = 0;

        if(plotterWidthHPGL != 0){
            float plotterWidthScaled = HPGLBuilder.fromHPGL(plotterWidthHPGL);
            float drawingWidthScaled = getEffectivePageWidth();
            switch (DrawingBotV3.INSTANCE.hpglAlignX.get()){
                case CENTER:
                    xOffset = (plotterWidthScaled - drawingWidthScaled)/2;
                    break;
                case LEFT:
                    xOffset = 0;
                    break;
                case RIGHT:
                    xOffset = plotterWidthScaled - drawingWidthScaled;
                    break;
            }
        }


        if(plotterHeightHPGL != 0){
            float plotterHeightScaled = HPGLBuilder.fromHPGL(plotterHeightHPGL);
            float drawingHeightScaled = getEffectivePageHeight();
            switch (DrawingBotV3.INSTANCE.hpglAlignY.get()){
                case CENTER:
                    yOffset = (plotterHeightScaled - drawingHeightScaled)/2;
                    break;
                case BOTTOM:
                    yOffset = 0;
                    break;
                case TOP:
                    yOffset = plotterHeightScaled - drawingHeightScaled;
                    break;
            }
        }

        command(HPGLDictionary.INITIALIZE);
        command(HPGLDictionary.DEFAULT_VALUES);
        command(HPGLDictionary.PEN_UP);

        if(DrawingBotV3.INSTANCE.hpglPenSpeed.get() != 0){
            command(HPGLDictionary.SELECT_VELOCITY_FOR_PEN, DrawingBotV3.INSTANCE.hpglPenSpeed.get());
        }
    }

    /**
     * Must be called to save the file
     */
    public void close() {
        movePenUp();
        command(HPGLDictionary.SELECT_PEN, 0);
        command(HPGLDictionary.INITIALIZE);

        output.flush();
        output.close();

        DrawingBotV3.logger.info("HPGL File: " + "X Range: " + dx.min + ", " + dx.max + " Y Range: " + dy.min + ", " + dy.max);
    }

    public void movePenUp() {
        if (isPenDown) {
            flushMove();
            isPenDown = false;
            penLifts++;
        }
    }

    public void movePenDown() {
        if (!isPenDown) {
            flushMove();
            isPenDown = true;
            penDrops++;
        }
    }

    public static List<String> moves = new ArrayList<>();

    public void flushMove(){
        if(!moves.isEmpty()){
            command(isPenDown ? HPGLDictionary.PEN_DOWN : HPGLDictionary.PEN_UP, moves.toArray());
            moves.clear();
        }
    }

    public void startLayer(int pen) {
        command(HPGLDictionary.SELECT_PEN, pen);
    }

    public void endLayer(int pen) {
        movePenUp();
    }

    public void move(float[] coords, int type) {
        switch (type) {
            case PathIterator.SEG_MOVETO:
                movePenUp();
                move(coords[0], coords[1]);
                movePenDown();
                break;
            case PathIterator.SEG_LINETO:
                move(coords[0], coords[1]);
                break;
            case PathIterator.SEG_QUADTO:
                //quadCurveG5(coords[0], coords[1], coords[2], coords[3]);
                break;
            case PathIterator.SEG_CUBICTO:
                //bezierCurveG5(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                break;
            case PathIterator.SEG_CLOSE:
                move(lastMoveX, lastMoveY);
                movePenUp();
                break;
        }
    }

    public void move(float xValue, float yValue) {
        int hpglXValue = getHPGLXValue(xValue, false);
        int hpglYValue = getHPGLYValue(yValue, false);
        moves.add(String.valueOf(hpglXValue));
        moves.add(String.valueOf(hpglYValue));
        logMove(hpglXValue, hpglYValue);
        lastMoveX = xValue;
        lastMoveY = yValue;
    }

    public void logMove(float xValue, float yValue){
        dx.update_limit(xValue);
        dy.update_limit(yValue);

        double distance = Point2D.distance(lastX, lastY, xValue, yValue);

        distanceMoved += distance;
        distanceUp += !isPenDown ? distance : 0;
        distanceDown += isPenDown ? distance : 0;

        if (isPenDown) {
            pointsDrawn++;
        }

        lastX = xValue;
        lastY = yValue;
    }

    public void command(String command, Object...values){
        if(values.length == 0){
            output.print(command + ";");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < values.length; i++){
            builder.append(i == 0 ? command : ",");
            builder.append(values[i]);
            //TODO if value is an array make it display with brackets.
        }
        builder.append(";");
        output.print(builder.toString());
    }

    public static float getHPGLUnits(){
        return DrawingBotV3.INSTANCE.hpglUnits.get();
    }

    public static int toHPGL(float f){
        return (int) (f * getHPGLUnits());
    }
    public static float fromHPGL(int f){
        return (float) f / getHPGLUnits();
    }

    public int getHPGLXValue(float f, boolean skipMirror){
        float xValue = f;
        if(!skipMirror && DrawingBotV3.INSTANCE.hpglXAxisMirror.getValue()){
            xValue = task.getResolution().getPrintPageWidth()-f;
        }
        xValue += xOffset; //move by x offset
        xValue *= getHPGLUnits(); // scale into hpgl units
        xValue += DrawingBotV3.INSTANCE.hpglXMin.get(); //now we're in HPGL scaling, add the offset set by the plotter
        return (int)xValue;
    }

    public int getHPGLYValue(float f, boolean skipMirror){
        float yValue = f;
        if(!skipMirror && !DrawingBotV3.INSTANCE.hpglYAxisMirror.getValue()){ //note y-axis is actually flipped by default, to match standard HP coordinates
            yValue = task.getResolution().getPrintPageHeight()-f;
        }
        yValue += yOffset; // move by y offset
        yValue *= getHPGLUnits(); //scale to HPGL units
        yValue += DrawingBotV3.INSTANCE.hpglYMin.get(); //now we're in HPGL scaling, add the offset set by the plotter
        return (int)yValue;
    }

    public boolean withinHPGLRange(){
        return withinHPGLRange((int)dx.min, (int)dy.min, (int)dx.max, (int)dy.max);
    }

    public boolean withinHPGLRange(int minX, int minY, int maxX, int maxY){
        return minX >= DrawingBotV3.INSTANCE.hpglXMin.get() && minY >= DrawingBotV3.INSTANCE.hpglYMin.get() && maxX <= DrawingBotV3.INSTANCE.hpglXMax.get() && maxY <= DrawingBotV3.INSTANCE.hpglYMax.get();
    }

}
