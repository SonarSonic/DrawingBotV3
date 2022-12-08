package drawingbot.image;

import drawingbot.api.IPixelData;
import drawingbot.api.IPlottingTools;
import drawingbot.pfm.AbstractDarkestPFM;

import java.util.*;

//TODO LISTENING, this method of caching assumes that the luminance will only increase, in the future we may need to handle decreasing luminance too
public class PixelTargetCache {

    public IPixelData data;
    public Deque<int[]> pointQueue;

    public PixelTargetCache(IPixelData data){
        this.data = data;
    }

    private void buildCache(){
        List<int[]> dstList = new ArrayList<>();
        AbstractDarkestPFM.findDarkestPixels(data, dstList);
        Collections.shuffle(dstList);
        pointQueue = new LinkedList<>(dstList);
    }

    public void invalidateCache(){
        pointQueue = null;
    }

    public void updateNextDarkestPixel(int[] dst){
        int[] nextPixel = getNextDarkestPixel(true);
        dst[0] = nextPixel[0];
        dst[1] = nextPixel[1];
    }

    public int[] getNextDarkestPixel(boolean remove){
        if(pointQueue == null || pointQueue.isEmpty()){
            buildCache();
        }
        while(!pointQueue.isEmpty()){
            int[] nextPoint = pointQueue.getFirst();

            //check the cache's data is still accurate
            if(nextPoint[2] == data.getLuminance(nextPoint[0], nextPoint[1])){
                if(remove){
                    pointQueue.removeFirst();
                }
                return nextPoint;
            }
            pointQueue.removeFirst();
        }

        return getNextDarkestPixel(remove);

    }

}
