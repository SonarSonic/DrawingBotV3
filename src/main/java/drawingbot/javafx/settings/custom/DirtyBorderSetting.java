package drawingbot.javafx.settings.custom;

import drawingbot.image.filters.SimpleBorderFilter;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.IntegerSetting;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.fxmisc.easybind.EasyBind;

import java.awt.image.BufferedImage;

public class DirtyBorderSetting<C> extends IntegerSetting<C> {

    protected DirtyBorderSetting(DirtyBorderSetting<C> toCopy) {
        super(toCopy);
    }

    public DirtyBorderSetting(Class<C> clazz, String category, String settingName, Integer defaultValue) {
        super(clazz, category, settingName, defaultValue);
    }

    public DirtyBorderSetting(Class<C> pfmClass, String category, String settingName, int defaultValue, int minValue, int maxValue) {
        super(pfmClass, category, settingName, defaultValue, minValue, maxValue);
    }

    @Override
    public Node createJavaFXNode(boolean label) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(300);
        EasyBind.subscribe(valueProperty(), v -> {
            BufferedImage borderImage = SimpleBorderFilter.getBorderImage(v);
            if(borderImage != null){
                imageView.setImage(SwingFXUtils.toFXImage(borderImage, null));
            }
        });
        VBox vBox = new VBox();
        vBox.getChildren().add(super.createJavaFXNode(label));
        vBox.getChildren().add(imageView);
        return vBox;
    }

    @Override
    public GenericSetting<C, Integer> copy() {
        return new DirtyBorderSetting<>(this);
    }

}
