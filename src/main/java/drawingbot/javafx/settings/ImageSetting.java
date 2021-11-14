package drawingbot.javafx.settings;

import drawingbot.DrawingBotV3;
import drawingbot.image.BufferedImageLoader;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericSetting;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ImageSetting<C> extends GenericSetting<C, String> {

    public SimpleObjectProperty<ImageView> imageView;
    public SimpleObjectProperty<WritableImage> thumbnail;

    public ImageSetting(Class<C> clazz, String settingName, String defaultValue, boolean shouldLock, BiConsumer<C, String> setter) {
        super(clazz, settingName, defaultValue, new DefaultStringConverter(), null, shouldLock, s -> s, setter);
    }

    @Override
    protected Node createJavaFXNode(boolean label) {

        this.imageView = new SimpleObjectProperty<>(new ImageView());
        this.thumbnail = new SimpleObjectProperty<>(null);

        this.imageView.get().imageProperty().bind(thumbnail);
        this.imageView.get().preserveRatioProperty().set(true);
        this.imageView.get().fitWidthProperty().bind(DrawingBotV3.INSTANCE.controller.versionThumbColumn.widthProperty());

        Button button = new Button("Select Image");

        button.setOnAction(event -> FXHelper.importFile(file -> {
            value.set(file.getPath());
        }));

        value.addListener((observable, oldValue, newValue) -> {
            BufferedImageLoader loader = new BufferedImageLoader(newValue, false);
            DrawingBotV3.INSTANCE.backgroundService.submit(loader);
            loader.setOnSucceeded(e -> thumbnail.set(SwingFXUtils.toFXImage(loader.getValue(), null)));
            loader.setOnFailed(e -> thumbnail.set(null));
        });

        VBox vBox = new VBox();
        vBox.getChildren().add(button);
        vBox.getChildren().add(imageView.get());

        return vBox;
    }

    @Override
    public GenericSetting<C, String> copy() {
        return new ImageSetting<>(clazz, settingName.get(), defaultValue, lock.get(), setter);
    }
}
