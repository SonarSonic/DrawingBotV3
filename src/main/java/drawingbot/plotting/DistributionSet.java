package drawingbot.plotting;

import drawingbot.geom.shapes.IGeometry;
import drawingbot.javafx.observables.ObservableDrawingPen;

import java.util.ArrayList;
import java.util.List;

public class DistributionSet {

    public final PlottedDrawing plottedDrawing;
    public final PlottedGroup source;
    public final List<ObservableDrawingPen> globalRenderOrder;
    public final List<PlottedGroup> plottedGroups;
    private final List<IGeometry> geometries;

    public DistributionSet(PlottedDrawing plottedDrawing, List<PlottedGroup> plottedGroups){
        this.plottedDrawing = plottedDrawing;
        this.source = plottedGroups.get(0);
        this.plottedGroups = plottedGroups;
        this.globalRenderOrder = PlottedDrawing.getGlobalRenderOrder(plottedGroups);
        if(plottedGroups.size() == 1){
            this.geometries = plottedGroups.get(0).geometries;
        }else{
            this.geometries = new ArrayList<>();
            this.plottedGroups.forEach(group -> geometries.addAll(group.geometries));
        }
    }

    public void clear(){
        if(plottedGroups.size() > 1){
            geometries.clear();
        }
    }

    public int getGeometryCount(){
        return geometries.size();
    }

    public List<IGeometry> getGeometryList(){
        return geometries;
    }

}