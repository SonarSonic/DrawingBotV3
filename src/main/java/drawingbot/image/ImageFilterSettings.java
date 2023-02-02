package drawingbot.image;

import drawingbot.api.IProperties;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.utils.SpecialListenable;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ImageFilterSettings extends SpecialListenable<ImageFilterSettings.Listener> implements IProperties {

    public final SimpleObjectProperty<ObservableList<ObservableImageFilter>> currentFilters = new SimpleObjectProperty<>(this, "", FXCollections.observableArrayList());

    public ImageFilterSettings(){
        PropertyUtil.addSpecialListenerWithSubList(this, currentFilters, Listener::onImageFilterAdded, Listener::onImageFilterRemoved);
    }

    public ImageFilterSettings copy(){
        ImageFilterSettings copy = new ImageFilterSettings();
        for(ObservableImageFilter filter : currentFilters.get()){
            copy.currentFilters.get().add(new ObservableImageFilter(filter));
        }
        return copy;
    }

    ///////////////////////////

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(List.of(currentFilters));
        }
        return propertyList;
    }

    ///////////////////////////

    public interface Listener extends ObservableImageFilter.Listener {

        default void onImageFilterAdded(ObservableImageFilter filter) {}

        default void onImageFilterRemoved(ObservableImageFilter filter) {}
    }
}
