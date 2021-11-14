package drawingbot.geom.tsp;

import drawingbot.geom.tree.MSTVertex;
import drawingbot.geom.tree.MinimumSpanningTree;
import drawingbot.geom.tree.NodeEdge;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TSPAlgorithmLKH extends TSPAlgorithmAbstract {

	public MinimumSpanningTree mst;

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

    /**
     * Constructor that creates an instance of the Lin-Kerninghan problem without
     * the optimizations. (Basically the tour it has is the drunken sailor)
     */
    public TSPAlgorithmLKH(MinimumSpanningTree mst) {
		super(mst.coordinateList);
		this.mst = mst;
    }

    @Override
    public void run(){
    	init();
		try {
			runFullTSP();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * Runs the tour until it is complete
     */
    public void runFullTSP() {
		double oldDistance = 0;
		double currentDistance = getDistance();

        do {
        	oldDistance = currentDistance;
        	improveTour();
			currentDistance = getDistance();
        	currentIteration++;
			currentChanges = 0;
			totalGain = 0;
			if(cancelCallback.get()){
				break;
			}
        } while(targetIterations == -1 ? getDistance() < oldDistance : currentIteration < targetIterations);
    }

	double oldDistance = 0;

	public void init(){

		nodes = mst.createNodes(mst);
		orderedNodes = nodes;
		mst.setupForLKH(5);
		initTour();

		oldDistance = getDistance();
		currentImprovement = 0;
		currentIteration = 0;
		currentChanges = 0;
		totalGain = 0;

	}

	/**
	 * @return true if the iterations should keep going
	 */
	public boolean runNextIteration(){
		oldDistance = getDistance();
		improveTour();
		currentIteration++;
		currentChanges = 0;
		totalGain = 0;
		return getDistance() < oldDistance && currentIteration < targetIterations;
	}


	/**
	 * @return true if the improvements should keep going
	 */
	public boolean runNextImprovement(){
		boolean improved = false;

		if(currentImprovement < size){
			improveTour(currentImprovement);
			currentImprovement++;
			improved = true;
		}else{
			improved = getDistance() < oldDistance;
			oldDistance = getDistance();
			currentImprovement = 0;
			currentChanges = 0;
			totalGain = 0;
			currentIteration++;
		}
		return targetIterations == -1 ? improved : currentIteration < targetIterations;
	}
    
    /**
     * This function tries to improve the tour
     */
    public void improveTour() {
    	for(currentImprovement = 0; currentImprovement < size; ++currentImprovement) {
    		improveTour(currentImprovement);
			progressCallback.accept((float)currentImprovement / size);
			improvementCallback.accept(this);
			if(cancelCallback.get()){
				break;
			}
    	}
    }
    
    /**
     * This functions tries to improve by stating from a particular node
     * @param cityIndex the reference to the city to start with.
     */
    public void improveTour(int cityIndex){
    	improveTour(cityIndex, false);
		improveTour(cityIndex, true);
    }
    
    /**
     * This functions attempts to improve the tour by stating from a particular node
     * @param cityIndex the reference to the city to start with.
     */
    public void improveTour(int cityIndex, boolean previous) {
    	int t2 = previous? getPreviousIdx(cityIndex): getNextIdx(cityIndex);

    	//if the edge belongs to the mst, skip it
		if(currentIteration == 0 && mst.getEdge(cityIndex, t2) != null){
			return;
		}

		TSPNode node = tour.get(t2);

    	for(MSTVertex vertex : node.vertexData.nearestVertices){
    		int t3 = getIndex(vertex.id);
			if(t3 != t2 && t3 != -1 && getDistanceFromIndex(t2, t3) < getDistanceFromIndex(cityIndex, t2)) { // Implementing the gain criteria
				startAlgorithm(cityIndex, t2, t3);
			}
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

    /**
     * This function is actually the step four from the lin-kernighan's original paper
     * @param t1 the index that references the chosen t1 in the tour
     * @param t2 the index that references the chosen t2 in the tour
     * @param t3 the index that references the chosen t3 in the tour
     * @return void
     */
    public void startAlgorithm(int t1, int t2, int t3) {
    	List<Integer> tIndex = new ArrayList<>();
    	tIndex.add(0, -1); // Start with the index 1 to be consistent with Lin-Kernighan Paper
    	tIndex.add(1, t1);
    	tIndex.add(2, t2);
    	tIndex.add(3, t3);
    	double initialGain = getDistanceFromIndex(t2, t1) - getDistanceFromIndex(t3, t2); // |x1| - |y1|
    	double GStar = 0;
    	double Gi = initialGain;
    	int k = 3;
    	for(int i = 4;; i+=2) {
    		int newT = selectNewT(tIndex);
    		if(newT == -1) {
    			break; // This should not happen according to the paper
    		}
    		tIndex.add(i, newT);
    		int tiplus1 = getNextPossibleYFromMST(tIndex);
    		if(tiplus1 == -1) {
    			break;
    		}
    		// Step 4.f from the paper
    		Gi += getDistanceFromIndex(tIndex.get(tIndex.size()-2), newT);
    		if(Gi - getDistanceFromIndex(newT, t1) > GStar) {
    			GStar = Gi - getDistanceFromIndex(newT, t1);
    			k = i;
    		}
    		
    		tIndex.add(tiplus1);
    		Gi -= getDistanceFromIndex(newT, tiplus1);
    		
    	}
    	if(GStar > 0) {
    		tIndex.set(k+1, tIndex.get(1));
    		tour = getTPrime(tIndex, k); // Update the tour
			improvementCallback.accept(this);
			currentChanges++;
			totalGain+=GStar;
    	}
    }

    public List<TSPNode> constructNewTour(List<TSPNode> tour2, List<Integer> tIndex, int newItem) {
		List<Integer> changes = new ArrayList<>(tIndex);
    	
    	changes.add(newItem);
    	changes.add(changes.get(1));
		return constructNewTourFast(tour2, changes);
	}

    @Nullable
	public List<TSPNode> constructNewTourFast(List<TSPNode> tour, List<Integer> changes) {

    	List<TSPNode> newTour = new ArrayList<>(tour);

		List<TSPNode> xNodes = deriveXNodes(changes);
		List<TSPNode> yNodes = deriveYNodes(changes);

		List<NodeEdge> xEdges = deriveXNodeEdges(changes);
		List<NodeEdge> yEdges = deriveYNodeEdges(changes);


		for(int i = 0; i < newTour.size(); i ++){
			TSPNode node = newTour.get(i);
			if(xNodes.contains(node)){
				newTour.set(i, null);
			}
		}

		TSPNode t1 = nodes.get(changes.get(1));
		TSPNode t2 = nodes.get(changes.get(2));
		TSPNode t3 = nodes.get(changes.get(3));

		int start = 0;

		//if the tour overlaps itself we should begin from the "start" of the marked region
		if(newTour.get(0) == null && newTour.get(newTour.size()-1) == null){
			for(int i = newTour.size()-1; i != 0; i--){
				if(newTour.get(i) == null){
					start = i;
				}
			}
		}else{
			for(int i = 0; i < newTour.size(); i++){
				if(newTour.get(i) == null){
					start = i;
				}
			}
		}

		newTour.set(start, t3);

		loop: for(int i = getNextIdx(start); !yEdges.isEmpty(); i = getNextIdx(i)){
			TSPNode previousNode = newTour.get(getPreviousIdx(i));
			TSPNode currentNode = newTour.get(i);
			if(currentNode == null){
				continue;
			}
			for(NodeEdge edge : yEdges){
				if(previousNode == edge.start){
					newTour.set(i, edge.stop);
					yEdges.remove(edge);
					continue loop;
				}
				if(previousNode == edge.stop){
					newTour.set(i, edge.start);
					yEdges.remove(edge);
					continue loop;
				}
			}
			//if we haven't found an edge to fill the gap, the tour is incomplete
			return null;
		}

		return newTour;
	}
}