package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.files.*;
import drawingbot.drawing.*;
import drawingbot.files.presets.*;
import drawingbot.files.presets.types.*;
import drawingbot.image.ImageFilterRegistry;
import drawingbot.api.IPathFindingModule;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.javafx.controls.*;
import drawingbot.plotting.PlottedPoint;
import drawingbot.pfm.PFMMasterRegistry;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

public class FXController {

    /**
     * starts the FXController, called internally by JavaFX
     */
    public void initialize(){
        DrawingBotV3.logger.entering("FX Controller", "initialize");

        initToolbar();
        initViewport();
        initPlottingControls();
        initProgressBar();
        initDrawingAreaPane();
        initPreProcessingPane();
        initPFMControls();
        initPenSettingsPane();
        initBatchProcessingPane();
        initGCodeSettingsPane();

        viewportStackPane.setOnMousePressed(DrawingBotV3.INSTANCE::mousePressedJavaFX);
        viewportStackPane.setOnMouseDragged(DrawingBotV3.INSTANCE::mouseDraggedJavaFX);
        viewportStackPane.getChildren().add(DrawingBotV3.INSTANCE.canvas);

        viewportStackPane.prefHeightProperty().bind(DrawingBotV3.INSTANCE.canvas.heightProperty().multiply(4));
        viewportStackPane.prefWidthProperty().bind(DrawingBotV3.INSTANCE.canvas.widthProperty().multiply(4));
        viewportScrollPane.setHvalue(0.5);
        viewportScrollPane.setVvalue(0.5);

        DrawingBotV3.logger.exiting("FX Controller", "initialize");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////GLOBAL CONTAINERS
    public ScrollPane scrollPaneSettings = null;
    public VBox vBoxSettings = null;

    public DialogPresetRename presetEditorDialog = new DialogPresetRename();

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// TOOL BAR

    //file
    public MenuItem menuImport = null;
    public MenuItem menuImportURL = null;
    public MenuItem menuExit = null;
    public Menu menuExport = null;
    public Menu menuExportPerPen = null;
    //view
    public Menu menuView = null;
    //help
    public MenuItem menuHelpPage = null;

    public void initToolbar(){
        //file
        menuImport.setOnAction(e -> importFile());
        menuExit.setOnAction(e -> Platform.exit());
        for(ExportFormats format : ExportFormats.values()){
            MenuItem item = new MenuItem(format.displayName);
            item.setOnAction(e -> exportFile(format, false));
            menuExport.getItems().add(item);
        }

        for(ExportFormats format : ExportFormats.values()){
            MenuItem item = new MenuItem(format.displayName);
            item.setOnAction(e -> exportFile(format, true));
            menuExportPerPen.getItems().add(item);
        }

        //view
        ArrayList<TitledPane> allPanes = new ArrayList<>();
        for(Node node : vBoxSettings.getChildren()){
            if(node instanceof TitledPane){
                allPanes.add((TitledPane) node);
            }
        }
        for(TitledPane pane : allPanes){
            MenuItem viewButton = new MenuItem(pane.getText());
            viewButton.setOnAction(e -> {
                allPanes.forEach(p -> p.expandedProperty().setValue(p == pane));
            });
            menuView.getItems().add(viewButton);
        }

        //help
        menuHelpPage.setOnAction(e -> openURL(Utils.URL_GITHUB_REPO));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// VIEWPORT PANE

    ////VIEWPORT WINDOW
    public VBox vBoxViewportContainer = null;
    public ScrollPane viewportScrollPane = null;
    public StackPane viewportStackPane = null;

    ////VIEWPORT SETTINGS
    public Slider sliderDisplayedLines = null;
    public TextField textFieldDisplayedLines = null;

    public ChoiceBox<EnumDisplayMode> choiceBoxDisplayMode = null;
    public CheckBox checkBoxShowGrid = null;
    public Button buttonZoomIn = null;
    public Button buttonZoomOut = null;
    public Button buttonResetView = null;

    ////PLOT DETAILS
    public Label labelElapsedTime = null;
    public Label labelPlottedLines = null;

    public void initViewport(){

        ////VIEWPORT SETTINGS
        sliderDisplayedLines.setMax(1);
        sliderDisplayedLines.valueProperty().addListener((observable, oldValue, newValue) -> {
            PlottingTask task = DrawingBotV3.INSTANCE.getActiveTask();
            if(task != null){
                int lines = (int)Utils.mapDouble(newValue.doubleValue(), 0, 1, 0, task.plottedDrawing.getPlottedLineCount());
                task.plottedDrawing.displayedLineCount.setValue(lines);
                textFieldDisplayedLines.setText(String.valueOf(lines));
                DrawingBotV3.INSTANCE.reRender();
            }
        });

        textFieldDisplayedLines.setOnAction(e -> {
            PlottingTask task = DrawingBotV3.INSTANCE.getActiveTask();
            if(task != null){
                int lines = (int)Math.max(0, Math.min(task.plottedDrawing.getPlottedLineCount(), Double.parseDouble(textFieldDisplayedLines.getText())));
                task.plottedDrawing.displayedLineCount.setValue(lines);
                textFieldDisplayedLines.setText(String.valueOf(lines));
                sliderDisplayedLines.setValue((double)lines / task.plottedDrawing.getPlottedLineCount());
                DrawingBotV3.INSTANCE.reRender();
            }
        });

        choiceBoxDisplayMode.getItems().addAll(EnumDisplayMode.values());
        choiceBoxDisplayMode.setValue(EnumDisplayMode.IMAGE);
        DrawingBotV3.INSTANCE.display_mode.bindBidirectional(choiceBoxDisplayMode.valueProperty());

        DrawingBotV3.INSTANCE.displayGrid.bind(checkBoxShowGrid.selectedProperty());
        DrawingBotV3.INSTANCE.displayGrid.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.reRender());

        buttonZoomIn.setOnAction(e -> {
            DrawingBotV3.INSTANCE.scaleMultiplier.set(DrawingBotV3.INSTANCE.scaleMultiplier.getValue() + 0.1);
        });
        buttonZoomOut.setOnAction(e -> {
            if(DrawingBotV3.INSTANCE.scaleMultiplier.getValue() > DrawingBotV3.minScale){
                DrawingBotV3.INSTANCE.scaleMultiplier.set(DrawingBotV3.INSTANCE.scaleMultiplier.getValue() - 0.1);
            }
        });

        buttonResetView.setOnAction(e -> {
            viewportScrollPane.setHvalue(0.5);
            viewportScrollPane.setVvalue(0.5);
            DrawingBotV3.INSTANCE.scaleMultiplier.set(1.0);
        });

        labelElapsedTime.setText("0 s");
        labelPlottedLines.setText("0 lines");
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// PLOTTING CONTROLS

    public Button buttonStartPlotting = null;
    public Button buttonStopPlotting = null;
    public Button buttonResetPlotting = null;

    public void initPlottingControls(){
        buttonStartPlotting.setOnAction(param -> DrawingBotV3.INSTANCE.startPlotting());
        buttonStartPlotting.disableProperty().bind(DrawingBotV3.INSTANCE.isPlotting);
        buttonStopPlotting.setOnAction(param -> DrawingBotV3.INSTANCE.stopPlotting());
        buttonStopPlotting.disableProperty().bind(DrawingBotV3.INSTANCE.isPlotting.not());
        buttonResetPlotting.setOnAction(param -> DrawingBotV3.INSTANCE.resetPlotting());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    ////PROGRESS BAR PANE

    public Pane paneProgressBar = null;
    public ProgressBar progressBarGeneral = null;
    public Label progressBarLabel = null;

    public void initProgressBar(){
        progressBarGeneral.prefWidthProperty().bind(paneProgressBar.widthProperty());
        progressBarLabel.setText("");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////DRAWING AREA PANE
    public ComboBox<GenericPreset<PresetDrawingArea>> comboBoxDrawingAreaPreset = null;
    public MenuButton menuButtonDrawingAreaPresets = null;

    /////SIZING OPTIONS
    public CheckBox checkBoxOriginalSizing = null;
    public ChoiceBox<Units> choiceBoxDrawingUnits = null;
    public Pane paneDrawingAreaCustom = null;
    public TextField textFieldDrawingWidth = null;
    public TextField textFieldDrawingHeight = null;
    public TextField textFieldPaddingLeft = null;
    public TextField textFieldPaddingRight = null;
    public TextField textFieldPaddingTop = null;
    public TextField textFieldPaddingBottom = null;
    public CheckBox checkBoxGangPadding = null;

    public ChoiceBox<EnumScalingMode> choiceBoxScalingMode = null;

    public void initDrawingAreaPane(){

        comboBoxDrawingAreaPreset.setItems(JsonLoaderManager.DRAWING_AREA.presets);
        comboBoxDrawingAreaPreset.setValue(JsonLoaderManager.DRAWING_AREA.getDefaultPreset());
        comboBoxDrawingAreaPreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                JsonLoaderManager.DRAWING_AREA.applyPreset(newValue);
            }
        });

        setupPresetMenuButton(JsonLoaderManager.DRAWING_AREA, menuButtonDrawingAreaPresets, comboBoxDrawingAreaPreset::getValue, (preset) -> {
            comboBoxDrawingAreaPreset.setValue(preset);

            ///force update rendering
            comboBoxDrawingAreaPreset.setItems(JsonLoaderManager.DRAWING_AREA.presets);
            comboBoxDrawingAreaPreset.setButtonCell(new ComboBoxListCell<>());
        });


        /////SIZING OPTIONS
        DrawingBotV3.INSTANCE.useOriginalSizing.bindBidirectional(checkBoxOriginalSizing.selectedProperty());

        paneDrawingAreaCustom.disableProperty().bind(checkBoxOriginalSizing.selectedProperty());
        choiceBoxDrawingUnits.disableProperty().bind(checkBoxOriginalSizing.selectedProperty());

        choiceBoxDrawingUnits.getItems().addAll(Units.values());
        choiceBoxDrawingUnits.setValue(Units.MILLIMETRES);
        DrawingBotV3.INSTANCE.inputUnits.bindBidirectional(choiceBoxDrawingUnits.valueProperty());

        DrawingBotV3.INSTANCE.drawingAreaWidth.bind(Bindings.createFloatBinding(() -> textFieldDrawingWidth.textProperty().get().isEmpty() ? 0F : Float.parseFloat(textFieldDrawingWidth.textProperty().get()), textFieldDrawingWidth.textProperty()));
        textFieldDrawingWidth.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.INSTANCE.drawingAreaHeight.bind(Bindings.createFloatBinding(() -> textFieldDrawingHeight.textProperty().get().isEmpty() ? 0F : Float.parseFloat(textFieldDrawingHeight.textProperty().get()), textFieldDrawingHeight.textProperty()));
        textFieldDrawingHeight.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.INSTANCE.drawingAreaPaddingLeft.bind(Bindings.createFloatBinding(() -> textFieldPaddingLeft.textProperty().get().isEmpty() ? 0F : Float.parseFloat(textFieldPaddingLeft.textProperty().get()), textFieldPaddingLeft.textProperty()));
        textFieldPaddingLeft.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.INSTANCE.drawingAreaPaddingRight.bind(Bindings.createFloatBinding(() -> textFieldPaddingRight.textProperty().get().isEmpty() ? 0F : Float.parseFloat(textFieldPaddingRight.textProperty().get()), textFieldPaddingRight.textProperty()));
        textFieldPaddingRight.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.INSTANCE.drawingAreaPaddingTop.bind(Bindings.createFloatBinding(() -> textFieldPaddingTop.textProperty().get().isEmpty() ? 0F : Float.parseFloat(textFieldPaddingTop.textProperty().get()), textFieldPaddingTop.textProperty()));
        textFieldPaddingTop.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.INSTANCE.drawingAreaPaddingBottom.bind(Bindings.createFloatBinding(() -> textFieldPaddingBottom.textProperty().get().isEmpty() ? 0F : Float.parseFloat(textFieldPaddingBottom.textProperty().get()), textFieldPaddingBottom.textProperty()));
        textFieldPaddingBottom.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        checkBoxGangPadding.setSelected(true);
        checkBoxGangPadding.selectedProperty().addListener((observable, oldValue, newValue) -> updatePaddingBindings(newValue));
        updatePaddingBindings(checkBoxGangPadding.isSelected());

        choiceBoxScalingMode.getItems().addAll(EnumScalingMode.values());
        choiceBoxScalingMode.setValue(EnumScalingMode.CROP_TO_FIT);
        DrawingBotV3.INSTANCE.scalingMode.bindBidirectional(choiceBoxScalingMode.valueProperty());

        ///generic listeners
        DrawingBotV3.INSTANCE.useOriginalSizing.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingAreaChanged());
        DrawingBotV3.INSTANCE.scalingMode.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingAreaChanged());
        DrawingBotV3.INSTANCE.inputUnits.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingAreaChanged());
        DrawingBotV3.INSTANCE.drawingAreaHeight.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingAreaChanged());
        DrawingBotV3.INSTANCE.drawingAreaPaddingLeft.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingAreaChanged());
        DrawingBotV3.INSTANCE.drawingAreaPaddingRight.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingAreaChanged());
        DrawingBotV3.INSTANCE.drawingAreaPaddingTop.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingAreaChanged());
        DrawingBotV3.INSTANCE.drawingAreaPaddingBottom.addListener((observable, oldValue, newValue) -> DrawingBotV3.INSTANCE.onDrawingAreaChanged());
    }

    public void updatePaddingBindings(boolean ganged){
        if(ganged){
            DrawingBotV3.INSTANCE.drawingAreaPaddingGang.set("0");
            textFieldPaddingLeft.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.drawingAreaPaddingGang);
            textFieldPaddingRight.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.drawingAreaPaddingGang);
            textFieldPaddingTop.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.drawingAreaPaddingGang);
            textFieldPaddingBottom.textProperty().bindBidirectional(DrawingBotV3.INSTANCE.drawingAreaPaddingGang);
        }else{
            textFieldPaddingLeft.textProperty().unbindBidirectional(DrawingBotV3.INSTANCE.drawingAreaPaddingGang);
            textFieldPaddingRight.textProperty().unbindBidirectional(DrawingBotV3.INSTANCE.drawingAreaPaddingGang);
            textFieldPaddingTop.textProperty().unbindBidirectional(DrawingBotV3.INSTANCE.drawingAreaPaddingGang);
            textFieldPaddingBottom.textProperty().unbindBidirectional(DrawingBotV3.INSTANCE.drawingAreaPaddingGang);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PRE PROCESSING PANE

    public ComboBox<GenericPreset<PresetImageFilters>> comboBoxImageFilterPreset = null;

    public MenuButton menuButtonFilterPresets = null;

    public TableView<ImageFilterRegistry.ObservableImageFilter> tableViewImageFilters = null;
    public TableColumn<ImageFilterRegistry.ObservableImageFilter, Boolean> columnEnableImageFilter = null;
    public TableColumn<ImageFilterRegistry.ObservableImageFilter, String> columnImageFilterType = null;
    public TableColumn<ImageFilterRegistry.ObservableImageFilter, String> columnImageFilterSettings = null;

    public ComboBox<EnumFilterTypes> comboBoxFilterType = null;
    public ComboBox<GenericFactory<BufferedImageOp>> comboBoxImageFilter = null;
    public Button buttonAddFilter = null;

    public void initPreProcessingPane(){
        comboBoxImageFilterPreset.setItems(ImageFilterRegistry.imagePresets);
        comboBoxImageFilterPreset.setValue(ImageFilterRegistry.getDefaultImageFilterPreset());
        comboBoxImageFilterPreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                JsonLoaderManager.FILTERS.applyPreset(newValue);
            }
        });

        setupPresetMenuButton(JsonLoaderManager.FILTERS, menuButtonFilterPresets, comboBoxImageFilterPreset::getValue, (preset) -> {
            comboBoxImageFilterPreset.setValue(preset);

            ///force update rendering
            comboBoxImageFilterPreset.setItems(ImageFilterRegistry.imagePresets);
            comboBoxImageFilterPreset.setButtonCell(new ComboBoxListCell<>());
        });

        tableViewImageFilters.setItems(ImageFilterRegistry.currentFilters);
        tableViewImageFilters.setRowFactory(param -> {
            TableRow<ImageFilterRegistry.ObservableImageFilter> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setOnMouseClicked(e -> {
                if(e.getClickCount() > 1){
                    openImageFilterDialog(row.getItem());
                }
            });
            row.setContextMenu(new ContextMenuObservableFilter(row));
            row.setPrefHeight(30);
            return row;
        });

        columnEnableImageFilter.setCellFactory(param -> new CheckBoxTableCell<>(index -> columnEnableImageFilter.getCellObservableValue(index)));
        columnEnableImageFilter.setCellValueFactory(param -> param.getValue().enable);

        columnImageFilterType.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        columnImageFilterType.setCellValueFactory(param -> param.getValue().name);


        columnImageFilterSettings.setCellValueFactory(param -> param.getValue().settingsString);

        comboBoxFilterType.setItems(FXCollections.observableArrayList(ImageFilterRegistry.filterFactories.keySet()));
        comboBoxFilterType.setValue(ImageFilterRegistry.getDefaultFilterType());
        comboBoxFilterType.valueProperty().addListener((observable, oldValue, newValue) -> {
            comboBoxImageFilter.setItems(ImageFilterRegistry.filterFactories.get(newValue));
            comboBoxImageFilter.setValue(ImageFilterRegistry.getDefaultFilter(newValue));
        });

        comboBoxImageFilter.setItems(ImageFilterRegistry.filterFactories.get(ImageFilterRegistry.getDefaultFilterType()));
        comboBoxImageFilter.setValue(ImageFilterRegistry.getDefaultFilter(ImageFilterRegistry.getDefaultFilterType()));
        buttonAddFilter.setOnAction(e -> {
            if(comboBoxImageFilter.getValue() != null){
                ImageFilterRegistry.ObservableImageFilter filter = new ImageFilterRegistry.ObservableImageFilter(comboBoxImageFilter.getValue());
                ImageFilterRegistry.currentFilters.add(filter);
                if(!filter.filterSettings.isEmpty()){
                    openImageFilterDialog(filter);
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PATH FINDING CONTROLS
    public ChoiceBox<GenericFactory<IPathFindingModule>> choiceBoxPFM = null;

    public ComboBox<GenericPreset<PresetPFMSettings>> comboBoxPFMPreset = null;
    public MenuButton menuButtonPFMPresets = null;

    public TableView<GenericSetting<?,?>> tableViewAdvancedPFMSettings = null;
    public TableColumn<GenericSetting<?, ?>, Boolean> tableColumnLock = null;
    public TableColumn<GenericSetting<?, ?>, String> tableColumnSetting = null;
    public TableColumn<GenericSetting<?, ?>, Object> tableColumnValue = null;
    public TableColumn<GenericSetting<?, ?>, Object> tableColumnControl = null;

    public Button buttonPFMSettingReset = null;
    public Button buttonPFMSettingRandom = null;
    public Button buttonPFMSettingHelp = null;

    public void initPFMControls(){

        ////PATH FINDING CONTROLS
        choiceBoxPFM.setItems(PFMMasterRegistry.getObservablePFMLoaderList());
        choiceBoxPFM.setValue(PFMMasterRegistry.getDefaultPFMFactory());
        choiceBoxPFM.setOnAction(e -> changePathFinderModule(choiceBoxPFM.getSelectionModel().getSelectedItem()));
        DrawingBotV3.INSTANCE.pfmFactory.bindBidirectional(choiceBoxPFM.valueProperty());


        comboBoxPFMPreset.setItems(PFMMasterRegistry.getObservablePFMPresetList());
        comboBoxPFMPreset.setValue(PFMMasterRegistry.getDefaultPFMPreset());
        comboBoxPFMPreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                JsonLoaderManager.PFM.applyPreset(newValue);
            }
        });

        setupPresetMenuButton(JsonLoaderManager.PFM, menuButtonPFMPresets, comboBoxPFMPreset::getValue, (preset) -> {
            comboBoxPFMPreset.setValue(preset);

            ///force update rendering
            comboBoxPFMPreset.setItems(PFMMasterRegistry.getObservablePFMPresetList());
            comboBoxPFMPreset.setButtonCell(new ComboBoxListCell<>());
        });

        DrawingBotV3.INSTANCE.pfmFactory.addListener((observable, oldValue, newValue) -> {
            comboBoxPFMPreset.setItems(PFMMasterRegistry.getObservablePFMPresetList(newValue));
            comboBoxPFMPreset.setValue(PFMMasterRegistry.getDefaultPFMPreset(newValue));
        });

        tableViewAdvancedPFMSettings.setItems(PFMMasterRegistry.getObservablePFMSettingsList());
        tableViewAdvancedPFMSettings.setRowFactory(param -> {
            TableRow<GenericSetting<?, ?>> row = new TableRow<>();
            row.setContextMenu(new ContextMenuPFMSetting(row));
            return row;
        });
        DrawingBotV3.INSTANCE.pfmFactory.addListener((observable, oldValue, newValue) -> tableViewAdvancedPFMSettings.setItems(PFMMasterRegistry.getObservablePFMSettingsList()));

        tableColumnLock.setCellFactory(param -> new CheckBoxTableCell<>(index -> tableColumnLock.getCellObservableValue(index)));
        tableColumnLock.setCellValueFactory(param -> param.getValue().lock);

        tableColumnSetting.setCellValueFactory(param -> param.getValue().settingName);

        tableColumnValue.setCellFactory(param -> {
            TextFieldTableCell<GenericSetting<?, ?>, Object> cell = new TextFieldTableCell<>();
            cell.setConverter(new StringConverterGenericSetting(() -> cell.tableViewProperty().get().getItems().get(cell.getIndex())));
            return cell;
        });
        tableColumnValue.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

        tableColumnControl.setCellFactory(param -> new TableCellSettingControl());
        tableColumnControl.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

        buttonPFMSettingReset.setOnAction(e -> {
            JsonLoaderManager.PFM.applyPreset(comboBoxPFMPreset.getValue());
        });

        buttonPFMSettingRandom.setOnAction(e -> GenericSetting.randomiseSettings(tableViewAdvancedPFMSettings.getItems()));
        buttonPFMSettingHelp.setOnAction(e -> openURL(Utils.URL_GITHUB_PFM_DOCS));

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PEN SETTINGS

    public ComboBox<String> comboBoxSetType = null;
    public ComboBox<IDrawingSet<IDrawingPen>> comboBoxDrawingSet = null;
    public MenuButton menuButtonDrawingSetPresets = null;

    public TableView<ObservableDrawingPen> penTableView = null;
    public TableColumn<ObservableDrawingPen, Boolean> penEnableColumn = null;
    public TableColumn<ObservableDrawingPen, String> penTypeColumn = null;
    public TableColumn<ObservableDrawingPen, String> penNameColumn = null;
    public TableColumn<ObservableDrawingPen, Color> penColourColumn = null;
    public TableColumn<ObservableDrawingPen, Float> penStrokeColumn = null;
    public TableColumn<ObservableDrawingPen, String> penPercentageColumn = null;
    public TableColumn<ObservableDrawingPen, Integer> penWeightColumn = null;
    public TableColumn<ObservableDrawingPen, Integer> penLinesColumn = null;

    public ComboBox<String> comboBoxPenType = null;
    public ComboBox<DrawingPen> comboBoxDrawingPen = null;
    public ComboBoxListViewSkin<DrawingPen> comboBoxDrawingPenSkin = null;
    public MenuButton menuButtonDrawingPenPresets = null;


    public Button buttonAddPen = null;
    public Button buttonRemovePen = null;
    public Button buttonDuplicatePen = null;
    public Button buttonMoveUpPen = null;
    public Button buttonMoveDownPen = null;



    public ComboBox<EnumDistributionOrder> renderOrderComboBox = null;
    public ComboBox<EnumBlendMode> blendModeComboBox = null;

    public void initPenSettingsPane(){

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        comboBoxSetType.setItems(FXCollections.observableArrayList(DrawingRegistry.INSTANCE.registeredSets.keySet()));
        comboBoxSetType.setValue(DrawingRegistry.INSTANCE.getDefaultSetType());
        comboBoxSetType.valueProperty().addListener((observable, oldValue, newValue) -> {
            comboBoxDrawingSet.setItems(DrawingRegistry.INSTANCE.registeredSets.get(newValue));
            comboBoxDrawingSet.setValue(DrawingRegistry.INSTANCE.getDefaultSet(newValue));
        });

        comboBoxDrawingSet.setItems(DrawingRegistry.INSTANCE.registeredSets.get(comboBoxSetType.getValue()));
        comboBoxDrawingSet.setValue(DrawingRegistry.INSTANCE.getDefaultSet(comboBoxSetType.getValue()));
        comboBoxDrawingSet.valueProperty().addListener((observable, oldValue, newValue) -> changeDrawingSet(newValue));
        comboBoxDrawingSet.setCellFactory(param -> new ComboCellDrawingSet());
        comboBoxDrawingSet.setButtonCell(new ComboCellDrawingSet());

        setupPresetMenuButton(JsonLoaderManager.DRAWING_SET, menuButtonDrawingSetPresets,
            () -> {
            if(comboBoxDrawingSet.getValue() instanceof PresetDrawingSet){
                PresetDrawingSet set = (PresetDrawingSet) comboBoxDrawingSet.getValue();
                return set.preset;
            }
            return null;
        }, (preset) -> {
            //force update rendering
            comboBoxSetType.setItems(FXCollections.observableArrayList(DrawingRegistry.INSTANCE.registeredSets.keySet()));
            comboBoxDrawingSet.setItems(DrawingRegistry.INSTANCE.registeredSets.get(comboBoxSetType.getValue()));
            comboBoxDrawingSet.setButtonCell(new ComboCellDrawingSet());
            if(preset != null){
                comboBoxSetType.setValue(preset.presetSubType);
                comboBoxDrawingSet.setValue(preset.data);
            }else{
                //don't set to avoid overwriting the users configured pens
                //comboBoxSetType.setValue(DrawingRegistry.INSTANCE.getDefaultSetType());
                //comboBoxDrawingSet.setValue(DrawingRegistry.INSTANCE.getDefaultSet(comboBoxSetType.getValue()));
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        penTableView.setRowFactory(param -> {
            TableRow<ObservableDrawingPen> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setContextMenu(new ContextMenuObservablePen(row));
            return row;
        });


        penTableView.setItems(DrawingBotV3.INSTANCE.observableDrawingSet.pens);
        penTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(DrawingBotV3.INSTANCE.display_mode.get() == EnumDisplayMode.SELECTED_PEN){
                DrawingBotV3.INSTANCE.reRender();
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        penNameColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        penNameColumn.setCellValueFactory(param -> param.getValue().name);

        penTypeColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        penTypeColumn.setCellValueFactory(param -> param.getValue().type);

        penColourColumn.setCellFactory(TableCellColorPicker::new);
        penColourColumn.setCellValueFactory(param -> param.getValue().javaFXColour);

        penStrokeColumn.setCellFactory(param -> new TextFieldTableCell<>(new FloatStringConverter()));
        penStrokeColumn.setCellValueFactory(param -> param.getValue().strokeSize.asObject());

        penEnableColumn.setCellFactory(param -> new CheckBoxTableCell<>(index -> penEnableColumn.getCellObservableValue(index)));
        penEnableColumn.setCellValueFactory(param -> param.getValue().enable);

        penPercentageColumn.setCellValueFactory(param -> param.getValue().currentPercentage);

        penWeightColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        penWeightColumn.setCellValueFactory(param -> param.getValue().distributionWeight.asObject());

        penLinesColumn.setCellValueFactory(param -> param.getValue().currentLines.asObject());

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        comboBoxPenType.setItems(FXCollections.observableArrayList(DrawingRegistry.INSTANCE.registeredPens.keySet()));
        comboBoxPenType.setValue(DrawingRegistry.INSTANCE.getDefaultPenType());

        comboBoxPenType.valueProperty().addListener((observable, oldValue, newValue) -> {
            comboBoxDrawingPen.setItems(DrawingRegistry.INSTANCE.registeredPens.get(newValue));
            comboBoxDrawingPen.setValue(DrawingRegistry.INSTANCE.getDefaultPen(newValue));
        });

        comboBoxDrawingPenSkin = new ComboBoxListViewSkin<>(comboBoxDrawingPen);
        comboBoxDrawingPenSkin.hideOnClickProperty().set(false);
        comboBoxDrawingPen.setSkin(comboBoxDrawingPenSkin);

        comboBoxDrawingPen.setItems(DrawingRegistry.INSTANCE.registeredPens.get(comboBoxPenType.getValue()));
        comboBoxDrawingPen.setValue(DrawingRegistry.INSTANCE.getDefaultPen(comboBoxPenType.getValue()));
        comboBoxDrawingPen.setCellFactory(param -> new ComboCellDrawingPen(true));
        comboBoxDrawingPen.setButtonCell(new ComboCellDrawingPen(false));

        setupPresetMenuButton(JsonLoaderManager.DRAWING_PENS, menuButtonDrawingPenPresets,
            () -> {
                if(comboBoxDrawingPen.getValue() instanceof PresetDrawingPen){
                    PresetDrawingPen set = (PresetDrawingPen) comboBoxDrawingPen.getValue();
                    return set.preset;
                }
                return null;
            }, (preset) -> {
                //force update rendering
                comboBoxPenType.setItems(FXCollections.observableArrayList(DrawingRegistry.INSTANCE.registeredPens.keySet()));
                comboBoxDrawingPen.setItems(DrawingRegistry.INSTANCE.registeredPens.get(comboBoxPenType.getValue()));
                comboBoxDrawingPen.setButtonCell(new ComboCellDrawingPen(false));

                if(preset != null){
                    comboBoxPenType.setValue(preset.presetSubType);
                    comboBoxDrawingPen.setValue(preset.data);
                }else{
                    comboBoxPenType.setValue(DrawingRegistry.INSTANCE.getDefaultPenType());
                    comboBoxDrawingPen.setValue(DrawingRegistry.INSTANCE.getDefaultPen(comboBoxPenType.getValue()));
                }
            });

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        buttonAddPen.setOnAction(e -> DrawingBotV3.INSTANCE.observableDrawingSet.addNewPen(comboBoxDrawingPen.getValue()));
        buttonRemovePen.setOnAction(e -> deleteItem(penTableView.getSelectionModel().getSelectedItem(), DrawingBotV3.INSTANCE.observableDrawingSet.pens));
        buttonDuplicatePen.setOnAction(e -> {
            ObservableDrawingPen pen = penTableView.getSelectionModel().getSelectedItem();
            if(pen != null)
                DrawingBotV3.INSTANCE.observableDrawingSet.addNewPen(pen);
        });
        buttonMoveUpPen.setOnAction(e -> moveItemUp(penTableView.getSelectionModel().getSelectedItem(), DrawingBotV3.INSTANCE.observableDrawingSet.pens));
        buttonMoveDownPen.setOnAction(e -> moveItemDown(penTableView.getSelectionModel().getSelectedItem(), DrawingBotV3.INSTANCE.observableDrawingSet.pens));
        buttonMoveDownPen.setOnAction(e -> moveItemDown(penTableView.getSelectionModel().getSelectedItem(), DrawingBotV3.INSTANCE.observableDrawingSet.pens));

        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        renderOrderComboBox.setItems(FXCollections.observableArrayList(EnumDistributionOrder.values()));
        renderOrderComboBox.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.observableDrawingSet.renderOrder);

        blendModeComboBox.setItems(FXCollections.observableArrayList(EnumBlendMode.values()));
        blendModeComboBox.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.observableDrawingSet.blendMode);


    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////BATCH PROCESSING

    public Label labelInputFolder = null;
    public Label labelOutputFolder = null;

    public Button buttonSelectInputFolder = null;
    public Button buttonSelectOutputFolder = null;
    public Button buttonStartBatchProcessing = null;
    public Button buttonStopBatchProcessing = null;

    public CheckBox checkBoxOverwrite = null;

    public TableView<BatchProcessing.BatchExportTask> tableViewBatchExport = null;
    public TableColumn<BatchProcessing.BatchExportTask, String> tableColumnFileFormat = null;
    public TableColumn<BatchProcessing.BatchExportTask, Boolean> tableColumnPerDrawing = null;
    public TableColumn<BatchProcessing.BatchExportTask, Boolean> tableColumnPerPen = null;

    public void initBatchProcessingPane(){

        labelInputFolder.textProperty().bindBidirectional(BatchProcessing.inputFolder);
        labelOutputFolder.textProperty().bindBidirectional(BatchProcessing.outputFolder);

        buttonSelectInputFolder.setOnAction(e -> BatchProcessing.selectFolder(true));
        buttonSelectInputFolder.disableProperty().bind(BatchProcessing.isBatchProcessing);

        buttonSelectOutputFolder.setOnAction(e -> BatchProcessing.selectFolder(false));
        buttonSelectOutputFolder.disableProperty().bind(BatchProcessing.isBatchProcessing);

        buttonStartBatchProcessing.setOnAction(e -> BatchProcessing.startProcessing());
        buttonStartBatchProcessing.disableProperty().bind(BatchProcessing.isBatchProcessing);

        buttonStopBatchProcessing.setOnAction(e -> BatchProcessing.finishProcessing());
        buttonStopBatchProcessing.disableProperty().bind(BatchProcessing.isBatchProcessing.not());

        checkBoxOverwrite.selectedProperty().bindBidirectional(BatchProcessing.overwriteExistingFiles);
        checkBoxOverwrite.disableProperty().bind(BatchProcessing.isBatchProcessing);

        tableViewBatchExport.setItems(BatchProcessing.exportTasks);
        tableViewBatchExport.disableProperty().bind(BatchProcessing.isBatchProcessing);

        tableColumnFileFormat.setCellValueFactory(task -> new SimpleStringProperty(task.getValue().formatName()));

        tableColumnPerDrawing.setCellFactory(param -> new CheckBoxTableCell<>(index -> tableColumnPerDrawing.getCellObservableValue(index)));
        tableColumnPerDrawing.setCellValueFactory(param -> param.getValue().enablePerDrawing);

        tableColumnPerPen.setCellFactory(param -> new CheckBoxTableCell<>(index -> tableColumnPerPen.getCellObservableValue(index)));
        tableColumnPerPen.setCellValueFactory(param -> param.getValue().enablePerPen);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////GCODE SETTINGS

    public ComboBox<GenericPreset<PresetGCodeSettings>> comboBoxGCodePreset = null;
    public MenuButton menuButtonGCodePresets = null;

    public TextField textFieldOffsetX = null;
    public TextField textFieldOffsetY = null;
    public TextField textFieldPenUpZ = null;
    public TextField textFieldPenDownZ = null;
    public CheckBox checkBoxAutoHome = null;


    public void initGCodeSettingsPane(){

        comboBoxGCodePreset.setItems(JsonLoaderManager.GCODE_SETTINGS.presets);
        comboBoxGCodePreset.setValue(JsonLoaderManager.GCODE_SETTINGS.getDefaultPreset());
        comboBoxGCodePreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                JsonLoaderManager.GCODE_SETTINGS.applyPreset(newValue);
            }
        });

        setupPresetMenuButton(JsonLoaderManager.GCODE_SETTINGS, menuButtonGCodePresets, comboBoxGCodePreset::getValue, (preset) -> {
            comboBoxGCodePreset.setValue(preset);

            ///force update rendering
            comboBoxGCodePreset.setItems(JsonLoaderManager.GCODE_SETTINGS.presets);
            comboBoxGCodePreset.setButtonCell(new ComboBoxListCell<>());
        });

        checkBoxAutoHome.setSelected(true);
        DrawingBotV3.INSTANCE.enableAutoHome.bindBidirectional(checkBoxAutoHome.selectedProperty());

        DrawingBotV3.INSTANCE.gcodeOffsetX.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldOffsetX.textProperty().get()), textFieldOffsetX.textProperty()));
        textFieldOffsetX.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.INSTANCE.gcodeOffsetY.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldOffsetY.textProperty().get()), textFieldOffsetY.textProperty()));
        textFieldOffsetY.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.INSTANCE.penUpZ.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldPenUpZ.textProperty().get()), textFieldPenUpZ.textProperty()));
        textFieldPenUpZ.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 5F));

        DrawingBotV3.INSTANCE.penDownZ.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldPenDownZ.textProperty().get()), textFieldPenDownZ.textProperty()));
        textFieldPenDownZ.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void changePathFinderModule(GenericFactory<IPathFindingModule> pfm){
        DrawingBotV3.INSTANCE.pfmFactory.set(pfm);
    }

    public void changeDrawingSet(IDrawingSet<IDrawingPen> set){
        if(set != null)
            DrawingBotV3.INSTANCE.observableDrawingSet.loadDrawingSet(set);
    }

    public void importFile(){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().add(FileUtils.IMPORT_IMAGES);
            d.setTitle("Select an image file to sketch");
            d.setInitialDirectory(new File(FileUtils.getUserHomeDirectory()));
            File file = d.showOpenDialog(null);
            if(file != null){
                DrawingBotV3.INSTANCE.openImage(file, false);
            }
        });
    }

    public void exportFile(ExportFormats format, boolean seperatePens){
        if(DrawingBotV3.INSTANCE.getActiveTask() == null){
            return;
        }
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(format.filters);
            d.setTitle(format.getDialogTitle());
            d.setInitialDirectory(DrawingBotV3.INSTANCE.getActiveTask().originalFile.getParentFile());
            d.setInitialFileName(FileUtils.removeExtension(DrawingBotV3.INSTANCE.getActiveTask().originalFile.getName()) + "_plotted");
            File file = d.showSaveDialog(null);
            if(file != null){
                DrawingBotV3.INSTANCE.createExportTask(format, DrawingBotV3.INSTANCE.getActiveTask(), PlottedPoint.DEFAULT_FILTER, d.getSelectedExtensionFilter().getExtensions().get(0).substring(1), file, seperatePens);
            }
        });
    }

    public void openURL(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (IOException e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error opening webpage: " + url);
        }
    }

    public String getClipboardString(){
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)){
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            //
        }
        return null;
    }

    public void importPreset(EnumJsonType presetType){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().add(FileUtils.FILTER_JSON);
            d.setTitle("Select a preset to import");
            d.setInitialDirectory(new File(FileUtils.getUserHomeDirectory()));
            File file = d.showOpenDialog(null);
            if(file != null){
                JsonLoaderManager.importPresetFile(file, presetType);
            }
        });
    }

    public void exportPreset(GenericPreset<?> preset){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(FileUtils.FILTER_JSON);
            d.setTitle("Save preset");
            d.setInitialDirectory(new File(FileUtils.getUserHomeDirectory()));
            d.setInitialFileName(preset.presetName + " - Preset");
            File file = d.showSaveDialog(null);
            if(file != null){
                JsonLoaderManager.exportPresetFile(file, preset);
            }
        });
    }

    //// EXTERNALLY TRIGGERED EVENTS / UI UPDATES

    public void onTaskStageFinished(PlottingTask task, EnumTaskStage stage){
        switch (stage){
            case QUEUED:
                break;
            case PRE_PROCESSING:
                Platform.runLater(() -> DrawingBotV3.INSTANCE.display_mode.setValue(EnumDisplayMode.DRAWING));
                break;
            case DO_PROCESS:
                Platform.runLater(() -> {
                    sliderDisplayedLines.setValue(1.0F);
                    textFieldDisplayedLines.setText(String.valueOf(task.plottedDrawing.getPlottedLineCount()));
                });
                break;
            case POST_PROCESSING:
                break;
            case FINISHING:
                break;
            case FINISHED:
                break;
        }
    }

    public ObservableDrawingPen getSelectedPen(){
        return penTableView.getSelectionModel().getSelectedItem();
    }

    //// PRESET MENU BUTTON \\\\

    public <O extends IJsonData> void setupPresetMenuButton(AbstractPresetLoader<O> presetManager, MenuButton button, Supplier<GenericPreset<O>> getter, Consumer<GenericPreset<O>> setter){

        MenuItem newPreset = new MenuItem("New Preset");
        MenuItem updatePreset = new MenuItem("Update Preset");
        MenuItem renamePreset = new MenuItem("Rename Preset");
        MenuItem deletePreset = new MenuItem("Delete Preset");

        MenuItem importPreset = new MenuItem("Import Preset");
        MenuItem exportPreset = new MenuItem("Export Preset");

        newPreset.setOnAction(e -> {
            GenericPreset<O> editingPreset = presetManager.createNewPreset();
            if(editingPreset == null){
                return;
            }
            editingPreset = presetManager.tryUpdatePreset(editingPreset);
            if(editingPreset != null){
                presetEditorDialog.setEditingPreset(editingPreset);
                presetEditorDialog.setTitle("Save new preset");
                Optional<GenericPreset<?>> result = presetEditorDialog.showAndWait();
                if(result.isPresent()){
                    presetManager.tryEditPreset(editingPreset);
                    presetManager.trySavePreset(editingPreset);
                    setter.accept(editingPreset);
                }
            }
        });

        updatePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            GenericPreset<O> preset = presetManager.tryUpdatePreset(current);
            if(preset != null){
                setter.accept(preset);
            }
        });

        renamePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null || !current.userCreated){
                return;
            }
            presetEditorDialog.setEditingPreset(current);
            presetEditorDialog.setTitle("Rename preset");
            Optional<GenericPreset<?>> result = presetEditorDialog.showAndWait();
            if(result.isPresent()){
                presetManager.tryEditPreset(current);
                setter.accept(current);
            }
        });

        deletePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            if(presetManager.tryDeletePreset(current)){
                setter.accept(presetManager.getDefaultPreset());
            }
        });

        importPreset.setOnAction(e -> {
            importPreset(presetManager.type);
        });
        exportPreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            exportPreset(current);
        });

        button.getItems().addAll(newPreset, updatePreset, renamePreset, deletePreset, new SeparatorMenuItem(), importPreset, exportPreset);
    }

    public static <O> void addDefaultTableViewContextMenuItems(ContextMenu menu, TableRow<O> row, ObservableList<O> list, Consumer<O> duplicate){

        MenuItem menuMoveUp = new MenuItem("Move Up");
        menuMoveUp.setOnAction(e -> moveItemUp(row.getItem(), list));
        menu.getItems().add(menuMoveUp);

        MenuItem menuMoveDown = new MenuItem("Move Down");
        menuMoveDown.setOnAction(e -> moveItemDown(row.getItem(), list));
        menu.getItems().add(menuMoveDown);

        menu.getItems().add(new SeparatorMenuItem());

        MenuItem menuDelete = new MenuItem("Delete");
        menuDelete.setOnAction(e -> deleteItem(row.getItem(), list));
        menu.getItems().add(menuDelete);

        MenuItem menuDuplicate = new MenuItem("Duplicate");
        menuDuplicate.setOnAction(e -> duplicate.accept(row.getItem()));
        menu.getItems().add(menuDuplicate);
    }

    public static <O> void moveItemUp(O item, ObservableList<O> list){
        if(item == null) return;
        int index = list.indexOf(item);
        if(index != 0){
            list.remove(index);
            list.add(index-1,item);
        }
    }

    public static <O> void moveItemDown(O item, ObservableList<O> list){
        if(item == null) return;
        int index = list.indexOf(item);
        if(index != list.size()-1){
            list.remove(index);
            list.add(index+1, item);
        }
    }

    public static <O> void deleteItem(O item, ObservableList<O> list){
        if(item == null) return;
        list.remove(item);
    }

    public static void openImageFilterDialog(ImageFilterRegistry.ObservableImageFilter filter){
        if(filter != null){
            Dialog<ImageFilterRegistry.ObservableImageFilter> dialog = ImageFilterRegistry.getDialogForFilter(filter);
            Optional<ImageFilterRegistry.ObservableImageFilter> result = dialog.showAndWait();
            //TODO MAKE DIALOG APPEAR ON THE CORRECT DISPLAY
            if(result.isPresent()){
                if(result.get() != filter){ //if the dialog was cancelled copy the settings of the original
                    GenericSetting.applySettings(result.get().filterSettings, filter.filterSettings);
                    DrawingBotV3.INSTANCE.onImageFiltersChanged();
                }
            }
        }
    }

}
