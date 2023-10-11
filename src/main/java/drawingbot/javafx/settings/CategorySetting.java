package drawingbot.javafx.settings;

public class CategorySetting<C>  extends BooleanSetting<C> {

    public static final int DEFAULT_PRIORITY = 5;

    public int priority = DEFAULT_PRIORITY;

    protected CategorySetting(CategorySetting<C> toCopy) {
        super(toCopy);
        setPriority(toCopy.getPriority());
    }

    public CategorySetting(Class<C> clazz, String category, String settingName, Boolean defaultValue) {
        super(clazz, category, settingName, defaultValue);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public CategorySetting<C> copy() {
        return new CategorySetting<>(this);
    }
}
