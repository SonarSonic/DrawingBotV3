package drawingbot.geom.masking;

import drawingbot.api.IProperties;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.render.shapes.JFXShape;
import drawingbot.render.shapes.JFXShapeManager;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class MaskingSettings implements IProperties {

    public final SimpleBooleanProperty bypassMasking = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty showMasks = new SimpleBooleanProperty(true);
    {
        JFXShapeManager.INSTANCE.globalShapeList.getShapeList().addListener((ListChangeListener<JFXShape>) c -> {
            while (c.next()) {
                for (JFXShape g : c.getRemoved()) {
                    g.displayedProperty().unbind();
                }
                for (JFXShape g : c.getAddedSubList()) {
                    g.displayedProperty().bind(showMasks.and(g.enabledProperty()));
                }
            }
        });
    }

    public final ObservableList<Property<?>> observables = PropertyUtil.createPropertiesList(bypassMasking);

    public ObservableList<JFXShape> getMasks(){
        return JFXShapeManager.INSTANCE.globalShapeList.getShapeList();
    }

    public void addShape(JFXShape shape){
        JFXShapeManager.INSTANCE.globalShapeList.addShapeLogged(shape);
    }

    public void addShape(JFXShape shape, JFXShape.Type type, String name){
        addShape(shape);
        shape.setType(type);
        shape.setName(name);
    }

    public void removeShape(JFXShape shape){
        JFXShapeManager.INSTANCE.globalShapeList.removeShapeLogged(shape);
    }

    public GeometryMask createGeometryMask(){
        if(bypassMasking.get() || JFXShapeManager.INSTANCE.globalShapeList.getShapeList().isEmpty()){
            return null;
        }
        GeometryMask mask = new GeometryMask();
        for(JFXShape geometry : JFXShapeManager.INSTANCE.globalShapeList.getShapeList()){
            if(geometry.isEnabled()){
                switch (geometry.type.get()){
                    case SUBTRACT -> mask.subtract.add(geometry.transformed.copyGeometry());
                    case ADD -> mask.add.add(geometry.transformed.copyGeometry());
                }
            }
        }
        return mask;
    }

    @Override
    public ObservableList<Property<?>> getProperties() {
        return observables;
    }
}