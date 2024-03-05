package drawingbot.render.overlays;

import drawingbot.render.viewport.Viewport;
import drawingbot.render.viewport.ViewportSkin;
import drawingbot.utils.Utils;
import drawingbot.utils.flags.Flags;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.util.function.Function;

/**
 * Responsible for adding the rulers to the left and top of the viewport
 */
public class RulerOverlays extends ViewportOverlayBase {

    public Label viewportAxisUnits;

    public AnchorPane anchorPaneXAxisWrapper;
    public NumberAxis xAxis;
    public AnchorPane anchorPaneYAxisWrapper;
    public NumberAxis yAxis;

    public DoubleProperty xAxisRulerHeight = new SimpleDoubleProperty(25);
    public DoubleProperty yAxisRulerWidth = new SimpleDoubleProperty(30);

    public RulerOverlays(){}

    @Override
    public String getName() {
        return "Rulers";
    }

    @Override
    public void initOverlay(){

        viewportAxisUnits = new Label();
        viewportAxisUnits.setManaged(false);
        viewportAxisUnits.resize(yAxisRulerWidth.get(), xAxisRulerHeight.get());
        viewportAxisUnits.setAlignment(Pos.CENTER);

        xAxis = new NumberAxis();
        xAxis.setAnimated(false);
        xAxis.setManaged(false);
        xAxis.setAutoRanging(false);
        xAxis.prefHeightProperty().bind(xAxisRulerHeight);
        xAxis.setSide(Side.TOP);
        xAxis.setTickLabelGap(0);
        xAxis.setTickLength(10);
        xAxis.setMinorTickLength(6);

        anchorPaneXAxisWrapper = new AnchorPane(xAxis);
        anchorPaneXAxisWrapper.maxHeightProperty().bind(xAxisRulerHeight);
        anchorPaneXAxisWrapper.minHeightProperty().bind(xAxisRulerHeight);
        anchorPaneXAxisWrapper.prefHeightProperty().bind(xAxisRulerHeight);
        HBox.setHgrow(anchorPaneXAxisWrapper, Priority.ALWAYS);

        yAxis = new NumberAxis();
        yAxis.setAnimated(false);
        yAxis.setManaged(false);
        yAxis.setRotate(180);
        yAxis.setTickLabelRotation(180);
        yAxis.setAutoRanging(false);
        yAxis.prefWidthProperty().bind(yAxisRulerWidth);
        yAxis.setSide(Side.RIGHT);
        yAxis.setTickLabelGap(1.0);
        yAxis.setTickLength(10);
        yAxis.setMinorTickLength(6);

        anchorPaneYAxisWrapper = new AnchorPane(yAxis);
        anchorPaneYAxisWrapper.maxWidthProperty().bind(yAxisRulerWidth);
        anchorPaneYAxisWrapper.minWidthProperty().bind(yAxisRulerWidth);
        anchorPaneYAxisWrapper.prefWidthProperty().bind(yAxisRulerWidth);
        VBox.setVgrow(anchorPaneYAxisWrapper, Priority.ALWAYS);

        Rectangle xAxisClip = new Rectangle();
        xAxisClip.xProperty().bind(yAxisRulerWidth);
        xAxisClip.widthProperty().bind(getViewport().widthProperty());
        xAxisClip.heightProperty().bind(xAxisRulerHeight);
        anchorPaneXAxisWrapper.setClip(xAxisClip);

        Rectangle yAxisClip = new Rectangle();
        yAxisClip.widthProperty().bind(yAxisRulerWidth);
        yAxisClip.heightProperty().bind(getViewport().heightProperty());
        anchorPaneYAxisWrapper.setClip(yAxisClip);
    }

    @Override
    public void activateViewportOverlay(Viewport viewport) {
        super.activateViewportOverlay(viewport);
        if(getViewport().getSkin() instanceof ViewportSkin skin){
            skin.rulerBorderPane.setLeft(anchorPaneYAxisWrapper);
            skin.rulerBorderPane.setTop(anchorPaneXAxisWrapper);
            skin.getChildren().add(viewportAxisUnits);
        }
    }

    @Override
    public void deactivateViewportOverlay(Viewport viewport) {
        if(getViewport().getSkin() instanceof ViewportSkin skin){
            skin.rulerBorderPane.setLeft(null);
            skin.rulerBorderPane.setTop(null);
            skin.getChildren().remove(viewportAxisUnits);
        }
        super.deactivateViewportOverlay(viewport);
    }

    @Override
    public void onRenderTick() {
        if(getViewport().getRenderFlags().anyMatchAndMarkClear(Flags.FORCE_REDRAW, Flags.CANVAS_MOVED, Flags.CANVAS_CHANGED, Flags.CHANGED_RENDERER)) {
            Point2D drawingOrigin = new Point2D(0D, 0D);
            Point2D drawingEnd = new Point2D(getViewport().getCanvasWidth(), getViewport().getCanvasHeight());

            Point2D drawingOriginScene = getViewport().getCanvasToSceneTransform().transform(drawingOrigin).subtract(getViewport().getViewportX(), getViewport().getViewportY());
            Point2D drawingEndScene = getViewport().getCanvasToSceneTransform().transform(drawingEnd).subtract(getViewport().getViewportX(), getViewport().getViewportY());

            double drawingEndX = drawingEnd.getX();
            double drawingEndY = drawingEnd.getY();
            double drawingWidthVP = Math.abs(drawingEndScene.getX() - drawingOriginScene.getX());
            double drawingHeightVP = Math.abs(drawingEndScene.getY() - drawingOriginScene.getY());


            Function<Double, Double> sceneToDrawingX = V -> Utils.mapDouble(V, drawingOriginScene.getX(), drawingEndScene.getX(), drawingOrigin.getX(), drawingEndX);
            Function<Double, Double> drawingToSceneX = V -> Utils.mapDouble(V, drawingOrigin.getX(), drawingEndX, drawingOriginScene.getX(), drawingEndScene.getX());

            Function<Double, Double> sceneToDrawingY = V -> Utils.mapDouble(V, drawingOriginScene.getY(), drawingEndScene.getY(), drawingOrigin.getY(), drawingEndY);
            Function<Double, Double> drawingToSceneY = V -> Utils.mapDouble(V, drawingOrigin.getY(), drawingEndY, drawingOriginScene.getY(), drawingEndScene.getY());

            //the unit scale is the size of a single unit in pixels, e.g. 1 mm = 40 pixels
            double unitScale = (drawingEndScene.getX()-drawingOriginScene.getX()) / (drawingEndX - drawingOrigin.getX());

            double tickUnit = -1;
            int minorTickCount = -1;

            if(unitScale < 0.1){
                tickUnit = drawingEndX;
            }else if (unitScale < 0.4){
                tickUnit = 100;
                minorTickCount = 2;
            }else if (unitScale < 0.5){
                tickUnit = 80;
                minorTickCount = 2;
            }else if (unitScale < 1){
                tickUnit = 40;
                minorTickCount = 2;
            }else if(unitScale < 2){
                tickUnit = 20;
                minorTickCount = 2;
            }else if(unitScale < 10){
                tickUnit = 10;
                minorTickCount = 10;
            }else if(unitScale < 20){
                tickUnit = 5;
                minorTickCount = 5;
            }else{
                tickUnit = 1;
                minorTickCount = 10;
            }

            double lowerBoundX = drawingOrigin.getX();
            double lowerBoundXVP = drawingOriginScene.getX();

            double upperBoundX = drawingEndX;
            double upperBoundXVP = drawingEndScene.getX();

            if(lowerBoundXVP < 0){
                lowerBoundX = Math.max(0, Utils.roundToMultiple(sceneToDrawingX.apply(0D)-(tickUnit*2), tickUnit));
                lowerBoundXVP = drawingToSceneX.apply(lowerBoundX);
                drawingWidthVP = drawingEndScene.getX() - lowerBoundXVP;
            }

            if(upperBoundXVP > getViewport().getViewportWidth()){
                upperBoundX = Math.min(drawingEndX, Utils.roundToMultiple(sceneToDrawingX.apply(getViewport().getViewportWidth())+(tickUnit*2), tickUnit));
                upperBoundXVP = drawingToSceneX.apply(upperBoundX);
                drawingWidthVP = drawingWidthVP - (drawingEndScene.getX() - upperBoundXVP);
            }

            double lowerBoundY = drawingOrigin.getY();
            double lowerBoundYVP = drawingOriginScene.getY();

            double upperBoundY = drawingEndY;
            double upperBoundYVP = drawingEndScene.getY();

            if(lowerBoundYVP < 0){
                lowerBoundY = Math.max(0, Utils.roundToMultiple(sceneToDrawingY.apply(0D)-(tickUnit*2), tickUnit));
                lowerBoundYVP = drawingToSceneY.apply(lowerBoundY);
                drawingHeightVP = drawingEndScene.getY() - lowerBoundYVP;
            }

            if(upperBoundYVP > getViewport().getViewportHeight()){
                upperBoundY = Math.min(drawingEndY, Utils.roundToMultiple(sceneToDrawingY.apply(getViewport().getViewportHeight())+(tickUnit*2), tickUnit));
                upperBoundYVP = drawingToSceneY.apply(upperBoundY);
                drawingHeightVP = drawingHeightVP - (drawingEndScene.getY() - upperBoundYVP);
            }

            xAxis.setTickUnit(tickUnit);
            xAxis.setMinorTickCount(minorTickCount);

            yAxis.setTickUnit(tickUnit);
            yAxis.setMinorTickCount(minorTickCount);

            xAxis.setLowerBound(lowerBoundX);
            xAxis.setUpperBound(upperBoundX);
            xAxis.resizeRelocate(lowerBoundXVP + yAxisRulerWidth.get(), 0, drawingWidthVP, xAxisRulerHeight.get());

            yAxis.setLowerBound(lowerBoundY);
            yAxis.setUpperBound(upperBoundY);
            yAxis.resizeRelocate(0, lowerBoundYVP, yAxisRulerWidth.get(), drawingHeightVP);

            viewportAxisUnits.setText(getViewport().getCanvasUnits().getSuffix());
        }
    }
}
