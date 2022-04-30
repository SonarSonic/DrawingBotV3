package drawingbot.utils;

import drawingbot.api.IProgressCallback;

import java.util.List;

public abstract class DistanceMap<O> {

    public List<O> objects;
    public float[][] distances;

    public DistanceMap(List<O> objects, IProgressCallback callback){
        this.objects = objects;
        this.distances = new float[objects.size()][objects.size()];

        for(int i = 0; i < objects.size()-1; ++i) {
            O p1 = objects.get(i);
            for (int j = i + 1; j < objects.size(); ++j) {
                O p2 = objects.get(j);
                distances[i][j] = calculate(p1, p2);
                distances[j][i] = distances[i][j];
            }
            if(callback != null){
                callback.updateProgress(i, objects.size());
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
