package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.Hooks;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.UpdateChecker;
import drawingbot.files.json.JsonData;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.integrations.vpype.FXVPypeController;
import drawingbot.integrations.vpype.VpypeHelper;
import drawingbot.javafx.controllers.*;
import drawingbot.javafx.controls.*;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.preferences.FXPreferences;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.MasterRegistry;
import drawingbot.render.IDisplayMode;
import drawingbot.render.overlays.NotificationOverlays;
import drawingbot.render.shapes.JFXShapeManager;
import drawingbot.utils.DBConstants;
import drawingbot.utils.EnumFilterTypes;
import drawingbot.utils.Utils;
import drawingbot.utils.flags.Flags;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
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
            initGlobals();
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
        //FXHelper.makePersistent(viewportScrollPane);

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

    public Stage vpypeSettingsStage;
    public FXVPypeController vpypeController;


    public Stage taskMonitorStage;
    public FXTaskMonitorController taskMonitorController;

    public Stage projectManagerStage;
    public FXProjectManagerController projectManagerController;

    public Stage preferencesStage;
    public FXPreferences preferencesController;

    public Stage documentationStage;
    public FXDocumentation documentationController;

    public void initSeparateStages() {
        vpypeController = FXHelper.initSeparateStage("/drawingbot/javafx/vpypesettings.fxml", vpypeSettingsStage = new Stage(), "vpype Settings", Modality.APPLICATION_MODAL);
        taskMonitorController = FXHelper.initSeparateStage("/drawingbot/javafx/taskmonitor.fxml", taskMonitorStage = new Stage(), "Task Monitor", Modality.NONE);
        projectManagerController = FXHelper.initSeparateStage("/drawingbot/javafx/projectmanager.fxml", projectManagerStage = new Stage(), "Project Manager", Modality.NONE);
        preferencesController = FXHelper.initSeparateStage("/drawingbot/javafx/preferences.fxml", preferencesStage = new Stage(), "Preferences", Modality.APPLICATION_MODAL);
        documentationController = FXHelper.initSeparateStage("/drawingbot/javafx/documentation.fxml", documentationStage = new Stage(), "Documentation", Modality.NONE);

        documentationStage.setResizable(true);

        FXHelper.initSeparateStageWithController("/drawingbot/javafx/serialportsettings.fxml", (Stage) Hooks.runHook(Hooks.SERIAL_CONNECTION_STAGE, new Stage())[0], Hooks.runHook(Hooks.SERIAL_CONNECTION_CONTROLLER, new DummyController())[0], "Plotter / Serial Port Connection", Modality.NONE);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////GLOBAL CONTAINERS
    public VBox vBoxMain = null;
    public SplitPane splitPane = null;

    public VBox vBoxLeftContainer;
    public ScrollPane scrollPaneSettingsLeft = null;
    public VBox vBoxSettingsLeft = null;

    public VBox vBoxRightContainer;
    public ScrollPane scrollPaneSettingsRight = null;
    public VBox vBoxSettingsRight = null;

    public void initGlobals(){
        FXHelper.makePersistent(splitPane);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// TOOL BAR

    public TabPane tabPaneProjects = null;

    public MenuBar menuBar = null;
    public Menu menuFile = null;
    public Menu menuView = null;
    public Menu menuFilters = null;
    public Menu menuHelp = null;
    public static ArrayList<Pane> parentPanes = new ArrayList<>();
    public static ArrayList<TitledPane> allPanes = new ArrayList<>();
    public Rectangle hiddenDragRectangleRight = null;
    public Rectangle hiddenDragRectangleLeft = null;

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
        menuOpen.setOnAction(e -> FXHelper.importProject(DrawingBotV3.context()));
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
        menuImport.setOnAction(e -> FXHelper.importImageFile(DrawingBotV3.context()));
        menuImport.setAccelerator(KeyCombination.valueOf("Ctrl + I"));
        menuFile.getItems().add(menuImport);

        MenuItem menuVideo = new MenuItem("Import Video");
        menuVideo.setOnAction(e -> FXHelper.importVideoFile(DrawingBotV3.context()));
        menuFile.getItems().add(menuVideo);

        if(FXApplication.isPremiumEnabled) {
            MenuItem menuSVG = new MenuItem("Import SVG");
            menuSVG.setOnAction(e -> FXHelper.importSVGFile(DrawingBotV3.context()));
            menuFile.getItems().add(menuSVG);
        }else{
            MenuItem menuSVG = new MenuItem("Import SVG " + "(Premium)");
            menuSVG.setOnAction(e -> showPremiumFeatureDialog());
            menuFile.getItems().add(menuSVG);
        }

        menuFile.getItems().add(new SeparatorMenuItem());

        MenuItem menuQuickExport = new MenuItem("Quick Export");
        menuQuickExport.textProperty().bind(Bindings.createStringBinding(() -> "Quick Export: " + DBPreferences.INSTANCE.getQuickExportHandler() + " - " + DBPreferences.INSTANCE.quickExportMode.get().getDisplayName(), DBPreferences.INSTANCE.quickExportMode, DBPreferences.INSTANCE.quickExportHandler));
        menuQuickExport.setOnAction(e -> FXHelper.exportFile(DrawingBotV3.context(), DBPreferences.INSTANCE.getQuickExportHandler(), DBPreferences.INSTANCE.quickExportMode.get()));
        menuQuickExport.disableProperty().bind(Bindings.isNull(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(project -> project.currentDrawing)));
        menuQuickExport.setAccelerator(KeyCombination.valueOf("Ctrl + E"));
        menuFile.getItems().add(menuQuickExport);

        menuFile.getItems().add(new SeparatorMenuItem());

        for(ExportTask.Mode exportMode : ExportTask.Mode.values()){
            Menu menuExport = new Menu("Export " + exportMode.getDisplayName());
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
                        item.setOnAction(e -> FXHelper.exportFile(DrawingBotV3.context(), format, exportMode));
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
        allPanes = new ArrayList<>();

        hiddenDragRectangleLeft = createHiddenDragOverlay(splitPane, HPos.LEFT);
        vBoxMain.getChildren().add(hiddenDragRectangleLeft);
        vBoxSettingsLeft.getChildren().addListener((InvalidationListener) observable -> {
            hiddenDragRectangleLeft.setVisible(vBoxSettingsLeft.getChildren().isEmpty());
        });
        setDefaultDragEvents(hiddenDragRectangleLeft);

        hiddenDragRectangleRight = createHiddenDragOverlay(splitPane, HPos.RIGHT);
        vBoxMain.getChildren().add(hiddenDragRectangleRight);
        vBoxSettingsRight.getChildren().addListener((InvalidationListener) observable -> {
            hiddenDragRectangleRight.setVisible(vBoxSettingsRight.getChildren().isEmpty());
        });
        setDefaultDragEvents(hiddenDragRectangleRight);

        for(Node node : vBoxSettingsLeft.getChildren()){
            if(node instanceof TitledPane){
                allPanes.add((TitledPane) node);
            }
        }
        parentPanes.add(vBoxSettingsLeft);
        setDefaultDragEvents(scrollPaneSettingsLeft);

        for(Node node : vBoxSettingsRight.getChildren()){
            if(node instanceof TitledPane){
                allPanes.add((TitledPane) node);
            }
        }
        parentPanes.add(vBoxSettingsRight);
        setDefaultDragEvents(scrollPaneSettingsRight);

        vBoxSettingsRight.getChildren().addListener((InvalidationListener) observable -> {
            if(vBoxSettingsRight.getChildren().isEmpty()){
                scrollPaneSettingsRight.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPaneSettingsRight.setMaxWidth(0);
                scrollPaneSettingsRight.setPrefWidth(0);
                vBoxRightContainer.setMaxWidth(0);
                splitPane.setDividerPositions(0, 1);
            }else{
                scrollPaneSettingsRight.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                scrollPaneSettingsRight.setMaxWidth(-1);
                scrollPaneSettingsRight.setPrefWidth(420);
                vBoxRightContainer.setMaxWidth(-1);
                splitPane.setDividerPositions(0, 1);
            }
        });

        vBoxSettingsLeft.getChildren().addListener((InvalidationListener) observable -> {
            if(vBoxSettingsLeft.getChildren().isEmpty()){
                scrollPaneSettingsLeft.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPaneSettingsLeft.setMaxWidth(0);
                scrollPaneSettingsLeft.setPrefWidth(0);
                vBoxLeftContainer.setMaxWidth(0);
                splitPane.setDividerPositions(1, 1);
            }else{
                scrollPaneSettingsLeft.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                scrollPaneSettingsLeft.setMaxWidth(-1);
                scrollPaneSettingsLeft.setPrefWidth(420);
                vBoxLeftContainer.setMaxWidth(-1);
                splitPane.setDividerPositions(0, 1);
            }
        });


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

            pane.pseudoClassStateChanged(PSEUDO_CLASS_DRAG_TITLE_PANE_OVER, false);
            pane.setOnDragDetected(onDragDetected(pane));
            setDefaultDragEvents(pane);
        }

        menuView.getItems().add(new SeparatorMenuItem());

        MenuItem expandAll = new MenuItem("Expand All");
        expandAll.setOnAction(e -> allPanes.forEach(pane -> pane.setExpanded(true)));
        menuView.getItems().add(expandAll);

        MenuItem closeAll = new MenuItem("Close All");
        closeAll.setOnAction(e -> allPanes.forEach(pane -> pane.setExpanded(false)));
        menuView.getItems().add(closeAll);

        menuView.getItems().add(new SeparatorMenuItem());

        MenuItem resetUI = new MenuItem("Reset UI");
        resetUI.setOnAction(e -> {
            FXHelper.loadDefaultUIStates();
            allPanes.forEach(this::redockSettingsPane);
        });
        menuView.getItems().add(resetUI);

        //filters
        for(Map.Entry<EnumFilterTypes, ObservableList<GenericFactory<BufferedImageOp>>> entry : MasterRegistry.INSTANCE.imgFilterFactories.entrySet()){
            Menu type = new Menu(entry.getKey().toString());

            for(GenericFactory<BufferedImageOp> factory : entry.getValue()){
                MenuItem item = new MenuItem(factory.getRegistryName());
                item.setOnAction(e -> FXHelper.addImageFilter(factory, DrawingBotV3.project().imageSettings.get()));
                type.getItems().add(item);
            }

            menuFilters.getItems().add(type);
        }

        //help
        if(!FXApplication.isPremiumEnabled){
            MenuItem upgrade = new MenuItem("Upgrade");
            upgrade.setOnAction(e -> FXHelper.openURL(DBConstants.URL_UPGRADE));
            upgrade.setGraphic(fontAwesome.create(FontAwesome.Glyph.ARROW_UP).color(Color.SLATEGRAY));
            menuHelp.getItems().add(upgrade);
            menuHelp.getItems().add(new SeparatorMenuItem());
        }

        MenuItem documentation = new MenuItem("Open Documentation");
        documentation.setOnAction(e -> FXDocumentation.navigate(DBConstants.URL_READ_THE_DOCS_HOME));
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

        menuHelp.getItems().add(new SeparatorMenuItem());

        MenuItem checkForUpdates = new MenuItem("Check For Updates");
        checkForUpdates.setOnAction(e -> UpdateChecker.INSTANCE.requestLatestUpdate());
        checkForUpdates.setGraphic(fontAwesome.create(FontAwesome.Glyph.BELL).color(Color.SLATEGRAY));
        menuHelp.getItems().add(checkForUpdates);

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


        NotificationOverlays.INSTANCE.setTarget(vBoxMain);

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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// VIEWPORT PANE

    ////VIEWPORT WINDOW
    public VBox vBoxViewportContainer = null;
    public NotificationPane notificationPane = null;

    public VBox viewport = null;
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

        setDefaultDragEvents(vBoxViewportContainer);

        ////VIEWPORT SETTINGS
        rangeSliderDisplayedLines.highValueProperty().addListener((observable, oldValue, newValue) -> {
            PlottedDrawing drawing = DrawingBotV3.project().getDisplayedDrawing();
            if(drawing != null){
                int lines = (int)Utils.mapDouble(newValue.doubleValue(), 0, 1, 0, drawing.getGeometryCount());
                drawing.displayedShapeMax = lines;
                textFieldDisplayedShapesMax.setText(String.valueOf(lines));
                DrawingBotV3.project().updatePenDistribution();
            }
        });

        rangeSliderDisplayedLines.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            PlottedDrawing drawing = DrawingBotV3.project().getDisplayedDrawing();
            if(drawing != null){
                int lines = (int)Utils.mapDouble(newValue.doubleValue(), 0, 1, 0, drawing.getGeometryCount());
                drawing.displayedShapeMin = lines;
                textFieldDisplayedShapesMin.setText(String.valueOf(lines));
                DrawingBotV3.project().updatePenDistribution();
            }
        });

        textFieldDisplayedShapesMax.setOnAction(e -> {
            PlottedDrawing drawing = DrawingBotV3.project().getDisplayedDrawing();
            if(drawing != null){
                int lines = (int)Math.max(0, Math.min(drawing.getGeometryCount(), Double.parseDouble(textFieldDisplayedShapesMax.getText())));
                drawing.displayedShapeMax = lines;
                textFieldDisplayedShapesMax.setText(String.valueOf(lines));
                rangeSliderDisplayedLines.setHighValue((double)lines / drawing.getGeometryCount());
                DrawingBotV3.project().updatePenDistribution();
            }
        });

        textFieldDisplayedShapesMin.setOnAction(e -> {
            PlottedDrawing drawing = DrawingBotV3.project().getDisplayedDrawing();
            if(drawing != null){
                int lines = (int)Math.max(0, Math.min(drawing.getGeometryCount(), Double.parseDouble(textFieldDisplayedShapesMin.getText())));
                drawing.displayedShapeMin = lines;
                textFieldDisplayedShapesMin.setText(String.valueOf(lines));
                rangeSliderDisplayedLines.setLowValue((double)lines / drawing.getGeometryCount());
                DrawingBotV3.project().updatePenDistribution();
            }
        });
        EasyBind.select(DrawingBotV3.INSTANCE.activeProject).selectObject(project -> project.displayedDrawing).addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                Platform.runLater(() -> {
                    rangeSliderDisplayedLines.setLowValue(0.0F);
                    rangeSliderDisplayedLines.setHighValue(1.0F);
                    textFieldDisplayedShapesMin.setText(String.valueOf(0));
                    textFieldDisplayedShapesMax.setText(String.valueOf(newValue.getGeometryCount()));
                });
            }else{
                rangeSliderDisplayedLines.setLowValue(0.0F);
                rangeSliderDisplayedLines.setHighValue(1.0F);
                textFieldDisplayedShapesMin.setText(String.valueOf(0));
                textFieldDisplayedShapesMax.setText(String.valueOf(0));
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
        viewportScrollPane.setId("viewportScrollPane");
        viewportScrollPane.scale.addListener((observable, oldValue, newValue) -> DrawingBotV3.project().setRenderFlag(Flags.CANVAS_MOVED));
        viewportScrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> DrawingBotV3.project().setRenderFlag(Flags.CANVAS_MOVED));
        viewportScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> DrawingBotV3.project().setRenderFlag(Flags.CANVAS_MOVED));
        viewportScrollPane.widthProperty().addListener((observable, oldValue, newValue) -> DrawingBotV3.project().setRenderFlag(Flags.CANVAS_MOVED));
        viewportScrollPane.heightProperty().addListener((observable, oldValue, newValue) -> DrawingBotV3.project().setRenderFlag(Flags.CANVAS_MOVED));

        viewportScrollPane.contentProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                ObservableObjectValue<Color> backgroundColor = EasyBind.select(DrawingBotV3.INSTANCE.activeProject).select(p -> p.drawingArea).selectObject(d -> d.backgroundColor);
                newValue.styleProperty().unbind();
                newValue.styleProperty().bind(Bindings.createStringBinding(() -> {
                    Color c = backgroundColor.get();

                    return "-fx-background-color: rgb(%s,%s,%s,%s)".formatted((int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255), c.getOpacity());
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

        //Wrap the viewport & overlay
        viewport = new VBox();
        VBox.setVgrow(viewport, Priority.ALWAYS);
        HBox.setHgrow(viewport, Priority.ALWAYS);

        viewport.getChildren().add(viewportScrollPane);
        viewport.getChildren().add(viewportOverlayAnchorPane);
        vBoxViewportContainer.getChildren().add(viewport);

        viewportScrollPane.setOnDragEntered(onDragEntered(viewportScrollPane));
        viewportScrollPane.setOnDragExited(onDragExited(viewportScrollPane));
        viewportScrollPane.setOnDragDone(onDragDone(viewportScrollPane));

        EventHandler<? super DragEvent> onPaneOver = onDragOver(viewportScrollPane);
        viewportScrollPane.setOnDragOver(event -> {

            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.LINK);
            }else{
                //divert to the pane event if we didn't find any files
                onPaneOver.handle(event);
                return;
            }
            event.consume();
        });

        EventHandler<? super DragEvent> onPaneDropped = onDragDropped(viewportScrollPane);
        viewportScrollPane.setOnDragDropped(event -> {

            Dragboard db = event.getDragboard();
            boolean success = false;
            if(db.hasFiles()){
                List<File> files = db.getFiles();
                DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), files.get(0), false, false);
                success = true;
            }

            if(!success){
                //divert to the pane event if we didn't find any files
                onPaneDropped.handle(event);
                return;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        labelElapsedTime.textProperty().bind(Bindings.createStringBinding(() -> {
            long minutes = (DrawingBotV3.INSTANCE.elapsedTimeMS.get() / 1000) / 60;
            long seconds = (DrawingBotV3.INSTANCE.elapsedTimeMS.get() / 1000) % 60;
            return minutes + " m " + seconds + " s";
        }, DrawingBotV3.INSTANCE.elapsedTimeMS));
        labelPlottedShapes.textProperty().bind(Bindings.createStringBinding(() -> Utils.defaultNF.format(DrawingBotV3.INSTANCE.geometryCount.get()), DrawingBotV3.INSTANCE.geometryCount));
        labelPlottedVertices.textProperty().bind(Bindings.createStringBinding(() -> Utils.defaultNF.format(DrawingBotV3.INSTANCE.vertexCount.get()), DrawingBotV3.INSTANCE.vertexCount));
        labelImageResolution.textProperty().bind(Bindings.createStringBinding(() -> DrawingBotV3.INSTANCE.imageResolutionWidth.getValue().intValue() + " x " + DrawingBotV3.INSTANCE.imageResolutionHeight.getValue().intValue() + " " + DrawingBotV3.INSTANCE.imageResolutionUnits.get().getSuffix(), DrawingBotV3.INSTANCE.imageResolutionWidth, DrawingBotV3.INSTANCE.imageResolutionHeight, DrawingBotV3.INSTANCE.imageResolutionUnits));
        labelPlottingResolution.textProperty().bind(Bindings.createStringBinding(() -> DrawingBotV3.INSTANCE.plottingResolutionWidth.getValue().intValue() + " x " + DrawingBotV3.INSTANCE.plottingResolutionHeight.getValue().intValue(), DrawingBotV3.INSTANCE.plottingResolutionWidth, DrawingBotV3.INSTANCE.plottingResolutionHeight));
        labelCurrentPosition.textProperty().bind(Bindings.createStringBinding(() -> DrawingBotV3.INSTANCE.relativeMousePosX.getValue() + ", " + DrawingBotV3.INSTANCE.relativeMousePosY.getValue() + " " + DrawingBotV3.INSTANCE.relativeMouseUnits.get().getSuffix(), DrawingBotV3.INSTANCE.relativeMousePosX, DrawingBotV3.INSTANCE.relativeMousePosY, DrawingBotV3.INSTANCE.relativeMouseUnits));

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

    public Pane panePlottingTools;
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


        vBoxSettingsLeft.getChildren().addListener((InvalidationListener) observable -> {
            if(vBoxSettingsLeft.getChildren().isEmpty() && panePlottingTools.getParent() != vBoxRightContainer){
                vBoxRightContainer.getChildren().add(panePlottingTools);
            }else if(panePlottingTools.getParent() != vBoxLeftContainer){
                vBoxLeftContainer.getChildren().add(panePlottingTools);
            }
        });

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ////PROGRESS BAR PANE

    public Pane paneProgressBar = null;
    public ProgressBar progressBarGeneral = null;
    public Label progressBarLabel = null;
    public Label labelCancelExport = null;
    public Label labelOpenDestinationFolder = null;

    public void initProgressBar(){
        progressBarLabel.setText("");

        progressBarGeneral.progressProperty().bind(DrawingBotV3.INSTANCE.taskMonitor.progressProperty);
        progressBarLabel.textProperty().bind(Bindings.createStringBinding(() -> DrawingBotV3.INSTANCE.taskMonitor.getCurrentTaskStatus(), DrawingBotV3.INSTANCE.taskMonitor.messageProperty, DrawingBotV3.INSTANCE.taskMonitor.titleProperty, DrawingBotV3.INSTANCE.taskMonitor.exceptionProperty));

        labelCancelExport.setOnMouseEntered(event -> labelCancelExport.textFillProperty().setValue(Color.BLANCHEDALMOND));
        labelCancelExport.setOnMouseExited(event -> labelCancelExport.textFillProperty().setValue(Color.BLACK));
        labelCancelExport.setOnMouseClicked(event -> {
            Task<?> task = DrawingBotV3.INSTANCE.taskMonitor.currentTask;
            if(task instanceof ExportTask){
                task.cancel();
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

    //// TITLED PANE - DRAG HANDLING \\\\

    public static final DataFormat DATA_FORMAT_DRAG_TITLE_PANE = new DataFormat("application/dbv3-drag");
    private static final PseudoClass PSEUDO_CLASS_DRAG_TITLE_PANE_OVER = PseudoClass.getPseudoClass("drag-over");
    private static final BooleanProperty isDragging = new SimpleBooleanProperty(false);

    /**
     * All events except on drag detected, i.e. allows receiving nodes but not dragging
     */
    public void setDefaultDragEvents(Node target){
        target.setOnDragEntered(onDragEntered(target));
        target.setOnDragOver(onDragOver(target));
        target.setOnDragExited(onDragExited(target));
        target.setOnDragDropped(onDragDropped(target));
        target.setOnDragDone(onDragDone(target));
    }

    public EventHandler<? super MouseEvent> onDragDetected(Node target){
        return event -> {
            Dragboard db = target.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();
            content.put(DATA_FORMAT_DRAG_TITLE_PANE, target.getId());
            db.setContent(content);

            isDragging.set(true);
        };
    }

    public EventHandler<? super DragEvent> onDragOver(Node target){
        return event -> {
            if (event.getGestureSource() != target && event.getDragboard().hasContent(DATA_FORMAT_DRAG_TITLE_PANE)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        };
    }

    public EventHandler<? super DragEvent> onDragEntered(Node target){
        return event -> {
            if (event.getGestureSource() != target && event.getDragboard().hasContent(DATA_FORMAT_DRAG_TITLE_PANE)) {
                target.pseudoClassStateChanged(PSEUDO_CLASS_DRAG_TITLE_PANE_OVER, true);
            }
            event.consume();
        };
    }

    public EventHandler<? super DragEvent> onDragExited(Node target){
        return event -> {
            if (event.getGestureSource() != target && event.getDragboard().hasContent(DATA_FORMAT_DRAG_TITLE_PANE)) {
                target.pseudoClassStateChanged(PSEUDO_CLASS_DRAG_TITLE_PANE_OVER, false);
            }
            event.consume();
        };
    }

    public EventHandler<? super DragEvent> onDragDropped(Node target){
        return event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(DATA_FORMAT_DRAG_TITLE_PANE)) {
                String sourceID = (String) db.getContent(DATA_FORMAT_DRAG_TITLE_PANE);
                TitledPane source = null;
                for(TitledPane titledPane : allPanes){
                    if(titledPane.getId().equals(sourceID)){
                        source = titledPane;
                    }
                }
                if(source != null){
                    if(target instanceof TitledPane){
                        if(target.getParent() instanceof VBox && source.getParent() instanceof VBox){
                            VBox srcParent = (VBox) source.getParent();
                            VBox dstParent = (VBox) target.getParent();
                            if(srcParent == dstParent){
                                int targetIndex = srcParent.getChildren().indexOf(target);
                                int currentIndex = srcParent.getChildren().indexOf(source);
                                srcParent.getChildren().remove(source);
                                srcParent.getChildren().add(targetIndex > currentIndex ? targetIndex : targetIndex, source);
                            }else{
                                srcParent.getChildren().remove(source);
                                dstParent.getChildren().add(dstParent.getChildren().indexOf(target), source);
                            }
                            success = true;
                        }
                    }else if(target == scrollPaneSettingsLeft || target == scrollPaneSettingsRight){
                        if(source.getParent() instanceof VBox){
                            VBox srcParent = (VBox) source.getParent();
                            VBox dstParent = (target == scrollPaneSettingsLeft ? vBoxSettingsLeft : vBoxSettingsRight);
                            srcParent.getChildren().remove(source);
                            dstParent.getChildren().add(source);
                            success = true;
                        }
                    }else if(target == viewportScrollPane || target == vBoxViewportContainer){
                        undockTitlePane(source);
                        success = true;
                    }else if(target == hiddenDragRectangleRight || target == hiddenDragRectangleLeft){
                        VBox srcParent = (VBox) source.getParent();
                        VBox dstParent = target == hiddenDragRectangleRight ? vBoxSettingsRight : vBoxSettingsLeft;
                        srcParent.getChildren().remove(source);
                        dstParent.getChildren().add(source);
                        success = true;
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        };
    }

    public EventHandler<? super DragEvent> onDragDone(Node target){
        return event -> {
            isDragging.set(false);
        };
    }

    public Map<TitledPane, Stage> settingsStages = new LinkedHashMap<>();
    public Map<TitledPane, Node> settingsContent = new LinkedHashMap<>();

    public void undockTitlePane(TitledPane pane){
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
            FXApplication.applyTheme(settingsStage);
            settingsStage.show();
        }else{
            redockSettingsPane(pane);
            currentStage.close();
        }
    }

    public void redockSettingsPane(TitledPane pane){
        Stage currentStage = settingsStages.get(pane);
        if(currentStage != null){
            Node content = settingsContent.get(pane);
            pane.setContent(content);

            settingsStages.put(pane, null);
            settingsContent.put(pane, null);
            currentStage.close();
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //TODO MAKE THIS INTO A MORE UNIVERSAL METHOD, i.e. not just a right hand rectangle
    public static Rectangle createHiddenDragOverlay(Region target, HPos pos){
        Rectangle rectangle = new Rectangle();
        rectangle.setManaged(false);
        rectangle.setWidth(60);
        switch (pos){
            case LEFT -> {
                rectangle.heightProperty().bind(target.heightProperty());
                rectangle.xProperty().bind(target.layoutXProperty());
                rectangle.yProperty().bind(target.layoutYProperty());
                rectangle.getStyleClass().add("drag-receiver-left");
            }
            case CENTER -> {
                //NOP
            }
            case RIGHT -> {
                rectangle.heightProperty().bind(target.heightProperty());
                rectangle.xProperty().bind(target.layoutXProperty().subtract(rectangle.widthProperty()).add(target.widthProperty()));
                rectangle.yProperty().bind(target.layoutYProperty());
                rectangle.getStyleClass().add("drag-receiver-right");
            }
        }

        rectangle.setFill(Color.TRANSPARENT);
        rectangle.mouseTransparentProperty().bind(isDragging.not());
        return rectangle;
    }

    public static void showPremiumFeatureDialog(){
        DialogPremiumFeature premiumFeature = new DialogPremiumFeature();

        Optional<Boolean> upgrade = premiumFeature.showAndWait();
        if(upgrade.isPresent() && upgrade.get()){
            FXHelper.openURL(DBConstants.URL_UPGRADE);
        }
    }

    @JsonData
    public static class NodePosition {

        public int childIndex;
        public String parentID;

        public NodePosition(int childIndex, String parentID) {
            this.childIndex = childIndex;
            this.parentID = parentID;
        }
    }

    public static boolean hasPosition(Node pane){
        return allPanes.contains(pane);
    }

    public static NodePosition getPosition(Node pane){
        if(!allPanes.contains(pane) || pane.getParent() == null){
            return null;
        }
        String parentID = pane.getParent().getId();
        int childIndex = pane.getParent().getChildrenUnmodifiable().indexOf(pane);

        return new NodePosition(childIndex, parentID);
    }

    public static void loadPosition(Node pane, NodePosition position){
        if(pane == null || position == null || pane.getParent() == null){
            return;
        }
        Pane srcParent = (Pane) pane.getParent();
        Pane dstParent = getParentPane(position.parentID);

        if(dstParent == null){
            return;
        }

        srcParent.getChildren().remove(pane);
        int targetIndex = Math.max(0, Math.min(position.childIndex, dstParent.getChildren().size()-1));
        dstParent.getChildren().add(targetIndex, pane);
    }

    public static Pane getParentPane(String parentID){
        return parentPanes.stream().filter(pane -> pane.getId().equals(parentID)).findFirst().orElse(null);
    }

    //// PRESET MENU BUTTON \\\\

    public static class DummyController {

        public void initialize(){
            ///NOP
        }

    }


}
