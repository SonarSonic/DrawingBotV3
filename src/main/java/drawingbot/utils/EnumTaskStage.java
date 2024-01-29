package drawingbot.utils;

public enum EnumTaskStage {

    START("Starting"),
    PRE_PROCESSING("Pre-Processing"),
    DO_PROCESS("Processing"),
    POST_PROCESSING("Post-Processing"),
    FINISH("Finished");

    String displayName;

    EnumTaskStage(String displayName){
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
