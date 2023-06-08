package drawingbot.javafx.settings;

import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
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

public class ImageSetting<C> extends GenericSetting<C, String> {

    public transient SimpleObjectProperty<ImageView> imageView;
    public transient SimpleObjectProperty<WritableImage> thumbnail;

    protected ImageSetting(GenericSetting<C, String> toCopy) {
        super(toCopy, toCopy.getValue());
    }

    public ImageSetting(Class<C> clazz, String category, String settingName, String defaultValue) {
        super(clazz, String.class, category, settingName, defaultValue);
    }

    @Override
    protected StringConverter<String> defaultStringConverter() {
        return StringSetting.stringConverter;
    }

    @Override
    protected Node createJavaFXNode(boolean label) {

        this.imageView = new SimpleObjectProperty<>(new ImageView());
        this.thumbnail = new SimpleObjectProperty<>(null);

        this.imageView.get().imageProperty().bind(thumbnail);
        this.imageView.get().preserveRatioProperty().set(true);
        this.imageView.get().setFitWidth(400);
        this.imageView.get().setFitHeight(400);
        //this.imageView.get().fitWidthProperty().bind(DrawingBotV3.INSTANCE.controller.versionThumbColumn.widthProperty()); //TODO CHECK ME!

        Button button = new Button("Select Image");

        button.setOnAction(event -> FXHelper.importFile((file, chooser) -> value.set(file.getPath()), FileUtils.IMPORT_IMAGES));

        value.addListener((observable, oldValue, newValue) -> {
            BufferedImageLoader loader = new BufferedImageLoader(DrawingBotV3.context(), newValue, false);
            DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.backgroundService, loader);
            loader.setOnSucceeded(e -> {
                thumbnail.set(SwingFXUtils.toFXImage(loader.getValue(), null));
                sendUserEditedEvent();
            });
            loader.setOnFailed(e -> thumbnail.set(null));
        });

        VBox vBox = new VBox();
        vBox.setSpacing(4);
        vBox.getChildren().add(button);
        vBox.getChildren().add(imageView.get());

        return vBox;
    }

    @Override
    public GenericSetting<C, String> copy() {
        return new ImageSetting<>(this);
    }
}
