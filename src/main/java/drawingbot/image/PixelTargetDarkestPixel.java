package drawingbot.image;

import drawingbot.api.IPixelData;
import drawingbot.pfm.AbstractDarkestPFM;

import java.util.*;

/**
 * Essentially a fast implementation of {@link drawingbot.pfm.AbstractDarkestPFM#findDarkestPixel(IPixelData, int[])}
 * It caches the current darkest pixels to provide them as quickly as possible
 * TODO LISTENING, this method of caching assumes that the luminance will only increase, in the future we may need to handle decreasing luminance too
 */
public class PixelTargetDarkestPixel extends PixelTargetCache {

    public IPixelData data;
    public Deque<int[]> pointQueue;
    public int limit = 50000;

    public PixelTargetDarkestPixel(IPixelData data){
        this.data = data;
    }

    public PixelTargetDarkestPixel(IPixelData data, int limit){
        this.data = data;
        this.limit = limit;
    }

    private void buildCache(){
        List<int[]> dstList = new ArrayList<>();
        AbstractDarkestPFM.findDarkestPixels(data, dstList);
        Collections.shuffle(dstList);
        if(dstList.size() > limit){
            pointQueue = new LinkedList<>(dstList.subList(0, limit));
        }else{
            pointQueue = new LinkedList<>(dstList);
        }
    }

    public void invalidateCache(){
        pointQueue = null;
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

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}