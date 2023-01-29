package drawingbot.api;

public interface IProgressCallback {

    IProgressCallback NULL = new IProgressCallback() {};

    default void updateTitle(String title){}

    default void updateMessage(String message){}

    default void updateProgress(double progress, double max){}

}
