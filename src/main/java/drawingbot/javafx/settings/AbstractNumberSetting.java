package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.Editors;
import drawingbot.javafx.editors.IEditorFactory;

public abstract class AbstractNumberSetting<C, V extends Number> extends GenericSetting<C, V> {

    public boolean isRanged;
    public V minValue;
    public V maxValue;

    public V safeMinValue;
    public V safeMaxValue;

    public V majorTick;
    public boolean snapToTicks;

    public boolean displaySlider = true;
    public boolean labelledSlider = true;

    protected AbstractNumberSetting(AbstractNumberSetting<C, V> toCopy, V newValue) {
        super(toCopy, newValue);
        this.isRanged = toCopy.isRanged;
        this.minValue = toCopy.minValue;
        this.maxValue = toCopy.maxValue;
        this.safeMinValue = toCopy.safeMinValue;
        this.safeMaxValue = toCopy.safeMaxValue;
        this.majorTick = toCopy.majorTick;
        this.snapToTicks = toCopy.snapToTicks;
    }

    public AbstractNumberSetting(Class<C> clazz, Class<V> type, String category, String settingName, V defaultValue) {
        super(clazz, type, category, settingName, defaultValue);
        this.isRanged = false;
    }

    public AbstractNumberSetting(Class<C> clazz, Class<V> type, String category, String settingName, V defaultValue, V minValue, V maxValue) {
        super(clazz, type, category, settingName, defaultValue);
        this.isRanged = true;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.safeMinValue = minValue;
        this.safeMaxValue = maxValue;
    }

    public AbstractNumberSetting<C, V> setSafeRange(V safeMinValue, V safeMaxValue){
        this.safeMinValue = safeMinValue;
        this.safeMaxValue = safeMaxValue;
        return this;
    }

    public AbstractNumberSetting<C, V> setMajorTick(V majorTick){
        this.majorTick = majorTick;
        return this;
    }

    public AbstractNumberSetting<C, V> setSnapToTicks(boolean snapToTicks){
        this.snapToTicks = snapToTicks;
        return this;
    }

    public AbstractNumberSetting<C, V> setLabelledSlider(boolean labelledSlider) {
        this.labelledSlider = labelledSlider;
        return this;
    }

    public AbstractNumberSetting<C, V> setDisplaySlider(boolean displaySlider) {
        this.displaySlider = displaySlider;
        return this;
    }

    @Override
    public IEditorFactory<V> defaultEditorFactory() {
        if(!isRanged || !displaySlider){
            return Editors::createGenericTextField; //TODO USE NUMBER TEXT FIELD?
        }
        return Editors::createRangedNumberEditor;
    }

    public abstract V fromNumber(Number number);
}