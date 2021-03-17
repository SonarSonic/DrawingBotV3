package drawingbot.geom;

import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GPath;

import java.util.ArrayList;
import java.util.List;

/*
* A path builder for use with a Plotting Task, allows for the creation of Catmull Rom Curves
 */
public class PathBuilder {

    public IPlottingTask task;
    public GPath path = null;
    public boolean hasMoveTo = false;

    public int pathCount = 0;

    public PathBuilder(IPlottingTask task){
        this.task = task;
    }

    public void startPath(){
        if(path != null){
            endPath();
        }
        path = new GPath();
        pathCount = 0;
        hasMoveTo = false;
    }

    public void checkPath(){
        if(path == null){
            startPath();
        }
    }

    public void endPath(){
        if(path != null){
            task.addGeometry(path);
        }
        path = null;
    }

    public void moveTo(float x, float y) {
        checkPath();
        path.moveTo(x, y);
        hasMoveTo = true;
    }

    public void lineTo(float x, float y) {
        if(!hasMoveTo){
            moveTo(x, y);
            return;
        }
        checkPath();
        path.lineTo(x, y);
    }

    public void quadTo(float x1, float y1, float x2, float y2){
        if(!hasMoveTo){
            moveTo(x1, y1);
            return;
        }
        checkPath();
        path.quadTo(x1, y1, x2, y2);
    }

    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3){
        if(!hasMoveTo){
            moveTo(x1, y1);
            return;
        }
        checkPath();
        path.curveTo(x1, y1, x2, y2, x3, y3);
    }

    public void closePath(){
        if(!hasMoveTo){
            return;
        }
        checkPath();
        path.closePath();
    }

    //// CATMULL ROM CURVES \\\\\

    private List<float[]> curvePath;
    private float tension = 1;

    public void setCurveTension(float tension){
        this.tension = tension;
    }

    public void startCurve(){
        if(curvePath != null){
            endCurve();
        }
        startPath();
        curvePath = new ArrayList<>();
    }

    public void checkCurve(){
        if(curvePath == null){
            startCurve();
        }
    }

    public void endCurve(){
        if(curvePath == null){
            return;
        }
        float[][] catmull = new float[4][2];
        float[][] bezier = new float[4][2];

        int index = 0;
        int curves = 0;
        for(float[] f : curvePath){
            catmull[index] = f;
            index++;
            if(index == 4){
                catmullToBezier(catmull, bezier, tension);
                if(curves == 0){
                    moveTo(bezier[0][0], bezier[0][1]);
                }
                curveTo(bezier[1][0], bezier[1][1], bezier[2][0], bezier[2][1], bezier[3][0], bezier[3][1]);
                index = 0;
                curves++;
            }
        }
        curvePath = null;
        endPath();
    }

    public void addCurveVertex(float x, float y){
        checkCurve();
        curvePath.add(new float[]{x, y});
    }

    //src: https://arxiv.org/abs/2011.08232
    public static float[][] catmullToBezier(float[][] catmull, float[][] bezier, float tension){
        bezier[0] = catmull[1];

        bezier[1][0] = catmull[1][0] + ((catmull[2][0]-catmull[0][0]) / (6*tension));
        bezier[1][1] = catmull[1][1] + ((catmull[2][1]-catmull[0][1]) / (6*tension));

        bezier[2][0] = catmull[2][0] + ((catmull[3][0]-catmull[1][0]) / (6*tension));
        bezier[2][1] = catmull[2][1] + ((catmull[3][1]-catmull[1][1]) / (6*tension));

        bezier[3] = catmull[2];
        return bezier;
    }

    //src: https://arxiv.org/abs/2011.08232
    public static float[][] bezierToCatmull(float[][] bezier, float[][] catmull){
        catmull[0][0] = bezier[3][0] + 6*(bezier[0][0] - bezier[1][0]);
        catmull[0][1] = bezier[3][1] + 6*(bezier[0][1] - bezier[1][1]);

        catmull[1] = bezier[0];

        catmull[2] = bezier[3];

        catmull[3][0] = bezier[0][0] + 6*(bezier[3][0] - bezier[2][0]);
        catmull[3][1] = bezier[0][1] + 6*(bezier[3][1] - bezier[2][1]);
        return catmull;
    }

}