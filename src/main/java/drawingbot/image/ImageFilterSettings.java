package drawingbot.image;

import drawingbot.api.IProperties;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.utils.SpecialListenable;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Image Filters: ");

        if(currentFilters.get().isEmpty()){
            builder.append("None");
        }

        for(int i = 0; i < currentFilters.get().size(); i++){
            ObservableImageFilter filter = currentFilters.get().get(i);
            builder.append(i == 0 ? "" : ", ");
            builder.append(filter.name.get());
            if(!filter.enable.get()){
                builder.append("(Disabled)");
            }
        }
        return builder.toString();
    }

    ///////////////////////////

    public interface Listener extends ObservableImageFilter.Listener {

        default void onImageFilterAdded(ObservableImageFilter filter) {}

        default void onImageFilterRemoved(ObservableImageFilter filter) {}
    }
}
