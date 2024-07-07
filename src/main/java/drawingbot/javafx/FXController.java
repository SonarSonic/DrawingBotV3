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
import drawingbot.javafx.controllers.*;
import drawingbot.javafx.controls.ContextMenuObservableProject;
import drawingbot.javafx.controls.DialogPremiumFeature;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.preferences.FXPreferences;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.registry.Register;
import drawingbot.render.modes.DisplayModeBase;
import drawingbot.render.overlays.*;
import drawingbot.render.viewport.Viewport;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.DBConstants;
import drawingbot.utils.EnumFilterTypes;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.skin.TitledPaneSkin;
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

    public FXDrawingArea drawingAreaController;
    public FXImageProcessing imageFiltersController;
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

        JFXUtils.subscribeListener(DrawingBotV3.INSTANCE.activeProject, (observable, oldValue, newValue) -> {
            if(oldValue != null){
                drawingAreaController.drawingArea.unbind();
                imageFiltersController.settings.unbind();
                imageFiltersController.image.unbind();
                pfmSettingsController.pfmSettings.unbind();
                drawingSetsController.drawingSets.unbind();
                versionControlController.versionControl.unbind();

                newValue.selectedPens.set(FXCollections.observableArrayList());
            }

            if(newValue != null){
                drawingAreaController.drawingArea.bind(newValue.drawingArea);
                imageFiltersController.settings.bind(newValue.imageSettings);
                imageFiltersController.image.bind(newValue.openImage);
                pfmSettingsController.pfmSettings.bind(newValue.pfmSettings);
                drawingSetsController.drawingSets.bind(newValue.drawingSets);
                versionControlController.versionControl.bind(newValue.versionControl);

                newValue.selectedPens.set(drawingSetsController.controlDrawingSetEditor.penTableView.getSelectionModel().getSelectedItems());
            }

        });
    }


    public Stage taskMonitorStage;
    public FXTaskMonitorController taskMonitorController;

    public Stage projectManagerStage;
    public FXProjectManagerController projectManagerController;

    public Stage preferencesStage;
    public FXPreferences preferencesController;

    public Stage documentationStage;
    public FXDocumentation documentationController;

    public Stage presetManagerStage;
    public FXPresetManager presetManagerController;

    public void initSeparateStages() {
        taskMonitorController = FXHelper.initSeparateStage("/drawingbot/javafx/taskmonitor.fxml", taskMonitorStage = new Stage(), "Task Monitor", Modality.NONE);
        projectManagerController = FXHelper.initSeparateStage("/drawingbot/javafx/projectmanager.fxml", projectManagerStage = new Stage(), "Project Manager", Modality.NONE);
        preferencesController = FXHelper.initSeparateStage("/drawingbot/javafx/preferences.fxml", preferencesStage = new Stage(), "Preferences", Modality.APPLICATION_MODAL);
        presetManagerController = FXHelper.initSeparateStage("/drawingbot/javafx/presetmanager.fxml", presetManagerStage = new Stage(), "Preset Manager", Modality.APPLICATION_MODAL);
    }

    //Lazy Load the documentation window to prevent crashes on boot with MacOS High Sierra: https://bugs.openjdk.org/browse/JDK-8305197
    private boolean initDocumentationStage;

    public boolean isDocumentationAvailable(){
        return initDocumentationStage;
    }

    public void initDocumentationStage(){
        if(initDocumentationStage) {
            return;
        }
        if(Utils.getOS().isMac() && Utils.compareVersion(System.getProperty("os.version"), "11", 1) < 0){
            DrawingBotV3.logger.warning("Documentation Integration: Disabled on " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
            return;
        }

        documentationController = FXHelper.initSeparateStage("/drawingbot/javafx/documentation.fxml", documentationStage = new Stage(), "Documentation", Modality.NONE);
        documentationStage.setResizable(true);
        initDocumentationStage = true;
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
        //FXHelper.makePersistent(splitPane); //TODO FIXME, breaks initial sizing with new viewport wrapper
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
                        MenuItem item = new MenuItem(format.description + " " + format.extensionString + " (Premium)");
                        item.setOnAction(e -> showPremiumFeatureDialog());
                        menuExport.getItems().add(item);
                    }else{
                        MenuItem item = new MenuItem(format.description + " " + format.extensionString);
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

        // Load special file menu options via Hooks
        Hooks.runHook(Hooks.FILE_MENU, menuFile);

        menuFile.getItems().add(new SeparatorMenuItem());

        MenuItem menuPreferences = new MenuItem("Preferences");
        menuPreferences.setOnAction(e -> {
            preferencesStage.show();
        });
        menuFile.getItems().add(menuPreferences);

        menuFile.getItems().add(new SeparatorMenuItem());

        MenuItem menuPresetManager = new MenuItem("Preset Manager");
        menuPresetManager.setOnAction(e -> {
            presetManagerStage.show();
        });
        menuFile.getItems().add(menuPresetManager);

        menuFile.getItems().add(new SeparatorMenuItem());

        MenuItem taskMonitor = new MenuItem("Open Task Monitor");
        taskMonitor.setOnAction(e -> taskMonitorStage.show());
        menuFile.getItems().add(taskMonitor);

        menuFile.getItems().add(new SeparatorMenuItem());

        MenuItem menuCloseProject = new MenuItem("Close Project");
        menuCloseProject.setOnAction(e -> {
            FXHelper.closeProject(DrawingBotV3.project(), p -> {});
        });
        menuFile.getItems().add(menuCloseProject);

        MenuItem menuCloseAllProjectS = new MenuItem("Close All Projects");
        menuCloseAllProjectS.setOnAction(e -> {
            FXHelper.saveAndCloseAllProjects(false);
        });
        menuFile.getItems().add(menuCloseAllProjectS);

        menuFile.getItems().add(new SeparatorMenuItem());

        MenuItem menuQuit = new MenuItem("Quit");
        menuQuit.setOnAction(e -> FXHelper.exit());
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
                scrollPaneSettingsRight.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPaneSettingsRight.setMaxWidth(-1);
                scrollPaneSettingsRight.setPrefWidth(-1);
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
                scrollPaneSettingsLeft.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPaneSettingsLeft.setMaxWidth(-1);
                scrollPaneSettingsLeft.setPrefWidth(-1);
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
                Platform.runLater(() -> allPanes.forEach(p -> {
                    if(p.getParent() == pane.getParent()){
                        p.expandedProperty().setValue(p == pane);
                    }
                }));
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

        MenuItem exportCrashReports = new MenuItem("Export Logs/Crash Reports");
        exportCrashReports.setOnAction(e -> FXHelper.exportLogFiles());
        exportCrashReports.setGraphic(fontAwesome.create(FontAwesome.Glyph.FILE_ZIP_ALT).color(Color.SLATEGRAY));
        menuHelp.getItems().add(exportCrashReports);

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
            if(newValue != null){
                ObservableProject project = (ObservableProject) newValue.getProperties().get(ObservableProject.class);
                DrawingBotV3.INSTANCE.activeProject.set(project);
            }
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

        tab.setOnCloseRequest(e -> {
            if(!FXHelper.closeProject(project, response -> {})){
                e.consume();
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
    public Viewport viewport = null;
    public VBox vBoxViewportContainer = null;
    public NotificationPane notificationPane = null;

    public RulerOverlays rulerOverlays = null;
    public BorderOverlays borderOverlays = null;
    public ExportedStatsOverlays exportStatsOverlays = null;
    public NotificationOverlays notificationOverlays = null;
    public ScaledNodeOverlays shapeOverlaysTest = null;

    ////VIEWPORT SETTINGS
    public RangeSlider rangeSliderDisplayedLines = null;
    public TextField textFieldDisplayedShapesMin = null;
    public TextField textFieldDisplayedShapesMax = null;

    public CheckBox checkBoxApplyToExport = null;

    public ChoiceBox<DisplayModeBase> choiceBoxDisplayMode = null;
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

        choiceBoxDisplayMode.getItems().addAll(MasterRegistry.INSTANCE.displayModes.filtered(d->!d.isHidden()));

        comboBoxBlendMode.setItems(FXCollections.observableArrayList(EnumBlendMode.ACTIVE_MODES));
        buttonResetView.setOnAction(e -> DrawingBotV3.INSTANCE.resetView());

        viewport = new Viewport();
        viewport.viewportBackgroundColorProperty().bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).select(p -> p.drawingArea).selectObject(d -> d.backgroundColor));
        viewport.canvasColorProperty().bind(EasyBind.select(DrawingBotV3.INSTANCE.activeProject).select(p -> p.drawingArea).selectObject(d -> d.canvasColor));

        //Register mouse handlers for the colour picker
        viewport.addEventFilter(MouseEvent.MOUSE_MOVED, this::onMouseMovedColourPicker);
        viewport.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onMousePressedColourPicker);
        viewport.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressedColourPicker);

        //Create bindings for the mouses position in the viewport
        DrawingBotV3.INSTANCE.relativeMousePosX.bind(viewport.relativeMouseXProperty());
        DrawingBotV3.INSTANCE.relativeMousePosY.bind(viewport.relativeMouseYProperty());
        DrawingBotV3.INSTANCE.relativeMouseUnits.bind(viewport.canvasUnitsProperty());

        vBoxViewportContainer.getChildren().add(viewport);

        rulerOverlays = new RulerOverlays();
        rulerOverlays.enabledProperty().bindBidirectional(DBPreferences.INSTANCE.rulersEnabled.asBooleanProperty());
        viewport.getViewportOverlays().add(rulerOverlays);

        borderOverlays = new BorderOverlays();
        borderOverlays.enabledProperty().bindBidirectional(DBPreferences.INSTANCE.drawingBordersEnabled.valueProperty());
        borderOverlays.borderColour.bindBidirectional(DBPreferences.INSTANCE.drawingBordersColor.valueProperty());
        viewport.getViewportOverlays().add(borderOverlays);

        exportStatsOverlays = new ExportedStatsOverlays();
        exportStatsOverlays.enabledProperty().bind(viewport.displayModeProperty().isEqualTo(Register.INSTANCE.DISPLAY_MODE_EXPORT_DRAWING));
        viewport.getViewportOverlays().add(exportStatsOverlays);
        notificationOverlays = NotificationOverlays.INSTANCE;
        notificationOverlays.enabledProperty().bindBidirectional(DBPreferences.INSTANCE.notificationsEnabled.asBooleanProperty());
        viewport.getViewportOverlays().add(notificationOverlays);

        viewport.setOnDragEntered(onDragEntered(viewport));
        viewport.setOnDragExited(onDragExited(viewport));
        viewport.setOnDragDone(onDragDone(viewport));

        EventHandler<? super DragEvent> onPaneOver = onDragOver(viewport);
        viewport.setOnDragOver(event -> {

            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.LINK);
            }else{
                //divert to the pane event if we didn't find any files
                onPaneOver.handle(event);
                return;
            }
            event.consume();
        });

        EventHandler<? super DragEvent> onPaneDropped = onDragDropped(viewport);
        viewport.setOnDragDropped(event -> {

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


        JFXUtils.subscribeListener(DrawingBotV3.INSTANCE.activeProject, (observable, oldValue, newValue) -> {
            if(oldValue != null){
                checkBoxApplyToExport.selectedProperty().unbindBidirectional(oldValue.exportRange);
                comboBoxBlendMode.valueProperty().unbindBidirectional(oldValue.blendMode);
                toggleDPIScaling.selectedProperty().unbindBidirectional(oldValue.dpiScaling);
                choiceBoxDisplayMode.valueProperty().unbindBidirectional(oldValue.displayMode);
                exportStatsOverlays.exportEntries.unbindBidirectional(oldValue.exportedDrawings);
                exportStatsOverlays.selectedEntry.unbindBidirectional(oldValue.selectedExportedDrawing);

                viewport.displayModeProperty().unbindBidirectional(oldValue.displayMode);
                viewport.rendererBlendModeProperty().unbindBidirectional(oldValue.blendMode);
                viewport.useDPIScalingProperty().unbindBidirectional(oldValue.dpiScaling);
            }
            if(newValue != null){
                checkBoxApplyToExport.selectedProperty().bindBidirectional(newValue.exportRange);
                comboBoxBlendMode.valueProperty().bindBidirectional(newValue.blendMode);
                toggleDPIScaling.selectedProperty().bindBidirectional(newValue.dpiScaling);
                choiceBoxDisplayMode.valueProperty().bindBidirectional(newValue.displayMode);
                exportStatsOverlays.exportEntries.bindBidirectional(newValue.exportedDrawings);
                exportStatsOverlays.selectedEntry.bindBidirectional(newValue.selectedExportedDrawing);

                viewport.displayModeProperty().bindBidirectional(newValue.displayMode);
                viewport.rendererBlendModeProperty().bindBidirectional(newValue.blendMode);
                viewport.useDPIScalingProperty().bindBidirectional(newValue.dpiScaling);
            }
        });

        labelElapsedTime.textProperty().bind(Bindings.createStringBinding(() -> {
            long millis = DrawingBotV3.INSTANCE.elapsedTimeMS.get();
            long minutes = (millis / 1000) / 60;
            long seconds = (millis / 1000) % 60;
            if(minutes == 0 && seconds == 0){
                return "%s ms".formatted(millis);
            }
            return minutes + " m " + seconds + " s";
        }, DrawingBotV3.INSTANCE.elapsedTimeMS, DrawingBotV3.INSTANCE.taskMonitor.isPlotting));
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
        viewport.setCursor(Cursor.CROSSHAIR);
        colourPickerRectangle.setVisible(true);
        colourPickerRectangle.setFill(pen.javaFXColour.get());
    }

    public void doColourPick(MouseEvent event, boolean update){
        Point2D localPoint = viewport.getParent().sceneToLocal(event.getSceneX(), event.getSceneY());

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setViewport(new Rectangle2D(localPoint.getX(), localPoint.getY(), 1, 1));

        viewport.snapshot((result) -> setPickResult(result, update), parameters, snapshotImage);
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
        viewport.setCursor(Cursor.DEFAULT);
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
                    }else if(target instanceof Viewport || target == vBoxViewportContainer){
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
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            if(allowResizing){
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                VBox.setVgrow(scrollPane, Priority.ALWAYS);
                HBox.setHgrow(scrollPane, Priority.ALWAYS);

                if(pane.getContent() instanceof VBox vBox){
                    scrollPane.setPrefWidth(vBox.getWidth());
                    scrollPane.setPrefHeight(vBox.getHeight()+10);
                }
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
        premiumFeature.initOwner(FXApplication.primaryStage);
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
