package drawingbot.javafx.controls;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;

public class SkinPresetEditor extends SkinBase<ControlPresetEditor> {

    protected SkinPresetEditor(ControlPresetEditor control) {
        super(control);

        ChangeListener<Node> nodeChangeListener = (observable, oldValue, newValue) -> {
            refreshChildren();
        };
        control.useTreeNodeProperty().addListener((observable, oldValue, newValue) -> {
            refreshChildren();
        });

        control.editorProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                oldValue.nodeProperty().removeListener(nodeChangeListener);
            }
            if(newValue != null){
                newValue.nodeProperty().addListener(nodeChangeListener);
            }
            refreshChildren();
        });
        refreshChildren();
    }

    public void refreshChildren(){
        getChildren().clear();
        if(getSkinnable().getEditor() != null && !getSkinnable().useTreeNode()){
            Node node = getSkinnable().getEditor().getNode();
            if(node == null){
                return;
            }
            getChildren().add(getSkinnable().getEditor().getNode());
        }
    }
}
