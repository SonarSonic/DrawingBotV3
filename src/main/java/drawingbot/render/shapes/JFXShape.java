package drawingbot.render.shapes;

import drawingbot.geom.GeometryUtils;
import drawingbot.geom.shapes.GPath;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.JFXAWTUtils;
import drawingbot.render.overlays.ShapeOverlays;
import drawingbot.utils.Utils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Affine;

import java.awt.geom.AffineTransform;
import java.util.UUID;

public class JFXShape {

    public static final String SHAPE_STYLE_CLASS = "jfx-shape";

    public UUID uuid;
    public IGeometry geometry;
    public IGeometry transformed;
    public final Shape jfxShape;

    public AffineTransform awtTransform = new AffineTransform();
    private final Affine jfxTransform;

    /**
     * Recommended for SVGs
     */
    public boolean useFastScaling;

    public JFXShape(IGeometry geometry) {
        this(geometry, false);
    }

    public JFXShape(IGeometry geometry, boolean useFastScaling) {
        this.uuid = UUID.randomUUID();
        this.useFastScaling = useFastScaling;
        this.geometry = new GPath(geometry.getAWTShape());
        this.transformed = this.geometry;
        this.jfxTransform = new Affine();

        GeometryUtils.copyGeometryData(this.transformed, geometry);

        this.jfxShape = GeometryUtils.convertGeometryToJFXShape(transformed);
        this.jfxShape.getTransforms().add(ShapeOverlays.INSTANCE.globalTransform);
        if(useFastScaling){
            this.jfxShape.getTransforms().add(jfxTransform);
        }
        this.jfxShape.setManaged(false);
        this.jfxShape.setPickOnBounds(false);

        this.jfxShape.getStyleClass().setAll(SHAPE_STYLE_CLASS);

        JFXShapeManager.INSTANCE.initJFXGeometry(this);
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
        awtTransform = newTransform;
        updateFromTransform(newTransform);
    }

    public void updateFromTransform(AffineTransform newTransform){
        transformed = geometry.copyGeometry().transformGeometry(newTransform);

        if(useFastScaling) {
            JFXAWTUtils.updateJFXAffineTransform(jfxTransform, newTransform);
        }else{
            GeometryUtils.updateJFXShapeFromGeometry(jfxShape, transformed);
        }
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

    public enum Type{
        RESHAPE,
        SUBTRACT,
        ADD,
        NONE;

        @Override
        public String toString() {
            return Utils.capitalize(name());
        }
    }


    public static final PseudoClass RESHAPE_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("reshape");
    public static final PseudoClass ADD_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("masking-add");
    public static final PseudoClass SUBTRACT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("masking-remove");

    public void updatePseudoClassState(){
        jfxShape.pseudoClassStateChanged(RESHAPE_PSEUDOCLASS_STATE, type.get()== Type.RESHAPE);
        jfxShape.pseudoClassStateChanged(SUBTRACT_PSEUDOCLASS_STATE, type.get()== Type.SUBTRACT);
        jfxShape.pseudoClassStateChanged(ADD_PSEUDOCLASS_STATE, type.get()== Type.ADD);
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