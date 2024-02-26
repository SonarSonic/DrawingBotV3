package drawingbot.javafx.editors;

import drawingbot.DrawingBotV3;
import drawingbot.files.FileUtils;
import drawingbot.image.BufferedImageLoader;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.controls.ControlDirectoryPicker;
import drawingbot.javafx.controls.ControlFilePicker;
import drawingbot.javafx.editors.custom.EditorRangedNumber;
import drawingbot.javafx.editors.custom.EditorTextInputControl;
import drawingbot.javafx.settings.AbstractNumberSetting;
import drawingbot.javafx.settings.ImageSetting;
import drawingbot.javafx.settings.OptionSetting;
import drawingbot.javafx.util.JFXUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Contains implementations of various generic {@link IEditorFactory}s / {@link IEditor}s which are used by {@link IEditableProperty}s
 */
public class Editors {

    public static IEditor<Boolean> createCheckBox(EditorContext context, IEditableProperty<Boolean> property){
        return new EditorSimple<>(context, property, new CheckBox()) {

            {
                getNode().selectedProperty().bindBidirectional(property.valueProperty());
                getNode().setOnAction(e -> getProperty().sendUserEditedEvent());
            }

            @Override
            public void dispose() {
                getNode().selectedProperty().unbindBidirectional(property.valueProperty());
                getNode().setOnAction(null);
            }
        };
    }

    public static IEditor<String> createTextField(EditorContext context, IEditableProperty<String> property){
        return new EditorSimple<>(context, property, new TextField()) {

            {
                getNode().textProperty().bindBidirectional(property.valueProperty());
                getNode().setOnAction(e -> property.sendUserEditedEvent());
            }

            @Override
            public void dispose() {
                getNode().textProperty().unbindBidirectional(property.valueProperty());
                getNode().setOnAction(null);
            }
        };
    }


    public static IEditor<Color> createColorPicker(EditorContext context, IEditableProperty<Color> property){
        return new EditorSimple<>(context, property, new ColorPicker()) {

            {
                getNode().valueProperty().bindBidirectional(property.valueProperty());
                getNode().setOnAction(e -> getProperty().sendUserEditedEvent());
            }

            @Override
            public void dispose() {
                getNode().valueProperty().unbindBidirectional(property.valueProperty());
                getNode().setOnAction(null);
            }
        };
    }

    public static IEditor<LocalDate> createDatePicker(EditorContext context, IEditableProperty<LocalDate> property){
        return new EditorSimple<>(context, property, new DatePicker()) {

            {
                getNode().valueProperty().bindBidirectional(property.valueProperty());
                getNode().setOnAction(e -> getProperty().sendUserEditedEvent());
            }

            @Override
            public void dispose() {
                getNode().valueProperty().unbindBidirectional(property.valueProperty());
                getNode().setOnAction(null);
            }
        };
    }

    public static IEditor<String> createDirectoryPicker(EditorContext context, IEditableProperty<String> property, Supplier<File> initialDirectorySupplier){
        return new EditorSimple<>(context, property, new ControlDirectoryPicker()) {

            {
                getNode().valueProperty().bindBidirectional(property.valueProperty());
                getNode().initialDirectoryProperty().bind(Bindings.createObjectBinding(initialDirectorySupplier::get, property.valueProperty()));
                getNode().setWindowTitle("Select Directory: " + property.getDisplayName());
                getNode().setOnEdit(s -> property.sendUserEditedEvent());
            }

            @Override
            public void dispose() {
                getNode().valueProperty().unbindBidirectional(property.valueProperty());
                getNode().setOnEdit(null);
            }
        };
    }

    public static IEditor<String> createFilePicker(EditorContext context, IEditableProperty<String> property, Supplier<File> initialDirectorySupplier, String initialFileName, FileChooser.ExtensionFilter ...filters){
        return new EditorSimple<>(context, property, new ControlFilePicker()) {

            {
                getNode().valueProperty().bindBidirectional(property.valueProperty());
                getNode().initialDirectoryProperty().bind(Bindings.createObjectBinding(initialDirectorySupplier::get, property.valueProperty()));
                getNode().setWindowTitle("Select File: " + property.getDisplayName());
                getNode().setInitialFileName(initialFileName);
                getNode().getExtensionFilters().addAll(filters);
                getNode().setOnEdit(s -> property.sendUserEditedEvent());
            }

            @Override
            public void dispose() {
                getNode().valueProperty().unbindBidirectional(property.valueProperty());
                getNode().setOnEdit(null);
            }
        };
    }

    public static <V> IEditor<V> createChoiceEditor(EditorContext context, IEditableProperty<V> property){
        return new EditorSimple<>(context, property, new ComboBox<V>()) {

            {
                getNode().valueProperty().bindBidirectional(property.valueProperty());
                if(property instanceof OptionSetting<?, V> optionSetting){
                    getNode().itemsProperty().set(optionSetting.getOptions());
                }
                getNode().setOnAction(e -> getProperty().sendUserEditedEvent());
            }

            @Override
            public void dispose() {
                getNode().valueProperty().unbindBidirectional(property.valueProperty());
                if(property instanceof OptionSetting<?, ?>){
                    getNode().itemsProperty().set(null);
                }
                getNode().setOnAction(null);
            }
        };
    }

    public static <V> IEditor<V> createComboBox(EditorContext context, IEditableProperty<V> property, boolean editable, ObjectProperty<ObservableList<V>> options){
        return new EditorSimple<>(context, property, new ComboBox<V>()) {

            {
                getNode().valueProperty().bindBidirectional(property.valueProperty());
                getNode().itemsProperty().bind(options);
                getNode().setOnAction(e -> getProperty().sendUserEditedEvent());
                getNode().setEditable(true);
            }

            @Override
            public void dispose() {
                getNode().valueProperty().unbindBidirectional(property.valueProperty());
                getNode().itemsProperty().unbind();
                getNode().setOnAction(null);
            }
        };
    }

    public static <V extends Number> IEditor<V> createRangedNumberEditor(EditorContext context, IEditableProperty<V> property){
        if(property instanceof AbstractNumberSetting<?, V> setting){
            return new EditorRangedNumber<>(context, setting);
        }
        return createGenericTextField(context, property);
    }

    public static <V> IEditor<V> createImageSelector(EditorContext context, IEditableProperty<V> property){

        if(property instanceof ImageSetting<?> setting){
            return new EditorSimple<>(context, property, new VBox()) {

                public transient ImageView imageView;
                public transient SimpleObjectProperty<WritableImage> thumbnail;
                public ChangeListener<? super String> valueListener;

                private Button button = null;

                {
                    this.imageView = new ImageView();
                    this.thumbnail = new SimpleObjectProperty<>(null);

                    this.imageView.imageProperty().bind(thumbnail);
                    this.imageView.preserveRatioProperty().set(true);
                    this.imageView.setFitWidth(400);
                    this.imageView.setFitHeight(400);
                    //this.imageView.get().fitWidthProperty().bind(DrawingBotV3.INSTANCE.controller.versionThumbColumn.widthProperty()); //TODO CHECK ME!

                    button = new Button("Select Image");

                    button.setOnAction(event -> FXHelper.importFile(DrawingBotV3.context(), (file, chooser) -> setting.value.set(file.getPath()), FileUtils.IMPORT_IMAGES));

                    setting.valueProperty().addListener(valueListener = (observable, oldValue, newValue) -> {
                        BufferedImageLoader loader = new BufferedImageLoader(DrawingBotV3.context(), newValue, false);
                        DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.backgroundService, loader);
                        loader.setOnSucceeded(e -> {
                            thumbnail.set(SwingFXUtils.toFXImage(loader.getValue(), null));
                            getProperty().sendUserEditedEvent();
                        });
                        loader.setOnFailed(e -> thumbnail.set(null));
                    });
                    node.setSpacing(4);
                    node.getChildren().add(button);
                    node.getChildren().add(imageView);
                }

                @Override
                public void dispose() {
                    this.imageView.setImage(null);
                    this.button.setOnAction(null);
                    setting.valueProperty().removeListener(valueListener);

                }
            };
        }
        return createGenericTextField(context, property);
    }

    public static <N extends Number> IEditor<N> createNumberTextField(EditorContext context, IEditableProperty<N> property){
        return new EditorSimple<>(context, property, new TextField()) {

            {
                StringConverter<N> converter = JFXUtils.getNumberStringConverter(property.getType());
                node.textProperty().bindBidirectional(property.valueProperty(), converter);
                node.setTextFormatter(new TextFormatter<>(converter));
                node.setOnAction(e -> property.sendUserEditedEvent());
            }

            @Override
            public void dispose() {
                node.textProperty().unbindBidirectional(property.valueProperty());
                node.setTextFormatter(null);
                node.setOnAction(null);
            }
        };
    }

    public static <V> IEditor<V> createGenericTextField(EditorContext context, IEditableProperty<V> property){
        return new EditorTextInputControl.Field<>(context, property);
    }

    public static <V> IEditor<V> createGenericTextArea(EditorContext context, IEditableProperty<V> property){
        return new EditorTextInputControl.Area<>(context, property);
    }

    public static <V> IEditor<V> createGenericDummyEditor(EditorContext context, IEditableProperty<V> property){
        return new EditorSimple<>(context, property, new Label()) {

            {
                getNode().setText(property.getDisplayName());
            }

            @Override
            public void dispose() {

            }
        };
    }

    ///////////////////////////////////////////////////

    /**
     * Utility method for creating {@link IEditableProperty} for wrapped {@link javafx.beans.property.Property}
     * Any {@link drawingbot.javafx.GenericSetting} should provide a direct reference to a {@link IEditorFactory} rather than using this general method
     * @param type the type of the value stored in the property
     * @return the {@link IEditorFactory}
     */
    public static <V> IEditorFactory<V> getDefaultEditorFactory(Class<V> type){
        if(type == Boolean.class){
            return (context, property) -> castEditor(createCheckBox(context, castProperty(property)));
        }
        if(Number.class.isAssignableFrom(type)){
            return (context, property) -> castEditor(createNumberTextField(context, castProperty(property)));
        }
        if(type == String.class){
            return (context, property) -> castEditor(createTextField(context, castProperty(property)));
        }
        if(Color.class.isAssignableFrom(type)){
            return (context, property) -> castEditor(createColorPicker(context, castProperty(property)));
        }
        if(type == Enum.class){
            return Editors::createChoiceEditor;
        }
        DrawingBotV3.logger.warning("Missing Editor Factory for " + type);
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <V> IEditableProperty<V> castProperty(IEditableProperty<?> property){
        return (IEditableProperty<V>) property;
    }

    @SuppressWarnings("unchecked")
    public static <V> IEditorFactory<V> castEditorFactory(IEditorFactory<?> factory){
        return (IEditorFactory<V>) factory;
    }

    @SuppressWarnings("unchecked")
    public static <V> IEditor<V> castEditor(IEditor<?> factory){
        return (IEditor<V>) factory;
    }

}
