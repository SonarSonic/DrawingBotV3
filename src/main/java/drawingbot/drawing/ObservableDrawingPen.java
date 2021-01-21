package drawingbot.drawing;

import drawingbot.DrawingBotV3;
import drawingbot.helpers.ImageTools;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

public class ObservableDrawingPen implements IDrawingPen {

    public SimpleBooleanProperty enable; //if the pen should be enabled in renders / exports
    public SimpleStringProperty name; //manufacturers colour/pen name
    public SimpleObjectProperty<Color> javaFXColour; //rgb pen colour

    public ObservableDrawingPen(IDrawingPen source){
        this(source.getName(), source.getRGBColour());
    }

    public ObservableDrawingPen(String name, int colour){
        this.enable = new SimpleBooleanProperty(true);
        this.name = new SimpleStringProperty(name);
        this.javaFXColour = new SimpleObjectProperty<>(ImageTools.getColorFromARGB(colour));

        this.enable.addListener((observable, oldValue, newValue) -> onPropertiesChanged());
        this.name.addListener((observable, oldValue, newValue) -> onPropertiesChanged());
        this.javaFXColour.addListener((observable, oldValue, newValue) -> onPropertiesChanged());
    }

    public void onPropertiesChanged(){
        DrawingBotV3.INSTANCE.reRender();
        System.out.println("CHANGED PEN!");
    }

    public boolean isEnabled(){ //TODO MAKE THIS AFFECT EXPORTS (VIA PEN DISTRIBUTION = 0)
        return enable.get();
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public int getRGBColour() {
        return ImageTools.getARGBFromColor(javaFXColour.get());
    }

    @Override
    public String toString(){
        return getName();
    }
}
