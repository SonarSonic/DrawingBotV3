package drawingbot.api;

import java.util.List;

public interface IPlugin {

    String getPluginName();

    default void registerPlugins(List<IPlugin> newPlugins){}

    default void tick(){}

    default void preInit(){}

    default void init(){}

    default void postInit(){}

    default void registerPFMS(){}

    default void registerPFMSettings(){}

    default void registerDrawingTools(){}

    default void registerImageFilters(){}

    default void registerDrawingExportHandlers(){}

    default void registerColourSplitterHandlers() {}
}
