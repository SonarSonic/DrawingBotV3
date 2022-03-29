package drawingbot.plotting;

import drawingbot.geom.shapes.GPath;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/*
* A path builder for use with a Plotting Task, allows for the creation of Catmull Rom Curves
 */
public class PathBuilder {

    public PlottingTools plottingTools;
    public GPath path = null;
    public boolean hasMoveTo = false;

    public int pathCount = 0;

    public PathBuilder(PlottingTools plottingTools){
        this.plottingTools = plottingTools;
    }

    public void startPath(){
        if(path != null){
            endPath();
        }
        path = new GPath();
        pathCount = 0;
        hasMoveTo = false;
    }

    private void checkPath(){
        if(path == null){
            startPath();
        }
    }

    public void endPath(){
        if(path != null){
            plottingTools.addGeometry(path);
        }
        path = null;
        hasMoveTo = false;
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

    private List<float[]> catmullCurvePath;
    private float catmullTension = 1;

    public float getCatmullTension(){
        return catmullTension;
    }

    public void setCatmullCurveTension(float tension){
        this.catmullTension = tension;
    }

    public void startCatmullCurve(){
        if(catmullCurvePath != null){
            endCatmullCurve();
        }
        startPath();
        catmullCurvePath = new ArrayList<>();
    }

    private void checkCatmullCurve(){
        if(catmullCurvePath == null){
            startCatmullCurve();
        }
    }

    public void endCatmullCurve(){
        if(catmullCurvePath == null){
            return;
        }
        float[][] catmull = new float[4][2];
        float[][] bezier = new float[4][2];


        int curves = 0;
        float[] p0 = null;
        float[] p1 = null;
        float[] p2 = null;
        float[] p3 = null;


        for(float[] point : catmullCurvePath){
            p0 = p1;
            p1 = p2;
            p2 = p3;
            p3 = point;

            if(p0 != null && p1 != null && p2 != null && p3 != null){
                catmullToBezier(new float[][]{p0, p1, p2, p3}, bezier, catmullTension);
                if(curves == 0){
                    moveTo(p0[0], p1[1]);
                }
                curveTo(bezier[1][0], bezier[1][1], bezier[2][0], bezier[2][1], bezier[3][0], bezier[3][1]);
                curves++;
            }

        }

        catmullCurvePath = null;
        endPath();
    }

    public void addCatmullCurveVertex(float x, float y){
        checkCatmullCurve();
        catmullCurvePath.add(new float[]{x, y});
    }

    public int getCatmullCurvePointCount(){
        return catmullCurvePath == null ? 0 : catmullCurvePath.size();
    }

    public boolean hasCurvePoints(){
        return getCatmullP0() != null;
    }

    @Nullable
    public float[] getCatmullP2(){
        return catmullCurvePath == null || catmullCurvePath.size() <= 0 ? null : catmullCurvePath.get(catmullCurvePath.size()-1);
    }

    @Nullable
    public float[] getCatmullP1(){
        return catmullCurvePath == null || catmullCurvePath.size() <= 1 ? null : catmullCurvePath.get(catmullCurvePath.size()-2);
    }

    @Nullable
    public float[] getCatmullP0(){
        return catmullCurvePath == null || catmullCurvePath.size() <= 2 ? null : catmullCurvePath.get(catmullCurvePath.size()-3);
    }


    //src: https://arxiv.org/abs/2011.08232
    public static float[][] catmullToBezier(float[][] catmull, float[][] bezier, float tension){
        bezier[0] = catmull[1];

        bezier[1][0] = catmull[1][0] + ((catmull[2][0]-catmull[0][0]) / (6*tension));
        bezier[1][1] = catmull[1][1] + ((catmull[2][1]-catmull[0][1]) / (6*tension));

        bezier[2][0] = catmull[2][0] - ((catmull[3][0]-catmull[1][0]) / (6*tension));
        bezier[2][1] = catmull[2][1] - ((catmull[3][1]-catmull[1][1]) / (6*tension));

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