package drawingbot.files;

import drawingbot.api.IProperties;
import drawingbot.javafx.observables.ObservableVersion;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.utils.SpecialListenable;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.stream.Collectors;

public final class VersionControl extends SpecialListenable<VersionControl.Listener> implements IProperties {

    public VersionControl(){
        init();
    }

    public VersionControl(VersionControl toCopy){
        projectVersions.get().setAll(toCopy.getProjectVersions().stream().map(ObservableVersion::copy).collect(Collectors.toList()));
        lastRun.set(toCopy.getLastRun() != null ? toCopy.getLastRun().copy() : null);
        init();
    }

    private void init(){
        PropertyUtil.addSimpleListListener(projectVersions, c -> {
            while(c.next()){
                c.getAddedSubList().forEach(v -> sendListenerEvent(listener -> listener.onVersionAdded(v)));
                c.getRemoved().forEach(v -> sendListenerEvent(listener -> listener.onVersionRemoved(v)));
            }
        });
    }


    ///////////////////////////

    public final ObjectProperty<ObservableList<ObservableVersion>> projectVersions = new SimpleObjectProperty<>(FXCollections.observableArrayList());

    public ObservableList<ObservableVersion> getProjectVersions() {
        return projectVersions.get();
    }

    public ObjectProperty<ObservableList<ObservableVersion>> projectVersionsProperty() {
        return projectVersions;
    }

    public void setProjectVersions(ObservableList<ObservableVersion> projectVersions) {
        this.projectVersions.set(projectVersions);
    }

    ///////////////////////////

    public final SimpleObjectProperty<ObservableVersion> lastRun = new SimpleObjectProperty<>();

    public ObservableVersion getLastRun() {
        return lastRun.get();
    }

    public SimpleObjectProperty<ObservableVersion> lastRunProperty() {
        return lastRun;
    }

    public void setLastRun(ObservableVersion lastRun) {
        this.lastRun.set(lastRun);
    }

    public VersionControl copy() {
        return new VersionControl(this);
    }

    ///////////////////////////

    private transient ObservableList<Observable> propertyList = null;

    @Override
    public ObservableList<Observable> getPropertyList() {
        if(propertyList == null){
            propertyList = PropertyUtil.createPropertiesList(projectVersions, lastRun);
        }
        return propertyList;
    }

    ///////////////////////////

    public interface Listener{

        default void onVersionAdded(ObservableVersion version){}

        default void onVersionRemoved(ObservableVersion version){}

    }
}
