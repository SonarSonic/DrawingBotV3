package drawingbot.api;

/**
 * A {@link IPFM} (or PFM) defines an algorithm/method for converting an image to lines/curves.
 */
public interface IPFM {


    void setPlottingTools(IPlottingTools tools);

    /**
     * Called immediately after the {@link IPFM}'s settings have been set.
     * Used to check the given settings and apply any special options to the plotting task}
     * Shouldn't be used for initial calculations the {@link IPlottingTools#getPixelData()} and {@link IPlottingTools#getReferencePixelData()} ()} will be initialized but the pixel data will not have been set
     *
     */
    default void onSettingsApplied(){

    }


    default void setup(){}

    /**
     * Runs the PFM, generating the lines from the pixel data provided by {@link IPlottingTools#getPixelData()}
     * Implementations should also update the progress of the process with {@link IPlottingTools#updateProgress(double, double)} ()}
     *
     */
    void run();


    default void onStopped(){}

    /**
     * The plotting resolution, how much to scale the image by before plotting.
     * @return typically = 1.0F
     */
    default float getPlottingResolution(){
        return 1.0F;
    }

}