package drawingbot.javafx.editors;

import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.*;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import org.controlsfx.control.ToggleSwitch;

import java.time.LocalDate;

public class SettingEditors {

    public static Node createEditor(GenericSetting<?, ?> generic){
        Node editor = null;
        if(generic instanceof BooleanSetting){
            BooleanSetting<?> setting = (BooleanSetting<?>) generic;
            editor = createSwitchEditor(setting.valueProperty());
        }else if(generic instanceof ColourSetting){
            ColourSetting<?> setting = (ColourSetting<?>) generic;
            editor = createColorEditor(setting.valueProperty());
        }else if(generic instanceof DoubleSetting){
            DoubleSetting<?> setting = (DoubleSetting<?>) generic;
            editor = createDoubleTextEditor(setting);
        }else if(generic instanceof FloatSetting){
            FloatSetting<?> setting = (FloatSetting<?>) generic;
            editor = createFloatTextEditor(setting);
        }else if(generic instanceof IntegerSetting){
            IntegerSetting<?> setting = (IntegerSetting<?>) generic;
            editor = createIntegerTextEditor(setting);
        }else if(generic instanceof LongSetting){
            LongSetting<?> setting = (LongSetting<?>) generic;
            editor = createLongTextEditor(setting);
        }else if(generic instanceof OptionSetting){
            OptionSetting<?, ?> setting = (OptionSetting<?, ?>) generic;
            editor = createChoiceEditor(setting);
        }else if(generic instanceof StringSetting){
            StringSetting<?> setting = (StringSetting<?>) generic;
            editor = createTextEditor(setting.valueProperty());
        }
        if(editor != null){
            editor.disableProperty().bindBidirectional(generic.disabledProperty());
        }
        return editor;
    }

    public static Node createCheckboxEditor(Property<Boolean> booleanProperty){
        CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().bindBidirectional(booleanProperty);
        return checkBox;
    }

    public static Node createSwitchEditor(Property<Boolean> booleanProperty){
        ToggleSwitch toggleSwitch = new ToggleSwitch();
        toggleSwitch.selectedProperty().bindBidirectional(booleanProperty);
        return toggleSwitch;
    }

    public static Node createTextEditor(Property<String> booleanProperty){
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(booleanProperty);
        return textField;
    }

    public static Node createDoubleTextEditor(DoubleSetting<?> doubleSetting){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), doubleSetting.getDefaultValue()));
        textField.textProperty().bindBidirectional(doubleSetting.valueProperty(), new DoubleStringConverter());
        textField.disableProperty().bindBidirectional(doubleSetting.disabledProperty());
        return textField;
    }

    public static Node createDoubleTextEditor(Property<Double> doubleProperty){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0D));
        textField.textProperty().bindBidirectional(doubleProperty, new DoubleStringConverter());
        return textField;
    }

    public static Node createFloatTextEditor(FloatSetting<?> floatProperty){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new FloatStringConverter(), floatProperty.getDefaultValue()));
        textField.textProperty().bindBidirectional(floatProperty.valueProperty(), new FloatStringConverter());
        textField.disableProperty().bindBidirectional(floatProperty.disabledProperty());
        return textField;
    }

    public static Node createFloatTextEditor(Property<Float> floatProperty){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new FloatStringConverter(), 0F));
        textField.textProperty().bindBidirectional(floatProperty, new FloatStringConverter());
        return textField;
    }

    public static Node createIntegerTextEditor(IntegerSetting<?> integerSetting){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), integerSetting.getDefaultValue()));
        textField.textProperty().bindBidirectional(integerSetting.valueProperty(), new IntegerStringConverter());
        textField.disableProperty().bindBidirectional(integerSetting.disabledProperty());
        return textField;
    }

    public static Node createIntegerTextEditor(Property<Integer> integerProperty){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0));
        textField.textProperty().bindBidirectional(integerProperty, new IntegerStringConverter());
        return textField;
    }

    public static Node createLongTextEditor(LongSetting<?> longSetting){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new LongStringConverter(), longSetting.getDefaultValue()));
        textField.textProperty().bindBidirectional(longSetting.valueProperty(), new LongStringConverter());
        textField.disableProperty().bindBidirectional(longSetting.disabledProperty());
        return textField;
    }

    public static Node createLongTextEditor(Property<Long> longProperty){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new LongStringConverter(), 0L));
        textField.textProperty().bindBidirectional(longProperty, new LongStringConverter());
        return textField;
    }

    public static <T> Node createChoiceEditor(OptionSetting<?, T> optionSetting){
        ComboBox<T> comboBox = new ComboBox<>();
        comboBox.setItems(optionSetting.getOptions());
        comboBox.valueProperty().bindBidirectional(optionSetting.valueProperty());
        return comboBox;
    }

    public static <T> Node createChoiceEditor(Property<T> selectProperty, ObservableList<T> options){
        ComboBox<T> comboBox = new ComboBox<>();
        comboBox.setItems(options);
        comboBox.valueProperty().bindBidirectional(selectProperty);
        return comboBox;
    }

    public static Node createColorEditor(Property<Color> selectProperty){
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.valueProperty().bindBidirectional(selectProperty);
        return colorPicker;
    }

    public static Node createDateEditor(Property<LocalDate> selectProperty){
        DatePicker datePicker = new DatePicker();
        datePicker.valueProperty().bindBidirectional(selectProperty);
        return datePicker;
    }

    public static Node createTextAreaEditor(Property<String> booleanProperty){
        TextArea textArea = new TextArea();
        textArea.textProperty().bindBidirectional(booleanProperty);
        return textArea;
    }

}