package drawingbot.api;

/**
 * A {@link IPFM} (or PFM) defines an algorithm/method for converting an image to lines/curves.
 */
public interface IPFM {

    /**
     * @param tools the {@link IPlottingTools} to control the drawing process
     */
    void setPlottingTools(IPlottingTools tools);

    /**
     * Called immediately after the {@link IPFM}'s settings have been set.
     * Used to check the given settings and apply any special options to the plotting task
     * Shouldn't be used for initial calculations of the {@link IPlottingTools#getPixelData()} and {@link IPlottingTools#getReferencePixelData()}. Which will be initialized but the pixel data will not have been set
     */
    default void onSettingsApplied(){}


    /**
     * Called when all settings have been applied correctly and the {@link IPlottingTools} and {@link IPixelData} have been created.
     */
    default void setup(){}

    /**
     * Runs the PFM, generating the lines from the pixel data provided by {@link IPlottingTools#getPixelData()}
     * Implementations should also update the progress of the process with {@link IPlottingTools#updateProgress(double, double)} ()}
     *
     */
    void run();

    /**
     * Called when the 'stop' button has been pressed by the user, used to safely stop the run() method. See {@link IPlottingTools#isFinished()}
     */
    default void stopElegantly(){}

    /**
     * Called after {@link #run()}, used to clean up resources, and potentially re-order geometries
     */
    default void postProcess(){}

    /**
     * Called when the PFM has completed, the PFM should destroy all of its own created resources in this method
     */
    default void destroy(){}

    /**
     * The plotting resolution, how much to scale the image by before plotting.
     * @return typically = 1.0F
     */
    default float getPlottingResolution(){
        return 1.0F;
    }

}