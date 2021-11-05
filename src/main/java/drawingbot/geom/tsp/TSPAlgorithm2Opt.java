package drawingbot.geom.tsp;

import drawingbot.geom.tree.MSTVertex;
import drawingbot.geom.tree.NodeEdge;
import drawingbot.geom.tree.NodeGraph;
import drawingbot.pfm.helpers.TSPHelper;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TSPAlgorithm2Opt extends TSPAlgorithmAbstract {

    // all the nodes in the order of their rank
	public List<TSPNode> orderedNodes;
	public List<NodeEdge> orderedEdges;

    // How many times the tour has been improved
	public int currentIteration = 0;
	public int currentImprovement = 0;
	public int currentChanges = 0;
	public double totalGain = 0;

    // When set to something other than -1 and findOptimalRoute is disabled the algorithm will stop when the iterations have been reached
    public int targetIterations = -1;

    // A function which is called every time an improvement has been attempted in the current iteration, a progress of -1 means the end is undefined
    public Consumer<Float> progressCallback = null;

	// A function which is called every time an improvement has been made in the current iteration
	public Consumer<TSPAlgorithm2Opt> improvementCallback = null;

    // A function which should return true if the process has been cancelled
    public Supplier<Boolean> cancelCallback = null;

    /**
     * Constructor that creates an instance of the Lin-Kerninghan problem without
     * the optimizations. (Basically the tour it has is the drunken sailor)
     */
    public TSPAlgorithm2Opt(List<Coordinate> coordinateList) {
    	super(coordinateList, new NodeGraph(coordinateList).nodes);
        this.orderedNodes = nodes;
    }


    public double currentDistance = -1;
    public double lastDistance = -1;

    public void run(){

    	currentIteration = 0;

		while ((currentDistance = getDistance()) < lastDistance  ||  lastDistance == -1) {
			lastDistance = currentDistance;

			for ( int i = 1; i < size-1; i++ ){
				currentImprovement = i;

				for ( int k = i + 1; k < size; k++){
					if(getDistance(i, k) < getDistance(i, i-1) || getDistance(i, k) < getDistance(i, i+1)){
						List<TSPNode> newTour = new ArrayList<>(tour);
						twoOptSwap(newTour, i, k);
						double newDistance = getDistance(newTour);

						if(newDistance < currentDistance){
							tour = newTour;
							currentChanges++;
							totalGain += currentDistance - newDistance;
							currentDistance = newDistance;
						}
					}
				}
				improvementCallback.accept(this);
				progressCallback.accept((float)i / size);
			}
			currentIteration++;
			if(cancelCallback.get()){
				break;
			}
		}
	}

	public void twoOptSwap(List<TSPNode> newTour, int i, int k ){
		// 1. take route[0] to route[i-1] and add them in order to new_route
		for ( int c = 0; c <= i - 1; ++c ){
			newTour.set(c, tour.get(c));
		}

		// 2. take route[i] to route[k] and add them in reverse order to new_route
		int dec = 0;
		for ( int c = i; c <= k; ++c ){
			newTour.set(c, tour.get(k - dec));
			dec++;
		}

		// 3. take route[k+1] to end and add them in order to new_route
		for ( int c = k + 1; c < size; ++c ){
			newTour.set(c, tour.get(c));
		}
	}
    
    /**
     * This function returns the nearest neighbor for an specific node
     * @param index index of the node
     * @return the index of the nearest node
     */
    public int getNearestNeighbor(int index) {
    	TSPNode node = tour.get(index);

		double minLength = Double.MAX_VALUE;
		MSTVertex dest = null;

		for(MSTVertex vertex : node.vertexData.nearestVertices){
			int i = vertex.node.id;

			double length = getDistance(node.id, i);
			if(length < minLength && i != node.id){
				minLength = length;
				dest = vertex;
			}
		}

    	if(dest != null){
    		return getIndex(dest.node.id);
		}
    	throw new NullPointerException("Missing Nearest Neighbours");
    }
}