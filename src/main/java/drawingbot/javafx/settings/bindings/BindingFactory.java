package drawingbot.javafx.settings.bindings;

import drawingbot.javafx.GenericSetting;

import java.util.Collection;

public abstract class BindingFactory {

    public abstract void setupBindings(GenericSetting<?, ?> setting, Collection<GenericSetting<?, ?>> allSettings);

}
