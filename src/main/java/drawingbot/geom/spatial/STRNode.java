package drawingbot.geom.spatial;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import java.util.function.Function;

public class STRNode<T> {

    public int index;
    public T city;
    public Coordinate startCoord;
    public Coordinate endCoord;
    public Envelope envelope;
    public boolean reverse;
    public Function<T, T> reverseFunc;


    public STRNode(int index, T city, Coordinate startCoord, Coordinate endCoord, Envelope envelope, Function<T, T> reverseFunc) {
        this.index = index;
        this.city = city;
        this.startCoord = startCoord;
        this.endCoord = endCoord;
        this.envelope = envelope;
        this.reverseFunc = reverseFunc;
    }

    public void reverse(){
        this.city = reverseFunc.apply(city);

        Coordinate oldStart = startCoord;
        startCoord = endCoord;
        endCoord = oldStart;
    }
}
