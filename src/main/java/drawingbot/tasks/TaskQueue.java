package drawingbot.tasks;


public class TaskQueue {

    /*
    private static LinkedList<PlottingTask> activeTasks = new LinkedList<>();
    public static PlottingTask activeTask = null;

    public static void addTask(PlottingTask task){
        activeTasks.add(task);
        task.prePlot();//MOVE ME
    }

    public static void plot() {
        activeTask = activeTasks.getFirst();
        activeTask.plot();
        if(activeTask.isFinished()){
            activeTask.postPlot();
            activeTasks.remove(activeTask);
        }else{
            DrawingBotV3.INSTANCE.loop();
        }
    }
    */
    public static PlottingTask activeTask = null;

    public static void addTask(PlottingTask task){
        activeTask = task;
    }

    public static void plot() {
        if(activeTask !=null && !activeTask.isTaskFinished()){
            activeTask.doTask();
        }
    }
}
