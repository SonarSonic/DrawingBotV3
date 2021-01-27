package drawingbot.drawing;

import drawingbot.DrawingBotV3;
import drawingbot.helpers.ImageTools;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

public class ObservableDrawingPen implements IDrawingPen {

    public SimpleIntegerProperty penNumber;
    public SimpleBooleanProperty enable; //if the pen should be enabled in renders / exports
    public SimpleStringProperty name; //manufacturers colour/pen name
    public SimpleObjectProperty<Color> javaFXColour; //rgb pen colour
    public SimpleIntegerProperty distributionWeight; //weight
    public SimpleStringProperty currentPercentage; //percentage
    public SimpleIntegerProperty currentLines; //lines

    public ObservableDrawingPen(int penNumber, IDrawingPen source){
        this(penNumber, source.getName(), source.getRGBColour());
    }

    public ObservableDrawingPen(int penNumber, String name, int colour){
        this.penNumber = new SimpleIntegerProperty(penNumber);
        this.enable = new SimpleBooleanProperty(true);
        this.name = new SimpleStringProperty(name);
        this.javaFXColour = new SimpleObjectProperty<>(ImageTools.getColorFromARGB(colour));
        this.distributionWeight = new SimpleIntegerProperty(100);
        this.currentPercentage = new SimpleStringProperty("0.0");
        this.currentLines = new SimpleIntegerProperty(0);

        this.enable.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.name.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.javaFXColour.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
        this.distributionWeight.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingPenChanged());
    }

    public boolean isEnabled(){
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
