package drawingbot.geom;

import java.awt.geom.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A path implementation which allows individual commands to have different colours, probably not needed
 */
public class CustomPath {

    //the wrapped AWT path
    public Path2D.Float path;

    public Map<Integer, Integer> penIndexes = new HashMap<>();
    public Map<Integer, Integer> rgbaSamples = new HashMap<>();

    private int length;
    private int lastType = -1;

    public CustomPath() {
        super();
    }

    public void onAdded(int type){
        if(lastType != type || !(type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_CLOSE)){
            length++;
        }
        lastType = type;
    }

    public void moveTo(float x, float y) {
        path.moveTo(x, y);
        onAdded(PathIterator.SEG_MOVETO);
    }

    public void lineTo(float x, float y){
        path.lineTo(x, y);
        onAdded(PathIterator.SEG_LINETO);
    }

    public void quadTo(float x1, float y1, float x2, float y2){
        path.quadTo(x1, y1, x2, y2);
        onAdded(PathIterator.SEG_QUADTO);
    }

    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3){
        path.curveTo(x1, y1, x2, y2, x3, y3);
        onAdded(PathIterator.SEG_CUBICTO);
    }

    public void closePath() {
        path.closePath();
        onAdded(PathIterator.SEG_CLOSE);
    }

    public static class ExtendedPathIterator implements PathIterator {

        public PathIterator iterator;
        public int index;

        public ExtendedPathIterator(PathIterator iterator){
            this.iterator = iterator;
            this.index = 0;
        }

        @Override
        public int getWindingRule() {
            return iterator.getWindingRule();
        }

        @Override
        public boolean isDone() {
            return iterator.isDone();
        }

        @Override
        public void next() {
            iterator.next();
            index++;
        }

        @Override
        public int currentSegment(float[] coords) {
            return iterator.currentSegment(coords);
        }

        @Override
        public int currentSegment(double[] coords) {
            return iterator.currentSegment(coords);
        }
    }

}
