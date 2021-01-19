package drawingbot.tasks;


import drawingbot.DrawingBotV3;
import javafx.application.Platform;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlottingThread extends Thread {

    public static PlottingThread INSTANCE;

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
                PlottingTask nextTask = globalQueue.remove();
                if(nextTask.init()){ //checks the image
                    activeTask = nextTask;
                }
            }

            if(activeTask != null){
                activeTask.doTask();
                setThreadProgress(activeTask.pfm.progress());

                if(activeTask.isTaskFinished()){
                    setThreadStatus("Plotting Image: Lines: " + activeTask.plottedDrawing.line_count + " FINISHED");
                    completedTask = activeTask;
                    activeTask = null;
                    //TODO REDRAW?
                }else{
                    setThreadStatus("Plotting Image: Lines: " + activeTask.plottedDrawing.line_count);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void createImagePlottingTask(String url){
        INSTANCE.globalQueue.add(new PlottingTask(DrawingBotV3.INSTANCE.pfmLoader, url));
    }

    public static void setThreadProgress(double progress){
        Platform.runLater(() -> DrawingBotV3.INSTANCE.controller.progressBarGeneral.setProgress(progress));
    }

    public static void setThreadStatus(String status){
        Platform.runLater(() -> DrawingBotV3.INSTANCE.controller.progressBarLabel.setText(status));
    }

}
