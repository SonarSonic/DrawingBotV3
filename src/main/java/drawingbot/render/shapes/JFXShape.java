package drawingbot.render.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.GPath;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.JFXAWTUtils;
import drawingbot.registry.MasterRegistry;
import drawingbot.render.overlays.ShapeOverlays;
import drawingbot.utils.Utils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.transform.Affine;

import java.awt.geom.AffineTransform;
import java.util.UUID;

public class JFXShape {

    public static final String SHAPE_STYLE_CLASS = "jfx-shape";

    public UUID uuid;
    public IGeometry geometry;
    public IGeometry transformed;
    public Path jfxShape;

    public AffineTransform awtTransform = new AffineTransform();
    private Affine jfxTransform;

    /**
     * Recommended for SVGs
     */
    public boolean useFastScaling;

    public JFXShape(){}

    public JFXShape(IGeometry geometry) {
        this(geometry, false);
    }

    public JFXShape(IGeometry geometry, boolean useFastScaling) {
        init(geometry, useFastScaling);
    }

    public void init(IGeometry geometry, boolean useFastScaling){
        this.uuid = UUID.randomUUID();
        this.useFastScaling = useFastScaling;
        this.geometry = new GPath(geometry.getAWTShape());
        this.transformed = this.geometry;
        this.jfxTransform = new Affine();
        GeometryUtils.copyGeometryData(this.transformed, geometry);

        this.jfxShape = (Path) MasterRegistry.INSTANCE.getFallbackJFXGeometryConverter().convert(geometry);//GeometryUtils.convertGeometryToJFXShape(transformed);
        this.jfxShape.getTransforms().add(ShapeOverlays.INSTANCE.globalTransform);
        this.jfxShape.strokeWidthProperty().bind(ShapeOverlays.INSTANCE.relativeStrokeSize);

        if(useFastScaling){
            this.jfxShape.getTransforms().add(jfxTransform);
        }

        this.jfxShape.setManaged(false);
        this.jfxShape.setPickOnBounds(false);

        this.jfxShape.getStyleClass().setAll(SHAPE_STYLE_CLASS);

        this.drawingProperty().addListener(observable -> updatePseudoClassState());
        this.jfxShape.mouseTransparentProperty().bind(drawingProperty());

        JFXShapeManager.INSTANCE.initJFXGeometry(this);
        updatePseudoClassState();
    }

    private AffineTransform liveTransform = null;

    public void startTransform() {
        liveTransform = new AffineTransform(awtTransform);
    }

    public void transform(AffineTransform newTransform) {
        liveTransform = new AffineTransform(awtTransform);
        liveTransform.preConcatenate(newTransform);

        updateFromTransform(liveTransform);
    }

    public void cancelTransform() {
        updateFromTransform(awtTransform);
    }

    public void finishTransform(){
        liveTransform = null;
    }

    public AffineTransform getCurrentTransform() {
        return awtTransform;
    }

    public AffineTransform getLiveTransform() {
        return liveTransform;
    }

    public void setAwtTransform(AffineTransform newTransform){
        if(!newTransform.equals(awtTransform)){
            awtTransform = newTransform;
            updateFromTransform(newTransform);
        }
    }

    public void updateFromTransform(AffineTransform newTransform){
        transformed = geometry.copyGeometry().transformGeometry(newTransform);

        if(useFastScaling) {
            JFXAWTUtils.updateJFXAffineTransform(jfxTransform, newTransform);
        }else{
            GeometryUtils.updateJFXShapeFromGeometry(jfxShape, transformed);
        }
    }

    public boolean canDraw(){
        return editable.get() && geometry instanceof GPath && jfxShape instanceof Path;
    }

    //TODO UPDATE AWT PATH AT THE END IF IT'S BEING EDITED
    public void addElement(PathElement element){
        if(geometry instanceof GPath && jfxShape != null){
            GPath gPath = (GPath) geometry;
            Path jfxPath = jfxShape;

            jfxPath.getElements().add(element);
            JFXAWTUtils.addJFXElementToAWTPath(gPath.awtPath, element);
        }
    }

    /**
     * To draw onto the end of the path, we create an editable PathElement, but we don't add it too the AWT path, so we can easily remove it if the user cancels.
     */
    private transient PathElement tempElement;

    public void addTempNextElement(PathElement element){
        jfxShape.getElements().add(tempElement = element);
    }

    public void removeTempNextElement(){
        jfxShape.getElements().remove(tempElement);
        tempElement = null;
    }

    public void confirmTempNextElement(){
        JFXAWTUtils.addJFXElementToAWTPath(((GPath) geometry).awtPath, tempElement);
    }


    /**
     * Called when the Geometry shape itself has been edited, so erase the transform.
     */
    public void updateGeometryFromJFXShape(){
        GPath path = JFXAWTUtils.convertJFXPathToGPath(jfxShape);
        GeometryUtils.copyGeometryData(path, geometry);

        geometry = path;
        transformed = geometry;
        setAwtTransform(new AffineTransform());
    }


    public Bounds getDrawingBounds() {
        return JFXAWTUtils.getJFXBounds(transformed.getAWTShape().getBounds2D());
    }

    public Bounds getViewportBounds() {
        return jfxShape.getBoundsInParent();
    }

    ////////////////////////////

    public SimpleStringProperty name = new SimpleStringProperty("Shape");

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    ////////////////////////////

    public SimpleBooleanProperty deletable = new SimpleBooleanProperty(true);

    public boolean isDeletable() {
        return deletable.get();
    }

    public SimpleBooleanProperty deletableProperty() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable.set(deletable);
    }

    ////////////////////////////

    public SimpleBooleanProperty selectable = new SimpleBooleanProperty(true);

    public boolean isSelectable() {
        return selectable.get();
    }

    public SimpleBooleanProperty selectableProperty() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable.set(selectable);
    }

    ////////////////////////////

    public SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    ////////////////////////////

    public SimpleBooleanProperty displayed = new SimpleBooleanProperty(true);

    public boolean isDisplayed() {
        return displayed.get();
    }

    public SimpleBooleanProperty displayedProperty() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed.set(displayed);
    }


    ////////////////////////////

    public SimpleBooleanProperty enabled = new SimpleBooleanProperty(true);

    public boolean isEnabled() {
        return enabled.get();
    }

    public SimpleBooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    ////////////////////////////

    /**
     * Currently only used to prevent the interior being filled which the shape is being drawin
     */
    public SimpleBooleanProperty drawing = new SimpleBooleanProperty(false);

    public boolean isDrawing() {
        return drawing.get();
    }

    public SimpleBooleanProperty drawingProperty() {
        return drawing;
    }

    public void setDrawing(boolean drawing) {
        this.drawing.set(drawing);
    }

    ////////////////////////////

    public SimpleBooleanProperty editable = new SimpleBooleanProperty(true);

    public boolean isEditable() {
        return editable.get();
    }

    public SimpleBooleanProperty editableProperty() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }

    ////////////////////////////

    public enum Type{
        SUBTRACT,
        ADD,
        SPECIAL,
        RESHAPE,
        NONE;

        @Override
        public String toString() {
            return Utils.capitalize(name());
        }
    }


    public static final PseudoClass RESHAPE_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("reshape");
    public static final PseudoClass ADD_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("masking-add");
    public static final PseudoClass SUBTRACT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("masking-remove");
    public static final PseudoClass DRAWING_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("masking-draw");

    public void updatePseudoClassState(){
        if(jfxShape == null){
            return;
        }
        jfxShape.pseudoClassStateChanged(RESHAPE_PSEUDOCLASS_STATE, !isDrawing() && type.get() == Type.RESHAPE);
        jfxShape.pseudoClassStateChanged(SUBTRACT_PSEUDOCLASS_STATE, !isDrawing() && type.get() == Type.SUBTRACT);
        jfxShape.pseudoClassStateChanged(ADD_PSEUDOCLASS_STATE, !isDrawing() && type.get() == Type.ADD);
        jfxShape.pseudoClassStateChanged(DRAWING_PSEUDOCLASS_STATE, isDrawing());
    }

    public ObjectProperty<Type> type = new ObjectPropertyBase<>() {

        public void invalidated() {
            updatePseudoClassState();
        }

        @Override
        public Object getBean() {
            return JFXShape.this;
        }

        @Override public String getName() {
            return "type";
        }
    };

    public Type getType() {
        return type.get();
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    public void onAdded(){

    }

    public void onRemove(){

    }

    ////////////////////////////

    public JFXShape copy(){
        JFXShape shape = new JFXShape(geometry, useFastScaling);
        shape.setAwtTransform(awtTransform);
        shape.setType(getType());
        shape.setName(getName());
        shape.setEnabled(isEnabled());

        return shape;
    }

}