package drawingbot.javafx.editors;

import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;

import java.time.LocalDate;
import java.util.Optional;

public class SettingEditors {

    public static DefaultPropertyEditorFactory defaultPropertyEditorFactory = new DefaultPropertyEditorFactory();

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
        return editor;
    }

    public static Node createEditor(Property property, Class<?> type){
        PropertyEditor editor = defaultPropertyEditorFactory.call(new PropertySheet.Item() {
            @Override
            public Class<?> getType() {
                return type;
            }

            @Override
            public String getCategory() {
                return "";
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public Object getValue() {
                return property.getValue();
            }

            @Override
            public void setValue(Object value) {
                property.setValue(value);
            }

            @Override
            public Optional<ObservableValue<? extends Object>> getObservableValue() {
                return Optional.of(property);
            }
        });
        editor.setValue(property.getValue());
        HBox.setHgrow(editor.getEditor(), Priority.ALWAYS);
        return editor.getEditor();
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

    public static Node createTextAreaEditorLazy(Property<?> stringProperty){
        return createTextAreaEditor((Property<String>)stringProperty);
    }

    public static Node createTextAreaEditor(Property<String> booleanProperty){
        TextArea textArea = new TextArea();
        textArea.textProperty().bindBidirectional(booleanProperty);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        textArea.setPrefRowCount(6);
        textArea.setMinHeight(100);
        return textArea;
    }

}