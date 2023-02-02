package drawingbot.javafx.observables;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingStats;
import drawingbot.utils.Utils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.LinkedHashMap;

public class ObservableDrawingStats {

    public SimpleDoubleProperty totalTravelMM = new SimpleDoubleProperty();
    public SimpleDoubleProperty distanceUpMM = new SimpleDoubleProperty();
    public SimpleDoubleProperty distanceDownMM = new SimpleDoubleProperty();

    public SimpleDoubleProperty maxX = new SimpleDoubleProperty();
    public SimpleDoubleProperty maxY = new SimpleDoubleProperty();

    public SimpleLongProperty geometryCount = new SimpleLongProperty();
    public SimpleLongProperty coordCount = new SimpleLongProperty();
    public SimpleLongProperty penLifts = new SimpleLongProperty();
    public SimpleObjectProperty<LinkedHashMap<DrawingPen, Double>> penStats = new SimpleObjectProperty<>(new LinkedHashMap<>());

    public void updateFromStatic(DrawingStats stats){
        this.totalTravelMM.set(Utils.roundToPrecision(stats.distanceUpMM + stats.distanceDownMM, 3));
        this.distanceUpMM.set(stats.distanceUpMM);
        this.distanceDownMM.set(stats.distanceDownMM);
        this.maxX.set(stats.maxX);
        this.maxY.set(stats.maxY);
        this.geometryCount.set(stats.geometryCount);
        this.coordCount.set(stats.coordCount);
        this.penLifts.set(stats.penLifts);
        this.penStats.set(stats.penStats);
    }

    public void reset(){
        this.totalTravelMM.set(0);
        this.distanceUpMM.set(0);
        this.distanceDownMM.set(0);
        this.maxX.set(0);
        this.maxY.set(0);
        this.geometryCount.set(0);
        this.coordCount.set(0);
        this.penLifts.set(0);
        this.penStats.set(new LinkedHashMap<>());
    }
}

