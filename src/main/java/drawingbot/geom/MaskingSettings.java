package drawingbot.geom;

import drawingbot.api.IProperties;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.render.shapes.JFXShape;
import drawingbot.render.shapes.JFXShapeList;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class MaskingSettings implements IProperties {

    ///////////////////////////////////////////////

    public final SimpleBooleanProperty bypassMasking = new SimpleBooleanProperty(false);

    public boolean isBypassMasking() {
        return bypassMasking.get();
    }

    public SimpleBooleanProperty bypassMaskingProperty() {
        return bypassMasking;
    }

    public void setBypassMasking(boolean bypassMasking) {
        this.bypassMasking.set(bypassMasking);
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

    public final SimpleObjectProperty<JFXShapeList> shapeList = new SimpleObjectProperty<>(new JFXShapeList());

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
                        g.displayedProperty().bind(showMasks.and(g.enabledProperty()));
                    }
                }
            });
        });


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

    public final ObservableList<Observable> observables = PropertyUtil.createPropertiesList(bypassMasking, showMasks);

    @Override
    public ObservableList<Observable> getObservables() {
        return observables;
    }
}