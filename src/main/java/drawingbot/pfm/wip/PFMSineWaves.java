package drawingbot.pfm.wip;

import drawingbot.pfm.AbstractPFM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO FIXME
public class PFMSineWaves extends AbstractPFM {

    public int ystep = 160;
    public int ymult = 6;
    public int xstep = 3;
    public float xsmooth = 128.0F;

    public int imageScaleUp = 3;

    public float r = 0.0F;
    public float a = 0.0F;
    public int strokeWidth = 1;

    public float startx, starty, z;

    public int b, oldb;
    public int maxB = 255;
    public int minB = 0;

    public boolean invert = false;

    public boolean connectEnds = false;


    public float tension = 0.4F;

    @Override
    public int getColourMode() {
        return 0;
    }

    //TODO make it work with curves!!!!
    @Override
    public void doProcess() {

        ///TODO set stroke width = strokeWidth
        float HALF_PI = (float) (Math.PI / 2.0);

        startx = 0.0F;
        starty = 0.0F;

        float scaleFactor = 1.0F; //TODO REMOVE
        float xOffset = 0;

        float deltaPhase;
        float deltaX;
        float deltaAmpl;

        /*
        The minimum phase increment should give about 40 vertices minimum
        across x. 40 vertices -> 10 * 2 pi.
        */
        float minPhaseIncr = 10F * (float)(2.0 * Math.PI) / ((float) task.getPixelData().getWidth() / xstep);

        /*
        Maximum phase increment (frequency cap) is based on line thickness and x step size.

        A full period of oscillation needn't be less than
        2 * strokeWidth in total width.

        The maximum number of full cycles that should be permitted in a
        horizontal distance of xstep should be:
        N = total width/width per cycle =  xstep / (2 * strokeWidth)

        The maximum phase increment in distance xstep should then be:

        maxPhaseIncr = 2 Pi * N = 2 * Pi *  xstep / (2 * strokeWidth)
        = 2Pi *  xstep / strokeWidth

        We do not need to include the scaling factors, since
        both the step size and stroke width are scaled the same way.
        */


        float maxPhaseIncr = (float)(2.0 * Math.PI) * xstep / strokeWidth;

        //strokeWeight(strokeWidth * scaleFactor); //TODO SET STROKE WIDTH

        if (connectEnds) {
            task.getPathBuilder().startCurve();
        }

        boolean oddRow = false;
        boolean finalRow = false;
        boolean reverseRow;
        float lastX;
        float scaledYstep = (float) task.getPixelData().getHeight() / ystep;

        for (int y = 0; y < task.getPixelData().getHeight(); y += scaledYstep) {

            oddRow = !oddRow;
            if (y + (scaledYstep) >= task.getPixelData().getHeight())
                finalRow = true;

            if (connectEnds && !oddRow)
                reverseRow = true;
            else
                reverseRow = false;

            a = 0.0F;


            if (!connectEnds) {
                task.getPathBuilder().startCurve();
            }

            // Add initial "extra" point to give splines a consistent visual endpoint,
            // IF we are not connecting rows.

            if (reverseRow) {
                if (!connectEnds || y == 0) {
                    // Always add the extra initial point if we're not connecting the ends, or if this is the first row.
                    task.getPathBuilder().addCurveVertex(xOffset + scaleFactor * (task.getPixelData().getWidth() + 0.1F * xstep), scaleFactor * y);
                }
                task.getPathBuilder().addCurveVertex(xOffset + scaleFactor * (task.getPixelData().getWidth()), scaleFactor * y);
            } else {
                if (!connectEnds || y == 0) {
                    // Always add the extra initial point if we're not connecting the ends, or if this is the first row.
                    task.getPathBuilder().addCurveVertex(xOffset - scaleFactor * (0.1F * xstep), y * scaleFactor);
                }
                task.getPathBuilder().addCurveVertex(xOffset, y * scaleFactor);
            }


            /*
            Step along width of image.

            For each step, get the image brightness for that XY position,
            and constrain it to our bright/dark cutoff window.

            Accumulated phase: increment by scaled brightness, so that the frequency
            increases in certain areas of the image.  Phase only advances with pigment,
            not simply by traversing across the image in X.

            Amplitude: A simple multiplier based on local brightness.

            To have high quality generated curves for display and plotting, we would like to:

            (1) Avoid aliasing. Aliasing happens when we plot a signal at a poorly
            representative set of points. By undersampling -- e.g., less than once per
            period -- you can very easily see what appears to be a sine wave, but does
            not actually represent the actual function being sampled.

            Two potential methods to avoid aliasing:
            (A) Increase the number of points, to ensure that some minimum number
            of points are sampeled per period, or
            (B) Plot the function at specific points {x_i} that are determined by
            the value of the function f(x) at those points, e.g., at every crest,
            trough, and zero crossing.

            (2) Place relatively few control points.
            CNC software tends to follow simply defined curves more easily than
            paths with a great many closely-spaced points.
            Side benefit: Potentially smaller file size.

            (3) Place an upper bound on the maximum frequency.
            Above a certain frequency, with a finite-width pen, increasing the frequency
            does not make the plot any darker.


            To achieve these goals, we will try:

            (1) Putting x-points (vertices) at every crest, trough, and zero crossing.
            Point x-positions may be approximated as necessary by interpolation.

            (2) Using Processing's curveVertex method, to create curvy lines
            (Catmull–Rom splines). These will only approximate sine waves, but
            should work well for this particular application.

            (3) Using the GUI line-width control to control the maximum frequency.

            */

            float phase = 0.0F;
            float lastPhase = 0; // accumulated phase at previous vertex
            float lastAmpl = 0; // amplitude at previous vertex
            boolean finalStep = false;

            int x;

            x = 1;
            lastX = 1;

            List<Float> xPoints = new ArrayList<>();
            List<Float> yPoints = new ArrayList<>();

            while (!finalStep) { // Iterate over each each x-step in the row

                // Moving right to left:
                x += xstep;
                if (x + xstep >= task.getPixelData().getWidth())
                    finalStep = true;
                else
                    finalStep = false;


                b = task.getPixelData().getBrightness(x, y);
                b = Math.max(minB, b);
                z = Math.max(maxB - b, 0);        // Brightness trimmed to range.

                r = z / ystep * ymult;        // ymult: Amplitude

                  /*
                   Enforce a minimum phase increment, to prevent large gaps in splines
                   This will add extra vertices in flat regions, but the amplitude remains
                   unaffected (near-zero amplitude), so it does not cause a significant
                   visual effect.
                   */

                float df = z / xsmooth;
                if (df < minPhaseIncr){
                    df = minPhaseIncr;
                }
                  /*
                   Enforce a maximum phase increment -- a frequency cap -- to prevent
                   unnecessary plotting time. Once the frequency is so high that the line widths
                   of neighboring crests overlap, there is no added benefit to having higher
                   frequency; it's just wasting memory (and ink + time, if plotting).
                   */

                if (df > maxPhaseIncr){
                    df = maxPhaseIncr;
                }

                phase += df;  // xsmooth: Frequency

                deltaX = x - lastX; // Distance between image sample location x and previous vertex

                deltaAmpl = r - lastAmpl;

                deltaPhase = phase - lastPhase; // Change in phase since last *vertex*

                // (Vertices do not fall along the x "grid", but where they need to.)

                if (!finalStep){  // Skip to end points if this is the last point in the row.
                    if (deltaPhase > HALF_PI){ // Only add vertices if true.
                        /*
                        Linearly interpolate phase and amplitude since last vertex added.
                        This treats the frequency as constant
                        between subsequent x-samples of the source image.
                        */
                        int vertexCount = (int)Math.floor(deltaPhase / HALF_PI); //  Add this many vertices

                        float integerPart = ((vertexCount * HALF_PI) / deltaPhase);
                        // "Integer" fraction (in terms of pi/2 phase segments) of deltaX.

                        float deltaX_truncate = deltaX * integerPart;
                        // deltaX_truncate: "Integer" part (in terms of pi/2 segments) of deltaX.

                        float xPerVertex = deltaX_truncate / vertexCount;
                        float amplPerVertex = (integerPart * deltaAmpl) / vertexCount;

                        // Add the vertices:
                        for (int i = 0; i < vertexCount; i = i + 1) {

                            lastX = lastX + xPerVertex;
                            lastPhase = lastPhase + HALF_PI;
                            lastAmpl = lastAmpl + amplPerVertex;

                            xPoints.add(xOffset + scaleFactor * lastX);
                            yPoints.add(scaleFactor * (y + (float)Math.sin(lastPhase) * lastAmpl));
                        }
                    }
                }
            }

            if (reverseRow) {
                Collections.reverse(xPoints);
                Collections.reverse(yPoints);
            }

            for (int i = 0; i < xPoints.size(); i++) {
                task.getPathBuilder().addCurveVertex(xPoints.get(i), yPoints.get(i));

            }


            // Add final "extra" point to give splines a consistent visual endpoint:
            if (reverseRow) {
                task.getPathBuilder().addCurveVertex(xOffset, y * scaleFactor);
                if (!connectEnds || finalRow) {
                    // Always add the extra final point if we're not connecting the ends, or if this is the first row.
                    task.getPathBuilder().addCurveVertex(xOffset - scaleFactor * (0.1F * xstep), y * scaleFactor);
                }
            } else {
                task.getPathBuilder().addCurveVertex(xOffset + scaleFactor * (task.getPixelData().getWidth()), scaleFactor * y);
                if (!connectEnds || finalRow) {
                    // Always add the extra final point if we're not connecting the ends, or if this is the first row.
                    task.getPathBuilder().addCurveVertex(xOffset + scaleFactor * (task.getPixelData().getWidth() + 0.1F * xstep), scaleFactor * y);
                }
            }


            if (connectEnds && !finalRow){  // Add curvy end connectors
                if (reverseRow) {
                    task.getPathBuilder().addCurveVertex(xOffset - scaleFactor * (0.1F * xstep + scaledYstep / 3), (y + scaledYstep / 2) * scaleFactor);
                } else {
                    task.getPathBuilder().addCurveVertex(xOffset + scaleFactor * (task.getPixelData().getWidth() + 0.1F * xstep + scaledYstep / 3), (y + scaledYstep / 2) * scaleFactor);
                }
            }

            if (!connectEnds) {
                task.getPathBuilder().endCurve();
            }
        }

        if (connectEnds) {
            task.getPathBuilder().endCurve();
        }
        task.finishProcess();
    }

}
