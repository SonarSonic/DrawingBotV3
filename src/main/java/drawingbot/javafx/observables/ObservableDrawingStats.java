package drawingbot.javafx.observables;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingStats;
import drawingbot.utils.UnitsLength;
import drawingbot.utils.Utils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.LinkedHashMap;

public class ObservableDrawingStats {

    public SimpleDoubleProperty totalTravelMM = new SimpleDoubleProperty();
    public SimpleDoubleProperty distanceUpMM = new SimpleDoubleProperty();
    public SimpleDoubleProperty distanceDownMM = new SimpleDoubleProperty();

    public SimpleDoubleProperty pageWidthMM = new SimpleDoubleProperty();
    public SimpleDoubleProperty pageHeightMM = new SimpleDoubleProperty();
    public SimpleDoubleProperty drawingWidthMM = new SimpleDoubleProperty();
    public SimpleDoubleProperty drawingHeightMM = new SimpleDoubleProperty();
    public SimpleObjectProperty<UnitsLength> drawingUnitsMM = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);

    public SimpleDoubleProperty minX = new SimpleDoubleProperty();
    public SimpleDoubleProperty minY = new SimpleDoubleProperty();

    public SimpleDoubleProperty maxX = new SimpleDoubleProperty();
    public SimpleDoubleProperty maxY = new SimpleDoubleProperty();

    public SimpleLongProperty geometryCount = new SimpleLongProperty();
    public SimpleLongProperty coordCount = new SimpleLongProperty();
    public SimpleLongProperty penLifts = new SimpleLongProperty();
    public SimpleObjectProperty<LinkedHashMap<DrawingPen, Double>> penStats = new SimpleObjectProperty<>(new LinkedHashMap<>());

    public void updateFromStatic(DrawingStats stats){
        this.totalTravelMM.set(Utils.roundToPrecision(stats.distanceUpM + stats.distanceDownM, 3));
        this.distanceUpMM.set(stats.distanceUpM);
        this.distanceDownMM.set(stats.distanceDownM);
        this.pageWidthMM.set(stats.pageWidth);
        this.pageHeightMM.set(stats.pageHeight);
        this.drawingWidthMM.set(stats.drawingWidth);
        this.drawingHeightMM.set(stats.drawingHeight);
        this.minX.set(stats.minX);
        this.minY.set(stats.minY);
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
        this.drawingUnitsMM.set(UnitsLength.MILLIMETRES);
        this.pageWidthMM.set(0);
        this.pageHeightMM.set(0);
        this.drawingWidthMM.set(0);
        this.drawingHeightMM.set(0);
        this.minX.set(0);
        this.minX.set(0);
        this.maxX.set(0);
        this.maxY.set(0);
        this.geometryCount.set(0);
        this.coordCount.set(0);
        this.penLifts.set(0);
        this.penStats.set(new LinkedHashMap<>());
    }
}

