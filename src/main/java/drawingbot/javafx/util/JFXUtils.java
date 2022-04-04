package drawingbot.javafx.util;

import javafx.application.Platform;

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

}
