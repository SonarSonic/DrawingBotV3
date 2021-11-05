package drawingbot.geom.tsp;

import drawingbot.geom.tree.NodeGraph;
import drawingbot.pfm.helpers.TSPHelper;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class TSPAlgorithmLK extends TSPAlgorithmAbstract {

    // How many times the tour has been improved
	public int currentIteration = 0;
	public int currentImprovement = 0;

    // When set to something other than -1 and findOptimalRoute is disabled the algorithm will stop when the iterations have been reached
    public int targetIterations = -1;

    // A function which is called every time an improvement has been attempted in the current iteration
    public Function<Float, Void> progressCallback = null;

    /**
     * Constructor that creates an instance of the Lin-Kerninghan problem without
     * the optimizations. (Basically the tour it has is the drunken sailor)
     * @param coordinates the coordinates of all the cities
     */ 
    public TSPAlgorithmLK(List<Coordinate> coordinates) {
    	super(coordinates, (new NodeGraph(coordinates).nodes));
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
        } while(targetIterations == -1 ? getDistance() < oldDistance : currentIteration < targetIterations);
    }

	double oldDistance = 0;

	public void init(){
		oldDistance = getDistance();
		currentImprovement = 0;
		currentIteration = 0;
	}

	/**
	 * @return true if the iterations should keep going
	 */
	public boolean runNextIteration(){
		oldDistance = getDistance();
		improveTour();
		currentIteration++;
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
			progressCallback.apply((float)currentImprovement / size);
    	}
    }
    
    /**
     * This functions tries to improve by stating from a particular node
     * @param cityIndex the reference to the city to start with.
     */
    public void improveTour(int cityIndex){
    	improveTour(cityIndex, false);
    }
    
    /**
     * This functions attempts to improve the tour by stating from a particular node
     * @param cityIndex the reference to the city to start with.
     */
    public void improveTour(int cityIndex, boolean previous) {
    	int t2 = previous? getPreviousIdx(cityIndex): getNextIdx(cityIndex);
    	int t3 = getNearestNeighbor(t2);
    	
    	if(t3 != -1 && getDistance(t2, t3) < getDistance(cityIndex, t2)) { // Implementing the gain criteria
    		startAlgorithm(cityIndex, t2, t3);
    	} else if(!previous) {
    		improveTour(cityIndex, true);
    	}
    }
    
    /**
     * This function returns the nearest neighbor for an specific node
     * @param index index of the node
     * @return the index of the nearest node
     */
    public int getNearestNeighbor(int index) {
    	double minDistance = Double.MAX_VALUE;
    	int nearestNode = -1;
		int actualNode = tour.get(index).id;
    	for(int i = 0; i < size; ++i) {
    		if(i != actualNode) {
    			double distance = getDistance(i, actualNode);
    			if(distance < minDistance) {
    				nearestNode = getIndex(i);
    				minDistance = distance; 
    			}
    		}
    	}
    	return nearestNode;
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
    	double initialGain = getDistance(t2, t1) - getDistance(t3, t2); // |x1| - |y1|
    	double GStar = 0;
    	double Gi = initialGain;
    	int k = 3;
    	for(int i = 4;; i+=2) {
    		int newT = selectNewT(tIndex);
    		if(newT == -1) {
    			break; // This should not happen according to the paper
    		}
    		tIndex.add(i, newT);
    		int tiplus1 = getNextPossibleY(tIndex);
    		if(tiplus1 == -1) {
    			break;
    		}
    		// Step 4.f from the paper
    		Gi += getDistance(tIndex.get(tIndex.size()-2), newT);
    		if(Gi - getDistance(newT, t1) > GStar) {
    			GStar = Gi - getDistance(newT, t1);
    			k = i;
    		}
    		
    		tIndex.add(tiplus1);
    		Gi -= getDistance(newT, tiplus1);
    		
    		
    	}
    	if(GStar > 0) {
    		tIndex.set(k+1, tIndex.get(1));
    		tour = getTPrime(tIndex, k); // Update the tour
    	}
    	
    }
}