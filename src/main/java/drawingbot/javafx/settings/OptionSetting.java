package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.concurrent.ThreadLocalRandom;

public class OptionSetting<C, V> extends GenericSetting<C, V> {

    public static StringConverter<Integer> stringConverter = new IntegerStringConverter();

    public ObservableList<V> values;

    protected OptionSetting(OptionSetting<C, V> toCopy) {
        super(toCopy, toCopy.getValue());
        this.values = toCopy.values; //NOTE: We're using the same instance here.
    }

    public OptionSetting(Class<C> clazz, Class<V> type, String category, String settingName, V defaultValue, ObservableList<V> values) {
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

    public final ObservableList<V> getOptions() {
        return values;
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
        HBox.setHgrow(choiceBox, Priority.ALWAYS);
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        choiceBox.setOnAction(e -> sendUserEditedEvent());
        return choiceBox;
    }

    @Override
    public GenericSetting<C, V> copy() {
        return new OptionSetting<>(this);
    }
}
