package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.Hooks;
import drawingbot.files.*;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.javafx.controllers.*;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.integrations.vpype.FXVPypeController;
import drawingbot.integrations.vpype.VpypeHelper;
import drawingbot.javafx.controls.*;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.preferences.FXPreferences;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.MasterRegistry;
import drawingbot.render.IDisplayMode;
import drawingbot.render.overlays.NotificationOverlays;
import drawingbot.render.shapes.JFXShapeManager;
import drawingbot.utils.*;
import drawingbot.utils.flags.Flags;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.*;

import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.fxmisc.easybind.EasyBind;

import java.awt.image.BufferedImageOp;
import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class FXController extends AbstractFXController {

    public static final String STYLESHEET_VIEWPORT_OVERLAYS = "viewport-overlays.css";

    public FXDrawingArea drawingAreaController;
    public FXImageFilters imageFiltersController;
    public FXPFMControls pfmSettingsController;
    public FXDrawingSets drawingSetsController;
    public FXVersionControl versionControlController;

    public TitledPane titledPaneBatchProcessing = null;
    public FXBatchProcessing batchProcessingController;

    /**
     * starts the FXController, called internally by JavaFX
     */
    @FXML
    public void initialize(){
        DrawingBotV3.logger.entering("FX Controller", "initialize");

        try{
            Hooks.runHook(Hooks.FX_CONTROLLER_PRE_INIT, this);
            initToolbar();
            initViewport();
            initPlottingControls();
            initProgressBar();
            //initPFMControls();
            Hooks.runHook(Hooks.FX_CONTROLLER_POST_INIT, this);

            viewportScrollPane.setHvalue(0.5);
            viewportScrollPane.setVvalue(0.5);

        }catch (Exception e){
            DrawingBotV3.logger.log(Level.SEVERE, "Failed to initialize JAVA FX", e);
        }

        //note: it's better to do this here, it means if we create other instances of the controller classes elsewhere their nodes won't clash with the main UIs ones
        FXHelper.makePersistent(drawingAreaController.getPersistentNodes());
        FXHelper.makePersistent(imageFiltersController.getPersistentNodes());
        FXHelper.makePersistent(pfmSettingsController.getPersistentNodes());
        FXHelper.makePersistent(drawingSetsController.getPersistentNodes());
        FXHelper.makePersistent(versionControlController.getPersistentNodes());
        FXHelper.makePersistent(batchProcessingController.getPersistentNodes());
        FXHelper.makePersistent(viewportScrollPane);


        DrawingBotV3.logger.exiting("FX Controller", "initialize");
    }

    public void setupBindings(){
        drawingAreaController.drawingArea.bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(p -> p.drawingArea));
        imageFiltersController.settings.bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(p -> p.imageSettings));
        imageFiltersController.image.bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(p -> p.openImage));
        pfmSettingsController.pfmSettings.bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(p -> p.pfmSettings));
        drawingSetsController.drawingSets.bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(p -> p.drawingSets));
        versionControlController.projectVersions.bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(p -> p.projectVersions));
        JFXShapeManager.INSTANCE.activeShapeList.bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(p -> p.maskingSettings.get().shapeList));
    }

    public Stage exportSettingsStage;
    public FXExportController exportController;

    public Stage vpypeSettingsStage;
    public FXVPypeController vpypeController;

    public Stage mosaicSettingsStage;
    public FXStylesController mosaicController;

    public Stage taskMonitorStage;
    public FXTaskMonitorController taskMonitorController;

    public Stage projectManagerStage;
    public FXProjectManagerController projectManagerController;

    public Stage preferencesStage;
    public FXPreferences preferencesController;


    public void initSeparateStages() {
        exportController = FXHelper.initSeparateStage("/drawingbot/javafx/exportsettings.fxml", exportSettingsStage = new Stage(), "Export Settings", Modality.APPLICATION_MODAL);
        vpypeController = FXHelper.initSeparateStage("/drawingbot/javafx/vpypesettings.fxml", vpypeSettingsStage = new Stage(), "vpype Settings", Modality.APPLICATION_MODAL);
        mosaicController = FXHelper.initSeparateStage("/drawingbot/javafx/mosaicsettings.fxml", mosaicSettingsStage = new Stage(), "Mosaic Settings", Modality.APPLICATION_MODAL);
        taskMonitorController = FXHelper.initSeparateStage("/drawingbot/javafx/taskmonitor.fxml", taskMonitorStage = new Stage(), "Task Monitor", Modality.NONE);
        projectManagerController = FXHelper.initSeparateStage("/drawingbot/javafx/projectmanager.fxml", projectManagerStage = new Stage(), "Project Manager", Modality.NONE);
        preferencesController = FXHelper.initSeparateStage("/drawingbot/javafx/preferences.fxml", preferencesStage = new Stage(), "Preferences", Modality.APPLICATION_MODAL);

        FXHelper.initSeparateStageWithController("/drawingbot/javafx/serialportsettings.fxml", (Stage) Hooks.runHook(Hooks.SERIAL_CONNECTION_STAGE, new Stage())[0], Hooks.runHook(Hooks.SERIAL_CONNECTION_CONTROLLER, new DummyController())[0], "Plotter / Serial Port Connection", Modality.NONE);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////GLOBAL CONTAINERS
    public VBox vBoxMain = null;
    public SplitPane splitPane = null;

    public ScrollPane scrollPaneSettings = null;
    public VBox vBoxSettings = null;

    public DialogPresetRename presetEditorDialog = new DialogPresetRename();

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// TOOL BAR

    public TabPane tabPaneProjects = null;

    public MenuBar menuBar = null;
    public Menu menuFile = null;
    public Menu menuView = null;
    public Menu menuFilters = null;
    public Menu menuHelp = null;

    public Map<TitledPane, Stage> settingsStages = new LinkedHashMap<>();
    public Map<TitledPane, Node> settingsContent = new LinkedHashMap<>();

    public void initToolbar(){

        FontAwesome fontAwesome = new FontAwesome();

        MenuItem menuNew = new MenuItem("New");
        menuNew.setOnAction(e -> {
            ObservableProject project = new ObservableProject();
            DrawingBotV3.INSTANCE.activeProjects.add(project);
            DrawingBotV3.INSTANCE.activeProject.set(project);
            NotificationOverlays.INSTANCE.show("Created New Project");
        });
        menuNew.setGraphic(fontAwesome.create(FontAwesome.Glyph.FILE).color(Color.SLATEGRAY));
        menuNew.setAccelerator(KeyCombination.valueOf("Ctrl + N"));
        menuFile.getItems().add(menuNew);

        MenuItem menuOpen = new MenuItem("Open");
        menuOpen.setOnAction(e -> FXHelper.importProject());
        menuOpen.setGraphic(fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN).color(Color.SLATEGRAY));
        menuOpen.setAccelerator(KeyCombination.valueOf("Ctrl + O"));
        menuFile.getItems().add(menuOpen);

        MenuItem menuSave = new MenuItem("Save");
        menuSave.setOnAction(e -> FXHelper.saveProject());
        menuSave.setGraphic(fontAwesome.create(FontAwesome.Glyph.SAVE).color(Color.SLATEGRAY));
        menuSave.setAccelerator(KeyCombination.valueOf("Ctrl + S"));
        menuFile.getItems().add(menuSave);

        MenuItem menuSaveAs = new MenuItem("Save As");
        menuSaveAs.setOnAction(e -> FXHelper.saveProjectAs());
        menuFile.getItems().add(menuSaveAs);

        menuFile.getItems().add(new SeparatorMenuItem());
        /*
        MenuItem projectManager = new MenuItem("Open Project Manager");
        projectManager.setOnAction(e -> projectManagerStage.show());
        menuFile.getItems().add(projectManager);

        menuFile.getItems().add(new SeparatorMenuItem());

         */


        MenuItem menuImport = new MenuItem("Import Image");
        menuImport.setOnAction(e -> FXHelper.importImageFile());
        menuFile.getItems().add(menuImport);

        MenuItem menuVideo = new MenuItem("Import Video");
        menuVideo.setOnAction(e -> FXHelper.importVideoFile());
        menuFile.getItems().add(menuVideo);

        if(FXApplication.isPremiumEnabled) {
            MenuItem menuSVG = new MenuItem("Import SVG");
            menuSVG.setOnAction(e -> FXHelper.importSVGFile());
            menuFile.getItems().add(menuSVG);
        }else{
            MenuItem menuSVG = new MenuItem("Import SVG " + "(Premium)");
            menuSVG.setOnAction(e -> showPremiumFeatureDialog());
            menuFile.getItems().add(menuSVG);
        }

        menuFile.getItems().add(new SeparatorMenuItem());

        for(ExportTask.Mode exportMode : ExportTask.Mode.values()){
            Menu menuExport = new Menu(exportMode.getDisplayName());
            for(DrawingExportHandler.Category category : DrawingExportHandler.Category.values()){
                for(DrawingExportHandler format : MasterRegistry.INSTANCE.drawingExportHandlers.values()){
                    if(format.category != category){
                        continue;
                    }
                    if(format.isPremium && !FXApplication.isPremiumEnabled){
                        MenuItem item = new MenuItem(format.description + " (Premium)");
                        item.setOnAction(e -> showPremiumFeatureDialog());
                        menuExport.getItems().add(item);
                    }else{
                        MenuItem item = new MenuItem(format.description);
                        item.setOnAction(e -> FXHelper.exportFile(format, exportMode));
                        menuExport.getItems().add(item);
                    }
                }
                if(category != DrawingExportHandler.Category.values()[DrawingExportHandler.Category.values().length-1]){
                    menuExport.getItems().add(new SeparatorMenuItem());
                }
            }
            menuExport.disableProperty().bind(Bindings.isNull(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(project -> project.currentDrawing)));
            menuFile.getItems().add(menuExport);
        }

        menuFile.getItems().add(new SeparatorMenuItem());

        Hooks.runHook(Hooks.FILE_MENU, menuFile);

        MenuItem menuExportToVPype = new MenuItem("Export to " + VpypeHelper.VPYPE_NAME);
        menuExportToVPype.setOnAction(e -> {
            if(DrawingBotV3.project().getCurrentDrawing() != null){
                vpypeSettingsStage.show();
            }
        });
        menuExportToVPype.disableProperty().bind(Bindings.isNull(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(project -> project.currentDrawing)));

        menuFile.getItems().add(menuExportToVPype);

        menuFile.getItems().add(new SeparatorMenuItem());

        MenuItem menuPreferences = new MenuItem("Preferences");
        menuPreferences.setOnAction(e -> {
            preferencesStage.show();
        });
        menuFile.getItems().add(menuPreferences);


        MenuItem menuExportSettings = new MenuItem("Export Settings");
        menuExportSettings.setOnAction(e -> exportSettingsStage.show());
        menuFile.getItems().add(menuExportSettings);

        menuFile.getItems().add(new SeparatorMenuItem());

        MenuItem taskMonitor = new MenuItem("Open Task Monitor");
        taskMonitor.setOnAction(e -> taskMonitorStage.show());
        menuFile.getItems().add(taskMonitor);

        menuFile.getItems().add(new SeparatorMenuItem());

        MenuItem menuQuit = new MenuItem("Quit");
        menuQuit.setOnAction(e -> Platform.exit());
        menuQuit.setGraphic(fontAwesome.create(FontAwesome.Glyph.CLOSE).color(Color.SLATEGRAY));
        menuFile.getItems().add(menuQuit);

        //view
        ArrayList<TitledPane> allPanes = new ArrayList<>();
        for(Node node : vBoxSettings.getChildren()){
            if(node instanceof TitledPane){
                allPanes.add((TitledPane) node);
            }
        }
        FXHelper.makePersistent(allPanes);

        MenuItem fullScreen = new MenuItem("Fullscreen Mode");
        fullScreen.setOnAction(a -> FXApplication.primaryStage.setFullScreen(!FXApplication.primaryStage.isFullScreen()));
        fullScreen.setAccelerator(KeyCombination.valueOf("Ctrl + F"));
        menuView.getItems().add(fullScreen);
        menuView.getItems().add(new SeparatorMenuItem());

        for(TitledPane pane : allPanes){
            MenuItem viewButton = new MenuItem(pane.getText());
            viewButton.setOnAction(e -> {
                Platform.runLater(() -> allPanes.forEach(p -> p.expandedProperty().setValue(p == pane)));
            });
            menuView.getItems().add(viewButton);
            Button undock = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.LINK));

            undock.setOnAction(e -> {
                Stage currentStage = settingsStages.get(pane);
                if(currentStage == null){

                    Node content = pane.getContent();

                    boolean allowResizing = content instanceof VBox;

                    //create the stage
                    Stage settingsStage = new Stage(StageStyle.DECORATED);
                    settingsStage.initModality(Modality.NONE);
                    settingsStage.initOwner(FXApplication.primaryStage);
                    settingsStage.setTitle(pane.getText());
                    settingsStage.setResizable(allowResizing);

                    //create the root node
                    ScrollPane scrollPane = new ScrollPane();
                    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                    if(allowResizing){
                        scrollPane.setFitToWidth(true);
                        scrollPane.setFitToHeight(true);
                        VBox.setVgrow(scrollPane, Priority.ALWAYS);
                        HBox.setHgrow(scrollPane, Priority.ALWAYS);
                        scrollPane.setPrefWidth(420);
                    }

                    //transfer the content
                    pane.setAnimated(false);
                    pane.setExpanded(true);
                    pane.layout();

                    pane.setContent(new AnchorPane());
                    scrollPane.setContent(content);

                    pane.setExpanded(false);
                    pane.setAnimated(true);

                    //save the reference for later
                    settingsStages.put(pane, settingsStage);
                    settingsContent.put(pane, content);


                    //show the scene
                    Scene scene = new Scene(scrollPane);
                    settingsStage.setScene(scene);
                    settingsStage.setOnCloseRequest(event -> redockSettingsPane(pane));
                    FXApplication.applyDBStyle(settingsStage);
                    settingsStage.show();
                }else{
                    redockSettingsPane(pane);
                    currentStage.close();
                }
            });

            pane.setContentDisplay(ContentDisplay.RIGHT);
            pane.setGraphic(undock);
            undock.translateXProperty().bind(Bindings.createDoubleBinding(
                    () -> pane.getWidth() - undock.getLayoutX() - undock.getWidth() - 30,
                    pane.widthProperty())
            );
        }



        //filters
        for(Map.Entry<EnumFilterTypes, ObservableList<GenericFactory<BufferedImageOp>>> entry : MasterRegistry.INSTANCE.imgFilterFactories.entrySet()){
            Menu type = new Menu(entry.getKey().toString());

            for(GenericFactory<BufferedImageOp> factory : entry.getValue()){
                MenuItem item = new MenuItem(factory.getName());
                item.setOnAction(e -> FXHelper.addImageFilter(factory, DrawingBotV3.project().imageSettings.get()));
                type.getItems().add(item);
            }

            menuFilters.getItems().add(type);
        }

        //help
        if(!FXApplication.isPremiumEnabled){
            MenuItem upgrade = new MenuItem("Upgrade");
            upgrade.setOnAction(e -> FXHelper.openURL(DBConstants.URL_UPGRADE));
            menuHelp.getItems().add(upgrade);
            menuHelp.getItems().add(new SeparatorMenuItem());
        }

        MenuItem documentation = new MenuItem("View Documentation");
        documentation.setOnAction(e -> FXHelper.openURL(DBConstants.URL_READ_THE_DOCS_HOME));
        documentation.setGraphic(fontAwesome.create(FontAwesome.Glyph.INFO).color(Color.SLATEGRAY));
        menuHelp.getItems().add(documentation);

        MenuItem sourceCode = new MenuItem("View Source Code");
        sourceCode.setOnAction(e -> FXHelper.openURL(DBConstants.URL_GITHUB_REPO));
        sourceCode.setGraphic(fontAwesome.create(FontAwesome.Glyph.CODE).color(Color.SLATEGRAY));
        menuHelp.getItems().add(sourceCode);

        MenuItem configFolder = new MenuItem("Open Configs Folder");
        configFolder.setOnAction(e -> FXHelper.openFolder(new File(FileUtils.getUserDataDirectory())));
        configFolder.setGraphic(fontAwesome.create(FontAwesome.Glyph.FOLDER).color(Color.SLATEGRAY));
        menuHelp.getItems().add(configFolder);


        if(!FXApplication.isPremiumEnabled){
            MenuItem upgrade = new MenuItem("Upgrade");
            upgrade.setOnAction(e -> FXHelper.openURL(DBConstants.URL_UPGRADE));
            upgrade.setGraphic(fontAwesome.create(FontAwesome.Glyph.ARROW_UP).color(Color.SLATEGRAY));
            menuHelp.getItems().add(upgrade);
        }

        DrawingBotV3.INSTANCE.activeProjects.forEach(this::onProjectAdded);
        DrawingBotV3.INSTANCE.activeProjects.addListener((ListChangeListener<ObservableProject>) c -> {
            while(c.next()){
                c.getRemoved().forEach(this::onProjectRemoved);
                c.getAddedSubList().forEach(this::onProjectAdded);
            }
        });

        tabPaneProjects.minHeightProperty().bind(Bindings.createDoubleBinding(() -> DrawingBotV3.INSTANCE.activeProjects.size() > 1 ? 27D : 0D, DrawingBotV3.INSTANCE.activeProjects));
        tabPaneProjects.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ObservableProject project = (ObservableProject) newValue.getProperties().get(ObservableProject.class);
            DrawingBotV3.INSTANCE.activeProject.set(project);
        });
        DrawingBotV3.INSTANCE.activeProject.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                tabPaneProjects.getSelectionModel().select(newValue.tab.get());
            }
        });

    }

    public void onProjectRemoved(ObservableProject project){
        Tab tab = project.tab.get();
        tabPaneProjects.getTabs().remove(tab);
    }

    public void onProjectAdded(ObservableProject project){
        Tab tab = new Tab();
        tab.textProperty().bind(project.name);
        tab.setContextMenu(new ContextMenuObservableProject(() -> DrawingBotV3.INSTANCE.activeProjects, () -> project));

        /*
        tab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && DrawingBotV3.INSTANCE.activeProject.get() != project){
                DrawingBotV3.INSTANCE.activeProject.set(project);
            }
        });

         */
        tab.getProperties().put(ObservableProject.class, project);
        tab.setOnClosed(e -> {
            DrawingBotV3.INSTANCE.activeProjects.remove(project);
            if(DrawingBotV3.INSTANCE.activeProjects.isEmpty()){
                DrawingBotV3.INSTANCE.activeProject.set(new ObservableProject());
                DrawingBotV3.INSTANCE.activeProjects.add(DrawingBotV3.INSTANCE.activeProject.get());
            }else{
                DrawingBotV3.INSTANCE.activeProject.set(DrawingBotV3.INSTANCE.activeProjects.get(0));
            }
        });
        tab.setClosable(true);
        tabPaneProjects.getTabs().add(tab);
        project.tab.set(tab);

        DrawingBotV3.INSTANCE.activeProject.set(project);
    }

    public void redockSettingsPane(TitledPane pane){
        Node content = settingsContent.get(pane);
        pane.setContent(content);

        settingsStages.put(pane, null);
        settingsContent.put(pane, null);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// VIEWPORT PANE

    ////VIEWPORT WINDOW
    public VBox vBoxViewportContainer = null;
    public NotificationPane notificationPane = null;
    public ZoomableScrollPane viewportScrollPane = null;
    public AnchorPane viewportOverlayAnchorPane = null;

    ////VIEWPORT SETTINGS
    public RangeSlider rangeSliderDisplayedLines = null;
    public TextField textFieldDisplayedShapesMin = null;
    public TextField textFieldDisplayedShapesMax = null;

    public CheckBox checkBoxApplyToExport = null;

    public ChoiceBox<IDisplayMode> choiceBoxDisplayMode = null;
    public ComboBox<EnumBlendMode> comboBoxBlendMode = null;
    public ToggleButton toggleDPIScaling = null;
    public Button buttonResetView = null;

    ////PLOT DETAILS
    public Label labelElapsedTime = null;
    public Label labelPlottedShapes = null;
    public Label labelPlottedVertices = null;
    public Label labelImageResolution = null;
    public Label labelPlottingResolution = null;
    public Label labelCurrentPosition = null;

    public Rectangle colourPickerRectangle;

    //public CheckBox checkBoxDarkTheme = null;

    public void initViewport(){

        ////VIEWPORT SETTINGS
        rangeSliderDisplayedLines.highValueProperty().addListener((observable, oldValue, newValue) -> {
            PlottedDrawing drawing = DrawingBotV3.project().getCurrentDrawing();
            if(drawing != null){
                int lines = (int)Utils.mapDouble(newValue.doubleValue(), 0, 1, 0, drawing.getGeometryCount());
                drawing.displayedShapeMax = lines;
                textFieldDisplayedShapesMax.setText(String.valueOf(lines));
                DrawingBotV3.INSTANCE.updatePenDistribution();
            }
        });

        rangeSliderDisplayedLines.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            PlottedDrawing drawing = DrawingBotV3.project().getCurrentDrawing();
            if(drawing != null){
                int lines = (int)Utils.mapDouble(newValue.doubleValue(), 0, 1, 0, drawing.getGeometryCount());
                drawing.displayedShapeMin = lines;
                textFieldDisplayedShapesMin.setText(String.valueOf(lines));
                DrawingBotV3.INSTANCE.updatePenDistribution();
            }
        });

        textFieldDisplayedShapesMax.setOnAction(e -> {
            PlottedDrawing drawing = DrawingBotV3.project().getCurrentDrawing();
            if(drawing != null){
                int lines = (int)Math.max(0, Math.min(drawing.getGeometryCount(), Double.parseDouble(textFieldDisplayedShapesMax.getText())));
                drawing.displayedShapeMax = lines;
                textFieldDisplayedShapesMax.setText(String.valueOf(lines));
                rangeSliderDisplayedLines.setHighValue((double)lines / drawing.getGeometryCount());
                DrawingBotV3.INSTANCE.updatePenDistribution();
            }
        });

        textFieldDisplayedShapesMin.setOnAction(e -> {
            PlottedDrawing drawing = DrawingBotV3.project().getCurrentDrawing();
            if(drawing != null){
                int lines = (int)Math.max(0, Math.min(drawing.getGeometryCount(), Double.parseDouble(textFieldDisplayedShapesMin.getText())));
                drawing.displayedShapeMin = lines;
                textFieldDisplayedShapesMin.setText(String.valueOf(lines));
                rangeSliderDisplayedLines.setLowValue((double)lines / drawing.getGeometryCount());
                DrawingBotV3.INSTANCE.updatePenDistribution();
            }
        });
        EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(project -> project.currentDrawing).addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Platform.runLater(() -> {
                    rangeSliderDisplayedLines.setLowValue(0.0F);
                    rangeSliderDisplayedLines.setHighValue(1.0F);
                    textFieldDisplayedShapesMin.setText(String.valueOf(0));
                    textFieldDisplayedShapesMax.setText(String.valueOf(newValue.getGeometryCount()));
                    labelPlottedShapes.setText(Utils.defaultNF.format(newValue.getGeometryCount()));
                    labelPlottedVertices.setText(Utils.defaultNF.format(newValue.getVertexCount()));
                });
            }else{
                rangeSliderDisplayedLines.setLowValue(0.0F);
                rangeSliderDisplayedLines.setHighValue(1.0F);
                textFieldDisplayedShapesMin.setText(String.valueOf(0));
                textFieldDisplayedShapesMax.setText(String.valueOf(0));
                labelPlottedShapes.setText(Utils.defaultNF.format(0));
                labelPlottedVertices.setText(Utils.defaultNF.format(0));
            }
        });

        JFXUtils.subscribeListener(DrawingBotV3.INSTANCE.activeProject, (observable, oldValue, newValue) -> {
            if(oldValue != null){
                checkBoxApplyToExport.selectedProperty().unbindBidirectional(oldValue.exportRange);
                comboBoxBlendMode.valueProperty().unbindBidirectional(oldValue.blendMode);
                toggleDPIScaling.selectedProperty().unbindBidirectional(oldValue.dpiScaling);
            }
            if(newValue != null){
                checkBoxApplyToExport.selectedProperty().bindBidirectional(newValue.exportRange);
                comboBoxBlendMode.valueProperty().bindBidirectional(newValue.blendMode);
                toggleDPIScaling.selectedProperty().bindBidirectional(newValue.dpiScaling);
            }
        });

        choiceBoxDisplayMode.getItems().addAll(MasterRegistry.INSTANCE.displayModes.filtered(d->!d.isHidden()));
        choiceBoxDisplayMode.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.displayMode);

        comboBoxBlendMode.setItems(FXCollections.observableArrayList(EnumBlendMode.values()));
        buttonResetView.setOnAction(e -> DrawingBotV3.INSTANCE.resetView());

        /*
        checkBoxDarkTheme.setSelected(ConfigFileHandler.getApplicationSettings().darkTheme);
        checkBoxDarkTheme.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            ConfigFileHandler.getApplicationSettings().darkTheme = isSelected;
            ConfigFileHandler.getApplicationSettings().markDirty();
            FXApplication.applyCurrentTheme();
        });

         */


        //DrawingBotV3.INSTANCE.displayGrid.bind(checkBoxShowGrid.selectedProperty());
        //DrawingBotV3.INSTANCE.displayGrid.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.reRender());

        viewportScrollPane = new ZoomableScrollPane();

        viewportScrollPane.setFitToWidth(true);
        viewportScrollPane.setFitToHeight(true);
        viewportScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        viewportScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        viewportScrollPane.setVvalue(0.5);
        viewportScrollPane.setHvalue(0.5);
        viewportScrollPane.setMaxWidth(Double.MAX_VALUE);
        viewportScrollPane.setMaxHeight(Double.MAX_VALUE);
        viewportScrollPane.setPannable(true);
        viewportScrollPane.scale.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.setRenderFlag(Flags.CANVAS_MOVED));
        viewportScrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.setRenderFlag(Flags.CANVAS_MOVED));
        viewportScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.setRenderFlag(Flags.CANVAS_MOVED));
        viewportScrollPane.widthProperty().addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.setRenderFlag(Flags.CANVAS_MOVED));
        viewportScrollPane.heightProperty().addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.setRenderFlag(Flags.CANVAS_MOVED));

        viewportScrollPane.contentProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){

                ObservableObjectValue<Color> backgroundColor = EasyBind.select(DrawingBotV3.INSTANCE.activeProject).select(p -> p.drawingArea).selectObject(d -> d.backgroundColor);
                newValue.styleProperty().unbind();
                newValue.styleProperty().bind(Bindings.createStringBinding(() -> {
                    Color c = backgroundColor.get();
                    return "-fx-background-color: rgb(" + (int)(c.getRed()*255) + "," + (int)(c.getGreen()*255) + ", " + (int)(c.getBlue()*255) + ", " + c.getOpacity() + ");";
                }, backgroundColor));
            }
        });



        VBox.setVgrow(viewportScrollPane, Priority.ALWAYS);
        HBox.setHgrow(viewportScrollPane, Priority.ALWAYS);

        /*
        viewportStackPane = new Pane();
        viewportStackPane.getChildren().add(viewportScrollPane);
        VBox.setVgrow(viewportStackPane, Priority.ALWAYS);
        HBox.setHgrow(viewportStackPane, Priority.ALWAYS);
        vBoxViewportContainer.getChildren().add(viewportStackPane);

         */

        viewportOverlayAnchorPane = new AnchorPane();
        viewportOverlayAnchorPane.setPickOnBounds(false);
        Rectangle overlayClip = new Rectangle(0, 0,0,0);
        overlayClip.widthProperty().bind(viewportScrollPane.widthProperty().subtract(14)); //14 = subtract the scrollbars
        overlayClip.heightProperty().bind(viewportScrollPane.heightProperty().subtract(14));
        viewportOverlayAnchorPane.layoutXProperty().bind(viewportScrollPane.layoutXProperty());
        viewportOverlayAnchorPane.layoutYProperty().bind(viewportScrollPane.layoutYProperty());
        viewportOverlayAnchorPane.setManaged(false);
        viewportOverlayAnchorPane.setClip(overlayClip);
        viewportOverlayAnchorPane.getStylesheets().add(FXController.class.getResource(STYLESHEET_VIEWPORT_OVERLAYS).toExternalForm());

        vBoxViewportContainer.getChildren().add(viewportScrollPane);
        vBoxViewportContainer.getChildren().add(viewportOverlayAnchorPane);

        viewportScrollPane.setOnDragOver(event -> {

            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.LINK);
            }
            event.consume();
        });

        viewportScrollPane.setOnDragDropped(event -> {

            Dragboard db = event.getDragboard();
            boolean success = false;
            if(db.hasFiles()){
                List<File> files = db.getFiles();
                DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), files.get(0), false, true);
                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });

        labelElapsedTime.setText("0 s");
        labelPlottedShapes.setText("0");
        labelPlottedVertices.setText("0");
        labelImageResolution.setText("0 x 0");
        labelPlottingResolution.setText("0 x 0");
        labelCurrentPosition.setText("0 x 0 y");
    }



    private final WritableImage snapshotImage = new WritableImage(1, 1);
    private ObservableDrawingPen penForColourPicker;
    private boolean colourPickerActive;

    public void startColourPick(ObservableDrawingPen pen){
        penForColourPicker = pen;
        colourPickerActive = true;
        viewportScrollPane.setCursor(Cursor.CROSSHAIR);
        colourPickerRectangle.setVisible(true);
        colourPickerRectangle.setFill(pen.javaFXColour.get());
    }

    public void doColourPick(MouseEvent event, boolean update){
        Point2D localPoint = viewportScrollPane.getParent().sceneToLocal(event.getSceneX(), event.getSceneY());

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setViewport(new Rectangle2D(localPoint.getX(), localPoint.getY(), 1, 1));

        viewportScrollPane.snapshot((result) -> setPickResult(result, update), parameters, snapshotImage);
    }

    public Void setPickResult(SnapshotResult result, boolean update){
        Color color = result.getImage().getPixelReader().getColor(0, 0);
        if(update){
            colourPickerRectangle.setFill(color);
        }else{
            penForColourPicker.javaFXColour.set(color);
            endColourPick();
        }
        return null;
    }

    public void endColourPick(){
        viewportScrollPane.setCursor(Cursor.DEFAULT);
        penForColourPicker = null;
        colourPickerActive = false;
        colourPickerRectangle.setVisible(false);
    }

    public void onMouseMovedColourPicker(MouseEvent event){
        if(colourPickerActive){
            doColourPick(event, true);
        }
    }

    public void onMousePressedColourPicker(MouseEvent event){
        if(colourPickerActive && event.isPrimaryButtonDown()){
            doColourPick(event, false);
            event.consume();
        }
    }

    public void onKeyPressedColourPicker(KeyEvent event){
        if(event.getCode() == KeyCode.ESCAPE){
            endColourPick();
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// PLOTTING CONTROLS

    public Button buttonStartPlotting = null;
    public Button buttonStopPlotting = null;
    public Button buttonResetPlotting = null;
    public Button buttonSaveVersion = null;

    public void initPlottingControls(){
        buttonStartPlotting.setOnAction(param -> DrawingBotV3.INSTANCE.startPlotting(DrawingBotV3.context()));
        buttonStartPlotting.disableProperty().bind(DrawingBotV3.INSTANCE.taskMonitor.isPlotting);
        buttonStopPlotting.setOnAction(param -> DrawingBotV3.INSTANCE.stopPlotting(DrawingBotV3.context()));
        buttonStopPlotting.disableProperty().bind(DrawingBotV3.INSTANCE.taskMonitor.isPlotting.not());
        buttonResetPlotting.setOnAction(param -> DrawingBotV3.INSTANCE.resetPlotting(DrawingBotV3.context()));

        Binding<PlottedDrawing> binding = EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(project -> project.currentDrawing);

        buttonSaveVersion.setOnAction(param -> versionControlController.saveVersion());
        buttonSaveVersion.disableProperty().bind(Bindings.createBooleanBinding(() -> DrawingBotV3.INSTANCE.taskMonitor.isPlotting.get() || binding.getValue() == null, DrawingBotV3.INSTANCE.taskMonitor.isPlotting, binding));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ////PROGRESS BAR PANE

    public Pane paneProgressBar = null;
    public ProgressBar progressBarGeneral = null;
    public Label progressBarLabel = null;
    public Label labelCancelExport = null;
    public Label labelOpenDestinationFolder = null;

    public void initProgressBar(){
        progressBarGeneral.prefWidthProperty().bind(paneProgressBar.widthProperty());
        progressBarLabel.setText("");

        progressBarGeneral.progressProperty().bind(DrawingBotV3.INSTANCE.taskMonitor.progressProperty);
        progressBarLabel.textProperty().bind(Bindings.createStringBinding(() -> DrawingBotV3.INSTANCE.taskMonitor.getCurrentTaskStatus(), DrawingBotV3.INSTANCE.taskMonitor.messageProperty, DrawingBotV3.INSTANCE.taskMonitor.titleProperty, DrawingBotV3.INSTANCE.taskMonitor.exceptionProperty));

        labelCancelExport.setOnMouseEntered(event -> labelCancelExport.textFillProperty().setValue(Color.BLANCHEDALMOND));
        labelCancelExport.setOnMouseExited(event -> labelCancelExport.textFillProperty().setValue(Color.BLACK));
        labelCancelExport.setOnMouseClicked(event -> {
            Task<?> task = DrawingBotV3.INSTANCE.taskMonitor.currentTask;
            if(task instanceof ExportTask){
                DrawingBotV3.INSTANCE.resetTaskService();
            }
        });
        labelCancelExport.visibleProperty().bind(DrawingBotV3.INSTANCE.taskMonitor.isExporting);

        labelOpenDestinationFolder.setOnMouseEntered(event -> labelOpenDestinationFolder.textFillProperty().setValue(Color.BLANCHEDALMOND));
        labelOpenDestinationFolder.setOnMouseExited(event -> labelOpenDestinationFolder.textFillProperty().setValue(Color.BLACK));
        labelOpenDestinationFolder.setOnMouseClicked(event -> {
            ExportTask task = DrawingBotV3.INSTANCE.taskMonitor.getDisplayedExportTask();
            if(task != null){
                FXHelper.openFolder(task.saveLocation.getParentFile());
            }
        });
        labelOpenDestinationFolder.visibleProperty().bind(Bindings.createBooleanBinding(() -> DrawingBotV3.INSTANCE.taskMonitor.wasExporting.get() || DrawingBotV3.INSTANCE.taskMonitor.isExporting.get(), DrawingBotV3.INSTANCE.taskMonitor.isExporting, DrawingBotV3.INSTANCE.taskMonitor.wasExporting));
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PRE PROCESSING PANE




    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PEN SETTINGS


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////VERSION CONTROL

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////BATCH PROCESSING


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void showPremiumFeatureDialog(){
        DialogPremiumFeature premiumFeature = new DialogPremiumFeature();

        Optional<Boolean> upgrade = premiumFeature.showAndWait();
        if(upgrade.isPresent() && upgrade.get()){
            FXHelper.openURL(DBConstants.URL_UPGRADE);
        }
    }

    //// PRESET MENU BUTTON \\\\

    public static class DummyController {

        public void initialize(){
            ///NOP
        }

    }


}
