package drawingbot.tasks;


import drawingbot.DrawingBotV3;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlottingThread extends Thread {

    public static PlottingThread INSTANCE;

    public static double progress;
    public static String status;

    //TODO STORAGE FOR COMPLETED TASKS / OR ONE TASK AT A TIME?
    public Queue<PlottingTask> globalQueue = new ConcurrentLinkedQueue<>();
    public PlottingTask activeTask = null;
    public PlottingTask completedTask = null;

    public PlottingThread() {
        super("DrawingBotV3 - Plotting Thread");
        INSTANCE = this;
    }

    @Override
    public void run() {
        while(true){
            if(activeTask == null && !globalQueue.isEmpty()){
                activeTask = globalQueue.remove();
            }

            if(activeTask != null){
                if(activeTask.doTask()){
                    if(activeTask.isTaskFinished()){
                        completedTask = activeTask;
                        activeTask = null;
                        //TODO REDRAW?
                    }
                }else{
                    //TASK FAILED TODO
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void createImagePlottingTask(String url){
        INSTANCE.globalQueue.add(new PlottingTask(DrawingBotV3.INSTANCE.pfmLoader, DrawingBotV3.INSTANCE.observableDrawingSet, url));
    }

    public static void setThreadProgress(double p){
        progress = p;
    }

    public static void setThreadStatus(String s){
        status = s;
    }

}
