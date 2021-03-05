package drawingbot.image.filters;

import drawingbot.DrawingBotV3;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericSetting;
import drawingbot.registry.MasterRegistry;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImageOp;

public class ObservableImageFilter {

    public SimpleBooleanProperty enable;
    public SimpleStringProperty name;
    public GenericFactory<BufferedImageOp> filterFactory;
    public ObservableList<GenericSetting<?, ?>> filterSettings;
    public SimpleStringProperty settingsString; //settings as a string

    public ObservableImageFilter(GenericFactory<BufferedImageOp> filterFactory) {
        this(true, filterFactory.getName(), filterFactory, MasterRegistry.INSTANCE.createObservableImageFilterSettings(filterFactory));
    }

    public ObservableImageFilter(ObservableImageFilter duplicate) {
        this(duplicate.enable.get(), duplicate.name.get(), duplicate.filterFactory, GenericSetting.copy(duplicate.filterSettings, FXCollections.observableArrayList()));
    }

    public ObservableImageFilter(boolean enable, String name, GenericFactory<BufferedImageOp> filterFactory, ObservableList<GenericSetting<?, ?>> filterSettings) {
        this.enable = new SimpleBooleanProperty(enable);
        this.name = new SimpleStringProperty(name);
        this.filterFactory = filterFactory;
        this.filterSettings = filterSettings;
        this.settingsString = new SimpleStringProperty(filterSettings.toString());

        this.filterSettings.forEach(s -> s.addListener((observable, oldValue, newValue) -> onSettingChanged()));
        this.enable.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onImageFiltersChanged());
        //changes to filter settings are called from the FXController
    }

    public void onSettingChanged() {
        ///actually updating the image filter rendering is done elsewhere, this shouldn't always change as values do
        this.settingsString.set(filterSettings.toString());
    }
}
