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

    public final ObservableList<Property<?>> observables = PropertyUtil.createPropertiesList(currentFilters);

    @Override
    public ObservableList<Property<?>> getProperties() {
        return observables;
    }

    public ImageFilterSettings copy(){
        ImageFilterSettings copy = new ImageFilterSettings();
        for(ObservableImageFilter filter : currentFilters.get()){
            copy.currentFilters.get().add(new ObservableImageFilter(filter));
        }
        return copy;
    }
}
