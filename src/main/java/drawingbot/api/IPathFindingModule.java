package drawingbot.api;

/**
 * A {@link IPathFindingModule} (or PFM) defines an algorithm/method for converting an image to lines/curves.
 * Specifically generating lines from raw pixel data and providing them to the {@link IPlottingTask}
 */
public interface IPathFindingModule {

    /**
     * The colour mode, currently supported 0 = ARGB, 1 = HSB, 2 = Grayscale
     * This setting is very important to allow for an efficient {@link IPathFindingModule} as the selected setting dictates if values are cached or calculated
     * If you primarily need red/green/blue/alpha calculations go with ARGB
     * If you primarily need hue/saturation/brightness calculations go with HSB
     * If you only need brightness calculations go with Grayscale
     * @return the default colour mode is ARGB, but this is often the least efficient, especially for brightness orientated PFMs using bresenham calculations
     */
    default int getColourMode(){
        return 0;
    }

    /**
     * the transparent ARGB value of the {@link IPixelData}, this is important for brightness orientated PFMs
     * @return the current transparent ARGB value
     */
    default int getTransparentARGB(){
        return -1;
    }

    /**
     * The plotting resolution, how much to scale the image by before plotting.
     * @return typically = 1.0F
     */
    default float getPlottingResolution(){
        return 1.0F;
    }

    /**
     * Called immediately after the {@link IPathFindingModule}'s settings have been set.
     * Used to check the given settings and apply any special options to the plotting task e.g. {@link IPlottingTask#setPlottingResolution(float)}, {@link IPlottingTask#setActivePen(int)} (float)}
     * Shouldn't be used for initial calculations the {@link IPlottingTask#getPixelData()} and {@link IPlottingTask#getReferencePixelData()} ()} will be initialized but the pixel data will not have been set
     * @param task the plotting task
     */
    void init(IPlottingTask task);

    /**
     * Called once before the first {@link IPathFindingModule#doProcess(IPlottingTask)}
     * Should be used for initial calculations the {@link IPlottingTask#getPixelData()} and {@link IPlottingTask#getReferencePixelData()} ()} will now have been set.
     * @param task the plotting task
     */
    default void preProcess(IPlottingTask task){}

    /**
     * Runs the PFM, generating the lines from the pixel data provided by {@link IPlottingTask#getPixelData()}
     * Called indefinitely until {@link IPlottingTask#finishProcess()} is called.
     * Implementations should also update the progress of the process with {@link IPlottingTask#updateProgess(float, float)} ()}
     * @param task the plotting task
     */
    void doProcess(IPlottingTask task);

    /**
     * Called once after the process has finished
     * @param task the plotting task
     */
    default void postProcess(IPlottingTask task){}
}