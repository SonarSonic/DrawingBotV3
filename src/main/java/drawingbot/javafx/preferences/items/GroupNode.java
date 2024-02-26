package drawingbot.javafx.preferences.items;

public class GroupNode extends ElementNode {

    public GroupNode(String name, TreeNode... children) {
        super(name, children);
    }

    @Override
    public void addElement(PageBuilder builder) {
        getChildren().forEach(child -> {
            if(child instanceof ElementNode elementNode){
                elementNode.addElement(builder);
            }
        });
    }

}
