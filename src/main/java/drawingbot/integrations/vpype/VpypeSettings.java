package drawingbot.integrations.vpype;

import drawingbot.api.IProperties;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.StringSetting;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.registry.Register;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class VpypeSettings implements IProperties {

    public final SimpleStringProperty vPypeExecutable = new SimpleStringProperty();
    public final SimpleStringProperty vPypeCommand = new SimpleStringProperty();
    public final SimpleBooleanProperty vPypeBypassOptimisation = new SimpleBooleanProperty();

    public final ObservableList<Property<?>> observables = PropertyUtil.createPropertiesList(vPypeExecutable, vPypeCommand, vPypeBypassOptimisation);


    @Override
    public ObservableList<Property<?>> getProperties() {
        return observables;
    }
}
