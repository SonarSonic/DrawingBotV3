package drawingbot.pfm;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// This is the pfm interface, it contains the only methods the main code can call.
// As well as any variables that all pfm modules must have.
public interface IPFM {

    boolean finished();

    float progress();

    void init();

    void preProcessing();

    void findPath();

    void postProcessing();

    void outputParameters();
}
