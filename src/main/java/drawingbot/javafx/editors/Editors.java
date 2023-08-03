package drawingbot.javafx.editors;

import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.IJsonData;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.settings.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Class for creating simple editor dialogs e.g. pages of settings which can be displayed as a tree or as actual settings.
 *
 */
public class Editors {

    public static TreeItem<TreeNode> build(TreeNode rootNode, String search){
        TreeItem<TreeNode> root = new TreeItem<>(rootNode);
        root.setExpanded(true);

        for(TreeNode child : rootNode.getChildren()){
            root.getChildren().add(build(child, search));
        }

        if(search != null){
            prune(root, search);
        }

        //sort(root, Comparator.comparing(o -> o.getValue().getName()));

        return root;
    }

    public static boolean prune(TreeItem<TreeNode> treeItem, String search){
        if(treeItem.getValue().isHiddenFromTree()){
            return false;
        }
        if(treeItem.isLeaf()){
            return treeItem.getValue().getName().toLowerCase().contains(search.toLowerCase());
        }else{
            List<TreeItem<TreeNode>> toRemove = new ArrayList<>();

            for (TreeItem<TreeNode> child : treeItem.getChildren()) {
                boolean keep = prune(child, search);
                if (! keep) {
                    toRemove.add(child);
                }
            }
            treeItem.getChildren().removeAll(toRemove);

            return !treeItem.getChildren().isEmpty();
        }
    }

    private static void sort(TreeItem<TreeNode> node, Comparator<TreeItem<TreeNode>> comparator) {
        node.getChildren().sort(comparator);
        for (TreeItem<TreeNode> child : node.getChildren()) {
            sort(child, comparator);
        }
    }

    public static TreeNode root(TreeNode...children){
        return new TreeNode("root", children);
    }

    public static TreeNode node(String name, TreeNode...children){
        return new TreeNode(name, children);
    }

    public static PageNode page(String name, TreeNode...children){
        return new PageNode(name, children){

            @Override
            public Node buildContent() {
                PageBuilder builder = new PageBuilder();
                builder.build(this.getChildren());
                return builder.gridPane;
            }
        };
    }
    public static PageNode page(String name, Consumer<ObservableList<TreeNode>> builder){
        PageNode pageNode = new PageNode(name){

            @Override
            public Node buildContent() {
                PageBuilder builder = new PageBuilder();
                builder.build(this.getChildren());
                return builder.gridPane;
            }
        };
        builder.accept(pageNode.children);
        return pageNode;
    }

    public static void property(GridPane gridPane, String displayName, Property<?> property, Class<?> type){
        Label label = new Label(displayName);
        gridPane.addRow(gridPane.getRowCount(), label, Editors.createEditor(property, type));
    }

    public static void node(GridPane gridPane, Node node){
        gridPane.add(node, 0, gridPane.getRowCount(), 2, 1);
    }

    /////////////////////////


    public static DefaultPropertyEditorFactory defaultPropertyEditorFactory = new DefaultPropertyEditorFactory();

    public static PropertyEditor<?> getPropertyEditor(PropertySheet.Item item){
        //TODO CUSTOM EDITORS
        if(item instanceof SettingProperty && ((SettingProperty) item).setting instanceof BooleanSetting){
            return createSwitchEditor(item);
        }
        return defaultPropertyEditorFactory.call(item);
    }

    public static PropertyEditor<?> createSwitchEditor( PropertySheet.Item property) {

        return new AbstractPropertyEditor<Boolean, ToggleSwitch>(property, new ToggleSwitch()) {

            @Override protected BooleanProperty getObservableValue() {
                return getEditor().selectedProperty();
            }

            @Override public void setValue(Boolean value) {
                getEditor().setSelected(value);
            }
        };
    }

    public static <O extends IJsonData> ComboBox<GenericPreset<O>> createDefaultPresetComboBox(AbstractPresetLoader<O> loader){
        ComboBox<GenericPreset<O>> comboBox = new ComboBox<>();
        comboBox.setItems(loader.presets);
        comboBox.setValue(loader.getDefaultPreset());
        DBPreferences.INSTANCE.flagDefaultPresetChange.addListener((observable) -> {
            comboBox.setValue(loader.getDefaultPreset());
        });
        comboBox.setOnAction(e -> {
            DBPreferences.INSTANCE.setDefaultPreset(comboBox.getValue());
        });
        return comboBox;
    }

    public static Node createDefaultFolderPicker(String title, Supplier<File> initialDirectory, Property<String> stringProperty){
        HBox hBox = new HBox();
        hBox.setSpacing(8);

        Button configure = new Button("Configure");
        configure.setOnAction(e -> FXHelper.selectFolder(title, initialDirectory.get(), file -> stringProperty.setValue(file.toString())));
        hBox.getChildren().add(configure);

        Label fileLabel = new Label();
        fileLabel.textProperty().bind(stringProperty);
        hBox.getChildren().add(fileLabel);
        fileLabel.setMaxWidth(275);
        fileLabel.setWrapText(true);
        return hBox;
    }

    @Nullable
    public static Node createNodeEditor(GenericSetting<?, ?> generic){
        if(generic instanceof AbstractNumberSetting){
            AbstractNumberSetting<?, ?> numberSetting = (AbstractNumberSetting<?, ?>) generic;
            if(numberSetting.isRanged){
                TextField textField = generic.getEditableTextField();
                Node node = generic.getJavaFXEditor(true);
                if(node == textField){
                    return node;
                }

                HBox hBox = new HBox();
                hBox.setSpacing(4);
                hBox.getChildren().addAll(node, textField);
                HBox.setHgrow(node, Priority.ALWAYS);
                HBox.setHgrow(textField, Priority.ALWAYS);
                textField.setPrefWidth(80);
                textField.setPrefHeight(12);
                return hBox;
            }
        }

        if(generic.hasCustomEditor()){
            return generic.getJavaFXEditor(true);
        }
        return generic.getEditableTextField();
    }

    public static Node createEditor(GenericSetting<?, ?> generic){
        /*
        Node editor = null;
        if(generic instanceof BooleanSetting){
            BooleanSetting<?> setting = (BooleanSetting<?>) generic;
            editor = createSwitchEditor(setting.valueProperty());
        }else if(generic instanceof ColourSetting){
            ColourSetting<?> setting = (ColourSetting<?>) generic;
            editor = createColorEditor(setting.valueProperty());
        }else if(generic instanceof DoubleSetting){
            DoubleSetting<?> setting = (DoubleSetting<?>) generic;
            editor = createDoubleTextEditor(setting);
        }else if(generic instanceof FloatSetting){
            FloatSetting<?> setting = (FloatSetting<?>) generic;
            editor = createFloatTextEditor(setting);
        }else if(generic instanceof IntegerSetting){
            IntegerSetting<?> setting = (IntegerSetting<?>) generic;
            editor = createIntegerTextEditor(setting);
        }else if(generic instanceof LongSetting){
            LongSetting<?> setting = (LongSetting<?>) generic;
            editor = createLongTextEditor(setting);
        }else if(generic instanceof OptionSetting){
            OptionSetting<?, ?> setting = (OptionSetting<?, ?>) generic;
            editor = createChoiceEditor(setting);
        }else if(generic instanceof StringSetting){
            StringSetting<?> setting = (StringSetting<?>) generic;
            editor = createTextEditor(setting.valueProperty());
        }

         */
        return generic.getJavaFXEditor(true);
    }

    public static Node createEditor(Property property, Class<?> type){
        PropertyEditor editor = defaultPropertyEditorFactory.call(new PropertySheet.Item() {
            @Override
            public Class<?> getType() {
                return type;
            }

            @Override
            public String getCategory() {
                return "";
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public Object getValue() {
                return property.getValue();
            }

            @Override
            public void setValue(Object value) {
                property.setValue(value);
            }

            @Override
            public Optional<ObservableValue<? extends Object>> getObservableValue() {
                return Optional.of(property);
            }
        });
        editor.setValue(property.getValue());
        HBox.setHgrow(editor.getEditor(), Priority.ALWAYS);
        return editor.getEditor();
    }

    public static Node createCheckboxEditor(Property<Boolean> booleanProperty){
        CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().bindBidirectional(booleanProperty);
        return checkBox;
    }

    public static Node createSwitchEditor(Property<Boolean> booleanProperty){
        ToggleSwitch toggleSwitch = new ToggleSwitch();
        toggleSwitch.selectedProperty().bindBidirectional(booleanProperty);
        return toggleSwitch;
    }

    public static Node createTextEditor(Property<String> booleanProperty){
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(booleanProperty);
        return textField;
    }

    public static Node createDoubleTextEditor(DoubleSetting<?> doubleSetting){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), doubleSetting.getDefaultValue()));
        textField.textProperty().bindBidirectional(doubleSetting.valueProperty(), new DoubleStringConverter());
        return textField;
    }

    public static Node createDoubleTextEditor(Property<Double> doubleProperty){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), 0D));
        textField.textProperty().bindBidirectional(doubleProperty, new DoubleStringConverter());
        return textField;
    }

    public static Node createFloatTextEditor(FloatSetting<?> floatProperty){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new FloatStringConverter(), floatProperty.getDefaultValue()));
        textField.textProperty().bindBidirectional(floatProperty.valueProperty(), new FloatStringConverter());
        return textField;
    }

    public static Node createFloatTextEditor(Property<Float> floatProperty){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new FloatStringConverter(), 0F));
        textField.textProperty().bindBidirectional(floatProperty, new FloatStringConverter());
        return textField;
    }

    public static Node createIntegerTextEditor(IntegerSetting<?> integerSetting){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), integerSetting.getDefaultValue()));
        textField.textProperty().bindBidirectional(integerSetting.valueProperty(), new IntegerStringConverter());
        return textField;
    }

    public static Node createIntegerTextEditor(Property<Integer> integerProperty){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0));
        textField.textProperty().bindBidirectional(integerProperty, new IntegerStringConverter());
        return textField;
    }

    public static Node createLongTextEditor(LongSetting<?> longSetting){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(longSetting.getStringConverter(), longSetting.getDefaultValue()));
        textField.textProperty().bindBidirectional(longSetting.valueProperty(), new LongStringConverter());
        return textField;
    }

    public static Node createLongTextEditor(Property<Long> longProperty){
        TextField textField = new TextField();
        textField.setTextFormatter(new TextFormatter<>(new LongStringConverter(), 0L));
        textField.textProperty().bindBidirectional(longProperty, new LongStringConverter());
        return textField;
    }

    public static <T> Node createChoiceEditor(OptionSetting<?, T> optionSetting){
        ComboBox<T> comboBox = new ComboBox<>();
        comboBox.setItems(optionSetting.getOptions());
        comboBox.valueProperty().bindBidirectional(optionSetting.valueProperty());
        return comboBox;
    }

    public static <T> Node createChoiceEditor(Property<T> selectProperty, ObservableList<T> options){
        ComboBox<T> comboBox = new ComboBox<>();
        comboBox.setItems(options);
        comboBox.valueProperty().bindBidirectional(selectProperty);
        return comboBox;
    }

    public static Node createColorEditor(Property<Color> selectProperty){
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.valueProperty().bindBidirectional(selectProperty);
        return colorPicker;
    }

    public static Node createDateEditor(Property<LocalDate> selectProperty){
        DatePicker datePicker = new DatePicker();
        datePicker.valueProperty().bindBidirectional(selectProperty);
        return datePicker;
    }

    public static Node createTextAreaEditorLazy(Property<?> stringProperty){
        return createTextAreaEditor((Property<String>)stringProperty);
    }

    public static Node createTextAreaEditor(Property<String> booleanProperty){
        TextArea textArea = new TextArea();
        textArea.textProperty().bindBidirectional(booleanProperty);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        textArea.setPrefRowCount(6);
        textArea.setMinHeight(100);
        return textArea;
    }
}