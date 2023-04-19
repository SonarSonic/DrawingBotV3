package drawingbot.javafx.observables;

import drawingbot.api.IProperties;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.SpecialListenable;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

public class ObservableImageFilter extends SpecialListenable<ObservableImageFilter.Listener> implements IProperties {

    public final SimpleBooleanProperty enable;
    public final SimpleStringProperty name;
    public final GenericFactory<BufferedImageOp> filterFactory;
    public final ObservableList<GenericSetting<?, ?>> filterSettings;
    public final SimpleStringProperty settingsString; //settings as a string

    public transient final SimpleBooleanProperty dirty;
    public transient final SimpleObjectProperty<BufferedImage> cached;

    public ObservableImageFilter(GenericFactory<BufferedImageOp> filterFactory) {
        this(true, filterFactory);
    }

    public ObservableImageFilter(boolean enable, GenericFactory<BufferedImageOp> filterFactory) {
        this(enable, filterFactory.getRegistryName(), filterFactory, MasterRegistry.INSTANCE.createObservableImageFilterSettings(filterFactory));
    }

    public ObservableImageFilter(ObservableImageFilter duplicate) {
        this(duplicate.enable.get(), duplicate.name.get(), duplicate.filterFactory, GenericSetting.copy(duplicate.filterSettings, FXCollections.observableArrayList()));
        GenericSetting.applySettings(duplicate.filterSettings, this.filterSettings);
    }

    public ObservableImageFilter(boolean enable, String name, GenericFactory<BufferedImageOp> filterFactory, ObservableList<GenericSetting<?, ?>> filterSettings) {
        this.enable = new SimpleBooleanProperty(enable);
        this.name = new SimpleStringProperty(name);
        this.filterFactory = filterFactory;
        this.filterSettings = filterSettings;
        this.settingsString = new SimpleStringProperty(filterSettings.toString());

        //changes to filter settings are called from the FXController
        this.dirty = new SimpleBooleanProperty(true);
        this.cached = new SimpleObjectProperty<>(null);

        InvalidationListener genericListener = observable -> sendListenerEvent(listener -> listener.onImageFilterPropertyChanged(this, observable));
        this.enable.addListener(genericListener);
        this.name.addListener(genericListener);
        this.filterSettings.forEach(s -> s.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(!s.isValueChanging()){
                genericListener.invalidated(s);
            }
            // Update the displayed settings string
            this.settingsString.set(filterSettings.toString());
        }));
    }

    public ObservableImageFilter copy(){
        return new ObservableImageFilter(this);
    }

    ///////////////////////////

    private ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(enable, name, filterSettings, settingsString);
        }
        return propertyList;
    }

    ///////////////////////////

    public interface Listener {

        default void onImageFilterPropertyChanged(ObservableImageFilter filter, Observable property) {}

    }
}
