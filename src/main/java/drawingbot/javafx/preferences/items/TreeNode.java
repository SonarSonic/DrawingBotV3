package drawingbot.javafx.preferences.items;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 * The most basic form of an editor node, a tree which has no content of it's own
 * All editors can have their own branches (sub nodes) but a basic tree without any displayable content is useful
 */
public class TreeNode implements Observable {

    public TreeNode(String name, TreeNode... children) {
        setName(name);
        getChildren().addAll(children);
    }

    ////////////

    public final ObservableList<TreeNode> children = FXCollections.observableArrayList();

    public ObservableList<TreeNode> getChildren() {
        return children;
    }

    ////////////

    public final StringProperty name = new SimpleStringProperty();

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public TreeNode setName(String name) {
        this.name.set(name);
        return this;
    }
    ////////////

    public final BooleanProperty hideFromTree = new SimpleBooleanProperty(false);

    public boolean isHiddenFromTree() {
        return hideFromTree.get();
    }

    public BooleanProperty hideFromTreeProperty() {
        return hideFromTree;
    }

    public TreeNode setHideFromTree(boolean hideFromTree) {
        this.hideFromTree.set(hideFromTree);
        return this;
    }

    public Node getContent() {
        return null;
    }

    ////////////

    public final BooleanProperty disabled = new SimpleBooleanProperty(false);

    public boolean isDisabled() {
        return disabled.get();
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }

    ////////////

    @Override
    public void addListener(InvalidationListener listener) {
        children.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        children.removeListener(listener);
    }

    public String getBreadcrumb() {
        return ""; //TODO
    }

    ////////////

    @Override
    public String toString() {
        return getName();
    }
}
