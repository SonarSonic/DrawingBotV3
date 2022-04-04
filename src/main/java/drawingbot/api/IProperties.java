package drawingbot.api;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;

public interface IProperties {

    ObservableList<Property<?>> getProperties();

}