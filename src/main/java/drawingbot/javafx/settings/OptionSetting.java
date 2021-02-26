package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.BiConsumer;

public class OptionSetting<C, V> extends GenericSetting<C, V> {

    public List<V> values;

    public OptionSetting(Class<C> clazz, String settingName, StringConverter<V> converter, List<V> values, V defaultValue, boolean shouldLock, BiConsumer<C, V> setter) {
        super(clazz, settingName, defaultValue, converter, random -> values.get(random.nextInt(values.size()-1)), shouldLock, v -> values.contains(v) ? v : defaultValue, setter);
        this.values = values;
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
        return new OptionSetting<C, V>(clazz, settingName.get(), stringConverter, List.copyOf(values), defaultValue, lock.get(), setter);
    }
}
