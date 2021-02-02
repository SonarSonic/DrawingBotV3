package drawingbot.pfm;

public interface IPFM {

    boolean finished();

    void finish();

    float progress();

    void init();

    void preProcess();

    void doProcess();

    void postProcess();

}
