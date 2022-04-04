package drawingbot.javafx.controls;


import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TableCellImage<S> extends TableCell<S, Image> {

    public ImageView imageView;

    public TableCellImage() {
        super();
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        tableColumnProperty().addListener((observable, oldValue, newValue) -> {
            imageView.fitWidthProperty().unbind();
            imageView.fitWidthProperty().bind(newValue.widthProperty());
        });
    }

    @Override
    protected void updateItem(Image item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            //FIXME sizing of the table doesn't update when this is used in the first row.
            setText(null);
            imageView.setImage(item);
            setGraphic(imageView);
            getTableView().refresh();
        }
    }
}
