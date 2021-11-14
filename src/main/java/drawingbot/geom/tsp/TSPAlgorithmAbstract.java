package drawingbot.geom.tsp;

import drawingbot.geom.tree.MSTVertex;
import drawingbot.geom.tree.NodeEdge;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TSPAlgorithmAbstract {

    // all the coordinates in the order of their id
    public List<Coordinate> coordinates;

    // all the nodes in the order of their id
    public List<TSPNode> nodes;

    // all the nodes in the order of their id
    public List<TSPNode> tour;

    // The distance table
    private double[][] distanceTable;

    // The number of cities of this instance
    public int size;

    // A function which is called every time an improvement has been attempted in the current iteration, a progress of -1 means the end is undefined
    public Consumer<Float> progressCallback = null;

    // A function which is called every time an improvement has been made in the current iteration
    public Consumer<TSPAlgorithmAbstract> improvementCallback = null;

    // A function which should return true if the process has been cancelled
    public Supplier<Boolean> cancelCallback = null;

    public TSPAlgorithmAbstract(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
        this.size = coordinates.size();
    }

    public void initTour(){
        this.tour = createRandomTour();
        this.distanceTable = initDistanceTable();
    }

    public abstract void run();

    /**
     * This functions creates a table with the distances of all the cities
     * @return double[][] a two dimensional array with all the distances
     */
    private double[][] initDistanceTable() {
        double[][] res = new double[this.size][this.size];

        for(int i = 0; i < this.size-1; ++i) {
            for(int j = i + 1; j < this.size; ++j) {
                Coordinate p1 = this.coordinates.get(i);
                Coordinate p2 = this.coordinates.get(j);

                res[i][j] = Math.sqrt(
                        Math.pow(p2.getX() - p1.getX(), 2) +
                                Math.pow(p2.getY() - p1.getY(), 2)
                );
                res[j][i] = res[i][j];
            }
        }
        return res;
    }

    /**
     * This function create a random tour using the dunken sailor algorithm
     * @return array with the list of nodes in the tour (sorted)
     */
    public List<TSPNode> createRandomTour() {
        List<TSPNode> arrayList = new ArrayList<>(nodes);

        Random random = new Random();

        for (int i = 0; i < size; ++i) {
            int index = random.nextInt(i + 1);
            // simple swap
            TSPNode a = arrayList.get(index);
            arrayList.set(index, arrayList.get(i));
            arrayList.set(i, a);
        }

        return arrayList;
    }

    public int[] createInitialTour(){
        int index = 0;
        int[] initialTour = new int[size];
        boolean[] sorted = new boolean[size];
        for(int p1 = 0; p1 < size; p1 ++){
            if(!sorted[p1]){
                TSPNode point1 = nodes.get(p1);
                int nearestP2 = -1;
                float distance = -1;
                for(int p2 = 0; p2 < size; p2++){
                    if(!sorted[p2] && p2 != p1){
                        TSPNode point2 = nodes.get(p2);
                        float testDistance = TSPAlgorithmGenetic.distanceFromCoordinates(point1.vertexData.coordinate, point2.vertexData.coordinate);
                        if(distance == -1 || testDistance < distance){
                            distance = testDistance;
                            nearestP2 = p2;
                        }
                    }
                }
                initialTour[index++]=p1;
                initialTour[index++]=nearestP2;
                sorted[p1] = true;
                sorted[nearestP2] = true;
            }
        }
        return initialTour;
    }

    public double getDistance() {
        return getDistance(tour);
    }

    /**
     * This function returns the current tour distance
     * @return double the distance of the tour
     */
    public double getDistance(List<TSPNode> tour) {
        double sum = 0;

        TSPNode lastNode = null;

        for(int i = 0; i < this.size; i++) {
            TSPNode nextNode = tour.get(i);
            if(lastNode != null){
                sum += distanceTable[nextNode.id][lastNode.id];
            }
            lastNode = nextNode;
        }
        sum += distanceTable[tour.get(0).id][lastNode.id];

        return sum;
    }

    /**
     * This functions retrieves the distance between two nodes given its indexes
     * @param n1 index of the first node
     * @param n2 index of the second node
     * @return double the distance from node 1 to node 2
     */
    public double getDistanceFromIndex(int n1, int n2) {
        return distanceTable[tour.get(n1).id][tour.get(n2).id];
    }

    public double getDistanceFromNodeId(int n1, int n2) {
        return distanceTable[n1][n2];
    }

    /**
     * This function returns the previous index for the tour, this typically should be x-1
     *  but if x is zero, well, it is the last index.
     *  @param index the index of the node
     *  @return the previous index
     */
    public int getPreviousIdx(int index) {
        return index == 0 ? size - 1: index - 1;
    }

    /**
     * This function returns the next index for the tour, this typically should be x+1
     *  but if x is the last index it should wrap to zero
     *  @param index the index of the node
     *  @return the next index
     */
    public int getNextIdx(int index) {
        return (index + 1) % size;
    }


    /**
     * This function gets all the ys that fit the criterion for step 4
     * @param tIndex the list of t's
     * @return an array with all the possible y's
     */
    public int getNextPossibleY(List<Integer> tIndex) {

        int ti = tIndex.get(tIndex.size() - 1);

        double minLength = Double.MAX_VALUE;
        int dest = -1;

        for(int i = 0; i < size; ++i) {

            if(!isDisjunctive(tIndex, i, ti)) {
                continue; // Disjunctive criteria
            }
            if(!isPositiveGain(tIndex, i)) {
                continue; // Gain criteria
            };
            if(!nextXPossible(tIndex, i)) {
                continue; // Step 4.f.
            }

            double length = getDistanceFromIndex(ti, i);
            if(length < minLength){
                minLength = length;
                dest = i;
            }
        }

        return dest;
    }

    /**
     * This function gets all the ys that fit the criterion for step 4
     * @param tIndex the list of t's
     * @return an array with all the possible y's
     */
    public int getNextPossibleYFromMST(List<Integer> tIndex) {

        int ti = tIndex.get(tIndex.size() - 1);
        TSPNode node = nodes.get(ti);

        double minLength = Double.MAX_VALUE;
        MSTVertex dest = null;

        for(MSTVertex vertex : node.vertexData.nearestVertices){
            int i = vertex.node.id;

            if(!isDisjunctive(tIndex, i, ti)) {
                continue; // Disjunctive criteria
            }
            if(!isPositiveGain(tIndex, i)) {
                continue; // Gain criteria
            };
            if(!nextXPossible(tIndex, i)) {
                continue; // Step 4.f.
            }

            double length = getDistanceFromIndex(node.id, i);
            if(length < minLength){
                minLength = length;
                dest = vertex;
            }
        }

        return dest == null ? -1 : dest.node.id;
    }

    /**
     * This function implements the part e from the point 4 of the paper
     * @param tIndex
     * @param i
     * @return
     */
    private boolean nextXPossible(List<Integer> tIndex, int i) {
        return isConnected(tIndex, i, getNextIdx(i)) || isConnected(tIndex, i, getPreviousIdx(i));
    }

    /**
     * This function allows to check if an edge is already on either X or Y (disjunctivity criteria)
     * @param tIndex the index of the nodes in the tour
     * @param x the index of one of the endpoints
     * @param y the index of one of the endpoints
     * @return true when it satisfy the criteria, false otherwise
     */
    private boolean isDisjunctive(List<Integer> tIndex, int x, int y) {
        if(x == y) return false;
        for(int i = 0; i < tIndex.size() -1 ; i++) {
            if(tIndex.get(i) == x && tIndex.get(i + 1) == y) return false;
            if(tIndex.get(i) == y && tIndex.get(i + 1) == x) return false;
        }
        return true;
    }

    private boolean isConnected(List<Integer> tIndex, int x, int y) {
        if(x == y) return false;
        for(int i = 1; i < tIndex.size() -1 ; i+=2) {
            if(tIndex.get(i) == x && tIndex.get(i + 1) == y) return false;
            if(tIndex.get(i) == y && tIndex.get(i + 1) == x) return false;
        }
        return true;
    }

    /**
     *
     * @param tIndex
     * @param ti
     * @return true if the gain would be positive
     */
    private boolean isPositiveGain(List<Integer> tIndex, int ti) {
        int gain = 0;
        for(int i = 1; i < tIndex.size() - 2; ++i) {
            int t1 = tIndex.get(i);
            int t2 = tIndex.get(i + 1);
            int t3 = i == tIndex.size() - 3 ? ti :tIndex.get(i + 2);

            gain += getDistanceFromIndex(t2, t3) - getDistanceFromIndex(t1, t2); // |yi| - |xi|

        }
        return gain > 0;
    }

    /**
     * This function gets a new t with the characteristics described in the paper in step 4.a.
     * @param tIndex
     * @return
     */
    public int selectNewT(List<Integer> tIndex) {
        int option1 = getPreviousIdx(tIndex.get(tIndex.size()-1));
        int option2 = getNextIdx(tIndex.get(tIndex.size()-1));

        List<TSPNode>tour1 = constructNewTour(tour, tIndex, option1);

        if(isTour(tour1)) {
            return option1;
        } else {
            List<TSPNode> tour2 = constructNewTour(tour, tIndex, option2);
            if(isTour(tour2)) {
                return option2;
            }
        }
        return -1;
    }

    private List<TSPNode> constructNewTour(List<TSPNode> tour2, List<Integer> tIndex, int newItem) {
        List<Integer> changes = new ArrayList<>(tIndex);

        changes.add(newItem);
        changes.add(changes.get(1));
        return constructNewTour(tour2, changes);
    }

    /**
     * This function validates whether a sequence of numbers constitutes a tour
     * @param tour an array with the node numbers
     * @return boolean true or false
     */
    public boolean isTour(List<TSPNode> tour) {
        if(tour.size() != size) {
            return false;
        }

        for(int i =0; i < size-1; ++i) {
            for(int j = i+1; j < size; ++j) {
                if(tour.get(i) == tour.get(j)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Construct T prime
     */
    public List<TSPNode> getTPrime(List<Integer> tIndex, int k) {
        List<Integer> al2 = new ArrayList<>(tIndex.subList(0, k + 2 ));
        return constructNewTour(tour, al2);
    }

    /**
     * This function constructs a new Tour deleting the X sets and adding the Y sets
     * @param tour The current tour
     * @param changes the list of t's to derive the X and Y sets
     * @return an array with the node numbers
     */
    public List<TSPNode> constructNewTour(List<TSPNode> tour, List<Integer> changes) {
        List<Edge> currentEdges = deriveEdgesFromTour(tour);

        List<Edge> X = deriveXEdges(changes);
        List<Edge> Y = deriveYEdges(changes);
        int s = currentEdges.size();

        // Remove Xs
        for(Edge e: X) {
            for(int j = 0; j < currentEdges.size(); ++j) {
                Edge m = currentEdges.get(j);
                if(e.equals(m)) {
                    s--;
                    currentEdges.set(j, null);
                    break;
                }
            }
        }

        // Add Ys
        for(Edge e: Y) {
            s++;
            currentEdges.add(e);
        }


        return createTourFromEdges(currentEdges, s);
    }



    /**
     * This function takes a list of edges and converts it into a tour
     * @param currentEdges The list of edges to convert
     * @return the array representing the tour
     */
    private List<TSPNode> createTourFromEdges(List<Edge> currentEdges, int s) {
        List<TSPNode> tour = Arrays.asList(new TSPNode[s]);

        int i = 0;
        int last = -1;

        for(; i < currentEdges.size(); ++i) {
            if(currentEdges.get(i) != null) {
                tour.set(0, nodes.get(currentEdges.get(i).get1()));
                tour.set(1, nodes.get(currentEdges.get(i).get2()));
                last = tour.get(1).id;
                break;
            }
        }

        currentEdges.set(i, null); // remove the edges

        int k=2;
        while(true) {
            // E = find()
            int j = 0;
            for(; j < currentEdges.size(); ++j) {
                Edge e = currentEdges.get(j);
                if(e != null && e.get1() == last) {
                    last = e.get2();
                    break;
                } else if(e != null && e.get2() == last) {
                    last = e.get1();
                    break;
                }
            }
            // If the list is empty
            if(j == currentEdges.size()) break;

            // Remove new edge
            currentEdges.set(j, null);
            if(k >= s) break;
            tour.set(k, nodes.get(last));
            k++;
        }

        return tour;
    }


    public List<TSPNode> deriveXNodes(List<Integer> changes) {
        List<TSPNode> es = new ArrayList<>();
        for(int i = 1; i < changes.size() - 2; i+=2) {
            es.add(tour.get(changes.get(i)));
            es.add(tour.get(changes.get(i+1)));
        }
        return es;
    }

    public List<TSPNode> deriveYNodes(List<Integer> changes) {
        List<TSPNode> es = new ArrayList<>();
        for(int i = 2; i < changes.size() - 1; i+=2) {
            es.add(tour.get(changes.get(i)));
            es.add(tour.get(changes.get(i+1)));
        }
        return es;
    }

    /**
     * Get the list of edges from the t index
     * @param changes the list of changes proposed to the tour
     * @return The list of edges that will be deleted
     */
    public List<Edge> deriveXEdges(List<Integer> changes) {
        List<Edge> es = new ArrayList<>();
        for(int i = 1; i < changes.size() - 2; i+=2) {
            Edge e = new Edge(tour.get(changes.get(i)).id, tour.get(changes.get(i+1)).id);
            es.add(e);
        }
        return es;
    }

    /**
     * Get the list of edges from the t index
     * @param changes the list of changes proposed to the tour
     * @return The list of edges that will be added
     */
    public List<Edge> deriveYEdges(List<Integer> changes) {
        List<Edge> es = new ArrayList<>();
        for(int i = 2; i < changes.size() - 1; i+=2) {
            Edge e = new Edge(tour.get(changes.get(i)).id, tour.get(changes.get(i+1)).id);
            es.add(e);
        }
        return es;
    }


    /**
     * Get the list of edges from the t index
     * @param changes the list of changes proposed to the tour
     * @return The list of edges that will be deleted
     */
    public List<NodeEdge> deriveXNodeEdges(List<Integer> changes) {
        List<NodeEdge> es = new ArrayList<>();
        for(int i = 1; i < changes.size() - 2; i+=2) {
            NodeEdge e = new NodeEdge(tour.get(changes.get(i)), tour.get(changes.get(i+1)));
            es.add(e);
        }
        return es;
    }

    /**
     * Get the list of edges from the t index
     * @param changes the list of changes proposed to the tour
     * @return The list of edges that will be added
     */
    public List<NodeEdge> deriveYNodeEdges(List<Integer> changes) {
        List<NodeEdge> es = new ArrayList<>();
        for(int i = 2; i < changes.size() - 1; i+=2) {
            NodeEdge e = new NodeEdge(tour.get(changes.get(i)), tour.get(changes.get(i+1)));
            es.add(e);
        }
        return es;
    }


    /**
     * Get the list of edges from the tour, it is basically a conversion from
     * a tour to an edge list
     * @param tour the array representing the tour
     * @return The list of edges on the tour
     */
    public List<Edge> deriveEdgesFromTour(List<TSPNode> tour) {
        List<Edge> es = new ArrayList<>();
        for(int i = 0; i < tour.size() ; ++i) {
            Edge e = new Edge(tour.get(i).id, tour.get((i+1)%tour.size()).id);
            es.add(e);
        }

        return es;
    }


    /**
     * This function gets the index of the node given the actual number of the node in the tour
     * @param node the node id
     * @return the index on the tour
     */
    public int getIndex(int node) {
        int i = 0;
        for(TSPNode t: tour) {
            if(node == t.id) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * This function returns a string with the current tour and its distance
     * @return String with the representation of the tour
     */
    public String toString() {
        StringBuilder str = new StringBuilder("[" + this.getDistance() + "] : ");
        boolean add = false;
        for(TSPNode city: this.tour) {
            if(add) {
                str.append(" => ").append(city);
            } else {
                str.append(city);
                add = true;
            }
        }
        return str.toString();
    }
}
