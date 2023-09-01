package drawingbot.javafx.controls;

import javafx.scene.Node;
import javafx.scene.control.TableCell;

import java.util.function.BiFunction;

public class TableCellNode<S,T> extends TableCell<S, T> {

    public BiFunction<S, T, Node> nodeFactory;

    public TableCellNode(BiFunction<S, T, Node> nodeFactory) {
        super();
        this.nodeFactory = nodeFactory;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {

            setText("");
            setGraphic(nodeFactory.apply(getTableRow().getItem(), item));
        }
    }
}
