package drawingbot.javafx.settings.bindings;

import drawingbot.javafx.GenericSetting;
import javafx.beans.binding.Bindings;

import java.util.Collection;
import java.util.Objects;

public class SimpleBindingFactory extends BindingFactory {

    public String targetKey;
    public Object value;
    public boolean invert = false;

    public SimpleBindingFactory(String targetKey, Object value) {
        this.targetKey = targetKey;
        this.value = value;
    }

    public SimpleBindingFactory invert(){
        this.invert = true;
        return this;
    }

    @Override
    public void setupBindings(GenericSetting<?, ?> setting, Collection<GenericSetting<?, ?>> allSettings) {
        GenericSetting<?, ?> disablingSetting = GenericSetting.findSetting(allSettings, targetKey);
        if(disablingSetting != null){
            setting.disabledProperty().bind(Bindings.createBooleanBinding(() -> invert != Objects.equals(disablingSetting.getValue(), value), disablingSetting.valueProperty()));
        }
    }
}