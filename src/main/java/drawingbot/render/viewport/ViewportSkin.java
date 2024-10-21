package drawingbot.render.viewport;

import drawingbot.javafx.FXController;
import drawingbot.javafx.util.JFXUtils;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.fxmisc.easybind.EasyBind;

public class ViewportSkin extends SkinBase<Viewport> {

    public static final String STYLESHEET_VIEWPORT_OVERLAYS = "viewport-overlays.css";

    public VBox viewport = null;
    public ZoomableScrollPane viewportScrollPane = null;
    public AnchorPane backgroundOverlays = null;
    public AnchorPane foregroundOverlays = null;
    public BorderPane rulerBorderPane = null;

    public ViewportSkin(Viewport control) {
        super(control);

        viewportScrollPane = new ZoomableScrollPane(control);

        viewportScrollPane.setFitToWidth(true);
        viewportScrollPane.setFitToHeight(true);
        viewportScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        viewportScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        viewportScrollPane.setVvalue(0.5);
        viewportScrollPane.setHvalue(0.5);
        viewportScrollPane.setPannable(true);
        viewportScrollPane.setId("viewportScrollPane");

        JFXUtils.subscribeListener(viewportScrollPane.contentProperty(), (observable, oldValue, newValue) -> {
            if(newValue != null){
                newValue.styleProperty().unbind();
                newValue.styleProperty().bind(Bindings.createStringBinding(() -> {
                    Color c = control.getViewportBackgroundColor();
                    return "-fx-background-color: rgb(%s,%s,%s,%s)".formatted((int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255), c.getOpacity());
                }, control.viewportBackgroundColorProperty()));
            }
        });

        VBox.setVgrow(viewportScrollPane, Priority.ALWAYS);
        HBox.setHgrow(viewportScrollPane, Priority.ALWAYS);

        Rectangle backgroundClip = new Rectangle(1, 1,0,0); //x/y of 1 to keep the ScrollPanes border
        backgroundClip.widthProperty().bind(getSkinnable().viewportWidthProperty()); //14 = subtract the scrollbars
        backgroundClip.heightProperty().bind(getSkinnable().viewportHeightProperty());

        backgroundOverlays = new AnchorPane();
        backgroundOverlays.setPickOnBounds(false);
        backgroundOverlays.layoutXProperty().bind(viewportScrollPane.layoutXProperty());
        backgroundOverlays.layoutYProperty().bind(viewportScrollPane.layoutYProperty());
        backgroundOverlays.setManaged(false);
        backgroundOverlays.setClip(backgroundClip);
        backgroundOverlays.getStylesheets().add(FXController.class.getResource(STYLESHEET_VIEWPORT_OVERLAYS).toExternalForm());
        EasyBind.listBind(backgroundOverlays.getChildren(), getSkinnable().getBackgroundOverlayNodes());

        backgroundOverlays.setOnScroll(e -> {
            //HACK: Solid overlays will block passing the zoom event, so we send the event down if we didn't use it
            Event.fireEvent(viewportScrollPane.getContent(), e.copyFor(e.getSource(), viewportScrollPane.getContent()));
            e.consume();
        });

        Rectangle foregroundClip = new Rectangle(1, 1,0,0); //x/y of 1 to keep the ScrollPanes border
        foregroundClip.widthProperty().bind(getSkinnable().viewportWidthProperty()); //14 = subtract the scrollbars
        foregroundClip.heightProperty().bind(getSkinnable().viewportHeightProperty());

        foregroundOverlays = new AnchorPane();
        foregroundOverlays.setPickOnBounds(false);
        foregroundOverlays.layoutXProperty().bind(viewportScrollPane.layoutXProperty());
        foregroundOverlays.layoutYProperty().bind(viewportScrollPane.layoutYProperty());
        foregroundOverlays.setManaged(false);
        foregroundOverlays.setClip(foregroundClip);
        foregroundOverlays.getStylesheets().add(FXController.class.getResource(STYLESHEET_VIEWPORT_OVERLAYS).toExternalForm());
        EasyBind.listBind(foregroundOverlays.getChildren(), getSkinnable().getForegroundOverlayNodes());

        foregroundOverlays.setOnScroll(e -> {
            //HACK: Solid overlays will block passing the zoom event, so we send the event down if we didn't use it
            Event.fireEvent(backgroundOverlays, e.copyFor(e.getSource(), backgroundOverlays));
            e.consume();
        });

        //Wrap the viewport & overlay
        viewport = new VBox();
        VBox.setVgrow(viewport, Priority.ALWAYS);
        HBox.setHgrow(viewport, Priority.ALWAYS);

        VBox.setVgrow(viewportScrollPane, Priority.ALWAYS);
        HBox.setHgrow(viewportScrollPane, Priority.ALWAYS);

        viewport.getChildren().add(viewportScrollPane);
        viewport.getChildren().add(backgroundOverlays);
        viewport.getChildren().add(foregroundOverlays);

        rulerBorderPane = new BorderPane();
        rulerBorderPane.setCenter(viewport);

        getChildren().add(rulerBorderPane);

        VBox.setVgrow(getSkinnable(), Priority.ALWAYS);
        HBox.setHgrow(getSkinnable(), Priority.ALWAYS);

        EasyBind.subscribe(viewportScrollPane.viewportBoundsProperty(), (value) -> {
            getSkinnable().setViewportWidth(value.getWidth());
            getSkinnable().setViewportHeight(value.getHeight());
        });

        EasyBind.subscribe(viewportScrollPane.localToSceneTransformProperty(), (value) -> {
            Point2D origin = value.transform(0, 0);
            getSkinnable().setViewportX(origin.getX());
            getSkinnable().setViewportY(origin.getY());
        });
    }

    public void resetView(){
        viewportScrollPane.resetView();
    }
}
