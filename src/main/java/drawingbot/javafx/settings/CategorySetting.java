package drawingbot.javafx.settings;

public class CategorySetting<C>  extends BooleanSetting<C> {

    public CategorySetting(BooleanSetting<C> toCopy) {
        super(toCopy);
    }

    public CategorySetting(Class<C> pfmClass, String category, String settingName, Boolean defaultValue) {
        super(pfmClass, category, settingName, defaultValue);
    }
}
