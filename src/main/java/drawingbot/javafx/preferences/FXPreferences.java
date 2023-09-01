package drawingbot.javafx.preferences;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.exporters.GCodeBuilder;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.controllers.AbstractFXController;
import drawingbot.javafx.controls.ComboCellDrawingPen;
import drawingbot.javafx.controls.ComboCellDrawingSet;
import drawingbot.javafx.controls.ComboCellNamedSetting;
import drawingbot.javafx.controls.ComboCellPreset;
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
import javafx.scene.layout.Priority;

/**
 * Note this implementation is inspired by / borrows from ControlsFX, PreferencesFX and FXSampler
 */
public class FXPreferences extends AbstractFXController {

    public TextField textFieldSearchBar = null;
    public Label labelHeading = null;
    public TreeView<TreeNode> treeViewCategories = null;
    public ScrollPane scrollPaneContent = null;

    public TreeNode root = null;

    public static PageNode gcodePage;
    public static PageNode imageAnimationPage;

    public static void registerDefaults(){
        DBPreferences settings = DBPreferences.INSTANCE;


        MasterRegistry.INSTANCE.registerPreferencesPage(Editors.page("General", builder -> {
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
                                    settings.defaultPenWidth.resetSetting();
                                    settings.defaultRescalingMode.resetSetting();
                                    settings.defaultCanvasColour.resetSetting();
                                    settings.defaultBackgroundColour.resetSetting();
                                    settings.defaultClippingMode.resetSetting();
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
                            new LabelNode("Preset Defaults").setTitleStyling(),
                            new LabelNode("Drawing Area", () -> Editors.createDefaultPresetComboBox(Register.PRESET_LOADER_DRAWING_AREA)),
                            new LabelNode("Image Processing", () -> Editors.createDefaultPresetComboBox(Register.PRESET_LOADER_FILTERS)),
                            new LabelNode("GCode Settings", () -> Editors.createDefaultPresetComboBox(Register.PRESET_LOADER_GCODE_SETTINGS)),
                            new LabelNode("vPype Settings", () -> Editors.createDefaultPresetComboBox(Register.PRESET_LOADER_VPYPE_SETTINGS)),
                            new LabelNode("Default Pen Set", () -> {
                                ComboBox<String> comboBoxSetType = new ComboBox<>();
                                ComboBox<IDrawingSet<IDrawingPen>> comboBoxDrawingSet = new ComboBox<>();
                                comboBoxSetType.setItems(FXCollections.observableArrayList(MasterRegistry.INSTANCE.registeredSets.keySet()));
                                comboBoxSetType.setValue(MasterRegistry.INSTANCE.getDefaultDrawingSet().getType());
                                comboBoxSetType.setPrefWidth(100);
                                comboBoxSetType.valueProperty().addListener((observable, oldValue, newValue) -> {
                                    comboBoxDrawingSet.setItems(MasterRegistry.INSTANCE.registeredSets.get(newValue));
                                    comboBoxDrawingSet.setValue(MasterRegistry.INSTANCE.registeredSets.get(newValue).get(0));
                                });
                                comboBoxDrawingSet.setItems(MasterRegistry.INSTANCE.registeredSets.get(comboBoxSetType.getValue()));
                                comboBoxDrawingSet.setValue(MasterRegistry.INSTANCE.getDefaultDrawingSet());
                                comboBoxDrawingSet.valueProperty().addListener((observable, oldValue, newValue) -> {
                                    if(newValue != null){
                                        DBPreferences.INSTANCE.setDefaultPreset(Register.PRESET_TYPE_DRAWING_SET.id, newValue.getCodeName());
                                    }
                                });
                                comboBoxDrawingSet.setPrefWidth(250);
                                comboBoxDrawingSet.setCellFactory(param -> new ComboCellDrawingSet<>());
                                comboBoxDrawingSet.setButtonCell(new ComboCellDrawingSet<>());
                                comboBoxDrawingSet.setPromptText("Select a Drawing Set");
                                HBox hBox = new HBox();
                                hBox.getChildren().add(comboBoxSetType);
                                hBox.getChildren().add(comboBoxDrawingSet);
                                hBox.setSpacing(4);

                                DBPreferences.INSTANCE.flagDefaultPresetChange.addListener((observable) -> {
                                    comboBoxSetType.setValue(MasterRegistry.INSTANCE.getDefaultDrawingSet().getType());
                                    comboBoxDrawingSet.setValue(MasterRegistry.INSTANCE.getDefaultDrawingSet());
                                });

                                return hBox;
                            }),
                            new LabelNode("Default Pen", () -> {
                                ComboBox<String> comboBoxPenType = new ComboBox<>();
                                ComboBox<DrawingPen> comboBoxDrawingPen = new ComboBox<>();

                                comboBoxPenType.setItems(FXCollections.observableArrayList(MasterRegistry.INSTANCE.registeredPens.keySet()));
                                comboBoxPenType.setValue(MasterRegistry.INSTANCE.getDefaultDrawingPen().getType());
                                comboBoxPenType.setPrefWidth(100);

                                comboBoxPenType.valueProperty().addListener((observable, oldValue, newValue) -> {
                                    comboBoxDrawingPen.setItems(MasterRegistry.INSTANCE.registeredPens.get(newValue));
                                    comboBoxDrawingPen.setValue(MasterRegistry.INSTANCE.getDefaultPen(newValue));
                                });

                                ComboBoxListViewSkin<DrawingPen> comboBoxDrawingPenSkin = new ComboBoxListViewSkin<>(comboBoxDrawingPen);
                                comboBoxDrawingPenSkin.hideOnClickProperty().set(false);
                                comboBoxDrawingPen.setSkin(comboBoxDrawingPenSkin);

                                comboBoxDrawingPen.setItems(MasterRegistry.INSTANCE.registeredPens.get(comboBoxPenType.getValue()));
                                comboBoxDrawingPen.setValue(MasterRegistry.INSTANCE.getDefaultDrawingPen());
                                comboBoxDrawingPen.setPrefWidth(250);
                                comboBoxDrawingPen.setCellFactory(param -> new ComboCellDrawingPen(null, false));
                                comboBoxDrawingPen.setButtonCell(new ComboCellDrawingPen(null,false));
                                comboBoxDrawingPen.valueProperty().addListener((observable, oldValue, newValue) -> {
                                    if(newValue != null){
                                        DBPreferences.INSTANCE.setDefaultPreset(Register.PRESET_TYPE_DRAWING_PENS.id, newValue.getCodeName());
                                    }
                                });
                                HBox hBox = new HBox();
                                hBox.getChildren().add(comboBoxPenType);
                                hBox.getChildren().add(comboBoxDrawingPen);
                                hBox.setSpacing(4);

                                DBPreferences.INSTANCE.flagDefaultPresetChange.addListener((observable) -> {
                                    comboBoxPenType.setValue(MasterRegistry.INSTANCE.getDefaultDrawingPen().getType());
                                    comboBoxDrawingPen.setValue(MasterRegistry.INSTANCE.getDefaultDrawingPen());
                                });
                                return hBox;
                            }),
                            new LabelNode("", () -> {
                                javafx.scene.control.Button button = new javafx.scene.control.Button("Reset All");
                                button.setOnAction(e -> {
                                    settings.clearDefaultPreset(Register.PRESET_LOADER_DRAWING_AREA.type.id);
                                    settings.clearDefaultPreset(Register.PRESET_LOADER_FILTERS.type.id);
                                    settings.clearDefaultPreset(Register.PRESET_LOADER_VPYPE_SETTINGS.type.id);
                                    settings.clearDefaultPreset(Register.PRESET_LOADER_DRAWING_SET.type.id);
                                    settings.clearDefaultPreset(Register.PRESET_LOADER_DRAWING_PENS.type.id);
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
                                comboBox.setValue(MasterRegistry.INSTANCE.getDefaultPreset(Register.PRESET_LOADER_PFM, factory.getRegistryName(), "Default"));
                                DBPreferences.INSTANCE.flagDefaultPresetChange.addListener((observable) -> {
                                    comboBox.setValue(MasterRegistry.INSTANCE.getDefaultPreset(Register.PRESET_LOADER_PFM, factory.getRegistryName(), "Default"));
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
                Editors.page("General",
                        new SettingNode("Show Exported Drawing", settings.showExportedDrawing).setTitleStyling().setHideFromTree(true)
                ),
                Editors.page("Path Optimisation",
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
                Editors.page("SVG",
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
                gcodePage = Editors.page("GCode",
                        new PropertyNode("GCode Preset", settings.selectedGCodePreset, GenericPreset.class){
                            @Override
                            public Node createEditor() {
                                HBox hBox = new HBox();
                                ComboBox<GenericPreset<PresetData>> comboBox = new ComboBox<>();
                                comboBox.setItems(Register.PRESET_LOADER_GCODE_SETTINGS.presets);
                                comboBox.setCellFactory(view -> new ComboCellPreset<>());
                                comboBox.valueProperty().bindBidirectional(settings.selectedGCodePreset);
                                HBox.setHgrow(comboBox, Priority.ALWAYS);
                                hBox.getChildren().add(comboBox);

                                MenuButton menuButton = FXHelper.createPresetMenuButton(Register.PRESET_LOADER_GCODE_SETTINGS, Register.PRESET_LOADER_GCODE_SETTINGS::getDefaultManager, false, settings.selectedGCodePreset);
                                HBox.setHgrow(menuButton, Priority.SOMETIMES);
                                hBox.getChildren().add(menuButton);
                                return hBox;
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
                imageAnimationPage = Editors.page("Image & Animation",
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
                MasterRegistry.INSTANCE.registerPreferencesPage(Editors.page("User Interface",
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
        DrawingBotV3.INSTANCE.controller.preferencesStage.setOnHidden(e -> Register.PRESET_LOADER_CONFIGS.markDirty());

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