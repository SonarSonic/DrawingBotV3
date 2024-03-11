package drawingbot.utils.flags;

import java.util.*;

public class FlagStates {

    public Map<Flags.Flag<?>, Object> STATE_MAP = new HashMap<>();
    public Set<Flags.Flag<?>> toClear = new HashSet<>();
    public Set<Flags.Flag<?>> toReset = new HashSet<>();

    public FlagStates(Flags.FlagCategory category){
        for(Flags.Flag<?> flag : Flags.ALL_FLAGS.get(category)){
            STATE_MAP.put(flag, flag.resetValue);
        }
    }

    public <T> void setFlag(Flags.Flag<T> flag, T value){
        assert flag.type.isInstance(value);
        STATE_MAP.put(flag, value);

        toClear.remove(flag);
        toReset.remove(flag);
    }

    public <T> void setFlags(T value, Flags.Flag<T> ...flagArray) {
        for(Flags.Flag<T> flag : flagArray){
            setFlag(flag, value);
        }
    }

    public <T> T getFlag(Flags.Flag<T> flag){
        return flag.type.cast(STATE_MAP.get(flag));
    }

    public boolean allMatch(Flags.BooleanFlag...flags){
        for(Flags.Flag<Boolean> flag : flags){
            if(!getFlag(flag)){
                return false;
            }
        }
        return true;
    }

    public boolean anyMatch(Flags.BooleanFlag ...flags){
        for(Flags.Flag<Boolean> flag : flags){
            if(getFlag(flag)){
                return true;
            }
        }
        return false;
    }

    public boolean anyMatchAndClearOnMatch(Flags.BooleanFlag ...flags){
        boolean match = anyMatch(flags);
        if(match){
            markForClear(flags);
        }
        return match;
    }

    public boolean anyMatchAndMarkClear(Flags.BooleanFlag ...flags){
        boolean match = anyMatch(flags);
        markForClear(flags);
        return match;
    }

    public void applyMarkedChanges(){
        for(Flags.Flag<?> flag : toClear){
            STATE_MAP.put(flag, flag.clearValue);
        }
        toClear.clear();

        for(Flags.Flag<?> flag : toReset){
            STATE_MAP.put(flag, flag.resetValue);
        }
        toReset.clear();
    }

    public void clear(){
        for(Map.Entry<Flags.Flag<?>, Object> entry : STATE_MAP.entrySet()){
            entry.setValue(entry.getKey().clearValue);
        }
    }

    public void reset(){
        for(Map.Entry<Flags.Flag<?>, Object> entry : STATE_MAP.entrySet()){
            entry.setValue(entry.getKey().resetValue);
        }
    }

    public void loadState(FlagStates otherState){
        clear();
        for(Map.Entry<Flags.Flag<?>, Object> entry : STATE_MAP.entrySet()){
            entry.setValue(otherState.getFlag(entry.getKey()));
        }
    }


    public void markForClear(Flags.Flag<?>...flags){
        toClear.addAll(Arrays.asList(flags));
    }

    public void markForReset(Flags.Flag<?>...flags){
        toReset.addAll(Arrays.asList(flags));
    }

}
