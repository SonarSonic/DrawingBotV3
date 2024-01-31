package drawingbot.api;

import drawingbot.software.SoftwareManager;
import drawingbot.software.IComponent;

/**
 * A plugin is a component which receives {@link SoftwareManager} events
 */
public interface IPlugin extends IComponent {

    default void tick(){}

    default void preInit(){}

    default void init(){}

    default void postInit(){}

    default void loadJavaFXStages() {}

    default void registerPFMS(){}

    default void registerPFMSettings(){}

    default void registerDrawingTools(){}

    default void registerImageFilters(){}

    default void registerDrawingExportHandlers(){}

    default void registerColourSplitterHandlers() {}

    default void registerPreferencePages() {}

    default void shutdown() {}

}
