package drawingbot.javafx.settings;

import drawingbot.javafx.GenericSetting;
import drawingbot.utils.Utils;
import javafx.scene.Node;
import javafx.scene.control.Slider;

public abstract class AbstractNumberSetting<C, V extends Number> extends GenericSetting<C, V> {

    public boolean isRanged;
    public V minValue;
    public V maxValue;

    public V safeMinValue;
    public V safeMaxValue;

    public V majorTick;
    public boolean snapToTicks;

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

    @Override
    public Node createJavaFXNode(boolean label) {
        if(!isRanged){
            return getEditableTextField();
        }

        //graphics
        Slider slider = new Slider();
        slider.setMin(safeMinValue.doubleValue());
        slider.setMax(safeMaxValue.doubleValue());
        slider.setValue(value.getValue().doubleValue());

        double[] initialValue = new double[1];
        boolean[] outOfRange = new boolean[1];
        outOfRange[0] = !Utils.within(value.getValue().doubleValue(), safeMinValue.doubleValue(), safeMaxValue.doubleValue());

        //bindings
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            //If the value isn't at the maximum extent of the slider, then the slider has been moved and is no longer out of range
            if(!outOfRange[0] || (newValue.doubleValue() != safeMaxValue.doubleValue() && newValue.doubleValue() != safeMinValue.doubleValue())){
                setValue(fromNumber(newValue));

                //If the user clicks on the slider but doesn't drag it.
                if(!isValueChanging()){
                    sendUserEditedEvent();
                }
            }
        });
        slider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            setValueChanging(newValue);
            if(!oldValue && newValue){
                initialValue[0] = slider.getValue();
            }
            if(!newValue && oldValue){
                double finalValue = slider.getValue();
                setValue(fromNumber(finalValue));
                sendUserEditedEvent();
            }
        });
        value.addListener((observable, oldValue, newValue) -> {
            //if(!slider.isValueChanging()){
            outOfRange[0] = !Utils.within(newValue.doubleValue(), safeMinValue.doubleValue(), safeMaxValue.doubleValue());
            if(!outOfRange[0]){
                slider.setValue(newValue.doubleValue());
            }else{
                slider.setValue(newValue.doubleValue() >= safeMaxValue.doubleValue() ? safeMaxValue.doubleValue() : safeMinValue.doubleValue());

                //The slider won't send edit events if the value is out of the safe range
                sendUserEditedEvent();
            }
            //}
        });

        if(label){
            //show markings
            slider.setMajorTickUnit(majorTick == null ? Math.min(Integer.MAX_VALUE, Math.abs(safeMaxValue.doubleValue()-safeMinValue.doubleValue())) : majorTick.doubleValue());
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            slider.setSnapToTicks(snapToTicks);
        }

        return slider;
    }

    public abstract V fromNumber(Number number);
}