package drawingbot.javafx.controls;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.TableCell;
import org.controlsfx.control.Rating;

import java.util.function.Function;

public class TableCellRating<S> extends TableCell<S, Double> {

    public Rating rating;

    public TableCellRating(int max, boolean partialRating, Function<S, DoubleProperty> propertySupplier){
        rating = new Rating(max);
        rating.setPartialRating(partialRating);
        rating.ratingProperty().addListener((observable, oldValue, newValue) -> {
            S item = getTableView().getItems().get(getIndex());
            if(item != null){
                DoubleProperty prop = propertySupplier.apply(item);
                if(prop.get() != newValue.doubleValue()){
                    prop.set(newValue.doubleValue());
                }
            }
        });
        rating.setMaxHeight(10);
        rating.getStylesheets().add(TableCellRating.class.getResource("ratingcell.css").toExternalForm());
    }

    @Override
    protected void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            rating.setRating(item);

            setText(null);
            setGraphic(rating);
        }
    }
}
