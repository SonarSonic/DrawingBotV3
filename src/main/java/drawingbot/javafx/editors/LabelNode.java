package drawingbot.javafx.editors;

import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Supplier;

/**
 * A simple node which just wraps a label
 */
public class LabelNode extends ElementNode {

    public Supplier<Node> supplier;
    private Node node;
    public boolean hideLabel = false;

    public LabelNode(String name, TreeNode... children) {
        super(name, children);
    }

    public LabelNode(String name, Supplier<Node> supplier, TreeNode... children) {
        super(name, children);
        this.supplier = supplier;
    }

    public LabelNode hideLabel() {
        this.hideLabel = true;
        return this;
    }

    @Override
    public void addElement(PageBuilder builder) {
        if (node == null && supplier != null) {
            node = supplier.get();
        }
        if (hideLabel) {
            if (node != null) {
                builder.addRow(node);
            }
            return;
        }

        Label label = new Label();
        label.textProperty().bind(nameProperty());
        label.getStyleClass().add(labelStyle);
        if (labelStyle.equals(TITLE_STYLE)) {

            HBox hBox = new HBox();
            HBox.setHgrow(label, Priority.ALWAYS);
            hBox.setMaxWidth(Double.MAX_VALUE);
            if (node != null) {
                hBox.getChildren().add(node);
            }
            hBox.getChildren().add(label);

            Separator separator = new Separator();
            separator.getStyleClass().add("preference-separator");
            separator.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(separator, Priority.SOMETIMES);
            VBox.setVgrow(separator, Priority.ALWAYS);
            separator.setValignment(VPos.CENTER);
            hBox.getChildren().add(separator);

            builder.addRow(hBox);
        } else {
            if (node != null) {
                builder.addRow(label, node);
            } else {
                builder.addRow(label);
            }
        }

        if (disabled != null) {
            label.disableProperty().bind(disabled);
        }
    }
}
