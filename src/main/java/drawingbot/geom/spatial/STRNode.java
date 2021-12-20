package drawingbot.geom.spatial;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

public class STRNode<T> {

    public int index;
    public T city;
    public Coordinate startCoord;
    public Envelope startEnvelope;

    public Coordinate endCoord;
    public Envelope endEnvelope;

    public STRNode(int index, T city, Coordinate startCoord, Envelope startEnvelope, Coordinate endCoord, Envelope endEnvelope) {
        this.index = index;
        this.city = city;
        this.startCoord = startCoord;
        this.startEnvelope = startEnvelope;
        this.endCoord = endCoord;
        this.endEnvelope = endEnvelope;
    }


    public STRNode<T> reverse(){
        Coordinate oldStart = startCoord;
        Envelope oldEnvelope = startEnvelope;

        startCoord = endCoord;
        startEnvelope = endEnvelope;

        endCoord = oldStart;
        endEnvelope = oldEnvelope;

        return this;
    }
}
