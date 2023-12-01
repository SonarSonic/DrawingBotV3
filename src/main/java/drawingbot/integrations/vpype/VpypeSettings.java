package drawingbot.integrations.vpype;

import drawingbot.api.IProperties;
import drawingbot.javafx.util.PropertyUtil;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class VpypeSettings implements IProperties {

    public final SimpleStringProperty vpypeExecutable = new SimpleStringProperty("");
    public final SimpleStringProperty vpypeCommand = new SimpleStringProperty("");
    public final SimpleBooleanProperty vpypeBypassOptimisation = new SimpleBooleanProperty();

    ///////////////////////////

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(vpypeExecutable, vpypeCommand, vpypeBypassOptimisation);
        }
        return propertyList;
    }
}
