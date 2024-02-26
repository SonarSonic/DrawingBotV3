package drawingbot.javafx.settings.custom;

import drawingbot.image.filters.SimpleBorderFilter;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.EditorSimple;
import drawingbot.javafx.editors.IEditor;
import drawingbot.javafx.editors.IEditorFactory;
import drawingbot.javafx.settings.IntegerSetting;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

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
    public IEditorFactory<Integer> defaultEditorFactory() {
        return (context, property) -> new EditorSimple<>(context, this, new VBox()) {

            private IEditor<Integer> internalEditor = null;
            private ImageView imageView = null;
            private Subscription imageSubscription;

            {
                imageView = new ImageView();
                imageView.setFitWidth(300);
                imageView.setFitHeight(300);
                imageSubscription = EasyBind.subscribe(valueProperty(), v -> {
                    BufferedImage borderImage = SimpleBorderFilter.getBorderImage(v);
                    if(borderImage != null){
                        imageView.setImage(SwingFXUtils.toFXImage(borderImage, null));
                    }
                });
                internalEditor = DirtyBorderSetting.super.defaultEditorFactory().createEditor(context(), DirtyBorderSetting.this);
                node.getChildren().add(internalEditor.getNode());
                node.getChildren().add(imageView);
                node.setSpacing(8);
            }

            @Override
            public void dispose() {
                imageSubscription.unsubscribe();
                imageView.setImage(null);
                internalEditor.dispose();
            }
        };
    }

    @Override
    public GenericSetting<C, Integer> copy() {
        return new DirtyBorderSetting<>(this);
    }

}
