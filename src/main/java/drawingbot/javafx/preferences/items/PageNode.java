package drawingbot.javafx.preferences.items;

import javafx.scene.Node;

/**
 * Can be initialized easily with {@link EditorSheet#page(String, TreeNode...)}
 * Alternatively override {@link PageNode#buildContent()} to create custom pages
 */
public abstract class PageNode extends TreeNode {

    private Node content;

    public PageNode(String name) {
        super(name);
    }

    public PageNode(String name, TreeNode... children) {
        super(name, children);
    }

    public Node getContent() {
        if (content == null) {
            content = buildContent();
        }
        return content;
    }

    public Node rebuild(){
        return content = buildContent();
    }

    public abstract Node buildContent();

}
