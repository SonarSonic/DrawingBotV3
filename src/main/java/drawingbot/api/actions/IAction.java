package drawingbot.api.actions;

public interface IAction {

    /**
     * Creates the inverse of the action, enabling undo/redo
     */
    IAction invert();

    /**
     * Runs the action
     */
    void apply();

    /**
     * True if the action has no effect
     */
    default boolean isIdentity(){
        return false;
    }
}
