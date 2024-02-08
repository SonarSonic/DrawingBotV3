package drawingbot.javafx.controls;

import drawingbot.api.IDrawingPen;
import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSets;
import drawingbot.image.ImageTools;
import drawingbot.javafx.preferences.DBPreferences;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ComboCellDrawingPen extends ComboBoxListCell<IDrawingPen> {

    public final HBox hbox;
    public final CheckBox checkBox;
    public final Rectangle colour;
    public final Label displayNameLabel;
    public final Label userCreatedLabel;
    public Property<DrawingSets> drawingSets;
    private BooleanProperty property;

    public ComboCellDrawingPen(Property<DrawingSets> drawingSets, boolean useCheckBox) {
        super();
        this.drawingSets = drawingSets;
        hbox = new HBox();

        if (useCheckBox) {
            property = new SimpleBooleanProperty(false);
            checkBox = new CheckBox();
            checkBox.selectedProperty().bindBidirectional(property);
            checkBox.setOnAction(event -> {
                if(checkBox.isSelected()){
                    drawingSets.getValue().activeDrawingSet.get().addNewPen(getItem(), true);
                }else{
                    drawingSets.getValue().activeDrawingSet.get().pens.removeIf((p) -> p.getCodeName().equals(getItem().getCodeName()));
                }
            });
            hbox.setSpacing(10);
            hbox.getChildren().add(checkBox);
            hbox.setAlignment(Pos.CENTER_LEFT);
        } else {
            checkBox = null;
        }

        colour = new Rectangle(20, 12, Color.AQUA);
        hbox.getChildren().add(colour);
        hbox.getChildren().add(displayNameLabel = new Label());
        hbox.getChildren().add(userCreatedLabel = ComboCellPreset.createUserLabel());
    }

    @Override
    public void updateItem(IDrawingPen item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("");
            setGraphic(hbox);
            if (checkBox != null) {
                property.set(drawingSets.getValue().activeDrawingSet.get().containsPen(item));
            }
            hbox.setSpacing(4);
            hbox.setAlignment(Pos.CENTER_LEFT);
            displayNameLabel.setText("  " + item.getName());
            displayNameLabel.textFillProperty().bind(DBPreferences.INSTANCE.defaultThemeColor);
            colour.setFill(ImageTools.getColorFromARGB(item.getARGB()));
            String userCreatedText = "";
            if(item.isUserCreated()){
                userCreatedText = " (User)";
            }
            userCreatedLabel.setText(userCreatedText);
        }
    }
}
