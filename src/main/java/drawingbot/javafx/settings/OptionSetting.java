package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class OptionSetting<C, V> extends GenericSetting<C, V> {

    public List<V> values;

    protected OptionSetting(OptionSetting<C, V> toCopy) {
        super(toCopy, toCopy.getValue());
        this.values = new ArrayList<>(toCopy.values);
    }

    public OptionSetting(Class<C> clazz, String category, String settingName, V defaultValue, StringConverter<V> converter, List<V> values, BiConsumer<C, V> setter) {
        super(clazz, category, settingName, defaultValue, converter, v -> values.contains(v) ? v : defaultValue, setter);
        this.values = values;
        this.setRandomiser(random -> values.get(random.nextInt(values.size()-1)));
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
