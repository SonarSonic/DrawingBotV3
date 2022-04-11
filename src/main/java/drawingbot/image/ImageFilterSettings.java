package drawingbot.image;

import drawingbot.api.IProperties;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.utils.EnumRotation;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ImageFilterSettings implements IProperties {

    public final SimpleObjectProperty<ObservableList<ObservableImageFilter>> currentFilters = new SimpleObjectProperty<>(FXCollections.observableArrayList());
    public final SimpleObjectProperty<EnumRotation> imageRotation = new SimpleObjectProperty<>(EnumRotation.R0);
    public final SimpleBooleanProperty imageFlipHorizontal = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty imageFlipVertical = new SimpleBooleanProperty(false);

    public final ObservableList<Property<?>> observables = PropertyUtil.createPropertiesList(currentFilters, imageRotation, imageFlipHorizontal, imageFlipVertical);

    @Override
    public ObservableList<Property<?>> getProperties() {
        return observables;
    }
}
