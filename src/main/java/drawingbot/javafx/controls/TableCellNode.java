package drawingbot.javafx.controls;

import javafx.scene.Node;
import javafx.scene.control.TableCell;

public class TableCellNode<S,T> extends TableCell<S, T> {

    public Node node;

    public TableCellNode() {
        super();
    }

    public void setNode(Node node){
        this.node = node;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("");
            setGraphic(node);
        }
    }
}
