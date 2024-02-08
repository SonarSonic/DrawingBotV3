package drawingbot.software;

import drawingbot.api.IPlugin;
import drawingbot.utils.ISpecialListenable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the {@link IComponent}s / {@link IPlugin}s from the active {@link ISoftware}
 */
public class SoftwareManager implements ISpecialListenable<SoftwareManager.Listener> {

    public static SoftwareManager INSTANCE = new SoftwareManager();

    private SoftwareManager(){}

    //// PLUGINS \\\\
    private final List<IComponent> loadedComponents = new ArrayList<>();
    private final List<IPlugin> loadedPlugins = new ArrayList<>();

    public void findComponents(){
        List<IComponent> available = new ArrayList<>();

        //Load all the components associated with the active software
        findAvailableComponents(getSoftware(), available);

        //Check the available components to see if they can be loaded
        for(IComponent component : available){
            if(loadedComponents.contains(component) || !component.isEnabled() || !hasRequiredDependencies(component.getOptionalComponents(), available)){
                continue;
            }
            loadedComponents.add(component);

            if(component instanceof IPlugin plugin){
                loadedPlugins.add(plugin);
            }
            sendListenerEvent(l -> l.onComponentLoaded(component));
        }
    }

    private void findAvailableComponents(IComponent component, List<IComponent> available){
        if(!component.checkAvailable() || available.contains(component)){
            return;
        }

        //Add the component to list of available components
        available.add(component);

        //Send the load event
        sendListenerEvent(l -> l.onComponentDetected(component));

        List<IComponent> subComponents = component.getSubComponents();

        if(subComponents != null){
            subComponents.forEach(c -> findAvailableComponents(c, available));
        }
    }

    private boolean hasRequiredDependencies(List<String> dependencies, List<IComponent> available){
        if(dependencies == null || dependencies.isEmpty()){
            return true;
        }
        for(String depName : dependencies){
            IComponent dep = getComponentFromList(depName, available);
            if(dep == null){
                return false;
            }
        }
        return true;
    }

    public IComponent getComponentFromList(String registryName, List<IComponent> available){
        return available.stream().filter(n -> n.getRegistryName().equals(registryName)).findFirst().orElse(null);
    }

    public IComponent getComponent(String registryName){
        return loadedComponents.stream().filter(n -> n.getRegistryName().equals(registryName)).findFirst().orElse(null);
    }

    ////////////////////////////////////////////////////////

    public ObjectProperty<ISoftware> software = new SimpleObjectProperty<>();

    public static ISoftware getSoftware(){
        return INSTANCE.software.get();
    }

    public static void setSoftware(ISoftware software){
        INSTANCE.software.set(software);
    }

    /////////////////////////////////

    public static List<IComponent> getLoadedComponents() {
        return INSTANCE.loadedComponents;
    }

    public static List<IPlugin> getLoadedPlugins() {
        return INSTANCE.loadedPlugins;
    }

    /////////////////////////////////


    private ObservableList<SoftwareManager.Listener> listeners = null;

    public ObservableList<SoftwareManager.Listener> listeners(){
        if(listeners == null){
            listeners = FXCollections.observableArrayList();
        }
        return listeners;
    }

    public interface Listener{

        default void onComponentDetected(IComponent component){}

        default void onComponentLoaded(IComponent component){}

    }
}
