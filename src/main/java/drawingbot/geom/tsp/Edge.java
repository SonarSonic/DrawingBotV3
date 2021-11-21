package drawingbot.geom.tsp;

/**
 * This class is meant for representing the edges, it allows to store
 * the endpoints ids and compare the edges
 */
public class Edge implements Comparable<Edge> {
    /*
     * Instance variables
     */

    // The first node
    private int endPoint1;
    
    // The second node
    private int endPoint2;
    
    /**
     * Constructor that takes the two endpoints id
     * @param a the id of the first endpoint
     * @param b the id of the second endpoint
     */
    public Edge(int a, int b) {
        this.endPoint1 = Math.max(a, b);
        this.endPoint2 = Math.min(a, b);
    }

    /**
     * Getter that returns the first endpoint id
     * @return the first endpoint id
     */
    public int get1() {
        return this.endPoint1;
    }

    /**
     * Getter that returns the second endpoint id
     * @return the second endpoint id
     */
    public int get2() {
        return this.endPoint2;
    }

    /**
     * Method that compares two edges, here to make this class {@link Comparable}
     * @param e2 the edge that is going to be compared against this one
     * @return int will return -1 if less, 0 if equal, and 1 if greater
     */
    public int compareTo(Edge e2) {
        if(this.get1() < e2.get1() || this.get1() == e2.get1() && this.get2() < e2.get2()) {
            return -1;
        } else if (this.equals(e2)) {
            return 0;
        } else {
            return 1;
        }

    }

    /**
     * Method that compares two edges, here to make this class {@link Comparable}
     * @param e2 the edge that is going to be compared against this one
     * @return boolean true if both share the same endpoints, false otherwise
     */
    public boolean equals(Edge e2) {
    	if(e2 == null) return false;
        return (this.get1() == e2.get1()) && (this.get2() == e2.get2());
    }
    
    public String toString() {
    	return "("+ endPoint1 + ", " + endPoint2 + ")";
    }

}