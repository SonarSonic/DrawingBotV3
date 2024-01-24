package drawingbot.utils;

import java.util.LinkedHashMap;

public class LazyTimerUtils {

    public static LinkedHashMap<String, LazyTimer> timersID = new LinkedHashMap<>();

    public static LazyTimer startTimer(){
        LazyTimer timer = new LazyTimer();
        timer.start();
        return timer;
    }

    public static LazyTimer startTimer(String id){
        LazyTimer timer = new LazyTimer();
        timer.start();
        timersID.put(id, timer);
        return timer;
    }

    public static LazyTimer finishTimer(String id){
        LazyTimer timer = timersID.remove(id);
        assert timer != null;
        timer.finish();
        return timer;
    }

}
