package drawingbot.javafx.preferences;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.exporters.GCodeSettings;
import drawingbot.files.json.IPresetLoader;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.presets.PresetGCodeSettingsEditor;
import drawingbot.integrations.vpype.VpypePlugin;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controllers.AbstractFXController;
import drawingbot.javafx.controls.ComboCellNamedSetting;
import drawingbot.javafx.controls.ControlPresetDrawingPen;
import drawingbot.javafx.controls.ControlPresetDrawingSet;
import drawingbot.javafx.controls.ControlPresetSelector;
import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.editors.EditorStyle;
import drawingbot.javafx.preferences.items.*;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.Utils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Note this implementation is inspired by / borrows from ControlsFX, PreferencesFX and FXSampler
 */
public class FXPreferences extends AbstractFXController {

    public TextField textFieldSearchBar = null;
    public Label labelHeading = null;
    public TreeView<TreeNode> treeViewCategories = null;
    public ScrollPane scrollPaneContent = null;

    public TreeNode root = null;

    public static PageNode pageGeneral;
    public static PageNode pageExportGeneral;
    public static PageNode pageExportPathOptimisation;
    public static PageNode pageSVG;
    public static PageNode pageGCode;
    public static PageNode pageImageAndAnimation;
    public static PageNode pageUserInterface;

    public static void registerDefaults(){
        DBPreferences settings = DBPreferences.INSTANCE;


        MasterRegistry.INSTANCE.registerPreferencesPage(
                pageGeneral = EditorSheet.page("General", builder -> {
                    builder.addAll(
                            new LabelNode("Defaults").setTitleStyling(),
                            new SettingNode<>("Path Finding Module", settings.defaultPFM) {
                                @Override
                                public Node getEditorNode(EditorContext context) {
                                    ComboBox<PFMFactory<?>> comboBoxPFM = new ComboBox<>();
                                    comboBoxPFM.setCellFactory(param -> new ComboCellNamedSetting<>());
                                    comboBoxPFM.setItems(MasterRegistry.INSTANCE.getObservablePFMLoaderList());
                                    comboBoxPFM.setValue(MasterRegistry.INSTANCE.getDefaultPFM());
                                    comboBoxPFM.setOnAction(e -> {
                                        settings.defaultPFM.setValue(comboBoxPFM.getValue().getRegistryName());
                                    });
                                    settings.defaultPFM.valueProperty().addListener((observable, oldValue, newValue) -> {
                                        PFMFactory<?> pfmFactory = MasterRegistry.INSTANCE.getPFMFactory(newValue);
                                        if (comboBoxPFM.getValue() != pfmFactory) {
                                            comboBoxPFM.setValue(pfmFactory);
                                        }
                                    });
                                    return comboBoxPFM;
                                }
                            },
                            new SettingNode<>("Auto Run PFM", settings.autoRunPFM),
                            new SettingNode<>("Pen Width (mm)", settings.defaultPenWidth),
                            new SettingNode<>("Rescaling Mode", settings.defaultRescalingMode),
                            new SettingNode<>("Canvas Colour", settings.defaultCanvasColour),
                            new SettingNode<>("Background Colour", settings.defaultBackgroundColour),
                            new SettingNode<>("Clipping Mode", settings.defaultClippingMode),
                            new SettingNode<>("Blend Mode", settings.defaultBlendMode),
                            new SettingNode<>("Apply Shapes Slider to Export", settings.defaultRangeExport),
                            new LabelNode("", () -> {
                                javafx.scene.control.Button button = new javafx.scene.control.Button("Reset All");
                                button.setOnAction(e -> {
                                    settings.defaultPFM.resetSetting();
                                    settings.autoRunPFM.resetSetting();
                                    settings.defaultPenWidth.resetSetting();
                                    settings.defaultRescalingMode.resetSetting();
                                    settings.defaultCanvasColour.resetSetting();
                                    settings.defaultBackgroundColour.resetSetting();
                                    settings.defaultClippingMode.resetSetting();
                                    settings.defaultBlendMode.resetSetting();
                                    settings.defaultRangeExport.resetSetting();
                                });
                                return button;
                            }),

                            new LabelNode("Quick Export").setTitleStyling(),
                            new SettingNode<>("Mode", settings.quickExportMode),
                            new SettingNode<>("Type", settings.quickExportHandler) {
                                @Override
                                public Node getEditorNode(EditorContext context) {
                                    ComboBox<DrawingExportHandler> comboBox = new ComboBox<>();
                                    comboBox.setItems(FXCollections.observableArrayList(MasterRegistry.INSTANCE.drawingExportHandlers.values()));
                                    comboBox.setValue(settings.getQuickExportHandler());
                                    comboBox.setOnAction(e -> {
                                        settings.quickExportHandler.set(comboBox.getValue().getRegistryName());
                                    });
                                    settings.quickExportHandler.addListener(observable -> {
                                        if (!settings.quickExportHandler.get().equals(comboBox.getValue().getRegistryName())) {
                                            comboBox.setValue(settings.getQuickExportHandler());
                                        }
                                    });
                                    return comboBox;
                                }
                            },

                            new LabelNode("Default Folders").setTitleStyling(),
                            new SettingNode<>("Import Folder", settings.defaultImportDirectory),
                            new SettingNode<>("Export Folder", settings.defaultExportDirectory),
                            new LabelNode("Advanced").setTitleStyling(),
                            new SettingNode<>("High Quality Mode DPI", settings.importDPI),

                            new LabelNode("Preset Defaults").setTitleStyling(),
                            new LabelNode("Drawing Area", () -> EditorSheet.createDefaultPresetComboBox(Register.PRESET_LOADER_DRAWING_AREA)),
                            new LabelNode("Image Processing", () -> EditorSheet.createDefaultPresetComboBox(Register.PRESET_LOADER_FILTERS)),
                            new LabelNode("GCode Settings", () -> EditorSheet.createDefaultPresetComboBox(Register.PRESET_LOADER_GCODE_SETTINGS)),
                            new LabelNode("vPype Settings", () -> EditorSheet.createDefaultPresetComboBox(VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS)),
                            new LabelNode("Default Pen Set", () -> {
                                ControlPresetDrawingSet control = new ControlPresetDrawingSet();
                                control.setDisablePresetMenu(true);
                                control.activePresetProperty().addListener((observable, oldValue, newValue) -> {
                                    control.getPresetLoader().setDefaultPreset(newValue);
                                });
                                DBPreferences.INSTANCE.flagDefaultPresetChange.addListener((observable) -> {
                                    control.setActivePreset(control.getPresetLoader().getDefaultPreset());
                                });
                                return control;
                            }),
                            new LabelNode("Default Pen", () -> {
                                ControlPresetDrawingPen control = new ControlPresetDrawingPen();
                                control.setDisablePresetMenu(true);
                                control.activePresetProperty().addListener((observable, oldValue, newValue) -> {
                                    control.getPresetLoader().setDefaultPreset(newValue);
                                });
                                DBPreferences.INSTANCE.flagDefaultPresetChange.addListener((observable) -> {
                                    control.setActivePreset(Register.PRESET_LOADER_DRAWING_PENS.getDefaultPreset());
                                });
                                return control;
                            }),
                            new LabelNode("", () -> {
                                javafx.scene.control.Button button = new javafx.scene.control.Button("Reset All");
                                button.setOnAction(e -> {
                                    Register.PRESET_LOADER_DRAWING_AREA.resetDefaultPreset();
                                    Register.PRESET_LOADER_FILTERS.resetDefaultPreset();
                                    VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.resetDefaultPreset();
                                    Register.PRESET_LOADER_GCODE_SETTINGS.resetDefaultPreset();
                                    Register.PRESET_LOADER_DRAWING_SET.resetDefaultPreset();
                                    Register.PRESET_LOADER_DRAWING_PENS.resetDefaultPreset();
                                });
                                return button;
                            })
                    );
                    builder.add(new LabelNode("PFM Default Presets").setTitleStyling());
                    for (PFMFactory<?> factory : MasterRegistry.INSTANCE.pfmFactories) {
                        if (!factory.isHidden() && (!factory.isPremiumFeature() || FXApplication.isPremiumEnabled)) {
                            builder.add(new LabelNode(factory.getDisplayName(), () -> {
                                ComboBox<GenericPreset<PresetData>> comboBox = EditorSheet.createDefaultPresetComboBox(Register.PRESET_LOADER_PFM, factory.getRegistryName());
                                comboBox.setPrefWidth(200);
                                return comboBox;
                            }));
                        }
                    }
                    builder.add(new LabelNode("", () -> {
                        javafx.scene.control.Button button = new Button("Reset All");
                        button.setOnAction(e -> {
                            for (PFMFactory<?> factory : MasterRegistry.INSTANCE.pfmFactories) {
                                Register.PRESET_LOADER_PFM.resetDefaultPreset();
                            }
                        });
                        return button;
                    }));


                }
        ));
        MasterRegistry.INSTANCE.registerPreferencesPage(EditorSheet.node("Export Settings",
                pageExportGeneral = EditorSheet.page("General",
                        new SettingNode<>("Show Exported Drawing", settings.showExportedDrawing).setTitleStyling().setHideFromTree(true)
                ),
                pageExportPathOptimisation = EditorSheet.page("Path Optimisation",
                        new SettingNode<>("Enabled", settings.pathOptimisationEnabled).setTitleStyling().setHideFromTree(true),
                        new LabelNode("Vector outputs (e.g. svg, pdf, gcode, hpgl) will be optimised before being exported, reducing plotting time.").setSubtitleStyling(),

                        new LabelNode("Line Simplifying").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setTitleStyling(),
                        new LabelNode("Simplifies lines using the Douglas Peucker Algorithm").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling(),
                        new SettingNode<>("Enabled", settings.lineSimplifyEnabled).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                        new SettingUnitsNode<>("Tolerance", settings.lineSimplifyTolerance, settings.lineSimplifyUnits).setDisabledProperty(settings.lineSimplifyEnabled.asBooleanProperty().not().or(settings.pathOptimisationEnabled.asBooleanProperty().not())),

                        new LabelNode("Line Merging").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setTitleStyling(),
                        new LabelNode("Merges start/end points within the given tolerance").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling(),
                        new SettingNode<>("Enabled", settings.lineMergingEnabled).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                        new SettingUnitsNode<>("Tolerance", settings.lineMergingTolerance, settings.lineMergingUnits).setDisabledProperty(settings.lineMergingEnabled.asBooleanProperty().not().or(settings.pathOptimisationEnabled.asBooleanProperty().not())),

                        new LabelNode("Line Filtering").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setTitleStyling(),
                        new LabelNode("Remove lines shorter than the tolerance").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling(),
                        new SettingNode<>("Enabled", settings.lineFilteringEnabled).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                        new SettingUnitsNode<>("Tolerance", settings.lineFilteringTolerance, settings.lineFilteringUnits).setDisabledProperty(settings.lineFilteringEnabled.asBooleanProperty().not().or(settings.pathOptimisationEnabled.asBooleanProperty().not())) ,

                        new LabelNode("Line Sorting").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setTitleStyling(),
                        new LabelNode("Sorts lines to minimise air time").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling(),
                        new SettingNode<>("Enabled", settings.lineSortingEnabled).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                        new SettingUnitsNode<>("Tolerance", settings.lineSortingTolerance, settings.lineSortingUnits).setDisabledProperty(settings.lineSortingEnabled.asBooleanProperty().not().or(settings.pathOptimisationEnabled.asBooleanProperty().not())),

                        new LabelNode("Line Multipass").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setTitleStyling(),
                        new LabelNode("Draws over each geometry multiple times").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling(),
                        new SettingNode<>("Enabled", settings.multipassEnabled).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                        new SettingNode<>("Passes", settings.multipassCount).setDisabledProperty(settings.multipassEnabled.asBooleanProperty().not().or(settings.pathOptimisationEnabled.asBooleanProperty().not())),

                        new SettingNode<>("Allow multiple moves in exported paths", settings.allowMultiplePathMoves).setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()),
                        new LabelNode("Reduces the amount of path elements in SVG/Vector exports").setDisabledProperty(settings.pathOptimisationEnabled.asBooleanProperty().not()).setSubtitleStyling()
                ),
                pageSVG = EditorSheet.page("SVG",
                        new LabelNode("General").setTitleStyling(),
                        new SettingNode<>("Export Background Layer", settings.exportSVGBackground),
                        new SettingNode<>("Save Drawing Stats to SVG", settings.svgDrawingStatsComment),
                        new SettingNode<>("Save PFM Settings to SVG", settings.svgPFMSettingsText),

                        new LabelNode("Inkscape SVG").setTitleStyling(),
                        new LabelNode("Create a custom layer naming pattern (with wildcards %INDEX% and %NAME%)").setSubtitleStyling(),
                        new SettingNode<>("Layer Name", settings.svgLayerNaming){
                            @Override
                            public Node getEditorNode(EditorContext context) {
                                ComboBox<String> comboBoxLayerNamingPattern = new ComboBox<>();
                                comboBoxLayerNamingPattern.setEditable(true);
                                comboBoxLayerNamingPattern.valueProperty().bindBidirectional(settings.svgLayerNaming.valueProperty());
                                comboBoxLayerNamingPattern.getItems().addAll("%NAME%", "%INDEX% - %NAME%", "Pen%INDEX%");
                                return comboBoxLayerNamingPattern;
                            }
                        }
                ),
                pageGCode = EditorSheet.page("GCode", builder -> {

                            builder.add(new PropertyNode("GCode Preset", settings.selectedGCodePreset, () -> Register.PRESET_LOADER_GCODE_SETTINGS.getDefaultPreset(), GenericPreset.class){
                                @Override
                                public Node getEditorNode(EditorContext context) {
                                    ControlPresetSelector<GCodeSettings, PresetData> controlGCodePreset = new ControlPresetSelector<>();
                                    controlGCodePreset.quickSetup(Register.PRESET_MANAGER_GCODE_SETTINGS);
                                    controlGCodePreset.setTarget(settings.gcodeSettings);
                                    controlGCodePreset.activePresetProperty().bindBidirectional(settings.selectedGCodePreset);
                                    controlGCodePreset.setActivePreset(Register.PRESET_LOADER_GCODE_SETTINGS.getDefaultPreset());
                                    return controlGCodePreset;
                                }
                            }.setTitleStyling());

                            PresetGCodeSettingsEditor editor = Register.PRESET_MANAGER_GCODE_SETTINGS.createPresetEditor();
                            editor.target.set(settings.gcodeSettings);

                            PresetEditorNode presetEditorNode = new PresetEditorNode("GCode Editor");
                            presetEditorNode.getPresetEditor().setCustomEditor(editor);
                            presetEditorNode.getPresetEditor().selectedPresetProperty().bind(settings.selectedGCodePreset);
                            builder.add(presetEditorNode);
                    }
                ),
                pageImageAndAnimation = EditorSheet.page("Image & Animation",
                        new LabelNode("Resolution").setTitleStyling(),
                        new SettingNode<>(settings.exportDPI),
                        new SettingNode<>(settings.transparentPNG),
                        new PropertyNode<>("Image Export Size", settings.imageExportSize, String.class).setEditable(false),
                        new LabelNode("Animations").setTitleStyling(),
                        new SettingNode<>("Frames per second", settings.framesPerSecond),
                        new SettingUnitsNode<>("Duration", settings.duration, settings.durationUnits),
                        new SettingNode<>("Frames Hold Start", settings.frameHoldStart),
                        new SettingNode<>("Frames Hold End", settings.frameHoldEnd),
                        new LabelNode(""),
                        new PropertyNode<>("Frame Count", settings.animationFrameCount, String.class).setEditable(false),
                        new PropertyNode<>("Geometries per frame", settings.animationGeometriesPFrame, String.class).setEditable(false),
                        new PropertyNode<>("Vertices per frame", settings.animationVerticesPFrame, String.class).setEditable(false)
                )
        ));



        Register.PRESET_LOADER_GCODE_SETTINGS.addSpecialListener(new IPresetLoader.Listener<PresetData>() {
            @Override
            public void onPresetEdited(GenericPreset<PresetData> preset) {
                if(preset == DBPreferences.INSTANCE.selectedGCodePreset.get()){
                    DBPreferences.INSTANCE.selectedGCodePreset.set(null);
                    DBPreferences.INSTANCE.selectedGCodePreset.set(preset);
                }
            }
        });

        MasterRegistry.INSTANCE.registerPreferencesPage(
                pageUserInterface = EditorSheet.page("User Interface",
                    new LabelNode("General").setTitleStyling(),
                    new SettingNode<>("Dark Theme", settings.darkTheme),
                    new SettingNode<>("Default Window Size", settings.uiWindowSize),
                    new SettingNode<>("Restore Last Layout", settings.restoreLayout),
                    new SettingNode<>("Restore Project Layout", settings.restoreProjectLayout),

                    new LabelNode("Rulers").setTitleStyling(),
                    new SettingNode<>("Enabled", settings.rulersEnabled),

                    new LabelNode("Drawing Borders").setTitleStyling(),
                    new SettingNode<>("Enabled", settings.drawingBordersEnabled),
                    new SettingNode<>( "Colour", settings.drawingBordersColor).setDisabledProperty(settings.drawingBordersEnabled.asBooleanProperty().not()),

                    new LabelNode("Notifications").setTitleStyling(),
                    new SettingNode<>("Enabled", settings.notificationsEnabled),
                    new SettingNode<>("Screen Time", settings.notificationsScreenTime).setDisabledProperty(settings.notificationsEnabled.asBooleanProperty().not()),
                    new LabelNode("Set this value to 0 if you don't want notifications to disappear").setSubtitleStyling()
        ));
    }

    @FXML
    public void initialize(){
        DrawingBotV3.INSTANCE.controller.preferencesStage.setResizable(true);
        DrawingBotV3.INSTANCE.controller.preferencesStage.setOnHidden(e -> Register.PRESET_LOADER_PREFERENCES.updateConfigs());

        root = MasterRegistry.INSTANCE.root;

        treeViewCategories.setShowRoot(false);
        treeViewCategories.setRoot(EditorSheet.build(root, textFieldSearchBar.getText()));

        textFieldSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            //rebuild the tree when the search changes
            treeViewCategories.setRoot(EditorSheet.build(root, textFieldSearchBar.getText()));

            //select the first tree item which has visible items
            if(!treeViewCategories.getRoot().getChildren().isEmpty()){
                TreeItem<TreeNode> select = treeViewCategories.getRoot().getChildren().get(0);
                while(select.getValue().getContent() == null && !select.getChildren().isEmpty()){
                    select = select.getChildren().get(0);
                }
                treeViewCategories.getSelectionModel().select(select);
            }else{
                treeViewCategories.getSelectionModel().clearSelection();
            }
            highlightSearch(scrollPaneContent.getContent(), textFieldSearchBar.getText());
        });

        treeViewCategories.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Node content = newValue.getValue().getContent();
                if(content == null){
                    if(!newValue.getValue().getChildren().isEmpty()){
                        //create a clickable link tree
                        PageBuilder builder = new PageBuilder(new EditorContext(newValue.getValue(), EditorStyle.DETAILED));
                        builder.init();
                        for(TreeItem<TreeNode> item : newValue.getChildren()){
                            TreeNode treeNode = item.getValue();
                            Label label = new Label();
                            label.getStyleClass().add(ElementNode.TITLE_STYLE);
                            label.textProperty().bind(treeNode.nameProperty());
                            label.setOnMouseClicked(e -> treeViewCategories.getSelectionModel().select(item));
                            builder.addRow(label);
                        }
                        content = builder.gridPane;
                    }
                }
                scrollPaneContent.setContent(content);
                scrollPaneContent.setHvalue(0);
                scrollPaneContent.setVvalue(0);
                labelHeading.setText(newValue.getValue().getName());
                highlightSearch(content, textFieldSearchBar.getText());
            }else{
                labelHeading.setText("No Search Results");
                scrollPaneContent.setContent(null);
            }
        });
        treeViewCategories.getSelectionModel().select(0);
    }

    private void scrollToNode(Node node){
        Node scrollContent = scrollPaneContent.getContent();

        while(node.getParent() != scrollContent){
            if(node.getParent() == null){
                return;
            }
            node = node.getParent();
        }
        Point2D position = node.localToParent(0, 0);

        scrollPaneContent.setVvalue(Utils.clamp((position.getY() - 4) / (scrollContent.getLayoutBounds().getHeight() - scrollPaneContent.getHeight()), 0, 1));
    }

    private void highlightSearch(Node contentNode, String search) {
        List<Node> highlighted = new ArrayList<>();
        highlightRecursive(contentNode, search.toLowerCase(), highlighted);
        if(!highlighted.isEmpty()){
            scrollToNode(highlighted.get(0));
        }
    }

    private void highlightRecursive(Node node, String search, List<Node> highlighted){
        if(node instanceof Label labeled){
            String text = labeled.getText();
            if(!search.isEmpty() && text != null && !text.isEmpty() && text.toLowerCase().contains(search)){
                addHighlights(labeled);
                highlighted.add(labeled);
            }else{
                clearHighlights(labeled);
            }
        }
        if(node instanceof Parent parent){
            for(Node child : parent.getChildrenUnmodifiable()){
                highlightRecursive(child, search, highlighted);
            }
        }
    }

    private final Background highlightbackground = new Background(new BackgroundFill(new Color(1, 1, 0, 0.5), CornerRadii.EMPTY, Insets.EMPTY));

    private void addHighlights(Labeled labeled){
        labeled.setBackground(highlightbackground);
    }

    private void clearHighlights(Labeled labeled){
        labeled.setBackground(null);
    }

}