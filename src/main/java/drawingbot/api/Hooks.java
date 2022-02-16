package drawingbot.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hooks {

    public static final String NEW_PLOTTING_TASK = "NEW_PLOTTING_TASK";
    public static final String NEW_EXPORT_TASK = "NEW_EXPORT_TASK";

    public static final String FX_CONTROLLER_PRE_INIT = "FX_CONTROLLER_PRE_INIT";
    public static final String FX_CONTROLLER_POST_INIT = "FX_CONTROLLER_POST_INIT";

    public static final String FX_EXPORT_CONTROLLER_PRE_INIT = "FX_EXPORT_CONTROLLER_PRE_INIT";
    public static final String FX_EXPORT_CONTROLLER_POST_INIT = "FX_EXPORT_CONTROLLER_POST_INIT";

    public static final String SERIAL_CONNECTION_CONTROLLER = "SERIAL_CONNECTION_CONTROLLER";
    public static final String SERIAL_CONNECTION_STAGE = "SERIAL_CONNECTION_STAGE";

    public static final String SERIALIZE_DRAWING_STATE = "SERIALIZE_DRAWING_STATE";
    public static final String DESERIALIZE_DRAWING_STATE = "DESERIALIZE_DRAWING_STATE";

    public static final String FILE_MENU = "FILE_MENU";

    public static final String CHANGE_DRAWING_SET = "CHANGE_DRAWING_SET";


    public static Map<String, List<IHook>> hookMap = new HashMap<>();

    public static void addHook(String name, IHook hook){
        hookMap.putIfAbsent(name, new ArrayList<>());
        hookMap.get(name).add(hook);
    }

    public static Object[] runHook(String name, Object... values){
        List<IHook> hooks = hookMap.get(name);
        if(hooks != null){
            for(IHook hook : hooks){
                values = hook.run(values);
            }
        }
        return values;
    }

    public interface IHook{
        Object[] run(Object... values);
    }

}