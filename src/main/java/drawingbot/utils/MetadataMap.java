package drawingbot.utils;

import com.google.gson.annotations.JsonAdapter;
import drawingbot.files.json.adapters.JsonAdapterMetadataMap;

import java.util.HashMap;
import java.util.Map;

@JsonAdapter(JsonAdapterMetadataMap.class)
public class MetadataMap {

    public Map<Metadata<?>, Object> data;

    public MetadataMap(){}

    public MetadataMap(Map<Metadata<?>, Object> data){
        this.data = data;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public <T> void setMetadata(Metadata<T> metadata, T value){
        if(value != null){
            data.put(metadata, value);
        }
    }

    public <T> T getMetadata(Metadata<T> metadata){
        Object obj = data.get(metadata);
        if(obj == null){
            return null;
        }
        if(!metadata.type.isInstance(obj)){
            return null;
        }
        return metadata.type.cast(obj);
    }

    public MetadataMap copy(){
        return new MetadataMap(new HashMap<>(data));
    }
}
