package drawingbot.javafx.preferences;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.IJsonData;
import drawingbot.files.json.presets.PresetGCodeSettings;
import drawingbot.files.json.presets.PresetPFMSettings;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controls.ComboCellNamedSetting;
import drawingbot.javafx.editors.SettingEditors;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.settings.BooleanSetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.UnitsLength;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Note this implementation is inspired by / borrows from ControlsFX, PreferencesFX and FXSampler
 */
public class FXPreferences {

    public TextField textFieldSearchBar = null;
    public Label labelHeading = null;
    public TreeView<TreeNode> treeViewCategories = null;
    public ScrollPane scrollPaneContent = null;
    public TreeNode root = null;

    @FXML
    public void initialize(){
        DrawingBotV3.INSTANCE.controller.preferencesStage.setResizable(true);
        DrawingBotV3.INSTANCE.controller.preferencesStage.setOnHidden(e -> Register.PRESET_LOADER_CONFIGS.markDirty());

        DBPreferences settings = DrawingBotV3.INSTANCE.getPreferences();
        root = root(
                page("General", builder -> {
                            builder.addAll(
                                    new LabelNode("Defaults").setTitleStyling(),
                                    new SettingNode("Path Finding Module", settings.defaultPFM) {
                                        @Override
                                        public Node createEditor() {
                                            ComboBox<PFMFactory<?>> comboBoxPFM = new ComboBox<>();
                                            comboBoxPFM.setCellFactory(param -> new ComboCellNamedSetting<>());
                                            comboBoxPFM.setItems(MasterRegistry.INSTANCE.getObservablePFMLoaderList());
                                            comboBoxPFM.setValue(MasterRegistry.INSTANCE.getDefaultPFM());
                                            comboBoxPFM.setOnAction(e -> {
                                                settings.defaultPFM.setValue(comboBoxPFM.getValue().getName());
                                            });
                                            return comboBoxPFM;
                                        }
                                    },
                                    new SettingNode("Pen Width (mm)", settings.defaultPenWidth),
                                    new SettingNode("Rescaling Mode", settings.defaultRescalingMode),
                                    new SettingNode("Canvas Colour", settings.defaultCanvasColour),
                                    new SettingNode("Background Colour", settings.defaultBackgroundColour),
                                    new SettingNode("Clipping Mode", settings.defaultClippingMode),
                                    new SettingNode("Blend Mode", settings.defaultBlendMode),
                                    new SettingNode("Apply Shapes Slider to Export", settings.defaultRangeExport),
                                    new LabelNode("", () -> {
                                        Button button = new Button("Reset All");
                                        button.setOnAction(e -> {
                                            settings.defaultPFM.resetSetting();
                                            settings.defaultPenWidth.resetSetting();
                                            settings.defaultRescalingMode.resetSetting();
                                            settings.defaultCanvasColour.resetSetting();
                                            settings.defaultBackgroundColour.resetSetting();
                                            settings.defaultClippingMode.resetSetting();
                                        });
                                        return button;
                                    }),
                                    new LabelNode("Preset Defaults").setTitleStyling(),
                                    new LabelNode("Drawing Area", () -> createDefaultPresetComboBox(Register.PRESET_LOADER_DRAWING_AREA)),
                                    new LabelNode("Image Processing", () -> createDefaultPresetComboBox(Register.PRESET_LOADER_FILTERS)),
                                    new LabelNode("VPype Settings", () -> createDefaultPresetComboBox(Register.PRESET_LOADER_VPYPE_SETTINGS)),
                                    new LabelNode("", () -> {
                                        Button button = new Button("Reset All");
                                        button.setOnAction(e -> {
                                            settings.clearDefaultPreset(Register.PRESET_LOADER_DRAWING_AREA.type.id);
                                            settings.clearDefaultPreset(Register.PRESET_LOADER_FILTERS.type.id);
                                            settings.clearDefaultPreset(Register.PRESET_LOADER_VPYPE_SETTINGS.type.id);
                                        });
                                        return button;
                                    })
                            );
                            builder.add(new LabelNode("PFM Default Presets").setTitleStyling());
                            for (PFMFactory<?> factory : MasterRegistry.INSTANCE.pfmFactories) {
                                if (!factory.isHidden() && (!factory.isPremiumFeature() || FXApplication.isPremiumEnabled)) {
                                    builder.add(new LabelNode(factory.getDisplayName(), () -> {
                                        ObservableList<GenericPreset<PresetPFMSettings>> presets = MasterRegistry.INSTANCE.getObservablePFMPresetList(factory);
                                        ComboBox<GenericPreset<PresetPFMSettings>> comboBox = new ComboBox<>();
                                        comboBox.setItems(presets);
                                        comboBox.setValue(MasterRegistry.INSTANCE.getDefaultPreset(Register.PRESET_LOADER_PFM, factory.getName(), "Default"));
                                        DBPreferences.INSTANCE.flagDefaultPresetChange.addListener((observable) -> {
                                            comboBox.setValue(MasterRegistry.INSTANCE.getDefaultPreset(Register.PRESET_LOADER_PFM, factory.getName(), "Default"));
                                        });
                                        comboBox.setOnAction(e -> {
                                            DBPreferences.INSTANCE.setDefaultPreset(comboBox.getValue());
                                        });
                                        comboBox.setPrefWidth(200);
                                        return comboBox;
                                    }));
                                }
                            }
                            builder.add(new LabelNode("", () -> {
                                Button button = new Button("Reset All");
                                button.setOnAction(e -> {
                                    for (PFMFactory<?> factory : MasterRegistry.INSTANCE.pfmFactories) {
                                        settings.clearDefaultPreset(Register.PRESET_TYPE_PFM, factory.getName());
                                    }
                                });
                                return button;
                            }));


                        }
                ),
                node("Export Settings",
                    page("Path Optimisation",
                            new SettingNode("Enabled", settings.pathOptimisationEnabled).setTitleStyling().setHideFromTree(true),
                            new LabelNode("Vector outputs (e.g. svg, pdf, gcode, hpgl) will be optimised before being exported, reducing plotting time.").setSubtitleStyling(),

                            new LabelNode("Line Simplifying").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setTitleStyling(),
                            new LabelNode("Simplifies lines using the Douglas Peucker Algorithm").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling(),
                            new SettingNode("Enabled", settings.lineSimplifyEnabled).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                            new SettingUnitsNode("Tolerance", settings.lineSimplifyTolerance, settings.lineSimplifyUnits).setDisabledProperty(settings.lineSimplifyEnabled.asBooleanProperty().not().or(settings.pathOptimisationEnabled.asBooleanProperty().not())),

                            new LabelNode("Line Merging").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setTitleStyling(),
                            new LabelNode("Merges start/end points within the given tolerance").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling(),
                            new SettingNode("Enabled", settings.lineMergingEnabled).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                            new SettingUnitsNode("Tolerance", settings.lineMergingTolerance, settings.lineMergingUnits).setDisabledProperty(settings.lineMergingEnabled.asBooleanProperty().not().or(settings.pathOptimisationEnabled.asBooleanProperty().not())),

                            new LabelNode("Line Filtering").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setTitleStyling(),
                            new LabelNode("Remove lines shorter than the tolerance").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling(),
                            new SettingNode("Enabled", settings.lineFilteringEnabled).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                            new SettingUnitsNode("Tolerance", settings.lineFilteringTolerance, settings.lineFilteringUnits).setDisabledProperty(settings.lineFilteringEnabled.asBooleanProperty().not().or(settings.pathOptimisationEnabled.asBooleanProperty().not())) ,

                            new LabelNode("Line Sorting").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setTitleStyling(),
                            new LabelNode("Sorts lines to minimise air time").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling(),
                            new SettingNode("Enabled", settings.lineSortingEnabled).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                            new SettingUnitsNode("Tolerance", settings.lineSortingTolerance, settings.lineSortingUnits).setDisabledProperty(settings.lineSortingEnabled.asBooleanProperty().not().or(settings.pathOptimisationEnabled.asBooleanProperty().not())),

                            new LabelNode("Line Multipass").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setTitleStyling(),
                            new LabelNode("Draws over each geometry multiple times").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling(),
                            new SettingNode("Enabled", settings.multipassEnabled).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                            new SettingNode("Passes", settings.multipassCount).setDisabledProperty(settings.multipassEnabled.asBooleanProperty().not().or(settings.pathOptimisationEnabled.asBooleanProperty().not()))

                    ),
                    page("SVG",
                            new LabelNode("General").setTitleStyling(),
                            new SettingNode("Export Background Layer", settings.exportSVGBackground),
                            new LabelNode("Inkscape SVG").setTitleStyling(),
                            new LabelNode("Create a custom layer naming pattern (with wildcards %INDEX% and %NAME%)").setSubtitleStyling(),
                            new SettingNode("Layer Name", settings.svgLayerNaming){
                                @Override
                                public Node createEditor() {
                                    ComboBox<String> comboBoxLayerNamingPattern = new ComboBox<>();
                                    comboBoxLayerNamingPattern.setEditable(true);
                                    comboBoxLayerNamingPattern.valueProperty().bindBidirectional(settings.svgLayerNaming.valueProperty());
                                    comboBoxLayerNamingPattern.getItems().addAll("%NAME%", "%INDEX% - %NAME%", "Pen%INDEX%");
                                    return comboBoxLayerNamingPattern;
                                }
                            }
                    ),
                    page("GCode",
                            new PropertyNode("GCode Preset", DrawingBotV3.INSTANCE.controller.exportController.selectedGCodePreset, GenericPreset.class){
                                @Override
                                public Node createEditor() {
                                    HBox hBox = new HBox();
                                    ComboBox<GenericPreset<PresetGCodeSettings>> comboBox = new ComboBox<>();
                                    comboBox.setItems(Register.PRESET_LOADER_GCODE_SETTINGS.presets);
                                    comboBox.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.controller.exportController.selectedGCodePreset);
                                    HBox.setHgrow(comboBox, Priority.ALWAYS);
                                    hBox.getChildren().add(comboBox);

                                    MenuButton menuButton = FXHelper.createPresetMenuButton(Register.PRESET_LOADER_GCODE_SETTINGS, Register.PRESET_LOADER_GCODE_SETTINGS::getDefaultManager, false, DrawingBotV3.INSTANCE.controller.exportController.selectedGCodePreset);
                                    HBox.setHgrow(menuButton, Priority.SOMETIMES);
                                    hBox.getChildren().add(menuButton);
                                    return hBox;
                                }
                            }.setTitleStyling(),
                            new LabelNode("Layout").setTitleStyling(),
                            new PropertyNode("Units", DrawingBotV3.INSTANCE.gcodeSettings.gcodeUnits, UnitsLength.class),
                            new PropertyNode("X Offset", DrawingBotV3.INSTANCE.gcodeSettings.gcodeOffsetX, Float.class),
                            new PropertyNode( "Y Offset", DrawingBotV3.INSTANCE.gcodeSettings.gcodeOffsetY, Float.class),
                            new PropertyNode("Curve Flattening", DrawingBotV3.INSTANCE.gcodeSettings.gcodeEnableFlattening, Boolean.class),
                            new PropertyNode( "Curve Flatness", DrawingBotV3.INSTANCE.gcodeSettings.gcodeCurveFlatness, Float.class).setDisabledProperty(DrawingBotV3.INSTANCE.gcodeSettings.gcodeEnableFlattening.not()),
                            new PropertyNode("Center Zero Point", DrawingBotV3.INSTANCE.gcodeSettings.gcodeCenterZeroPoint, Boolean.class),
                            new PropertyNode("Comment Type", DrawingBotV3.INSTANCE.gcodeSettings.gcodeCommentType, GCodeBuilder.CommentType.class),
                            new LabelNode("Custom GCode").setTitleStyling(),
                            new PropertyNode("Start", DrawingBotV3.INSTANCE.gcodeSettings.gcodeStartCode, String.class){
                                @Override
                                public Node createEditor() {
                                    return SettingEditors.createTextAreaEditorLazy(property);
                                }
                            },
                            new PropertyNode("End", DrawingBotV3.INSTANCE.gcodeSettings.gcodeEndCode, String.class){
                                @Override
                                public Node createEditor() {
                                    return SettingEditors.createTextAreaEditorLazy(property);
                                }
                            },
                            new PropertyNode("Pen Down", DrawingBotV3.INSTANCE.gcodeSettings.gcodePenDownCode, String.class){
                                @Override
                                public Node createEditor() {
                                    return SettingEditors.createTextAreaEditorLazy(property);
                                }
                            },
                            new PropertyNode("Pen Up", DrawingBotV3.INSTANCE.gcodeSettings.gcodePenUpCode, String.class){
                                @Override
                                public Node createEditor() {
                                    return SettingEditors.createTextAreaEditorLazy(property);
                                }
                            },
                            new LabelNode("").setTitleStyling(),
                            new LabelNode("With wildcard %LAYER_NAME%").setSubtitleStyling(),
                            new PropertyNode("Start Layer", DrawingBotV3.INSTANCE.gcodeSettings.gcodeStartLayerCode, String.class){
                                @Override
                                public Node createEditor() {
                                    return SettingEditors.createTextAreaEditorLazy(property);
                                }
                            },
                            new LabelNode("").setTitleStyling(),
                            new LabelNode("With wildcard %LAYER_NAME%").setSubtitleStyling(),
                            new PropertyNode("End Layer", DrawingBotV3.INSTANCE.gcodeSettings.gcodeEndLayerCode, String.class){
                                @Override
                                public Node createEditor() {
                                    return SettingEditors.createTextAreaEditorLazy(property);
                                }
                            }
                        ),
                    //node("HPGL"),
                    page("Image & Animation")
                ),
                page("User Interface",

                    new LabelNode("Rulers").setTitleStyling(),
                    new SettingNode("Enabled", settings.rulersEnabled),

                    new LabelNode("Drawing Borders").setTitleStyling(),
                    new SettingNode("Enabled", settings.drawingBordersEnabled),
                    new SettingNode( "Colour", settings.drawingBordersColor).setDisabledProperty(settings.drawingBordersEnabled.asBooleanProperty().not()),

                    new LabelNode("Notifications").setTitleStyling(),
                    new SettingNode("Enabled", settings.notificationsEnabled),
                    new SettingNode("Screen Time", settings.notificationsScreenTime).setDisabledProperty(settings.notificationsEnabled.asBooleanProperty().not()),
                    new LabelNode("Set this value to 0 if you don't want notifications to disappear").setSubtitleStyling()
                )
        );

        treeViewCategories.setShowRoot(false);
        treeViewCategories.setRoot(build(root));

        textFieldSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            //rebuild the tree when the search changes
            treeViewCategories.setRoot(build(root));
            treeViewCategories.getSelectionModel().select(1);
        });

        treeViewCategories.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Node content = newValue.getValue().getContent();
                if(content == null){
                    if(!newValue.getValue().getChildren().isEmpty()){
                        //create a clickable link tree
                        PageBuilder builder = new PageBuilder();
                        builder.init();
                        for(TreeItem<TreeNode> item : newValue.getChildren()){
                            TreeNode treeNode = item.getValue();
                            Label label = new Label();
                            label.textProperty().bind(treeNode.nameProperty());
                            label.setOnMouseClicked(e -> treeViewCategories.getSelectionModel().select(item));
                            builder.addRow(label);
                        }
                        content = builder.gridPane;
                    }

                }
                scrollPaneContent.setContent(content);
                labelHeading.setText(newValue.getValue().getName());
            }
        });
    }

    public TreeItem<TreeNode> build(TreeNode rootNode){
        TreeItem<TreeNode> root = new TreeItem<>(rootNode);
        root.setExpanded(true);

        for(TreeNode child : rootNode.getChildren()){
            root.getChildren().add(build(child));
        }

        String search = textFieldSearchBar.getText();
        if(search != null){
            prune(root, search);
        }

        //sort(root, Comparator.comparing(o -> o.getValue().getName()));

        return root;
    }

    public boolean prune(TreeItem<TreeNode> treeItem, String search){
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

    private void sort(TreeItem<TreeNode> node, Comparator<TreeItem<TreeNode>> comparator) {
        node.getChildren().sort(comparator);
        for (TreeItem<TreeNode> child : node.getChildren()) {
            sort(child, comparator);
        }
    }

    public TreeNode root(TreeNode...children){
        return new TreeNode("root", children);
    }

    public TreeNode node(String name, TreeNode...children){
        return new TreeNode(name, children);
    }

    public PageNode page(String name, TreeNode...children){
        return new PageNode(name, children){

            @Override
            public Node buildContent() {
                PageBuilder builder = new PageBuilder();
                builder.build(this.getChildren());
                return builder.gridPane;
            }
        };
    }
    public PageNode page(String name, Consumer<ObservableList<TreeNode>> builder){
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
    /*

    public void title(GridPane gridPane, String title){
        title(gridPane, title, null);
    }

    public void title(GridPane gridPane, String title, ObservableValue<Boolean> disabled){
        Label label = new Label(title);
        label.setFont(new Font(18));
        if(disabled != null){
            label.disableProperty().bind(disabled);
        }
        node(gridPane, label);
    }

    public void subtitle(GridPane gridPane, String title){
         subtitle(gridPane, title, null);
    }

    public void subtitle(GridPane gridPane, String title, ObservableValue<Boolean> disabled){
        Label label = new Label(title);
        label.setFont(new Font(12));
        label.setPadding(new Insets(0, 0, 0, 0));
        if(disabled != null){
            label.disableProperty().bind(disabled);
        }
        node(gridPane, label);
    }

    public void gap(GridPane gridPane, int size){
        Label label = new Label(" ");
        label.setFont(new Font(size));
        node(gridPane, label);
    }


    public void setting(GridPane gridPane, GenericSetting<?, ?> setting){
        setting(gridPane, setting.getDisplayName(), setting, null);
    }

    public void setting(GridPane gridPane, String displayName, GenericSetting<?, ?> setting){
        setting(gridPane, displayName, setting, null);
    }

    public void setting(GridPane gridPane, GenericSetting<?, ?> setting, ObservableValue<Boolean> disabled){
        setting(gridPane, setting.getDisplayName(), setting, disabled);
    }

    public void setting(GridPane gridPane, String displayName, GenericSetting<?, ?> setting, ObservableValue<Boolean> disabled){
        setting(gridPane, displayName, setting, disabled, 30, false);
    }

    public void settingTitle(GridPane gridPane, String displayName, GenericSetting<?, ?> setting, ObservableValue<Boolean> disabled){
        setting(gridPane, displayName, setting, disabled, 0, true);
    }

    public void setting(GridPane gridPane, String displayName, GenericSetting<?, ?> setting, ObservableValue<Boolean> disabled, int inset, boolean title){
        Label label = new Label(displayName);
        if(title){
            label.setFont(new Font(18));
        }
        Node editor = createEditor(setting);
        gridPane.addRow(gridPane.getRowCount(), label, editor);
        label.setPadding(new Insets(6, 0, 2, inset));

        if(disabled != null){
            label.disableProperty().bind(Bindings.createBooleanBinding(() -> disabled.getValue() || setting.isDisabled(), disabled, setting.disabledProperty()));
            editor.disableProperty().bind(Bindings.createBooleanBinding(() -> disabled.getValue() || setting.isDisabled(), disabled, setting.disabledProperty()));
        }else{
            label.disableProperty().bind(setting.disabledProperty());
            editor.disableProperty().bind(setting.disabledProperty());
        }
    }

     */

    public void property(GridPane gridPane, String displayName, Property<?> property, Class<?> type){
        Label label = new Label(displayName);
        gridPane.addRow(gridPane.getRowCount(), label, SettingEditors.createEditor(property, type));
    }

    public void node(GridPane gridPane, Node node){
        gridPane.add(node, 0, gridPane.getRowCount(), 2, 1);
    }

    public static DefaultPropertyEditorFactory defaultPropertyEditorFactory = new DefaultPropertyEditorFactory();

    public PropertyEditor<?> getPropertyEditor(PropertySheet.Item item){
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
                getEditor().setSelected((Boolean)value);
            }
        };
    }

    public static class SettingProperty implements PropertySheet.Item {

        public GenericSetting<?, ?> setting;

        public SettingProperty(GenericSetting<?, ?> setting){
            this.setting = setting;
        }

        @Override
        public Class<?> getType() {
            return setting.type;
        }

        @Override
        public String getCategory() {
            return setting.getCategory();
        }

        @Override
        public String getName() {
            return setting.getDisplayName();
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public Object getValue() {
            return setting.getValue();
        }

        @Override
        public void setValue(Object value) {
            setting.setValue(value);
        }

        @Override
        public Optional<ObservableValue<? extends Object>> getObservableValue() {
            return Optional.of(setting.valueProperty());
        }
    }

    public static class PageBuilder{

        public GridPane gridPane;

        public void init(){
            if(gridPane != null){
                return;
            }
            gridPane = new GridPane();
            gridPane.getStyleClass().add("preference-grid");

            ColumnConstraints column1 = new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, true);
            ColumnConstraints column2 = new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, true);
            ColumnConstraints column3 = new ColumnConstraints(-1, -1, -1, Priority.NEVER, HPos.LEFT, true);

            gridPane.getColumnConstraints().addAll(column1, column2, column3);

            HBox.setHgrow(gridPane, Priority.ALWAYS);
            gridPane.setMaxWidth(Double.MAX_VALUE);
        }

        public void build(List<TreeNode> nodes){
            init();

            for(TreeNode node : nodes){
                if(node instanceof ElementNode){
                    ElementNode settingNode = (ElementNode) node;
                    settingNode.addElement(this);
                }
            }

        }

        public void addRow(Node node){
            gridPane.add(node, 0, gridPane.getRowCount(), 2, 1);
        }

        public void addRow(Node label, Node editor){
            gridPane.addRow(gridPane.getRowCount(), label, editor);
        }

        public void addRow(Node label, Node editor, Node reset){
            gridPane.addRow(gridPane.getRowCount(), label, editor, reset);
        }

        public Node getContent(){
            return gridPane;
        }

    }

    public static class TreeNode implements Observable {

        public TreeNode(String name, TreeNode...children){
            setName(name);
            getChildren().addAll(children);
        }

        ////////////

        public final ObservableList<TreeNode> children = FXCollections.observableArrayList();

        public ObservableList<TreeNode> getChildren() {
            return children;
        }

        ////////////

        public final StringProperty name = new SimpleStringProperty();

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public TreeNode setName(String name) {
            this.name.set(name);
            return this;
        }

        ////////////

        public final BooleanProperty hideFromTree = new SimpleBooleanProperty(false);

        public boolean isHiddenFromTree() {
            return hideFromTree.get();
        }

        public BooleanProperty hideFromTreeProperty() {
            return hideFromTree;
        }

        public TreeNode setHideFromTree(boolean hideFromTree) {
            this.hideFromTree.set(hideFromTree);
            return this;
        }

        public Node getContent(){
            return null;
        }

        ////////////

        @Override
        public void addListener(InvalidationListener listener) {
            children.addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            children.removeListener(listener);
        }

        public String getBreadcrumb(){
            return ""; //TODO
        }

        ////////////

        @Override
        public String toString() {
            return getName();
        }
    }

    public static abstract class PageNode extends TreeNode {

        private Node content;

        public PageNode(String name) {
            super(name);
        }

        public PageNode(String name, TreeNode...children){
            super(name, children);
        }

        public Node getContent(){
            //if(content == null){
                content = buildContent();
            //}
            return content;
        }

        public abstract Node buildContent();

    }

    public static abstract class ElementNode extends TreeNode{

        public static final String DEFAULT_STYLE = "preference-default";
        public static final String TITLE_STYLE = "preference-title";
        public static final String SUBTITLE_STYLE = "preference-subtitle";

        public String labelStyle = DEFAULT_STYLE;
        public String controlStyle = DEFAULT_STYLE;

        public ObservableValue<Boolean> disabled;

        public ElementNode(String name, TreeNode... children) {
            super(name, children);
            setDefaultStyling();
        }

        public ElementNode setDefaultStyling(){
            this.labelStyle = DEFAULT_STYLE;
            this.setHideFromTree(true);
            return this;
        }

        public ElementNode setTitleStyling(){
            this.labelStyle = TITLE_STYLE;
            this.setHideFromTree(true); //TODO MAKE TITLES VISIBLE, WAY TO JUMP STRAIGHT TO SETTINGS
            return this;
        }

        public ElementNode setSubtitleStyling(){
            this.labelStyle = SUBTITLE_STYLE;
            this.setHideFromTree(true);
            return this;
        }

        public ElementNode setDisabledProperty(ObservableValue<Boolean> disabled){
            this.disabled = disabled;
            return this;
        }

        public abstract void addElement(PageBuilder builder);
    }

    public static class LabelNode extends ElementNode {

        public boolean showSeperator;
        public Supplier<Node> supplier;
        private Node node;
        public boolean hideLabel = false;

        public LabelNode(String name, TreeNode... children) {
            super(name, children);
        }

        public LabelNode(String name, Supplier<Node> supplier, TreeNode... children) {
            super(name, children);
            this.supplier = supplier;
        }

        public LabelNode hideLabel(){
            this.hideLabel = true;
            return this;
        }

        @Override
        public void addElement(PageBuilder builder) {
            if(node == null && supplier != null){
                node = supplier.get();
            }
            if(hideLabel){
                if(node != null){
                    builder.addRow(node);
                }
                return;
            }

            Label label = new Label();
            label.textProperty().bind(nameProperty());
            label.getStyleClass().add(labelStyle);
            if(labelStyle.equals(TITLE_STYLE)){

                HBox hBox = new HBox();
                HBox.setHgrow(label, Priority.ALWAYS);
                hBox.setMaxWidth(Double.MAX_VALUE);
                if(node != null){
                    hBox.getChildren().add(node);
                }
                hBox.getChildren().add(label);

                Separator separator = new Separator();
                separator.getStyleClass().add("preference-separator");
                separator.setMaxWidth(Double.MAX_VALUE);

                HBox.setHgrow(separator, Priority.SOMETIMES);
                VBox.setVgrow(separator, Priority.ALWAYS);
                separator.setValignment(VPos.CENTER);
                hBox.getChildren().add(separator);

                builder.addRow(hBox);
            }else{
                if(node != null){
                    builder.addRow(label, node);
                }else{
                    builder.addRow(label);
                }
            }

            if(disabled != null){
                label.disableProperty().bind(disabled);
            }
        }
    }

    public static class SettingNode extends ElementNode {

        public GenericSetting<?, ?> setting;

        public SettingNode(GenericSetting<?, ?> setting, TreeNode... children) {
            super(setting.getDisplayName(), children);
            this.setting = setting;
            this.nameProperty().bind(setting.displayNameProperty());
        }

        public SettingNode(String overrideName, GenericSetting<?, ?> setting, TreeNode... children) {
            super(overrideName, children);
            this.setting = setting;
        }

        public Node createEditor(){
            /*
            //just to make titles slightly different give them toggle editors instead of check boxes
            if(styleClass.equals(TITLE_STYLE) && setting instanceof BooleanSetting){
                BooleanSetting<?> booleanSetting = (BooleanSetting<?>) setting;
                return SettingEditors.createSwitchEditor(booleanSetting.valueProperty());
            }
             */
            return SettingEditors.createEditor(setting.valueProperty(), setting.type);
        }

        public void addElement(PageBuilder builder){
            Label label = new Label();
            label.textProperty().bind(nameProperty());
            label.getStyleClass().add(labelStyle);

            Node editor = createEditor();
            editor.getStyleClass().add(labelStyle);

            Button resetButton = new Button("", new Glyph("FontAwesome", "ROTATE_LEFT"));
            resetButton.getStyleClass().add("preference-reset-button");
            resetButton.setOnAction(e -> setting.resetSetting());

            builder.addRow(label, editor, resetButton);

            if(disabled != null){
                label.disableProperty().bind(Bindings.createBooleanBinding(() -> disabled.getValue() || setting.isDisabled(), disabled, setting.disabledProperty()));
                editor.disableProperty().bind(Bindings.createBooleanBinding(() -> disabled.getValue() || setting.isDisabled(), disabled, setting.disabledProperty()));
            }else{
                label.disableProperty().bind(setting.disabledProperty());
                editor.disableProperty().bind(setting.disabledProperty());
            }

        }

    }

    public static class PropertyNode extends ElementNode {

        public Property<?> property;
        public Class<?> type;

        public PropertyNode(String name, Property<?> property, Class<?> type, TreeNode... children) {
            super(name, children);
            this.property = property;
            this.type = type;
        }

        public Node createEditor(){
            return SettingEditors.createEditor(property, type);
        }

        @Override
        public void addElement(PageBuilder builder) {
            Label label = new Label();
            label.textProperty().bind(nameProperty());
            label.getStyleClass().add(labelStyle);


            Node editor = createEditor();
            editor.getStyleClass().add(labelStyle);
            if(editor instanceof Control){
                Control control = (Control) editor;
                control.setPrefWidth(200);
            }

            builder.addRow(label, editor);

            if(disabled != null){
                label.disableProperty().bind(disabled);
                editor.disableProperty().bind(disabled);
            }
        }
    }

    public static class SettingUnitsNode extends SettingNode {

        public GenericSetting<?, ?> units;

        public SettingUnitsNode(GenericSetting<?, ?> setting, GenericSetting<?, ?> units, TreeNode... children) {
            super(setting, children);
            this.units = units;
        }

        public SettingUnitsNode(String overrideName, GenericSetting<?, ?> setting, GenericSetting<?, ?> units, TreeNode... children) {
            super(overrideName, setting, children);
            this.units = units;
        }

        public Node createEditor(){
            HBox hBox = new HBox();

            Node settingEditor = SettingEditors.createEditor(setting.valueProperty(), setting.type);
            HBox.setHgrow(settingEditor, Priority.ALWAYS);
            hBox.getChildren().add(settingEditor);

            Node unitsEditor = SettingEditors.createEditor(units.valueProperty(), units.type);
            HBox.setHgrow(settingEditor, Priority.SOMETIMES);
            hBox.getChildren().add(unitsEditor);

            return hBox;
        }

    }

    /////////////////////////

    public <O extends IJsonData> ComboBox<GenericPreset<O>> createDefaultPresetComboBox(AbstractPresetLoader<O> loader){
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

}