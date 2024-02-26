package drawingbot.javafx.preferences.items;

import javafx.beans.value.ObservableValue;

/**
 * An {@link ElementNode} will have some visual representation on a page and can have styles applied too it
 * It has a basic Style System with DEFAULT, TITLE & SUBTITLE, which should be supported by all editors.
 */
public abstract class ElementNode extends TreeNode {

    public static final String DEFAULT_STYLE = "preference-default";
    public static final String TITLE_STYLE = "preference-title";
    public static final String SUBTITLE_STYLE = "preference-subtitle";

    public String labelStyle = DEFAULT_STYLE;

    public ElementNode(String name, TreeNode... children) {
        super(name, children);
        setDefaultStyling();
    }

    public ElementNode setDefaultStyling() {
        this.labelStyle = DEFAULT_STYLE;
        this.setHideFromTree(true);
        return this;
    }

    public ElementNode setTitleStyling() {
        this.labelStyle = TITLE_STYLE;
        this.setHideFromTree(true); //TODO MAKE TITLES VISIBLE, WAY TO JUMP STRAIGHT TO SETTINGS
        return this;
    }

    public ElementNode setSubtitleStyling() {
        this.labelStyle = SUBTITLE_STYLE;
        this.setHideFromTree(true);
        return this;
    }

    public ElementNode setDisabledProperty(ObservableValue<Boolean> disabled) {
        this.disabledProperty().unbind();
        this.disabledProperty().bind(disabled);
        return this;
    }

    public abstract void addElement(PageBuilder builder);
}
