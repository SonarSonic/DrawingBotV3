package drawingbot.api_impl.actions;

import drawingbot.api.actions.IAction;
import drawingbot.api.actions.ValueChange;

import java.util.*;

public class ActionUtils {

    public static List<IAction> invertActionList(List<IAction> actions){
        List<IAction> invertedActions = new ArrayList<>();
        for(IAction action : actions){
            invertedActions.add(action.invert());
        }
        Collections.reverse(invertedActions);
        return invertedActions;
    }

    public static <K> LinkedHashMap<K, IAction> invertActionsMap(LinkedHashMap<K, IAction> changes){
        LinkedHashMap<K, IAction> invertedValueChanges = new LinkedHashMap<>();
        for(Map.Entry<K, IAction> entry : changes.entrySet()){
            invertedValueChanges.put(entry.getKey(), entry.getValue().invert());
        }
        return reverseMap(invertedValueChanges);
    }

    public static <T> List<ValueChange<T>> invertValueChangesList(List<ValueChange<T>> changes){
        List<ValueChange<T>> invertedValueChanges = new ArrayList<>();
        for(ValueChange<T> change : changes){
            invertedValueChanges.add(change.invert());
        }
        Collections.reverse(invertedValueChanges);
        return invertedValueChanges;
    }

    public static <K, T> LinkedHashMap<K, ValueChange<T>> invertValueChangesMap(LinkedHashMap<K, ValueChange<T>> changes){
        LinkedHashMap<K, ValueChange<T>> invertedValueChanges = new LinkedHashMap<>();
        for(Map.Entry<K, ValueChange<T>> entry : changes.entrySet()){
            invertedValueChanges.put(entry.getKey(), entry.getValue().invert());
        }
        return reverseMap(invertedValueChanges);
    }

    public static <T, Q> LinkedHashMap<T, Q> reverseMap(LinkedHashMap<T, Q> toReverse){
        LinkedHashMap<T, Q> reversedMap = new LinkedHashMap<>();
        List<T> reverseOrderedKeys = new ArrayList<>(toReverse.keySet());
        Collections.reverse(reverseOrderedKeys);
        reverseOrderedKeys.forEach((key) -> reversedMap.put(key, toReverse.get(key)));
        return reversedMap;
    }

}
