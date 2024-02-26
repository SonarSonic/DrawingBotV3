package drawingbot.javafx.editors;

/**
 * Provides style information for the {@link IEditor} via the {@link EditorContext}
 * Basic for now, may be extended in the future
 */
public enum EditorStyle {

    /**
     * Style used in PFM Controls, without additional labels / text fields
     */
    SIMPLE,
    /**
     * Style typically used in Preferences / Preset Editor dialogs
     */
    DETAILED;

    public boolean isSimple(){
        return this == SIMPLE;
    }

    public boolean isDetailed(){
        return this == DETAILED;
    }

}
