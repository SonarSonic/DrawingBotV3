package drawingbot.geom;

import drawingbot.api.IProperties;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.render.shapes.JFXShape;
import drawingbot.render.shapes.JFXShapeList;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class MaskingSettings implements IProperties {

    ///////////////////////////////////////////////

    public final SimpleBooleanProperty enableMasking = new SimpleBooleanProperty(true);

    public boolean getEnableMasking() {
        return enableMasking.get();
    }

    public SimpleBooleanProperty enableMaskingProperty() {
        return enableMasking;
    }

    public void setEnableMasking(boolean enableMasking) {
        this.enableMasking.set(enableMasking);
    }

    ///////////////////////////////////////////////

    public final SimpleBooleanProperty showMasks = new SimpleBooleanProperty(true);

    public boolean isShowMasks() {
        return showMasks.get();
    }

    public SimpleBooleanProperty showMasksProperty() {
        return showMasks;
    }

    public void setShowMasks(boolean showMasks) {
        this.showMasks.set(showMasks);
    }

    ///////////////////////////////////////////////

    public final SimpleBooleanProperty softClipping = new SimpleBooleanProperty(false);

    public boolean isSoftClipping() {
        return softClipping.get();
    }

    public SimpleBooleanProperty softClippingProperty() {
        return softClipping;
    }

    public void setSoftClipping(boolean softClipping) {
        this.softClipping.set(softClipping);
    }

    ///////////////////////////////////////////////

    public final SimpleObjectProperty<JFXShapeList> shapeList = new SimpleObjectProperty<>();

    public JFXShapeList getShapeList() {
        return shapeList.get();
    }

    public SimpleObjectProperty<JFXShapeList> shapeListProperty() {
        return shapeList;
    }

    public void setShapeList(JFXShapeList shapeList) {
        this.shapeList.set(shapeList);
    }

    {
        shapeList.addListener((observable, oldValue, newValue) -> {
            newValue.getShapeList().addListener((ListChangeListener<JFXShape>) c -> {
                while (c.next()) {
                    for (JFXShape g : c.getRemoved()) {
                        g.displayedProperty().unbind();
                    }
                    for (JFXShape g : c.getAddedSubList()) {
                        g.displayedProperty().bind(enableMasking.and(showMasks.and(g.enabledProperty())));
                    }
                }
            });
        });
        shapeList.set(new JFXShapeList());
    }

    public ObservableList<JFXShape> getMasks(){
        return shapeList.get().getShapeList();
    }

    public void addShape(JFXShape shape){
        shapeList.get().addShapeLogged(shape);
    }

    public void addShape(JFXShape shape, JFXShape.Type type, String name){
        addShape(shape);
        shape.setType(type);
        shape.setName(name);
    }

    public void removeShape(JFXShape shape){
        shapeList.get().removeShapeLogged(shape);
    }

    ///////////////////////////////////////////////

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(enableMasking, showMasks, softClipping);
        }
        return propertyList;
    }
}