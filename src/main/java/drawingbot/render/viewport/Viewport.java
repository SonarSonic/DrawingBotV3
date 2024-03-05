package drawingbot.render.viewport;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.observables.ObservableRectangle;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.render.renderer.RendererFactory;
import drawingbot.render.renderer.RendererBase;
import drawingbot.javafx.observables.TransformedPoint;
import drawingbot.render.modes.DisplayModeBase;
import drawingbot.render.overlays.ViewportOverlayBase;
import drawingbot.utils.LazyTimer;
import drawingbot.utils.UnitsLength;
import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;
import javafx.animation.AnimationTimer;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.*;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Screen;

import java.util.HashMap;
import java.util.Map;

/**
 * A JavaFX Control which provides a viewport, for showing a {@link DisplayModeBase}
 *
 * The viewport will automatically create renderer instances as required by the enabled Display Mode it can use both JavaFX and OpenGL renderers.
 * Overlays can be added to customise the viewports appearance as well as adding controls for editing the drawing/image displayed see {@link ViewportOverlayBase}
 *
 */
public class Viewport extends Control {

    private final Map<RendererFactory, RendererBase> renderers = new HashMap<>();
    public final FlagStates renderFlags = new FlagStates(Flags.RENDER_CATEGORY);
    private final ChangeListener<Boolean> overlayEnabledListener;
    private final LazyTimer performanceTimer = new LazyTimer();
    private final AnimationTimer ticker = new AnimationTimer() {
        @Override
        public void handle(long now) {
            tick();
        }
    };

    public Viewport() {
        //Listener or the display mode property, when it changes we will deactivate/activate the display mode as needed and set the correct renderer for the display mode
        displayModeProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.setActive(false);
                oldValue.deactivateDisplayMode(this);
                oldValue.setViewport(null);
                canvas.unbind();
            }

            if(newValue != null){
                setRenderer(getOrCreateRenderer(newValue));
                newValue.setViewport(this);
                newValue.setActive(true);
                newValue.activateDisplayMode(this);
                canvas.bind(newValue.canvasProperty());
            }
            getRenderFlags().setFlag(Flags.FORCE_REDRAW, true);
            resetView();
        });

        InvalidationListener transformChangedListener = observable -> getRenderFlags().setFlag(Flags.CANVAS_MOVED, true);

        //Monitor the renderer property, this wll only triggered when the new display mode uses a different renderer
        rendererProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.displayModeProperty().unbind();

                oldValue.rendererToSceneTransformProperty().removeListener(transformChangedListener);

                displayedWidth.unbind();
                displayedHeight.unbind();
                dpiScaling.unbind();

                oldValue.deactivateRenderer();
                oldValue.setViewport(null);
                ticker.stop();
            }
            if(newValue != null){
                newValue.setViewport(this);
                newValue.displayModeProperty().bind(displayModeProperty());

                newValue.rendererToSceneTransformProperty().addListener(transformChangedListener);

                displayedWidth.bind(Bindings.createDoubleBinding(() -> getCanvasScaledWidth() * newValue.getRenderScale(), canvasScaledWidthProperty(), newValue.renderScaleProperty()));
                displayedHeight.bind(Bindings.createDoubleBinding(() -> getCanvasScaledHeight() * newValue.getRenderScale(), canvasScaledHeightProperty(), newValue.renderScaleProperty()));
                dpiScaling.bind(Bindings.createDoubleBinding(this::calculateDPIScaling, canvasUnitsProperty(), canvasWidthProperty(), newValue.renderScaleProperty()));

                newValue.activateRenderer();
                ticker.start();
            }

            getRenderFlags().setFlag(Flags.FORCE_REDRAW, true);
            getRenderFlags().setFlag(Flags.CHANGED_RENDERER, true);
            resetView();

        });

        ObservableCanvas.Listener observableCanvasListener = new ObservableCanvas.Listener() {
            @Override
            public void onCanvasPropertyChanged(ObservableCanvas canvas, Observable property) {
                getRenderFlags().setFlag(Flags.CANVAS_CHANGED, true);
            }
        };

        canvasProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                canvasUnits.unbind();
                canvasScale.unbind();
                canvasWidth.unbind();
                canvasHeight.unbind();
                canvasScaledWidth.unbind();
                canvasScaledHeight.unbind();
                if(newValue instanceof ObservableCanvas observableCanvas) {
                    observableCanvas.removeSpecialListener(observableCanvasListener);
                }
            }
            if(newValue != null){
                //If the bound canvas is observable we will observe it to monitor for changes
                if(newValue instanceof ObservableCanvas observableCanvas){
                    canvasUnits.bind(observableCanvas.inputUnits);
                    canvasScale.bind(Bindings.createDoubleBinding(observableCanvas::getPlottingScale, observableCanvas));
                    canvasWidth.bind(observableCanvas.width);
                    canvasHeight.bind(observableCanvas.height);
                    canvasScaledWidth.bind(Bindings.createDoubleBinding(observableCanvas::getScaledWidth, observableCanvas));
                    canvasScaledHeight.bind(Bindings.createDoubleBinding(observableCanvas::getScaledHeight, observableCanvas));
                    observableCanvas.addSpecialListener(observableCanvasListener);
                }else{
                    //If the bound canvas is a simple canvas instead we set the given properties
                    canvasUnits.set(newValue.getUnits());
                    canvasScale.set(newValue.getPlottingScale());
                    canvasWidth.set(newValue.getWidth());
                    canvasHeight.set(newValue.getHeight());
                    canvasScaledWidth.set(newValue.getScaledWidth());
                    canvasScaledHeight.set(newValue.getScaledHeight());
                }
            }
        });

        //Listener to monitor when a viewport overlay is enabled / disabled so we can activate/deactivate it as needed
        overlayEnabledListener = (observable, oldValue, newValue) -> {
            if(getSkin() != null && observable instanceof BooleanProperty prop && prop.getBean() instanceof ViewportOverlayBase overlay){
                if(newValue){
                    overlay.activateViewportOverlay(this);
                }else{
                    overlay.deactivateViewportOverlay(this);
                }
            }
        };

        //Observe the viewport overlays set to deactivate / active them as needed
        viewportOverlays.addListener((SetChangeListener<? super ViewportOverlayBase>) c -> {
            if(c.wasRemoved()){
                ViewportOverlayBase removed = c.getElementRemoved();
                removed.enabledProperty().removeListener(overlayEnabledListener);

                if(getSkin() != null && removed.getEnabled()) {
                    removed.deactivateViewportOverlay(this);
                }
            }
            if(c.wasAdded()){
                ViewportOverlayBase added = c.getElementAdded();
                added.enabledProperty().addListener(overlayEnabledListener);

                if(getSkin() != null && added.getEnabled()) {
                    added.activateViewportOverlay(this);
                }
            }
        });

        // Only when the skin is set do we activate the viewport overlays, as some more depend on some internal skin components
        skinProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                viewportOverlays.forEach(o -> {
                    if(o.getEnabled()){
                        o.deactivateViewportOverlay(this);
                    }
                });
            }
            if(newValue != null){
                viewportOverlays.forEach(o -> {
                    if(o.getEnabled()){
                        o.activateViewportOverlay(this);
                        o.onRenderTick();
                    }
                });
            }
            resetView();
        });

        //When using "DPI Scaling" we disable the zoom functionality and bind it too the "DPI Scaling" instead
        useDPIScalingProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                zoomProperty().bind(dpiScalingProperty());
                resetView();
            }else{
                zoomProperty().unbind();
                setZoom(1);
                resetView();
            }
        });

        //Create the scale to fit binding
        scaleToFit.bind(Bindings.createDoubleBinding(this::calculateScaleToFit, useDPIScalingProperty(), viewportWidthProperty(), viewportHeightProperty(), displayedWidthProperty(), displayedHeightProperty()));

        // Add canvas move listeners
        zoomProperty().addListener((observable, oldValue, newValue) -> markCanvasMoved());
        scaleToFitProperty().addListener((observable, oldValue, newValue) -> markCanvasMoved());
        dpiScalingProperty().addListener((observable, oldValue, newValue) -> markCanvasMoved());
        viewportXProperty().addListener((observable, oldValue, newValue) -> markCanvasMoved());
        viewportYProperty().addListener((observable, oldValue, newValue) -> markCanvasMoved());
        viewportWidthProperty().addListener((observable, oldValue, newValue) -> markCanvasMoved());
        viewportHeightProperty().addListener((observable, oldValue, newValue) -> markCanvasMoved());
        displayedWidthProperty().addListener((observable, oldValue, newValue) -> markCanvasMoved());
        displayedHeightProperty().addListener((observable, oldValue, newValue) -> markCanvasMoved());

        // Canvas change listeners
        canvasUnitsProperty().addListener((observable, oldValue, newValue) -> markCanvasChanged());
        canvasScaledWidthProperty().addListener((observable, oldValue, newValue) -> markCanvasChanged());
        canvasScaledHeightProperty().addListener((observable, oldValue, newValue) -> markCanvasChanged());

        // Viewport scaling change listeners
        displayedWidthProperty().addListener((observable, oldValue, newValue) -> markViewportScalingChanged());
        displayedHeightProperty().addListener((observable, oldValue, newValue) -> markViewportScalingChanged());
        scaleToFitProperty().addListener((observable, oldValue, newValue) -> markViewportScalingChanged());
    }

    /**
     * The viewports render flags, which can be used by {@link RendererBase}, {@link DisplayModeBase} and {@link ViewportOverlayBase}
     * To check if updates / re-renders are required, by default when a display mode is changed the {@link Flags#FORCE_REDRAW} flag will be true
     * @return a {@link FlagStates} object containing the render flags
     */
    public FlagStates getRenderFlags() {
        return renderFlags;
    }

    private void markCanvasMoved(){
        getRenderFlags().setFlag(Flags.CANVAS_MOVED, true);
    }

    private void markCanvasChanged(){
        getRenderFlags().setFlag(Flags.CANVAS_CHANGED, true);
        resetView();
    }

    private void markViewportScalingChanged(){
        resetView();
    }

    private RendererBase getOrCreateRenderer(DisplayModeBase mode){
        return renderers.computeIfAbsent(mode.getRendererFactory(), (key) -> {
            RendererBase renderer = mode.getRendererFactory().create();
            //Renderers must always be created on the JavaFX Thread
            JFXUtils.runNow(() -> {
                renderer.setViewport(this);
                renderer.initRenderer();
            });
            return renderer;
        });
    }

    /**
     * Calculates the scale value to use to "scaleToFit" the renderer
     * So that once {@link #getZoom()} is set to 1, the renderer will fit exactly within the viewport (if {@link #useDPIScaling()} is false)
     * @return the "scaleToFit" scale value
     */
    private double calculateScaleToFit(){
        if(useDPIScaling()){
            return 1;
        }
        double scaleX = getViewportWidth() / getDisplayedWidth();
        double scaleY = getViewportHeight() / getDisplayedHeight();
        return  Math.min(scaleX, scaleY);
    }

    /**
     * Calculates the exact scale value required to display a 1 to 1 representation of the generated drawing.
     * e.g. if A4 paper has been selected the render will match the paper size on screen.
     * @return the "DPI Scaling"
     */
    private double calculateDPIScaling(){
        if(getCanvasUnits() == UnitsLength.PIXELS || getRenderer() == null){
            return 1;
        }
        double screenDPI = Screen.getPrimary().getDpi();
        double targetWidth = UnitsLength.convert(getCanvasWidth(), getCanvasUnits(), UnitsLength.INCHES) * screenDPI;
        double normalWidth = getCanvasScaledWidth();
        return (targetWidth/normalWidth) / getRenderer().getRenderScale();
    }

    /**
     * Re-centers and re-scales the viewport to it's default setting, if {@link #getDPIScaling()} is enabled, the drawing will be re-centered but not re-scaled
     */
    public void resetView() {
        if(getSkin() instanceof ViewportSkin skin){
            skin.resetView();
        }
    }

    /**
     * Internal use only: runs on every frame, renderers the current display mode and updates the viewport overlays
     */
    private void tick(){
        if(getSkin() == null || getRenderer() == null){
            return;
        }
        performanceTimer.start();

        updateCanvasToViewportTransforms();
        updateSceneToCanvasTransform();
        updateCanvasToSceneTransform();

        getRenderer().doRender();
        viewportOverlays.forEach(o -> {
            if(o.getEnabled()){
                o.onRenderTick();
            }
        });

        getRenderFlags().applyMarkedChanges();

        performanceTimer.finish();


        if(!getRenderer().isOpenGLRenderer() && performanceTimer.getElapsedTime() > 1000/60){
            DrawingBotV3.logger.finest("RENDERER TOOK: " + performanceTimer.getElapsedTimeFormatted() + " milliseconds" + " expected " + 1000/60);
        }
    }

    ////////////////////////////////////////////////////////

    /**
     * The scaling taken directly from the {@link #getCanvasToViewportTransform()}, used by masks to keep their stroke-width consistent
     */
    private DoubleProperty canvasToViewportScale;

    public double getCanvasToViewportScale() {
        return canvasToViewportScaleProperty().get();
    }

    public ReadOnlyDoubleProperty canvasToViewportScaleProperty() {
        if(canvasToViewportScale == null){
            canvasToViewportScale = new SimpleDoubleProperty();
            getCanvasToViewportTransform(); //init the transform
            updateCanvasToViewportTransforms();
        }
        return canvasToViewportScale;
    }

    private Affine canvasToViewportTransform;

    /**
     * @return JavaFX transform which converts from canvas space to scene space, used by masks to position them relative to the renderer
     */
    public Affine getCanvasToViewportTransform() {
        if(canvasToViewportTransform == null){
            canvasToViewportTransform = new Affine();
            updateCanvasToViewportTransforms();
        }
        return canvasToViewportTransform;
    }

    private void updateCanvasToViewportTransforms() {
        if(canvasToViewportTransform == null){
            return;
        }
        if(getRenderer() == null || getCanvas() == null){
            canvasToViewportTransform.setToIdentity();
            return;
        }
        canvasToViewportTransform.setToTransform(getRenderer().getRendererToSceneTransform());
        canvasToViewportTransform.prependTranslation((float) -getViewportX(), (float) -getViewportY(), 0F);
        canvasToViewportTransform.appendScale(getCanvasScale(), getCanvasScale());
        canvasToViewportTransform.appendScale(getCanvasUnits().convertToMM, getCanvasUnits().convertToMM);
        if(canvasToViewportScale != null){
            canvasToViewportScale.set(Math.abs(1 / canvasToViewportTransform.getMxx()));
        }
    }


    ////////////////////////////////////////////////////////

    private Affine sceneToCanvasTransform;

    /**
     * @return JavaFX transform which converts from scene space to canvas space, the values returned will be in the units of the canvas
     */
    public Affine getSceneToCanvasTransform(){
        if(sceneToCanvasTransform == null){
            sceneToCanvasTransform = new Affine();
            updateSceneToCanvasTransform();
        }
        return sceneToCanvasTransform;
    }

    private void updateSceneToCanvasTransform(){
        if(sceneToCanvasTransform == null){
            return;
        }
        if(getRenderer() == null || getCanvas() == null) {
            sceneToCanvasTransform.setToIdentity();
            return;
        }
        sceneToCanvasTransform.setToTransform(getRenderer().getSceneToRendererTransform());
        sceneToCanvasTransform.prependScale(1 / getCanvasScale(), 1 / getCanvasScale());
        sceneToCanvasTransform.prependScale(1 / getCanvasUnits().convertToMM, 1 / getCanvasUnits().convertToMM);
    }


    ////////////////////////////////////////////////////////

    private Affine canvasToSceneTransform;

    /**
     * @return JavaFX transform which converts from scene space to canvas space, from canvas position in it's own input units
     */
    public Affine getCanvasToSceneTransform(){
        if(canvasToSceneTransform == null){
            canvasToSceneTransform = new Affine();
            updateCanvasToSceneTransform();
        }
        return canvasToSceneTransform;
    }

    private void updateCanvasToSceneTransform(){
        if(canvasToSceneTransform == null){
            return;
        }
        if(getRenderer() == null || getCanvas() == null) {
            canvasToSceneTransform.setToIdentity();
            return;
        }
        canvasToSceneTransform.setToTransform(getRenderer().getRendererToSceneTransform());
        canvasToSceneTransform.appendScale(getCanvasScale(), getCanvasScale());
        canvasToSceneTransform.appendScale(getCanvasUnits().convertToMM, getCanvasUnits().convertToMM);
    }


    ////////////////////////////////////////////////////////

    /**
     * The current {@link ICanvas} used for scaling the viewport, it is bound to the value defined by the {@link DisplayModeBase#canvasProperty()}
     */
    private final ObjectProperty<ICanvas> canvas = new SimpleObjectProperty<>();

    public ICanvas getCanvas() {
        return canvas.get();
    }

    public ReadOnlyObjectProperty<ICanvas> canvasProperty() {
        return canvas;
    }

    private void setCanvas(ICanvas canvas) {
        this.canvas.set(canvas);
    }

    ////////////////////////////////////////////////////////

    /**
     * The active {@link RendererBase} being used to render the {@link DisplayModeBase}
     */
    public ObjectProperty<RendererBase> renderer = new SimpleObjectProperty<>();

    public RendererBase getRenderer() {
        return renderer.get();
    }

    public ReadOnlyObjectProperty<RendererBase> rendererProperty() {
        return renderer;
    }

    private void setRenderer(RendererBase renderer) {
        this.renderer.set(renderer);
    }

    ////////////////////////////////////////////////////////

    /**
     * The active {@link DisplayModeBase}
     */
    public ObjectProperty<DisplayModeBase> displayMode = new SimpleObjectProperty<>();

    public DisplayModeBase getDisplayMode() {
        return displayMode.get();
    }

    public ObjectProperty<DisplayModeBase> displayModeProperty() {
        return displayMode;
    }

    public void setDisplayMode(DisplayModeBase displayMode) {
        this.displayMode.set(displayMode);
    }

    ////////////////////////////////////////////////////////

    /**
     * JavaFX node for the current {@link RendererBase}
     */
    public ObjectProperty<Node> rendererNode = new SimpleObjectProperty<>();

    public Node getRendererNode() {
        return rendererNode.get();
    }

    public ObjectProperty<Node> rendererNodeProperty() {
        return rendererNode;
    }

    public void setRendererNode(Node rendererNode) {
        this.rendererNode.set(rendererNode);
    }

    ////////////////////////////////////////////////////////

    public ObservableSet<ViewportOverlayBase> viewportOverlays = FXCollections.observableSet();

    public ObservableSet<ViewportOverlayBase> getViewportOverlays() {
        return viewportOverlays;
    }

    public void setViewportOverlays(ObservableSet<ViewportOverlayBase> viewportOverlays) {
        this.viewportOverlays = viewportOverlays;
    }

    ////////////////////////////////////////////////////////

    public ObservableList<Node> backgroundOverlayNodes = FXCollections.observableArrayList();

    public ObservableList<Node> getBackgroundOverlayNodes() {
        return backgroundOverlayNodes;
    }

    public void setBackgroundOverlayNodes(ObservableList<Node> backgroundOverlayNodes) {
        this.backgroundOverlayNodes = backgroundOverlayNodes;
    }

    ////////////////////////////////////////////////////////

    public ObservableList<Node> foregroundOverlayNodes = FXCollections.observableArrayList();

    public ObservableList<Node> getForegroundOverlayNodes() {
        return foregroundOverlayNodes;
    }

    public void setForegroundOverlayNodes(ObservableList<Node> foregroundOverlayNodes) {
        this.foregroundOverlayNodes = foregroundOverlayNodes;
    }

    ////////////////////////////////////////////////////////

    public final ObjectProperty<EnumBlendMode> rendererBlendMode = new SimpleObjectProperty<>(EnumBlendMode.NORMAL);

    public EnumBlendMode getRendererBlendMode() {
        return rendererBlendMode.get();
    }

    public ObjectProperty<EnumBlendMode> rendererBlendModeProperty() {
        return rendererBlendMode;
    }

    public void setRendererBlendMode(EnumBlendMode rendererBlendMode) {
        this.rendererBlendMode.set(rendererBlendMode);
    }

    ////////////////////////////////////////////////////////

    public DoubleProperty zoom = new SimpleDoubleProperty(1D);

    public double getZoom() {
        return zoom.get();
    }

    public DoubleProperty zoomProperty() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom.set(zoom);
    }

    ////////////////////////////////////////////////////////

    public DoubleProperty zoomIntensity = new SimpleDoubleProperty(0.02D);

    public double getZoomIntensity() {
        return zoomIntensity.get();
    }

    public DoubleProperty zoomIntensityProperty() {
        return zoomIntensity;
    }

    public void setZoomIntensity(double zoomIntensity) {
        this.zoomIntensity.set(zoomIntensity);
    }

    ////////////////////////////////////////////////////////

    public BooleanProperty useDPIScaling = new SimpleBooleanProperty(false);

    public boolean useDPIScaling() {
        return useDPIScaling.get();
    }

    public BooleanProperty useDPIScalingProperty() {
        return useDPIScaling;
    }

    public void useDPIScaling(boolean useDPIScaling) {
        this.useDPIScaling.set(useDPIScaling);
    }

    ////////////////////////////////////////////////////////

    private final DoubleProperty dpiScaling = new SimpleDoubleProperty();

    public double getDPIScaling() {
        return dpiScaling.get();
    }

    public ReadOnlyDoubleProperty dpiScalingProperty() {
        return dpiScaling;
    }

    private void setDPIScaling(double dpiScaling) {
        this.dpiScaling.set(dpiScaling);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Viewport -> Canvas Scaling
     * Additional scaling applied to the canvas when placed in a viewport
     *
     * This scaling is used to "scale to fit" the canvas, so when resizing the DrawingBotV3 window the canvas will remain fixed within the viewport
     *
     * It also means that using a {@link Viewport#getZoom()} of 1.0 will result in a canvas matching the viewports dimensions
     */
    private final DoubleProperty scaleToFit = new SimpleDoubleProperty(1);

    public double getScaleToFit() {
        return scaleToFit.get();
    }

    public ReadOnlyDoubleProperty scaleToFitProperty() {
        return scaleToFit;
    }

    private void setScaleToFit(double scaleToFit) {
        this.scaleToFit.set(scaleToFit);
    }

    ////////////////////////////////////////////////////////

    private final DoubleProperty viewportX = new SimpleDoubleProperty(0);

    public double getViewportX() {
        return viewportX.get();
    }

    public ReadOnlyDoubleProperty viewportXProperty() {
        return viewportX;
    }

    protected void setViewportX(double viewportX) {
        this.viewportX.set(viewportX);
    }

    ////////////////////////////////////////////////////////

    private final DoubleProperty viewportY = new SimpleDoubleProperty(0);

    public double getViewportY() {
        return viewportY.get();
    }

    public ReadOnlyDoubleProperty viewportYProperty() {
        return viewportY;
    }

    protected void setViewportY(double viewportY) {
        this.viewportY.set(viewportY);
    }

    ////////////////////////////////////////////////////////

    private final DoubleProperty viewportWidth = new SimpleDoubleProperty(0);

    public double getViewportWidth() {
        return viewportWidth.get();
    }

    public ReadOnlyDoubleProperty viewportWidthProperty() {
        return viewportWidth;
    }

    protected void setViewportWidth(double viewportWidth) {
        this.viewportWidth.set(viewportWidth);
    }

    ////////////////////////////////////////////////////////

    private final DoubleProperty viewportHeight = new SimpleDoubleProperty(0);

    public double getViewportHeight() {
        return viewportHeight.get();
    }

    public ReadOnlyDoubleProperty viewportHeightProperty() {
        return viewportHeight;
    }

    protected void setViewportHeight(double viewportHeight) {
        this.viewportHeight.set(viewportHeight);
    }

    ////////////////////////////////////////////////////////

    private DoubleProperty relativeMouseX;

    public double getRelativeMouseX() {
        return relativeMouseXProperty().get();
    }

    public ReadOnlyDoubleProperty relativeMouseXProperty() {
        if(relativeMouseX == null){
            initRelativeMouseValues();
        }
        return relativeMouseX;
    }

    private DoubleProperty relativeMouseY;

    public double getRelativeMouseY() {
        return relativeMouseYProperty().get();
    }

    public DoubleProperty relativeMouseYProperty() {
        if(relativeMouseY == null){
            initRelativeMouseValues();
        }
        return relativeMouseY;
    }

    private void initRelativeMouseValues() {
        this.relativeMouseX = new SimpleDoubleProperty(0);
        this.relativeMouseY = new SimpleDoubleProperty(0);
        addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            Point2D point2D = getSceneToCanvasTransform().transform(event.getSceneX(), event.getSceneY());
            relativeMouseX.set(point2D.getX());
            relativeMouseY.set(point2D.getY());
        });
        addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            relativeMouseX.set(0);
            relativeMouseY.set(0);
        });
    }

    ////////////////////////////////////////////////////////

    /**
     * The {@link ICanvas#getUnits()} of the current canvas, bound for simplicity with non-observable {@link ICanvas} implementations
     */
    private final ObjectProperty<UnitsLength> canvasUnits = new SimpleObjectProperty<>(UnitsLength.PIXELS);

    public UnitsLength getCanvasUnits() {
        return canvasUnits.get();
    }

    public ReadOnlyObjectProperty<UnitsLength> canvasUnitsProperty() {
        return canvasUnits;
    }

    protected void setCanvasUnits(UnitsLength canvasUnits) {
        this.canvasUnits.set(canvasUnits);
    }

    ////////////////////////////////////////////////////////

    private final DoubleProperty canvasScale = new SimpleDoubleProperty(1);

    public double getCanvasScale() {
        return canvasScale.get();
    }

    public DoubleProperty canvasScaleProperty() {
        return canvasScale;
    }

    public void setCanvasScale(double canvasScale) {
        this.canvasScale.set(canvasScale);
    }

    ////////////////////////////////////////////////////////

    private final ObjectProperty<Color> viewportBackgroundColor = new SimpleObjectProperty<>(Color.DARKGRAY);

    public Color getViewportBackgroundColor() {
        return viewportBackgroundColor.get();
    }

    public ObjectProperty<Color> viewportBackgroundColorProperty() {
        return viewportBackgroundColor;
    }

    public void setViewportBackgroundColor(Color viewportBackgroundColor) {
        this.viewportBackgroundColor.set(viewportBackgroundColor);
    }

    ////////////////////////////////////////////////////////

    /**
     * The {@link ICanvas#getWidth()} of the current canvas, bound for simplicity with non-observable {@link ICanvas} implementations
     */
    private final DoubleProperty canvasWidth = new SimpleDoubleProperty(0);

    public double getCanvasWidth() {
        return canvasWidth.get();
    }

    public ReadOnlyDoubleProperty canvasWidthProperty() {
        return canvasWidth;
    }

    protected void setCanvasWidth(double canvasWidth) {
        this.canvasWidth.set(canvasWidth);
    }

    ////////////////////////////////////////////////////////

    /**
     * The {@link ICanvas#getHeight()} of the current canvas, bound for simplicity with non-observable {@link ICanvas} implementations
     */
    private final DoubleProperty canvasHeight = new SimpleDoubleProperty(0);

    public double getCanvasHeight() {
        return canvasHeight.get();
    }

    public ReadOnlyDoubleProperty canvasHeightProperty() {
        return canvasHeight;
    }

    protected void setCanvasHeight(double canvasHeight) {
        this.canvasHeight.set(canvasHeight);
    }

    ////////////////////////////////////////////////////////

    /**
     * The {@link ICanvas#getScaledWidth()} of the current canvas, bound for simplicity with non-observable {@link ICanvas} implementations
     */
    private final DoubleProperty canvasScaledWidth = new SimpleDoubleProperty(0);

    public double getCanvasScaledWidth() {
        return canvasScaledWidth.get();
    }

    public ReadOnlyDoubleProperty canvasScaledWidthProperty() {
        return canvasScaledWidth;
    }

    protected void setCanvasScaledWidth(double canvasScaledWidth) {
        this.canvasScaledWidth.set(canvasScaledWidth);
    }

    ////////////////////////////////////////////////////////

    /**
     * The {@link ICanvas#getScaledHeight()} of the current canvas, bound for simplicity with non-observable {@link ICanvas} implementations
     */
    private final DoubleProperty canvasScaledHeight = new SimpleDoubleProperty(0);

    public double getCanvasScaledHeight() {
        return canvasScaledHeight.get();
    }

    public ReadOnlyDoubleProperty canvasScaledHeightProperty() {
        return canvasScaledHeight;
    }

    protected void setCanvasScaledHeight(double canvasScaledHeight) {
        this.canvasScaledHeight.set(canvasScaledHeight);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The actual width of the renderer in the viewport
     * e.g. if this is a JFXRenderer this will be the JavaFX Canvas width
     */
    private final DoubleProperty displayedWidth = new SimpleDoubleProperty(0);

    public double getDisplayedWidth() {
        return displayedWidth.get();
    }

    public ReadOnlyDoubleProperty displayedWidthProperty() {
        return displayedWidth;
    }

    protected void setDisplayedWidth(double displayedWidth) {
        this.displayedWidth.set(displayedWidth);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The actual height of the renderer in the viewport
     * e.g. if this is a JFXRenderer this will be the JavaFX Canvas height
     */
    private final DoubleProperty displayedHeight = new SimpleDoubleProperty(0);

    public double getDisplayedHeight() {
        return displayedHeight.get();
    }

    public ReadOnlyDoubleProperty displayedHeightProperty() {
        return displayedHeight;
    }

    protected void setDisplayedHeight(double displayedHeight) {
        this.displayedHeight.set(displayedHeight);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    protected Skin<?> createDefaultSkin() {
        return new ViewportSkin(this);
    }
}
