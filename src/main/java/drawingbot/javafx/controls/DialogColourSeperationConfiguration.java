package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.settings.RangedNumberSetting;
import drawingbot.utils.EnumColourSplitter;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.List;

public class DialogColourSeperationConfiguration extends Dialog<Boolean> {

    public static final GenericSetting<DrawingBotV3, Float> cyanMultiplier = GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "Cyan Multiplier", 1F, 0F, 1F, false, (dbv3, value) -> dbv3.cyanMultiplier.set(value)).setGetter(dbv3 -> dbv3.cyanMultiplier.get());
    public static final GenericSetting<DrawingBotV3, Float> magentaMultiplier = GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "Magenta Multiplier", 1F, 0F, 1F, false, (dbv3, value) -> dbv3.magentaMultiplier.set(value)).setGetter(dbv3 -> dbv3.magentaMultiplier.get());
    public static final GenericSetting<DrawingBotV3, Float> yellowMultiplier = GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "Yellow Multiplier", 1F, 0F, 1F, false, (dbv3, value) -> dbv3.yellowMultiplier.set(value)).setGetter(dbv3 -> dbv3.yellowMultiplier.get());
    public static final GenericSetting<DrawingBotV3, Float> keyMultiplier = GenericSetting.createRangedFloatSetting(DrawingBotV3.class, "Key Multiplier", 0.75F, 0F, 1F, false, (dbv3, value) -> dbv3.keyMultiplier.set(value)).setGetter(dbv3 -> dbv3.keyMultiplier.get());
    public static final List<GenericSetting<DrawingBotV3, Float>> settings = List.of(cyanMultiplier, magentaMultiplier, yellowMultiplier, keyMultiplier);

    public float cyan, magenta, yellow, key;


    public DialogColourSeperationConfiguration(EnumColourSplitter splitter) {
        super();
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_LEFT);

        gridPane.setVgap(4);
        gridPane.setHgap(4);

        settings.forEach(setting -> setting.updateSetting(DrawingBotV3.INSTANCE));

        cyan = DrawingBotV3.INSTANCE.cyanMultiplier.get();
        magenta = DrawingBotV3.INSTANCE.magentaMultiplier.get();
        yellow = DrawingBotV3.INSTANCE.yellowMultiplier.get();
        key = DrawingBotV3.INSTANCE.keyMultiplier.get();

        int i = 1;
        for(GenericSetting<?, ?> setting : settings){
            Label label = new Label(setting.settingName.getValue() + ": ");
            label.setAlignment(Pos.TOP_LEFT);
            Node node = setting.getJavaFXNode(true);
            node.minWidth(200);
            node.prefHeight(30);

            if(!(setting instanceof RangedNumberSetting)){
                //check boxes don't need a value label.
                gridPane.addRow(i, label, node);
            }else{
                TextField field = setting.getEditableTextField();
                gridPane.addRow(i, label, node, setting.getEditableTextField());
                field.setOnAction(e -> setting.setValueFromString(field.getText()));
            }
            i++;
        }
        setGraphic(gridPane);
        setTitle("Colour Seperation Configuration");
        getDialogPane().setPrefWidth(400);

        setResultConverter(param -> {
            if(param == ButtonType.APPLY){
                settings.forEach(setting -> setting.applySetting(DrawingBotV3.INSTANCE));
                return true;
            }else{
                cyanMultiplier.setValue(cyan);
                magentaMultiplier.setValue(magenta);
                yellowMultiplier.setValue(yellow);
                keyMultiplier.setValue(key);
                return false;
            }
        });

        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        FXApplication.applyDBIcon((Stage)getDialogPane().getScene().getWindow());

    }

}
