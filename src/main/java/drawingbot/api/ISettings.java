package drawingbot.api;

import drawingbot.javafx.GenericSetting;
import javafx.collections.ObservableList;

public interface ISettings extends IProperties {

    ObservableList<GenericSetting<?, ?>> getObservables();

}