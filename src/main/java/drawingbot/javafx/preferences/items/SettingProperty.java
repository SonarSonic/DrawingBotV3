package drawingbot.javafx.preferences.items;

import drawingbot.javafx.GenericSetting;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

@Deprecated
public class SettingProperty implements PropertySheet.Item {

    public GenericSetting<?, ?> setting;

    public SettingProperty(GenericSetting<?, ?> setting) {
        this.setting = setting;
    }

    @Override
    public Class<?> getType() {
        return setting.type;
    }

    @Override
    public String getCategory() {
        return setting.getCategory();
    }

    @Override
    public String getName() {
        return setting.getDisplayName();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Object getValue() {
        return setting.getValue();
    }

    @Override
    public void setValue(Object value) {
        setting.setValue(value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.of(setting.valueProperty());
    }
}
