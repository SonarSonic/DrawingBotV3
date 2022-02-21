package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.GenericSetting;
import javafx.util.StringConverter;

import java.util.function.Supplier;

public class StringConverterGenericSetting<V> extends StringConverter<V> {

    public Supplier<GenericSetting<?, V>> supplier;

    public StringConverterGenericSetting(GenericSetting settings) {
        this.supplier = () -> settings;
    }

    public StringConverterGenericSetting(Supplier<GenericSetting<?, V>> supplier) {
        this.supplier = supplier;
    }

    public String toString(V object) {
        GenericSetting<?, V> setting = supplier.get();
        return setting.stringConverter.toString(object);
    }

    public V fromString(String string) {
        GenericSetting<?, V> setting = supplier.get();
        if(setting.hasEditableTextField()){
            try {
                V value = setting.stringConverter.fromString(string);
                return setting.validator.apply(value);
            } catch (Exception e) {
                DrawingBotV3.logger.info("Invalid input: " + string + " for setting " + setting.key.getName());
            }
        }
        return setting.value.get();
    }
}
