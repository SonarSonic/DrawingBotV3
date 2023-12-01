package drawingbot.javafx.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fxmisc.easybind.Subscription;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class JFXUtils {

    public static void runNowOrLater(Runnable runnable){
        if(Platform.isFxApplicationThread()){
            runnable.run();
        }else{
            Platform.runLater(runnable);
        }
    }

    public static void runNow(Runnable runnable) {
        if(Platform.isFxApplicationThread()){
            runnable.run();
        }else{
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                runnable.run();
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    public static <V> V runTaskNow(FutureTask<V> task){
        runNowOrLater(task);
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Subscription subscribeListener(ObservableValue<T> observable, ChangeListener<? super T> subscriber) {
        subscriber.changed(observable, null, observable.getValue());
        observable.addListener(subscriber);
        return () -> observable.removeListener(subscriber);
    }

}
