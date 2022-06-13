package drawingbot.render.overlays;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.utils.Utils;
import drawingbot.utils.flags.Flags;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

import java.util.function.Function;

public class RulerOverlays extends AbstractOverlay {

    public static final RulerOverlays INSTANCE = new RulerOverlays();

    public Separator separator;
    public Label viewportAxisUnits;
    public AnchorPane anchorPaneXAxisWrapper;
    public NumberAxis xAxis;
    public HBox hBoxXAxisWrapper;
    public AnchorPane anchorPaneYAxisWrapper;
    public NumberAxis yAxis;
    public HBox hBoxYAxisWrapper;

    private RulerOverlays(){}

    @Override
    public String getName() {
        return "Rulers";
    }

    @Override
    public void init(){
        separator = new Separator();

        viewportAxisUnits = new Label();
        viewportAxisUnits.setMaxWidth(30);
        viewportAxisUnits.setMinWidth(30);
        viewportAxisUnits.setPrefWidth(30);
        viewportAxisUnits.setTextAlignment(TextAlignment.CENTER);
        HBox.setMargin(viewportAxisUnits, new Insets(0, -4, 0, 4));


        xAxis = new NumberAxis();
        xAxis.setAnimated(false);
        xAxis.setManaged(false);
        xAxis.setAutoRanging(false);
        xAxis.setSide(Side.TOP);
        xAxis.setTickLabelGap(0);

        yAxis = new NumberAxis();
        yAxis.setAnimated(false);
        yAxis.setManaged(false);
        yAxis.setRotate(180);
        yAxis.setTickLabelRotation(180);
        yAxis.setAutoRanging(false);
        yAxis.setPrefWidth(30);
        yAxis.setSide(Side.RIGHT);
        yAxis.setTickLabelGap(1.0);
        yAxis.setLayoutY(2);

        anchorPaneXAxisWrapper = new AnchorPane();
        anchorPaneXAxisWrapper.getChildren().add(xAxis);
        anchorPaneXAxisWrapper.setMaxHeight(20);
        anchorPaneXAxisWrapper.setMinHeight(20);
        anchorPaneXAxisWrapper.setPrefHeight(20);
        HBox.setHgrow(anchorPaneXAxisWrapper, Priority.ALWAYS);

        hBoxXAxisWrapper = new HBox();
        hBoxXAxisWrapper.getChildren().add(viewportAxisUnits);
        hBoxXAxisWrapper.getChildren().add(anchorPaneXAxisWrapper);

        anchorPaneYAxisWrapper = new AnchorPane();
        anchorPaneYAxisWrapper.getChildren().add(yAxis);
        anchorPaneYAxisWrapper.setMaxWidth(30);
        anchorPaneYAxisWrapper.setMinWidth(30);
        anchorPaneYAxisWrapper.setPrefWidth(30);
        VBox.setVgrow(anchorPaneXAxisWrapper, Priority.ALWAYS);

        hBoxYAxisWrapper = new HBox();
        VBox.setVgrow(hBoxYAxisWrapper, Priority.ALWAYS);
        hBoxYAxisWrapper.getChildren().add(anchorPaneYAxisWrapper);

        Rectangle xAxisClip = new Rectangle(0, 0, 200, 25);
        xAxisClip.widthProperty().bind(DrawingBotV3.INSTANCE.controller.viewportScrollPane.widthProperty());
        anchorPaneXAxisWrapper.setClip(xAxisClip);

        Rectangle yAxisClip = new Rectangle(0, 0, 30, 200);
        yAxisClip.heightProperty().bind(DrawingBotV3.INSTANCE.controller.viewportScrollPane.heightProperty());
        anchorPaneYAxisWrapper.setClip(yAxisClip);
    }

    @Override
    protected void activate() {
        DrawingBotV3.INSTANCE.controller.vBoxViewportContainer.getChildren().remove(DrawingBotV3.INSTANCE.controller.viewportScrollPane);
        DrawingBotV3.INSTANCE.controller.vBoxViewportContainer.getChildren().remove(DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane);
        DrawingBotV3.INSTANCE.controller.vBoxViewportContainer.getChildren().add(separator);
        DrawingBotV3.INSTANCE.controller.vBoxViewportContainer.getChildren().add(hBoxXAxisWrapper);
        DrawingBotV3.INSTANCE.controller.vBoxViewportContainer.getChildren().add(hBoxYAxisWrapper);
        hBoxYAxisWrapper.getChildren().add(DrawingBotV3.INSTANCE.controller.viewportScrollPane);
        hBoxYAxisWrapper.getChildren().add(DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane);
    }

    @Override
    protected void deactivate() {

        DrawingBotV3.INSTANCE.controller.vBoxViewportContainer.getChildren().remove(separator);
        DrawingBotV3.INSTANCE.controller.vBoxViewportContainer.getChildren().remove(hBoxXAxisWrapper);
        DrawingBotV3.INSTANCE.controller.vBoxViewportContainer.getChildren().remove(hBoxYAxisWrapper);
        DrawingBotV3.INSTANCE.controller.vBoxViewportContainer.getChildren().add(DrawingBotV3.INSTANCE.controller.viewportScrollPane);
        DrawingBotV3.INSTANCE.controller.vBoxViewportContainer.getChildren().add(DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane);
        hBoxYAxisWrapper.getChildren().remove(DrawingBotV3.INSTANCE.controller.viewportScrollPane);
        hBoxYAxisWrapper.getChildren().remove(DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane);

    }

    @Override
    public void doRender() {
        if(DrawingBotV3.INSTANCE.displayMode.get().getRenderFlags().anyMatch(Flags.FORCE_REDRAW, Flags.CANVAS_MOVED, Flags.CANVAS_CHANGED, Flags.CHANGED_RENDERER)) {
            DrawingBotV3.INSTANCE.displayMode.get().getRenderFlags().markForClear(Flags.FORCE_REDRAW, Flags.CANVAS_MOVED, Flags.CANVAS_CHANGED, Flags.CHANGED_RENDERER);
            Point2D drawingOrigin = new Point2D(0D, 0D);
            ICanvas canvas = DrawingBotV3.INSTANCE.displayMode.get().getRenderer().getRefCanvas();

            Point2D viewportOrigin = DrawingBotV3.INSTANCE.controller.viewportScrollPane.localToScene(0, 0);

            Point2D drawingOriginScene = DrawingBotV3.INSTANCE.displayMode.get().getRenderer().rendererToScene(drawingOrigin).subtract(viewportOrigin);
            Point2D drawingEndScene = DrawingBotV3.INSTANCE.displayMode.get().getRenderer().rendererToScene(new Point2D(canvas.getScaledWidth(), canvas.getScaledHeight())).subtract(viewportOrigin);

            double drawingEndX = canvas.getWidth();
            double drawingEndY = canvas.getHeight();
            double drawingWidthVP = drawingEndScene.getX() - drawingOriginScene.getX();
            double drawingHeightVP = drawingEndScene.getY() - drawingOriginScene.getY();


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

            if(upperBoundXVP > DrawingBotV3.INSTANCE.controller.viewportScrollPane.getWidth()){
                upperBoundX = Math.min(drawingEndX, Utils.roundToMultiple(sceneToDrawingX.apply(DrawingBotV3.INSTANCE.controller.viewportScrollPane.getWidth())+(tickUnit*2), tickUnit));
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

            if(upperBoundYVP > DrawingBotV3.INSTANCE.controller.viewportScrollPane.getHeight()){
                upperBoundY = Math.min(drawingEndY, Utils.roundToMultiple(sceneToDrawingY.apply(DrawingBotV3.INSTANCE.controller.viewportScrollPane.getHeight())+(tickUnit*2), tickUnit));
                upperBoundYVP = drawingToSceneY.apply(upperBoundY);
                drawingHeightVP = drawingHeightVP - (drawingEndScene.getY() - upperBoundYVP);
            }


            xAxis.setTickUnit(tickUnit);
            xAxis.setMinorTickCount(minorTickCount);
            xAxis.setMinorTickLength(9);
            xAxis.setTickLength(12);

            yAxis.setTickUnit(tickUnit);
            yAxis.setMinorTickCount(minorTickCount);
            yAxis.setMinorTickLength(8);
            yAxis.setTickLength(8);



            xAxis.setLowerBound(lowerBoundX);
            xAxis.setUpperBound(upperBoundX);
            xAxis.resizeRelocate(lowerBoundXVP, 0, drawingWidthVP, 25);

            yAxis.setLowerBound(lowerBoundY);
            yAxis.setUpperBound(upperBoundY);
            yAxis.resizeRelocate(0, lowerBoundYVP, 30, drawingHeightVP);

            viewportAxisUnits.setText(canvas.getUnits().getSuffix());
        }
    }
}
