package drawingbot.javafx.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fxmisc.easybind.Subscription;

import java.util.concurrent.CountDownLatch;

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

    public static <T> Subscription subscribeListener(ObservableValue<T> observable, ChangeListener<? super T> subscriber) {
        subscriber.changed(observable, null, observable.getValue());
        observable.addListener(subscriber);
        return () -> observable.removeListener(subscriber);
    }

}
