package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.files.ExportTask;
import drawingbot.utils.DBTask;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

import java.util.concurrent.ExecutorService;

public class TaskMonitor {

    public ExecutorService service;
    public Task<?> currentTask;
    public Task<?> completedTask;

    public SimpleBooleanProperty isExporting = new SimpleBooleanProperty();
    public SimpleBooleanProperty wasExporting = new SimpleBooleanProperty();
    public SimpleBooleanProperty isPlotting = new SimpleBooleanProperty();
    public SimpleIntegerProperty processingCount = new SimpleIntegerProperty();
    public SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(0);
    public SimpleStringProperty titleProperty = new SimpleStringProperty("");
    public SimpleStringProperty messageProperty = new SimpleStringProperty("");
    public SimpleObjectProperty<Throwable> exceptionProperty = new SimpleObjectProperty<>(null);



    public TaskMonitor(ExecutorService service){
        this.service = service;
    }

    public ExportTask getDisplayedExportTask(){
        if(currentTask instanceof ExportTask){
            return (ExportTask) currentTask;
        }
        if(wasExporting.get() && completedTask instanceof ExportTask){
            return (ExportTask) completedTask;
        }
        return null;
    }

    public void resetMonitor(ExecutorService service){
        this.service = service;
        this.processingCount.set(0);

        this.progressProperty.unbind();
        this.progressProperty.set(0);

        this.titleProperty.unbind();
        this.titleProperty.set("");

        this.messageProperty.unbind();
        this.messageProperty.set("");

        this.exceptionProperty.unbind();
        this.exceptionProperty.set(null);
    }

    public String getCurrentTaskStatus(){

        String title = "";
        if(!titleProperty.getValue().isEmpty()){
            title = titleProperty.getValue();
        }

        String message = "";
        if(exceptionProperty.getValue() != null){
            message = " - " + exceptionProperty.getValue().getMessage();
        }else if(!messageProperty.getValue().isEmpty()){
            message = " - " + messageProperty.getValue();
        }

        return title + message;
    }

    public void queueTask(Task<?> task){
        Platform.runLater(() -> {
            processingCount.setValue(processingCount.getValue() + 1);
            task.stateProperty().addListener((observable, oldValue, newValue) -> onTaskStateChanged(task, observable, oldValue, newValue));
            service.submit(task);
            logTask(task);
        });
    }

    public void logTask(Task<?> task){
        Platform.runLater(() -> {
            DrawingBotV3.INSTANCE.controller.taskMonitorController.taskProgressView.getTasks().add(task);
        });
    }

    //TODO BATCH PROCESSING?
    public void onTaskStateChanged(Task<?> task, ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue){
        switch (newValue){
            case READY:
                break;
            case SCHEDULED:
                break;
            case RUNNING:
                currentTask = task;

                isPlotting.set(task instanceof DBTask && ((DBTask<?>) task).isPlottingTask());
                isExporting.set(task instanceof ExportTask);
                wasExporting.set(false);

                progressProperty.unbind();
                progressProperty.bind(task.progressProperty());

                titleProperty.unbind();
                titleProperty.bind(task.titleProperty());

                messageProperty.unbind();
                messageProperty.bind(task.messageProperty());

                exceptionProperty.unbind();
                exceptionProperty.bind(task.exceptionProperty());
                break;
            case SUCCEEDED:
            case CANCELLED:
            case FAILED:
                completedTask = currentTask;
                currentTask = null;
                isPlotting.set(false);
                isExporting.set(false);
                wasExporting.set(task instanceof ExportTask);
                processingCount.setValue(processingCount.getValue() - 1);
                break;
        }
    }

    public void tick(){
        if(currentTask instanceof DBTask){
            ((DBTask<?>) currentTask).tick();
        }else if(completedTask instanceof DBTask){
            ((DBTask<?>) completedTask).tick();
        }
    }



}
