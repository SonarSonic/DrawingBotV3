package drawingbot.pfm.modules.shapes;

import drawingbot.api.IPixelData;
import drawingbot.geom.basic.GEllipse;
import drawingbot.geom.basic.GLine;
import drawingbot.geom.tree.MSTEdge;
import drawingbot.geom.tree.MinimumSpanningTree;
import drawingbot.pfm.modules.PositionEncoder;
import drawingbot.pfm.modules.ShapeEncoder;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

// encodes a Minimum Spanning Tree, only activates on the final iteration
public class TreeShapeEncoder extends ShapeEncoder {

    public MinimumSpanningTree mst;
    public boolean create1Tree = false;

    @Override
    public void doProcess(IPixelData data, PositionEncoder positionEncoder) {
        if(pfmModular.currentIteration != pfmModular.targetIteration-1){

            List<Coordinate> coordinateList = positionEncoder.getCoordinates();
            for(Coordinate coord : coordinateList){
                pfmModular.task.addGeometry(new GEllipse.Filled((float)coord.x - 1/2F, (float)coord.y - 1/2F, 1, 1));
            }
            return;
        }

        pfmModular.task.updateProgress(-1 , 1);

        MinimumSpanningTree mst = new MinimumSpanningTree(positionEncoder.getCoordinates(), create1Tree){

            @Override
            public void onEdgeCreated(MSTEdge edge) {
                super.onEdgeCreated(edge);
                pfmModular.task.addGeometry(new GLine(edge.dest.coordX, edge.dest.coordY, edge.origin.coordX, edge.origin.coordY));
            }

            @Override
            public boolean shouldStop() {
                return pfmModular.task.isFinished();
            }
        };

        mst.progressCallback = f -> pfmModular.task.updateProgress(f, 1);


        pfmModular.task.updateMessage(create1Tree ? "Preparing Reference 1-tree" : "Preparing Minimum Spanning Tree");
        mst.prepareTree();
        pfmModular.task.updateMessage(create1Tree ? "Building Reference 1-tree" : "Building Minimum Spanning Tree");
        mst.buildTree();
        this.mst = mst;
    }

}
