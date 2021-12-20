package drawingbot.geom;

import java.util.List;

public abstract class DistanceMap<O> {

    public List<O> objects;
    public float[][] distances;

    public DistanceMap(List<O> objects){
        this.objects = objects;
        this.distances = new float[objects.size()][objects.size()];
        for(int x = 0; x < objects.size(); x++){
            O xObject = objects.get(x);
            for(int y = 0; y < objects.size(); y++){
                O yObject = objects.get(y);
                distances[x][y] = calculate(xObject, yObject);
            }
        }
    }

    public abstract float calculate(O object1, O object2);

    public double getDistance(O object1, O object2){
        return distances[objects.indexOf(object1)][objects.indexOf(object2)];
    }

    public double getDistance(int x, int y){
        return distances[x][y];
    }

}
