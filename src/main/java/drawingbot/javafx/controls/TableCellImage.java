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
        setGraphic(imageView);
    }

    @Override
    protected void updateItem(Image item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            imageView.setVisible(false);
        } else {
            imageView.setVisible(true);
            imageView.setImage(item);
        }
    }
}
