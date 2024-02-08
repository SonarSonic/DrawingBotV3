package drawingbot.javafx.preferences;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.exporters.GCodeSettings;
import drawingbot.files.json.PresetData;
import drawingbot.integrations.vpype.VpypePlugin;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controllers.AbstractFXController;
import drawingbot.javafx.controls.*;
import drawingbot.javafx.editors.*;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.UnitsLength;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.HBox;

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
                pageGeneral = Editors.page("General", builder -> {
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
                                        settings.defaultPFM.setValue(comboBoxPFM.getValue().getRegistryName());
                                    });
                                    settings.defaultPFM.valueProperty().addListener((observable, oldValue, newValue) -> {
                                        PFMFactory<?> pfmFactory = MasterRegistry.INSTANCE.getPFMFactory(newValue);
                                        if(comboBoxPFM.getValue() != pfmFactory){
                                            comboBoxPFM.setValue(pfmFactory);
                                        }
                                    });
                                    return comboBoxPFM;
                                }
                            },
                            new SettingNode("Auto Run PFM", settings.autoRunPFM),
                            new SettingNode("Pen Width (mm)", settings.defaultPenWidth),
                            new SettingNode("Rescaling Mode", settings.defaultRescalingMode),
                            new SettingNode("Canvas Colour", settings.defaultCanvasColour),
                            new SettingNode("Background Colour", settings.defaultBackgroundColour),
                            new SettingNode("Clipping Mode", settings.defaultClippingMode),
                            new SettingNode("Blend Mode", settings.defaultBlendMode),
                            new SettingNode("Apply Shapes Slider to Export", settings.defaultRangeExport),
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
                            new SettingNode("Mode", settings.quickExportMode),
                            new LabelNode("Type", () -> {
                                ComboBox<DrawingExportHandler> comboBox = new ComboBox<>();
                                comboBox.setItems(FXCollections.observableArrayList(MasterRegistry.INSTANCE.drawingExportHandlers.values()));
                                comboBox.setValue(settings.getQuickExportHandler());
                                comboBox.setOnAction(e -> {
                                    settings.quickExportHandler.set(comboBox.getValue().getRegistryName());
                                });
                                settings.quickExportHandler.addListener(observable -> {
                                    if(!settings.quickExportHandler.get().equals(comboBox.getValue().getRegistryName())){
                                        comboBox.setValue(settings.getQuickExportHandler());
                                    }
                                });
                                return comboBox;
                            }),

                            new LabelNode("Default Folders").setTitleStyling(),
                            new LabelNode("Import Folder", () -> Editors.createDefaultFolderPicker("Select Default Import Folder", () -> DrawingBotV3.project().getImportDirectory(), settings.defaultImportDirectory.valueProperty())),
                            new LabelNode("Export Folder", () -> Editors.createDefaultFolderPicker("Select Default Export Folder", () -> DrawingBotV3.project().getExportDirectory(), settings.defaultExportDirectory.valueProperty())),

                            new LabelNode("Advanced").setTitleStyling(),
                            new SettingNode("Hiqh Quality Mode DPI", settings.importDPI),

                            new LabelNode("Preset Defaults").setTitleStyling(),
                            new LabelNode("Drawing Area", () -> Editors.createDefaultPresetComboBox(Register.PRESET_LOADER_DRAWING_AREA)),
                            new LabelNode("Image Processing", () -> Editors.createDefaultPresetComboBox(Register.PRESET_LOADER_FILTERS)),
                            new LabelNode("GCode Settings", () -> Editors.createDefaultPresetComboBox(Register.PRESET_LOADER_GCODE_SETTINGS)),
                            new LabelNode("vPype Settings", () -> Editors.createDefaultPresetComboBox(VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS)),
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
                                    settings.clearDefaultPreset(Register.PRESET_LOADER_DRAWING_AREA.presetType.id);
                                    settings.clearDefaultPreset(Register.PRESET_LOADER_FILTERS.presetType.id);
                                    settings.clearDefaultPreset(VpypePlugin.PRESET_LOADER_VPYPE_SETTINGS.presetType.id);
                                    settings.clearDefaultPreset(Register.PRESET_LOADER_GCODE_SETTINGS.presetType.id);
                                    settings.clearDefaultPreset(Register.PRESET_LOADER_DRAWING_SET.presetType.id);
                                    settings.clearDefaultPreset(Register.PRESET_LOADER_DRAWING_PENS.presetType.id);
                                });
                                return button;
                            })
                    );
                    builder.add(new LabelNode("PFM Default Presets").setTitleStyling());
                    for (PFMFactory<?> factory : MasterRegistry.INSTANCE.pfmFactories) {
                        if (!factory.isHidden() && (!factory.isPremiumFeature() || FXApplication.isPremiumEnabled)) {
                            builder.add(new LabelNode(factory.getDisplayName(), () -> {
                                ObservableList<GenericPreset<PresetData>> presets = MasterRegistry.INSTANCE.getObservablePFMPresetList(factory);
                                ComboBox<GenericPreset<PresetData>> comboBox = new ComboBox<>();
                                comboBox.setCellFactory(view -> new ComboCellPreset<>());
                                comboBox.setItems(presets);
                                comboBox.setValue(MasterRegistry.INSTANCE.getDefaultPresetWithFallback(Register.PRESET_LOADER_PFM, factory.getRegistryName(), "Default", true));
                                DBPreferences.INSTANCE.flagDefaultPresetChange.addListener((observable) -> {
                                    comboBox.setValue(MasterRegistry.INSTANCE.getDefaultPresetWithFallback(Register.PRESET_LOADER_PFM, factory.getRegistryName(), "Default", true));
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
                        javafx.scene.control.Button button = new Button("Reset All");
                        button.setOnAction(e -> {
                            for (PFMFactory<?> factory : MasterRegistry.INSTANCE.pfmFactories) {
                                settings.clearDefaultPreset(Register.PRESET_TYPE_PFM, factory.getRegistryName());
                            }
                        });
                        return button;
                    }));


                }
        ));
        MasterRegistry.INSTANCE.registerPreferencesPage(Editors.node("Export Settings",
                pageExportGeneral = Editors.page("General",
                        new SettingNode("Show Exported Drawing", settings.showExportedDrawing).setTitleStyling().setHideFromTree(true)
                ),
                pageExportPathOptimisation = Editors.page("Path Optimisation",
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
                pageSVG = Editors.page("SVG",
                        new LabelNode("General").setTitleStyling(),
                        new SettingNode("Export Background Layer", settings.exportSVGBackground),
                        new SettingNode("Save Drawing Stats to SVG", settings.svgDrawingStatsComment),
                        new SettingNode("Save PFM Settings to SVG", settings.svgPFMSettingsText),

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
                pageGCode = Editors.page("GCode",
                        new PropertyNode("GCode Preset", settings.selectedGCodePreset, GenericPreset.class){
                            @Override
                            public Node createEditor() {
                                ControlPresetSelection<GCodeSettings, PresetData> controlGCodePreset = new ControlPresetSelection<>();
                                controlGCodePreset.setPresetManager(Register.PRESET_MANAGER_GCODE_SETTINGS);
                                controlGCodePreset.setAvailablePresets(Register.PRESET_MANAGER_GCODE_SETTINGS.getPresetLoader().getPresets());
                                controlGCodePreset.setTarget(settings.gcodeSettings);
                                controlGCodePreset.activePresetProperty().bindBidirectional(settings.selectedGCodePreset);
                                return controlGCodePreset;
                            }
                        }.setTitleStyling(),
                        new LabelNode("Layout").setTitleStyling(),
                        new PropertyNode("Units", settings.gcodeSettings.gcodeUnits, UnitsLength.class),
                        new PropertyNode("X Offset", settings.gcodeSettings.gcodeOffsetX, Float.class),
                        new PropertyNode( "Y Offset", settings.gcodeSettings.gcodeOffsetY, Float.class),
                        new PropertyNode("Curve Flattening", settings.gcodeSettings.gcodeEnableFlattening, Boolean.class),
                        new PropertyNode( "Curve Flatness", settings.gcodeSettings.gcodeCurveFlatness, Float.class).setDisabledProperty(settings.gcodeSettings.gcodeEnableFlattening.not()),
                        new PropertyNode("Center Zero Point", settings.gcodeSettings.gcodeCenterZeroPoint, Boolean.class),
                        new PropertyNode("Comment Type", settings.gcodeSettings.gcodeCommentType, GCodeBuilder.CommentType.class),
                        new LabelNode("Custom GCode").setTitleStyling(),
                        new PropertyNode("Start", settings.gcodeSettings.gcodeStartCode, String.class){
                            @Override
                            public Node createEditor() {
                                return Editors.createTextAreaEditorLazy(property);
                            }
                        },
                        new PropertyNode("End", settings.gcodeSettings.gcodeEndCode, String.class){
                            @Override
                            public Node createEditor() {
                                return Editors.createTextAreaEditorLazy(property);
                            }
                        },
                        new PropertyNode("Pen Down", settings.gcodeSettings.gcodePenDownCode, String.class){
                            @Override
                            public Node createEditor() {
                                return Editors.createTextAreaEditorLazy(property);
                            }
                        },
                        new PropertyNode("Pen Up", settings.gcodeSettings.gcodePenUpCode, String.class){
                            @Override
                            public Node createEditor() {
                                return Editors.createTextAreaEditorLazy(property);
                            }
                        },
                        new LabelNode("").setTitleStyling(),
                        new LabelNode("With wildcard %LAYER_NAME%").setSubtitleStyling(),
                        new PropertyNode("Start Layer", settings.gcodeSettings.gcodeStartLayerCode, String.class){
                            @Override
                            public Node createEditor() {
                                return Editors.createTextAreaEditorLazy(property);
                            }
                        },
                        new LabelNode("").setTitleStyling(),
                        new LabelNode("With wildcard %LAYER_NAME%").setSubtitleStyling(),
                        new PropertyNode("End Layer", settings.gcodeSettings.gcodeEndLayerCode, String.class){
                            @Override
                            public Node createEditor() {
                                return Editors.createTextAreaEditorLazy(property);
                            }
                        }
                ),
                pageImageAndAnimation = Editors.page("Image & Animation",
                        new LabelNode("Resolution").setTitleStyling(),
                        new SettingNode("Export DPI", settings.exportDPI),
                        new SettingNode("Export Transparent PNGs", settings.transparentPNG),
                        new PropertyNode("Image Export Size", settings.imageExportSize, String.class).setEditable(false),
                        new LabelNode("Animations").setTitleStyling(),
                        new SettingNode("Frames per second", settings.framesPerSecond),
                        new SettingUnitsNode("Duration", settings.duration, settings.durationUnits),
                        new SettingNode("Frames Hold Start", settings.frameHoldStart),
                        new SettingNode("Frames Hold End", settings.frameHoldEnd),
                        new LabelNode(""),
                        new PropertyNode("Frame Count", settings.animationFrameCount, String.class).setEditable(false),
                        new PropertyNode("Geometries per frame", settings.animationGeometriesPFrame, String.class).setEditable(false),
                        new PropertyNode("Vertices per frame", settings.animationVerticesPFrame, String.class).setEditable(false)
                )
        ));
        MasterRegistry.INSTANCE.registerPreferencesPage(
                pageUserInterface = Editors.page("User Interface",
                    new LabelNode("General").setTitleStyling(),
                    new SettingNode("Dark Theme", settings.darkTheme),
                    new SettingNode("Default Window Size", settings.uiWindowSize),
                    new SettingNode("Restore Last Layout", settings.restoreLayout),
                    new SettingNode("Restore Project Layout", settings.restoreProjectLayout),

                    new LabelNode("Rulers").setTitleStyling(),
                    new SettingNode("Enabled", settings.rulersEnabled),

                    new LabelNode("Drawing Borders").setTitleStyling(),
                    new SettingNode("Enabled", settings.drawingBordersEnabled),
                    new SettingNode( "Colour", settings.drawingBordersColor).setDisabledProperty(settings.drawingBordersEnabled.asBooleanProperty().not()),

                    new LabelNode("Notifications").setTitleStyling(),
                    new SettingNode("Enabled", settings.notificationsEnabled),
                    new SettingNode("Screen Time", settings.notificationsScreenTime).setDisabledProperty(settings.notificationsEnabled.asBooleanProperty().not()),
                    new LabelNode("Set this value to 0 if you don't want notifications to disappear").setSubtitleStyling()
        ));
    }

    @FXML
    public void initialize(){
        DrawingBotV3.INSTANCE.controller.preferencesStage.setResizable(true);
        DrawingBotV3.INSTANCE.controller.preferencesStage.setOnHidden(e -> Register.PRESET_LOADER_PREFERENCES.updateConfigs());

        root = MasterRegistry.INSTANCE.root;

        treeViewCategories.setShowRoot(false);
        treeViewCategories.setRoot(Editors.build(root, textFieldSearchBar.getText()));

        textFieldSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            //rebuild the tree when the search changes
            treeViewCategories.setRoot(Editors.build(root, textFieldSearchBar.getText()));
            treeViewCategories.getSelectionModel().select(0);
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
                scrollPaneContent.setHvalue(0);
                scrollPaneContent.setVvalue(0);
                labelHeading.setText(newValue.getValue().getName());
            }
        });
        treeViewCategories.getSelectionModel().select(0);
    }
}