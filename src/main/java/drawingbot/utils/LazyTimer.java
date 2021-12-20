package drawingbot.utils;

/**
 * A lazy way to time things when debugging
 */
public class LazyTimer {

    public long startTime;

    public LazyTimer(){}

    /**
     * Also works as a reset
     */
    public void start(){
        startTime = System.currentTimeMillis();
    }

    /**
     * Prints out the finishing time
     */
    public String finish(){
        long endTime = System.currentTimeMillis();

        long elapsedTime = (endTime - startTime);

        int seconds = (int) (elapsedTime / 1000) % 60 ;
        int minutes = (int) ((elapsedTime / (1000*60)) % 60);
        int hours = (int) ((elapsedTime / (1000*60*60)) % 24);

        if(seconds == 0){
            return elapsedTime + " ms";
        }

        if(minutes == 0){
            return seconds + " s";
        }

        if(hours == 0){
            return minutes + " m " + seconds + " s";
        }

        return hours + " h " + minutes + " m " + seconds + " s";
    }


}
