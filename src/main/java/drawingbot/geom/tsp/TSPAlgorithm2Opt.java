package drawingbot.geom.tsp;

import drawingbot.geom.tree.*;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;
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

    public MinimumSpanningTree mst = null;
    public MSTGraph mstGraph = null;

    /**
     * Constructor that creates an instance of the Lin-Kerninghan problem without
     * the optimizations. (Basically the tour it has is the drunken sailor)
     */
    public TSPAlgorithm2Opt(List<Coordinate> coordinateList) {
    	super(coordinateList);
    }


    public double currentDistance = -1;
    public double lastDistance = -1;

    public void run(){
		this.mst = new MinimumSpanningTree(coordinates, false);
		this.mst.prepareTree();

		this.mstGraph = new MSTGraph(mst);
		this.nodes = new ArrayList<>(mstGraph.nodes.values());
		this.orderedNodes = nodes;

		initTour();

    	currentIteration = 0;
    	int passType = 0;
    	boolean shouldContinue = true;

		while (shouldContinue) {
			lastDistance = currentDistance;

			for ( int i = 1; i < size-1; i++ ){
				currentImprovement = i;

				switch (passType){
					case 0:
						findNextBestTourFast(i);
						break;
					case 1:
						findNextBestTourSlow(i);
						break;
					case 2:
						//findNextBestTourSlowest(i);
						break;
				}

				improvementCallback.accept(this);
				progressCallback.accept((float)i / size);
			}


			shouldContinue = (currentDistance = getDistance()) < lastDistance  ||  lastDistance == -1;

			///move onto the slow iteration if the fast iteration hasn't improved the tour
			if(!shouldContinue && passType != 2){
				passType++;
				currentDistance = -1;
				lastDistance = -1;
				shouldContinue = true;
			}

			currentIteration++;
			if(cancelCallback.get()){
				break;
			}
		}
	}

	private List<TSPNode> newTour = new ArrayList<>();

	public void findNextBestTourFast(int i){
		TSPNode node = tour.get(i);
		for(MSTVertex vertex : node.vertexData.edges.keySet()){
			int index = getIndex(vertex.node.id);

			if(getDistanceFromIndex(i, index) < getDistanceFromIndex(i, i-1) || getDistanceFromIndex(i, index) < getDistanceFromIndex(i, i+1)){
				List<TSPNode> newTour = new ArrayList<>(tour);

				twoOptSwap(newTour, i, index);
				double newDistance = getDistance(newTour);

				if(newDistance < currentDistance){
					tour = newTour;
					currentChanges++;
					totalGain += currentDistance - newDistance;
					currentDistance = newDistance;
					return;
				}
			}
		}
	}

	public void findNextBestTourSlow(int i){
		for (int k = i + 1; k < size; k++){
			if(getDistanceFromIndex(i, k) < getDistanceFromIndex(i, i-1) || getDistanceFromIndex(i, k) < getDistanceFromIndex(i, i+1)){
				List<TSPNode> newTour = new ArrayList<>(tour);

				twoOptSwap(newTour, i, k);
				double newDistance = getDistance(newTour);

				if(newDistance < currentDistance){
					tour = newTour;
					currentChanges++;
					totalGain += currentDistance - newDistance;
					currentDistance = newDistance;
					return;
				}
			}
		}
	}

	public void findNextBestTourSlowest(int i){
		for ( int k = i + 1; k < size; k++){
			newTour.clear();
			newTour.addAll(tour);

			twoOptSwap(newTour, i, k);
			double newDistance = getDistance(newTour);

			if(newDistance < currentDistance){
				tour = newTour;
				currentChanges++;
				totalGain += currentDistance - newDistance;
				currentDistance = newDistance;
				//return;
			}
		}
	}


	public void twoOptSwap(List<TSPNode> newTour, int i, int k){
    	if(k < i){
    		int store = i;
    		i = k;
    		k = store;
		}

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

			double length = getDistanceFromIndex(node.id, i);
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