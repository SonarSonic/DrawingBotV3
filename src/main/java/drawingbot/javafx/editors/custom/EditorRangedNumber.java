package drawingbot.javafx.editors.custom;

import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.editors.Editors;
import drawingbot.javafx.editors.IEditor;
import drawingbot.javafx.settings.AbstractNumberSetting;
import drawingbot.utils.Utils;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * An custom {@link IEditor} which creates a ranged slider for a {@link AbstractNumberSetting}
 * It keeps track of when the slider is being moved and only sends updates after the slider control is released.
 * It also handles out-of-bound values / values out of the safe range
 * When the {@link drawingbot.javafx.editors.EditorStyle} is detailed, it will include labels and a text field
 */
public class EditorRangedNumber<V extends Number> implements IEditor<V> {

    protected final EditorContext context;
    protected final AbstractNumberSetting<?, V> setting;
    protected final Node node;

    protected Slider slider;
    protected IEditor<V> textFieldEditor;

    private ChangeListener<V> valueListener = null;
    private ChangeListener<? super Number> sliderValueListener = null;
    private ChangeListener<Boolean> sliderValueChangingListener = null;

    public EditorRangedNumber(EditorContext context, AbstractNumberSetting<?, V> setting){
        this.context = context;
        this.setting = setting;
        //graphics
        slider = new Slider();
        slider.setMin(setting.safeMinValue.doubleValue());
        slider.setMax(setting.safeMaxValue.doubleValue());
        slider.setValue(setting.value.getValue().doubleValue());

        double[] initialValue = new double[1];
        boolean[] outOfRange = new boolean[1];
        outOfRange[0] = !Utils.within(setting.value.getValue().doubleValue(), setting.safeMinValue.doubleValue(), setting.safeMaxValue.doubleValue());

        //bindings
        slider.valueProperty().addListener(sliderValueListener = (observable, oldValue, newValue) -> {
            //If the value isn't at the maximum extent of the slider, then the slider has been moved and is no longer out of range
            if(!outOfRange[0] || (newValue.doubleValue() != setting.safeMaxValue.doubleValue() && newValue.doubleValue() != setting.safeMinValue.doubleValue())){
                setting.setValue(setting.fromNumber(newValue));

                //If the user clicks on the slider but doesn't drag it.
                if(!setting.isValueChanging()){
                    getProperty().sendUserEditedEvent();
                }
            }
        });
        slider.valueChangingProperty().addListener(sliderValueChangingListener = (observable, oldValue, newValue) -> {
            setting.setValueChanging(newValue);
            if(!oldValue && newValue){
                initialValue[0] = slider.getValue();
            }
            if(!newValue && oldValue){
                double finalValue = slider.getValue();
                setting.setValue(setting.fromNumber(finalValue));
                getProperty().sendUserEditedEvent();
            }
        });
        setting.valueProperty().addListener(valueListener = (observable, oldValue, newValue) -> {
            //if(!slider.isValueChanging()){
            outOfRange[0] = !Utils.within(newValue.doubleValue(), setting.safeMinValue.doubleValue(), setting.safeMaxValue.doubleValue());
            if(!outOfRange[0]){
                slider.setValue(newValue.doubleValue());
            }else{
                slider.setValue(newValue.doubleValue() >= setting.safeMaxValue.doubleValue() ? setting.safeMaxValue.doubleValue() : setting.safeMinValue.doubleValue());

                //The slider won't send edit events if the value is out of the safe range
                getProperty().sendUserEditedEvent();
            }
            //}
        });

        if(context.style.isDetailed()){
            if(setting.labelledSlider){
                slider.setMajorTickUnit(setting.majorTick == null ? Math.min(Integer.MAX_VALUE, Math.abs(setting.safeMaxValue.doubleValue()-setting.safeMinValue.doubleValue())) : setting.majorTick.doubleValue());
                slider.setShowTickLabels(true);
                slider.setShowTickMarks(true);
                slider.setSnapToTicks(setting.snapToTicks);
            }
            textFieldEditor = Editors.createGenericTextField(context, getProperty());
            if(textFieldEditor.getNode() instanceof TextField field){
                field.setPrefWidth(80);
            }
            node = new HBox(10, slider, textFieldEditor.getNode());
        }else{
            node = slider;
        }

    }

    @Override
    public EditorContext context() {
        return context;
    }

    @Override
    public AbstractNumberSetting<?, V> getProperty() {
        return setting;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public void dispose() {
        if(textFieldEditor != null){
            textFieldEditor.dispose();
        }
        slider.valueProperty().removeListener(sliderValueListener);
        slider.valueChangingProperty().removeListener(sliderValueChangingListener);
        setting.valueProperty().removeListener(valueListener);

        sliderValueListener = null;
        sliderValueChangingListener = null;
        valueListener = null;
    }
}
