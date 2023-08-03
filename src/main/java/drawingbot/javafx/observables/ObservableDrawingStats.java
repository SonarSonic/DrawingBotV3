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

    public SimpleDoubleProperty totalTravelM = new SimpleDoubleProperty();
    public SimpleDoubleProperty distanceUpM = new SimpleDoubleProperty();
    public SimpleDoubleProperty distanceDownM = new SimpleDoubleProperty();

    public SimpleDoubleProperty pageWidth = new SimpleDoubleProperty();
    public SimpleDoubleProperty pageHeight = new SimpleDoubleProperty();
    public SimpleDoubleProperty drawingWid = new SimpleDoubleProperty();
    public SimpleDoubleProperty drawingHeight = new SimpleDoubleProperty();
    public SimpleObjectProperty<UnitsLength> drawingUnits = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);

    public SimpleDoubleProperty minX = new SimpleDoubleProperty();
    public SimpleDoubleProperty minY = new SimpleDoubleProperty();

    public SimpleDoubleProperty maxX = new SimpleDoubleProperty();
    public SimpleDoubleProperty maxY = new SimpleDoubleProperty();

    public SimpleLongProperty geometryCount = new SimpleLongProperty();
    public SimpleLongProperty coordCount = new SimpleLongProperty();
    public SimpleLongProperty penLifts = new SimpleLongProperty();
    public SimpleObjectProperty<LinkedHashMap<DrawingPen, Double>> penStats = new SimpleObjectProperty<>(new LinkedHashMap<>());

    public void updateFromStatic(DrawingStats stats){
        this.totalTravelM.set(Utils.roundToPrecision(stats.distanceUpM + stats.distanceDownM, 2));
        this.distanceUpM.set(stats.distanceUpM);
        this.distanceDownM.set(stats.distanceDownM);
        this.pageWidth.set(stats.pageWidth);
        this.pageHeight.set(stats.pageHeight);
        this.drawingWid.set(stats.drawingWidth);
        this.drawingHeight.set(stats.drawingHeight);
        this.drawingUnits.set(stats.drawingUnits);
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
        this.totalTravelM.set(0);
        this.distanceUpM.set(0);
        this.distanceDownM.set(0);
        this.drawingUnits.set(UnitsLength.MILLIMETRES);
        this.pageWidth.set(0);
        this.pageHeight.set(0);
        this.drawingWid.set(0);
        this.drawingHeight.set(0);
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

