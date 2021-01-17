package drawingbot.pfm;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// This is the pfm interface, it contains the only methods the main code can call.
// As well as any variables that all pfm modules must have.
public interface IPFM {

    void pre_processing();

    void find_path();

    void post_processing();

    void output_parameters();
}
