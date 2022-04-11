package drawingbot.pfm;

import drawingbot.api.IProperties;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.pfm.PFMFactory;
import drawingbot.utils.EnumDistributionType;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

public class PFMSettings implements IProperties {

    public final SimpleObjectProperty<PFMFactory<?>> factory = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<ObservableList<GenericSetting<?, ?>>> settings = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<EnumDistributionType> nextDistributionType = new SimpleObjectProperty<>();

    public final ObservableList<Property<?>> observables = PropertyUtil.createPropertiesList(factory, settings, nextDistributionType);

    @Override
    public ObservableList<Property<?>> getProperties() {
        return observables;
    }
}
