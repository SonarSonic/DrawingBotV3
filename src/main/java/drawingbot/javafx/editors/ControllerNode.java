package drawingbot.javafx.editors;

import drawingbot.javafx.controllers.AbstractFXController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class ControllerNode<C extends AbstractFXController> extends TreeNode {

    private Node root = null;
    private C controller = null;

    private final String fxmlPath;
    private boolean loadFailed = false;

    public ControllerNode(String name, String fxmlPath) {
        super(name);
        this.fxmlPath = fxmlPath;
    }

    public C getRoot(){
        return controller;
    }

    public C getController(){
        return controller;
    }

    public boolean loadFailed(){
        return loadFailed;
    }

    @Override
    public Node getContent() {
        if(root == null && !loadFailed){
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(ControllerNode.class.getResource(fxmlPath));
                root = fxmlLoader.load();
                controller = fxmlLoader.getController();
                fxmlLoader.setController(controller);
            } catch (IOException e) {
                e.printStackTrace();
                loadFailed = true;
            }
        }
        return root;
    }

}
