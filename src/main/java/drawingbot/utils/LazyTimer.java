package drawingbot.utils;

import drawingbot.DrawingBotV3;

/**
 * A lazy way to time things when debugging --todo use DATES?
 */
public class LazyTimer {

    private long startTime = -1;
    private long endTime = -1;

    private long startPause = -1;
    private long totalPausedTime = 0;

    public LazyTimer(){}

    /**
     * Called to mark the start of the event/code to be timed. It resets the timer if it has already been called.
     */
    public void start(){
        startTime = System.currentTimeMillis();
        endTime = -1;
    }

    public void pause(){
        if(startPause == -1 && startTime != -1){
            startPause = System.currentTimeMillis();
        }
    }

    public void resume(){
        if(startPause != -1){
            totalPausedTime += System.currentTimeMillis() - startPause;
            startPause = -1;
        }
    }

    /**
     * Called to mark the finish of the event/code to be timed.
     */
    public void finish(){
        endTime = System.currentTimeMillis();
    }

    public boolean hasStarted(){
        return startTime != -1;
    }

    public boolean hasFinished(){
        return endTime != -1;
    }

    public boolean isPaused(){
        return startPause != -1;
    }

    public long getElapsedTime(){
        return (endTime == -1 ? System.currentTimeMillis() : endTime) - startTime - totalPausedTime;
    }

    public long estimateTotalTime(double taskProgress){
        return (long)(getElapsedTime() / taskProgress);
    }

    public long estimateRemainingTime(double taskProgress){
        long elapsedTime = getElapsedTime();
        return (long)(elapsedTime / taskProgress) - elapsedTime;
    }

    public String getElapsedTimeFormatted(){
        return getElapsedTimeFormatted(getElapsedTime());
    }

    /**
     * Prints out the finishing time
     */
    public static String getElapsedTimeFormatted(long elapsedTime){
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) ((elapsedTime / (1000*60)) % 60);
        int hours = (int) ((elapsedTime / (1000*60*60)) % 24);

        if(hours == 0 && minutes == 0 && seconds == 0){
            return elapsedTime + " ms";
        }

        if(hours == 0 && minutes == 0){
            return seconds + " s";
        }

        if(hours == 0){
            return minutes + " m " + seconds + " s";
        }

        return hours + " h " + minutes + " m " + seconds + " s";
    }

    public void logSimple(String message){
        DrawingBotV3.logger.config(message + ": " + getElapsedTimeFormatted());
    }

}
