package drawingbot.api;

public interface IProgressCallback {

    void updateTitle(String title);

    void updateMessage(String message);

    void updateProgress(float progress, float max);

}
