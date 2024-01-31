package drawingbot.software;

import javafx.beans.property.BooleanProperty;

import java.util.List;

/**
 * Represents a single component / or software which can have it's own independent version / be updated seperately
 */
public interface IComponent {

    /**
     * @return the code-name for the component used to identify it, it must be unique across all components
     */
    String getRegistryName();

    /**
     * @return the unformatted version of the component, used for checking compatibility / for updates
     */
    default String getVersion(){
        return "1.0.0";
    }

    /**
     * @return an observable JavaFX property which monitors if this component is enabled
     */
    BooleanProperty enabledProperty();

    /**
     * @return true if the component and all it's sub components can be loaded
     */
    default boolean checkAvailable(){
        return true;
    }

    /**
     * @return true if the component is enabled
     */
    default boolean isEnabled(){
        return enabledProperty() == null || enabledProperty().get();
    }

    /**
     * Note: Components can't be de-activated once running so this must be set during the start in lifecycle
     * @param enabled if the component should be enabled
     */
    default void setEnabled(boolean enabled){
        if(enabledProperty() != null){
            enabledProperty().set(enabled);
        }
    }

    /**
     * @return the formatted name of the component, as it should be displayed in the UI
     */
    default String getDisplayName(){
        return getRegistryName();
    }

    /**
     * @return the formatted version of the component, as it should be displayed in the UI
     */
    default String getDisplayVersion(){
        return getVersion();
    }

    /**
     * @return the update link for this component, this link should take the user to a page to download the latest version, null if no updates can be provided
     */
    default String getUpdateLink(){
        return null;
    }

    /**
     * @return list of components which are supported by this component, and could be loaded if the versions match and the components are available, null if none
     */
    default List<String> getOptionalComponents(){
        return null;
    }

    /**
     * @return list of components which are required by this component, if they are not available the component will not load, null if none are required
     */
    default List<String> getRequiredComponents(){
        return null;
    }

    /**
     * @return list o sub components which can be provided by this component, this could be external libraries it is responsible for loading that are optional, null if none are available
     */
    default List<IComponent> getSubComponents(){
        return null;
    }

}