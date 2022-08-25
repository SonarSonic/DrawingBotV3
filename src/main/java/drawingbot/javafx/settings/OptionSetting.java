package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

public class OptionSetting<C, V> extends GenericSetting<C, V> {

    public static StringConverter<Integer> stringConverter = new IntegerStringConverter();

    public List<V> values;

    protected OptionSetting(OptionSetting<C, V> toCopy) {
        super(toCopy, toCopy.getValue());
        this.values = new ArrayList<>(toCopy.values);
    }

    public OptionSetting(Class<C> clazz, Class<V> type, String category, String settingName, V defaultValue, List<V> values) {
        super(clazz, type, category, settingName, defaultValue);
        this.values = values;
        this.setStringConverter(new StringConverter<V>() {
            @Override
            public String toString(V object) {
                return object.toString();
            }
            @Override
            public V fromString(String string) {
                for(V v : values){
                    if(v.toString().equals(string)){
                        return v;
                    }
                }
                return null;
            }
        });
    }

    @Override
    protected V defaultValidate(V value) {
        if(values == null){
            return value;//avoid crash when the setting is being initialised
        }
        return values.contains(value) ? value : defaultValue;
    }

    @Override
    protected V defaultRandomise(ThreadLocalRandom random) {
        return values.get(random.nextInt(values.size()-1));
    }

    @Override
    protected Node createJavaFXNode(boolean label) {
        ChoiceBox<V> choiceBox = new ChoiceBox<>();
        choiceBox.setItems(FXCollections.observableArrayList(values));
        choiceBox.valueProperty().bindBidirectional(value);
        return choiceBox;
    }

    @Override
    public GenericSetting<C, V> copy() {
        return new OptionSetting<>(this);
    }
}
