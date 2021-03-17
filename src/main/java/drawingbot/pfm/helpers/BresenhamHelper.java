package drawingbot.pfm.helpers;

import java.util.function.BiFunction;

public class BresenhamHelper {

    public int sx;
    public int sy;

    public int error;

    public int deltaX;
    public int deltaY;
    public int deltaErr;

    public int pointX, pointY;
    public int lastX, lastY;

    public int pointCount;

    public BresenhamHelper(){}

    public void line(int originX, int originY, int targetX, int targetY, BiFunction<Integer, Integer, Boolean> func) {
        lastX = pointX = originX;
        lastY = pointY = originY;

        sx = pointX < targetX ? 1 : -1;
        sy = pointY < targetY ? 1 : -1;

        deltaX = Math.abs(targetX-pointX);
        deltaY = Math.abs(targetY-pointY);
        deltaErr = deltaX-deltaY;

        error = 0;
        pointCount = 0;

        while (true) {
            pointCount++;

            boolean cancel = func.apply(pointX, pointY);
            lastX = pointX;
            lastY = pointY;

            if (cancel || (pointX == targetX) && (pointY == targetY)) {
                return;
            }

            error = 2 * deltaErr;
            if (error > -deltaY) {
                deltaErr -= deltaY;
                pointX += sx;
            }
            if (error < deltaX) {
                deltaErr += deltaX;
                pointY += sy;
            }
        }
    }

    public void rectangle(int startX, int startY, int endX, int endY, BiFunction<Integer, Integer, Boolean> func) {
        line(startX, startY, endX, startY, func); //top
        line(startX, endY, endX, endY, func); //bottom

        line(startX, startY, startX, endY, func); //left
        line(endX, startY, endX, endY, func); //right
    }

}
