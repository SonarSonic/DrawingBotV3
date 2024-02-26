package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.Editors;
import drawingbot.javafx.editors.IEditorFactory;
import javafx.util.StringConverter;

public class ImageSetting<C> extends GenericSetting<C, String> {

    protected ImageSetting(GenericSetting<C, String> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public ImageSetting(Class<C> clazz, String category, String settingName, String defaultValue) {
        super(clazz, String.class, category, settingName, defaultValue);
    }

    @Override
    public IEditorFactory<String> defaultEditorFactory() {
        return Editors::createImageSelector;
    }

    @Override
    protected StringConverter<String> defaultStringConverter() {
        return StringSetting.stringConverter;
    }

    @Override
    public GenericSetting<C, String> copy() {
        return new ImageSetting<>(this);
    }
}
