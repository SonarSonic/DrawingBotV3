package drawingbot.javafx;

import drawingbot.files.BatchProcessingTask;
import drawingbot.plotting.PlottingTask;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

import java.util.concurrent.ExecutorService;

public class TaskMonitor {

    public ExecutorService service;

    public SimpleBooleanProperty isPlotting = new SimpleBooleanProperty();
    public SimpleIntegerProperty processingCount = new SimpleIntegerProperty();
    public SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(0);
    public SimpleStringProperty titleProperty = new SimpleStringProperty("");
    public SimpleStringProperty messageProperty = new SimpleStringProperty("");
    public SimpleObjectProperty<Throwable> exceptionProperty = new SimpleObjectProperty<>(null);

    public TaskMonitor(ExecutorService service){
        this.service = service;
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
            task.stateProperty().addListener((observable, oldValue, newValue) -> onTaskStateChanged(task, observable, oldValue, newValue));
            service.submit(task);
        });
    }
    //TODO BATCH PROCESSING?
    public void onTaskStateChanged(Task<?> task, ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue){
        switch (newValue){
            case READY:
                break;
            case SCHEDULED:
                processingCount.setValue(processingCount.getValue() + 1);
                break;
            case RUNNING:
                if(task instanceof PlottingTask || task instanceof BatchProcessingTask){
                    isPlotting.set(true);
                }
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
                if(task instanceof PlottingTask || task instanceof BatchProcessingTask){
                    isPlotting.set(false);
                }
                processingCount.setValue(processingCount.getValue() - 1);
                break;
        }
    }



}
