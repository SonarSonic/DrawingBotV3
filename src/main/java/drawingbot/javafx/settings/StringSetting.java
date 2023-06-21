package drawingbot.javafx.settings;

import com.google.gson.JsonElement;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.Register;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class StringSetting<C> extends GenericSetting<C, String> {

    public static StringConverter<String> stringConverter = new DefaultStringConverter();

    protected StringSetting(GenericSetting<C, String> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public StringSetting(Class<C> pfmClass, String settingName, String defaultValue) {
        this(pfmClass, Register.CATEGORY_UNIQUE, settingName, defaultValue);
    }

    public StringSetting(Class<C> pfmClass, String category, String settingName, String defaultValue) {
        super(pfmClass, String.class, category, settingName, defaultValue);
    }

    @Override
    protected StringConverter<String> defaultStringConverter() {
        return stringConverter;
    }

    @Override
    public Node createJavaFXNode(boolean label) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(value);
        textField.setOnAction(e -> sendUserEditedEvent());
        return getEditableTextField();
    }

    @Override
    public GenericSetting<C, String> copy() {
        return new StringSetting<>(this);
    }

    //////////////////////////

    public String getValueFromJsonElement(JsonElement element){
        return element.getAsString();
    }

    //////////////////////////

    private StringProperty property = null;

    public StringProperty asStringProperty(){
        if(property == null){
            property = new SimpleStringProperty(getValue());
            property.bindBidirectional(valueProperty());
        }
        return property;
    }
}
