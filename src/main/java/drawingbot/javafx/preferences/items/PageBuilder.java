package drawingbot.javafx.preferences.items;

import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.editors.EditorStyle;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.*;

import java.util.List;

/**
 * The logic responsible for the layout of the page, at the moment this is a basic 3 column structure e.g. Label, Editor (Custom Node), Reset
 * An instance of the {@link PageBuilder} is passed when constucting pages via the methods in {@link EditorSheet}
 */
public class PageBuilder {

    public EditorContext context = null;
    public GridPane gridPane;

    public PageBuilder(PageNode page){
        this.context = new EditorContext(page, EditorStyle.DETAILED);
    }

    public PageBuilder(EditorContext context){
        this.context = context;
    }

    public void init() {
        if (gridPane != null) {
            return;
        }
        gridPane = new GridPane();
        gridPane.getStyleClass().add("preference-grid");

        ColumnConstraints column1 = new ColumnConstraints(100, -1, -1, Priority.SOMETIMES, HPos.LEFT, true);
        ColumnConstraints column2 = new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.LEFT, true);
        ColumnConstraints column3 = new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, true);

        gridPane.getColumnConstraints().addAll(column1, column2, column3);

        HBox.setHgrow(gridPane, Priority.ALWAYS);
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        gridPane.setMaxWidth(Double.MAX_VALUE);
    }

    public void build(List<TreeNode> nodes) {
        init();

        for (TreeNode node : nodes) {
            if (node instanceof ElementNode elementNode) {
                elementNode.addElement(this);
            }
        }

    }

    /**
     * Adds a row which consists of only one {@link Node} which spans all the columns
     */
    public void addRow(Node node) {
        int row = gridPane.getRowCount();
        gridPane.add(node, 0, row, 3, 1);
        gridPane.getRowConstraints().add(new RowConstraints(-1, -1, -1, Priority.NEVER, VPos.TOP, false));
    }

    /**
     * Adds a row which consists of a label and editor, spanning only the first 2 columns
     */
    public void addRow(Node label, Node editor) {
        int row = gridPane.getRowCount();
        HBox.setHgrow(editor, Priority.ALWAYS);
        GridPane.setHgrow(editor, Priority.ALWAYS);
        gridPane.addRow(row, label, editor);
        gridPane.getRowConstraints().add(new RowConstraints(-1, -1, -1, Priority.NEVER, VPos.TOP, false));
    }

    /**
     * Adds a row which consists of a label, editor and reset button
     */
    public void addRow(Node label, Node editor, Node reset) {
        int row = gridPane.getRowCount();
        HBox.setHgrow(editor, Priority.ALWAYS);
        GridPane.setHgrow(editor, Priority.ALWAYS);
        gridPane.addRow(row, label, editor, reset);
        gridPane.getRowConstraints().add(new RowConstraints(-1, -1, -1, Priority.NEVER, VPos.TOP, false));
    }

    public Node getContent() {
        return gridPane;
    }

}
