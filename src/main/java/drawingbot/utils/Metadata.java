package drawingbot.utils;

import java.util.Objects;

public class Metadata<T> {

    public String key;
    public Class<T> type;
    public boolean serialize;

    public Metadata(String key, Class<T> type, boolean serialize){
        this.key = key;
        this.type = type;
        this.serialize = serialize;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Metadata){
            return Objects.equals(((Metadata<?>) obj).key, key);
        }
        return super.equals(obj);
    }
}
