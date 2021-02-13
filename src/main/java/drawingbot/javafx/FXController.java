package drawingbot.javafx;

import drawingbot.api.IDrawingPen;
import drawingbot.files.*;
import drawingbot.DrawingBotV3;
import drawingbot.drawing.*;
import drawingbot.image.ImageFilterRegistry;
import drawingbot.image.ImageTools;
import drawingbot.api.IPathFindingModule;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.utils.GenericPreset;
import drawingbot.utils.GenericSetting;
import drawingbot.utils.GenericFactory;
import drawingbot.pfm.PFMMasterRegistry;
import drawingbot.plotting.PlottingTask;
import drawingbot.utils.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

public class FXController {

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

        viewportStackPane.setOnMousePressed(DrawingBotV3::mousePressedJavaFX);
        viewportStackPane.setOnMouseDragged(DrawingBotV3::mouseDraggedJavaFX);
        viewportStackPane.getChildren().add(DrawingBotV3.canvas);

        viewportStackPane.prefHeightProperty().bind(DrawingBotV3.canvas.heightProperty().multiply(4));
        viewportStackPane.prefWidthProperty().bind(DrawingBotV3.canvas.widthProperty().multiply(4));
        viewportScrollPane.setHvalue(0.5);
        viewportScrollPane.setVvalue(0.5);

        DrawingBotV3.logger.exiting("FX Controller", "initialize");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////GLOBAL CONTAINERS
    public ScrollPane scrollPaneSettings = null;
    public VBox vBoxSettings = null;

    public PresetRenameDialog presetEditorDialog = new PresetRenameDialog();
    public static GenericPreset editingPreset = null;

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
        menuImportURL.setOnAction(e -> importURL());
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
            PlottingTask task = DrawingBotV3.getActiveTask();
            if(task != null){
                int lines = (int)Utils.mapDouble(newValue.doubleValue(), 0, 1, 0, task.plottedDrawing.getPlottedLineCount());
                task.plottedDrawing.displayedLineCount.setValue(lines);
                textFieldDisplayedLines.setText(String.valueOf(lines));
                DrawingBotV3.reRender();
            }
        });

        textFieldDisplayedLines.setOnAction(e -> {
            PlottingTask task = DrawingBotV3.getActiveTask();
            if(task != null){
                int lines = (int)Math.max(0, Math.min(task.plottedDrawing.getPlottedLineCount(), Double.parseDouble(textFieldDisplayedLines.getText())));
                task.plottedDrawing.displayedLineCount.setValue(lines);
                textFieldDisplayedLines.setText(String.valueOf(lines));
                sliderDisplayedLines.setValue((double)lines / task.plottedDrawing.getPlottedLineCount());
                DrawingBotV3.reRender();
            }
        });

        choiceBoxDisplayMode.getItems().addAll(EnumDisplayMode.values());
        choiceBoxDisplayMode.setValue(EnumDisplayMode.DRAWING);
        DrawingBotV3.display_mode.bindBidirectional(choiceBoxDisplayMode.valueProperty());
        DrawingBotV3.display_mode.addListener((observable, oldValue, newValue) -> DrawingBotV3.reRender());

        DrawingBotV3.displayGrid.bind(checkBoxShowGrid.selectedProperty());
        DrawingBotV3.displayGrid.addListener((observable, oldValue, newValue) -> DrawingBotV3.reRender());

        buttonZoomIn.setOnAction(e -> {
            DrawingBotV3.scaleMultiplier.set(DrawingBotV3.scaleMultiplier.getValue() + 0.1);
        });
        buttonZoomOut.setOnAction(e -> {
            if(DrawingBotV3.scaleMultiplier.getValue() > DrawingBotV3.minScale){
                DrawingBotV3.scaleMultiplier.set(DrawingBotV3.scaleMultiplier.getValue() - 0.1);
            }
        });
        DrawingBotV3.scaleMultiplier.addListener((observable, oldValue, newValue) -> DrawingBotV3.canvasNeedsUpdate = true);

        buttonResetView.setOnAction(e -> {
            viewportScrollPane.setHvalue(0.5);
            viewportScrollPane.setVvalue(0.5);
            DrawingBotV3.scaleMultiplier.set(1.0);
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
        buttonStartPlotting.setOnAction(param -> DrawingBotV3.startPlotting());
        buttonStartPlotting.disableProperty().bind(DrawingBotV3.isPlotting);
        buttonStopPlotting.setOnAction(param -> DrawingBotV3.stopPlotting());
        buttonStopPlotting.disableProperty().bind(DrawingBotV3.isPlotting.not());
        buttonResetPlotting.setOnAction(param -> DrawingBotV3.resetPlotting());
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

    /////SIZING OPTIONS
    public CheckBox checkBoxOriginalSizing = null;
    public TextField textFieldDrawingWidth = null;
    public TextField textFieldDrawingHeight = null;
    public ChoiceBox<Units> choiceBoxDrawingUnits = null;

    ////GCODE SETTINGS
    public TextField textFieldOffsetX = null;
    public TextField textFieldOffsetY = null;
    public TextField textFieldPenUpZ = null;
    public TextField textFieldPenDownZ = null;
    public CheckBox checkBoxAutoHome = null;

    public void initDrawingAreaPane(){

        /////SIZING OPTIONS
        DrawingBotV3.useOriginalSizing.bind(checkBoxOriginalSizing.selectedProperty());
        textFieldDrawingWidth.disableProperty().bind(checkBoxOriginalSizing.selectedProperty());
        textFieldDrawingHeight.disableProperty().bind(checkBoxOriginalSizing.selectedProperty());

        DrawingBotV3.drawingAreaWidth.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldDrawingWidth.textProperty().get()), textFieldDrawingWidth.textProperty()));
        textFieldDrawingWidth.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.drawingAreaHeight.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldDrawingHeight.textProperty().get()), textFieldDrawingHeight.textProperty()));
        textFieldDrawingHeight.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        //choiceBoxDrawingUnits.disableProperty().bind(checkBoxOriginalSizing.selectedProperty());
        choiceBoxDrawingUnits.getItems().addAll(Units.values());
        choiceBoxDrawingUnits.setValue(Units.MILLIMETRES);
        DrawingBotV3.inputUnits.bindBidirectional(choiceBoxDrawingUnits.valueProperty());

        ////GCODE SETTINGS
        checkBoxAutoHome.setSelected(true);
        DrawingBotV3.enableAutoHome.bind(checkBoxAutoHome.selectedProperty());

        DrawingBotV3.gcodeOffsetX.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldOffsetX.textProperty().get()), textFieldOffsetX.textProperty()));
        textFieldOffsetX.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.gcodeOffsetY.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldOffsetY.textProperty().get()), textFieldOffsetY.textProperty()));
        textFieldOffsetY.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));

        DrawingBotV3.penUpZ.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldPenUpZ.textProperty().get()), textFieldPenUpZ.textProperty()));
        textFieldPenUpZ.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 5F));

        DrawingBotV3.penDownZ.bind(Bindings.createFloatBinding(() -> Float.valueOf(textFieldPenDownZ.textProperty().get()), textFieldPenDownZ.textProperty()));
        textFieldPenDownZ.textFormatterProperty().setValue(new TextFormatter<>(new FloatStringConverter(), 0F));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PRE PROCESSING PANE

    public ComboBox<GenericPreset> comboBoxImageFilterPreset = null; //TODO PRESETS

    public MenuItem menuNewFilterPreset = null; //TODO MAKE DEFAULT FILTER MENU
    public MenuItem menuUpdateFilterPreset = null;
    public MenuItem menuDeleteFilterPreset = null;
    public MenuItem menuImportFilterPreset = null;
    public MenuItem menuExportFilterPreset = null;

    public TableView<ImageFilterRegistry.ObservableImageFilter> tableViewImageFilters = null;
    public TableColumn<ImageFilterRegistry.ObservableImageFilter, Boolean> columnEnableImageFilter = null;
    public TableColumn<ImageFilterRegistry.ObservableImageFilter, String> columnImageFilterType = null;
    public TableColumn<ImageFilterRegistry.ObservableImageFilter, ObservableList<GenericSetting<?, ?>>> columnImageFilterSettings = null;

    public ComboBox<GenericFactory<ImageFilterRegistry.IImageFilter>> comboBoxImageFilter = null;
    public Button buttonAddFilter = null;

    public void initPreProcessingPane(){
        comboBoxImageFilterPreset.setItems(ImageFilterRegistry.imagePresets);
        comboBoxImageFilterPreset.setValue(ImageFilterRegistry.getDefaultImageFilterPreset());
        comboBoxImageFilterPreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                PresetManager.FILTERS.loadSettings(newValue);
            }
        });

        tableViewImageFilters.setItems(ImageFilterRegistry.currentFilters);
        tableViewImageFilters.setRowFactory(param -> {
            TableRow<ImageFilterRegistry.ObservableImageFilter> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setContextMenu(new ObservableFilterContextMenu(row));
            row.setPrefHeight(30);
            return row;
        });

        columnEnableImageFilter.setCellFactory(param -> new CheckBoxTableCell<>(index -> columnEnableImageFilter.getCellObservableValue(index)));
        columnEnableImageFilter.setCellValueFactory(param -> param.getValue().enable);

        columnImageFilterType.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        columnImageFilterType.setCellValueFactory(param -> param.getValue().name);

        columnImageFilterSettings.setCellFactory(param -> new ImageFilterSettingsTableCell());
        columnImageFilterSettings.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().filterSettings));

        comboBoxImageFilter.setItems(ImageFilterRegistry.filterFactories);
        comboBoxImageFilter.setValue(ImageFilterRegistry.filterFactories.get(0));
        buttonAddFilter.setOnAction(e -> {
            if(comboBoxImageFilter.getValue() != null){
                ImageFilterRegistry.currentFilters.add(new ImageFilterRegistry.ObservableImageFilter(comboBoxImageFilter.getValue()));
            }
        });

        menuNewFilterPreset.setOnAction(e -> {
            editingPreset = PresetManager.FILTERS.createNewPreset("", "New Preset", true);
            PresetManager.FILTERS.saveSettings(editingPreset);

            presetEditorDialog.updateDialog();
            presetEditorDialog.setTitle("Save new preset");
            Optional<GenericPreset> result = presetEditorDialog.showAndWait();
            if(result.isPresent()){
                PresetManager.FILTERS.savePreset(editingPreset);
                comboBoxImageFilterPreset.setValue(editingPreset);
            }
        });

        menuUpdateFilterPreset.setOnAction(e -> {
            GenericPreset preset = PresetManager.FILTERS.updatePreset(comboBoxImageFilterPreset.getValue());
            if(preset != null){
                comboBoxImageFilterPreset.setValue(preset);
                comboBoxImageFilterPreset.setItems(ImageFilterRegistry.imagePresets);
            }
        });

        menuDeleteFilterPreset.setOnAction(e -> {
            if(PresetManager.FILTERS.deletePreset(comboBoxImageFilterPreset.getValue())){
                comboBoxImageFilterPreset.setValue(ImageFilterRegistry.getDefaultImageFilterPreset());
            }
        });

        menuImportFilterPreset.setOnAction(e -> importPFMPreset(EnumPresetType.IMAGE_FILTER_PRESET));
        menuExportFilterPreset.setOnAction(e -> exportPFMPreset(comboBoxImageFilterPreset.getValue()));
    }

    public static class ImageFilterSettingsTableCell extends TableCell<ImageFilterRegistry.ObservableImageFilter, ObservableList<GenericSetting<?, ?>>>{

        public ImageFilterSettingsTableCell(){
            super();
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        protected void updateItem(ObservableList<GenericSetting<?, ?>> item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.toString());
                if(item.isEmpty()){
                    setGraphic(null);
                }else{
                    HBox hBox = new HBox();
                    hBox.maxWidth(getWidth());
                    hBox.maxHeight(getHeight());
                    for(GenericSetting<?, ?> setting : item){
                        TextField field = new TextField();
                        GenericSettingStringConverter<?> stringConverter = new GenericSettingStringConverter<>(() -> setting);
                        field.setText(setting.getValueAsString());
                        field.setOnAction(e -> {
                            Object obj = stringConverter.fromString(field.getText());
                            setting.setValue(obj);
                            if(!field.getText().equals(setting.getValueAsString())){
                                field.setText(setting.getValueAsString());
                            }
                        });
                        field.setMaxWidth(80);
                        field.maxHeight(getHeight());
                        Label label = new Label(setting.settingName.getValue() + ": ");
                        label.setGraphic(field);
                        label.setContentDisplay(ContentDisplay.RIGHT);
                        hBox.getChildren().add(label);
                    }
                    setGraphic(hBox);
                }

            }
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PATH FINDING CONTROLS
    public ChoiceBox<GenericFactory<IPathFindingModule>> choiceBoxPFM = null;

    public ComboBox<GenericPreset> comboBoxPFMPreset = null;
    public MenuItem menuNewPreset = null;
    public MenuItem menuUpdatePreset = null;
    public MenuItem menuDeletePreset = null;
    public MenuItem menuImportPreset = null;
    public MenuItem menuExportPreset = null;


    public TableView<GenericSetting<?,?>> tableViewAdvancedPFMSettings = null;
    public TableColumn<GenericSetting<?, ?>, String> tableColumnSetting = null;
    public TableColumn<GenericSetting<?, ?>, Object> tableColumnValue = null;

    public Button buttonPFMSettingReset = null;
    public Button buttonPFMSettingRandom = null;
    public Button buttonPFMSettingHelp = null;

    public void initPFMControls(){

        ////PATH FINDING CONTROLS
        choiceBoxPFM.setItems(PFMMasterRegistry.getObservablePFMLoaderList());
        choiceBoxPFM.setValue(PFMMasterRegistry.getDefaultPFMFactory());
        choiceBoxPFM.setOnAction(e -> changePathFinderModule(choiceBoxPFM.getSelectionModel().getSelectedItem()));
        DrawingBotV3.pfmFactory.bindBidirectional(choiceBoxPFM.valueProperty());


        comboBoxPFMPreset.setItems(PFMMasterRegistry.getObservablePFMPresetList());
        comboBoxPFMPreset.setValue(PFMMasterRegistry.getDefaultPFMPreset());
        comboBoxPFMPreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                PresetManager.PFM.loadSettings(newValue);
            }
        });

        DrawingBotV3.pfmFactory.addListener((observable, oldValue, newValue) -> {
            comboBoxPFMPreset.setItems(PFMMasterRegistry.getObservablePFMPresetList(newValue));
            comboBoxPFMPreset.setValue(PFMMasterRegistry.getDefaultPFMPreset(newValue));
        });

        menuNewPreset.setOnAction(e -> {
            editingPreset = PresetManager.PFM.createNewPreset(DrawingBotV3.pfmFactory.get().getName(), "New Preset", true);
            PresetManager.PFM.saveSettings(editingPreset);

            presetEditorDialog.updateDialog();
            presetEditorDialog.setTitle("Save new preset");
            Optional<GenericPreset> result = presetEditorDialog.showAndWait();
            if(result.isPresent()){
                PresetManager.PFM.savePreset(editingPreset);
                comboBoxPFMPreset.setValue(editingPreset);
            }
        });

        menuUpdatePreset.setOnAction(e -> {
            GenericPreset preset = PresetManager.PFM.updatePreset(comboBoxPFMPreset.getValue());
            if(preset != null){
                comboBoxPFMPreset.setValue(preset);
                comboBoxPFMPreset.setItems(PFMMasterRegistry.getObservablePFMPresetList());
            }
        });

        menuDeletePreset.setOnAction(e -> {
            if(PresetManager.PFM.deletePreset(comboBoxPFMPreset.getValue())){
                comboBoxPFMPreset.setValue(PFMMasterRegistry.getDefaultPFMPreset());
            }
        });

        menuImportPreset.setOnAction(e -> importPFMPreset(EnumPresetType.PFM_PRESET));
        menuExportPreset.setOnAction(e -> exportPFMPreset(comboBoxPFMPreset.getValue()));

        tableViewAdvancedPFMSettings.setItems(PFMMasterRegistry.getObservablePFMSettingsList());
        DrawingBotV3.pfmFactory.addListener((observable, oldValue, newValue) -> tableViewAdvancedPFMSettings.setItems(PFMMasterRegistry.getObservablePFMSettingsList()));

        tableColumnSetting.setCellValueFactory(param -> param.getValue().settingName);

        tableColumnValue.setCellFactory(param -> {
            TextFieldTableCell<GenericSetting<?, ?>, Object> cell = new TextFieldTableCell<>();
            cell.setConverter(new GenericSettingStringConverter(() -> cell.tableViewProperty().get().getItems().get(cell.getIndex())));
            return cell;
        });
        tableColumnValue.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

        buttonPFMSettingReset.setOnAction(e -> {
            PresetManager.PFM.loadSettings(comboBoxPFMPreset.getValue());
        });

        buttonPFMSettingRandom.setOnAction(e -> PFMMasterRegistry.randomiseSettings(tableViewAdvancedPFMSettings.getItems()));
        buttonPFMSettingHelp.setOnAction(e -> openURL(Utils.URL_GITHUB_PFM_DOCS));

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PEN SETTINGS

    public ComboBox<DrawingSet> comboBoxDrawingSet = null;

    public TableView<ObservableDrawingPen> penTableView = null;
    public TableColumn<ObservableDrawingPen, Boolean> penEnableColumn = null;
    public TableColumn<ObservableDrawingPen, String> penNameColumn = null;
    public TableColumn<ObservableDrawingPen, Color> penColourColumn = null;
    public TableColumn<ObservableDrawingPen, String> penPercentageColumn = null;
    public TableColumn<ObservableDrawingPen, Integer> penWeightColumn = null;
    public TableColumn<ObservableDrawingPen, Integer> penLinesColumn = null;

    public ComboBox<DrawingPen> comboBoxDrawingPen = null;
    public Button buttonAddPen = null;

    public ComboBox<EnumDistributionOrder> renderOrderComboBox = null;
    public ComboBox<EnumBlendMode> blendModeComboBox = null;

    public void initPenSettingsPane(){

        comboBoxDrawingSet.setItems(FXCollections.observableArrayList(DrawingRegistry.INSTANCE.registeredSets.values()));
        comboBoxDrawingSet.setValue(DrawingRegistry.INSTANCE.getDefaultSet());
        comboBoxDrawingSet.setOnAction(e -> changeDrawingSet(comboBoxDrawingSet.getSelectionModel().getSelectedItem()));
        comboBoxDrawingSet.setCellFactory(param -> new ComboCellDrawingSet());
        comboBoxDrawingSet.setButtonCell(new ComboCellDrawingSet());

        penTableView.setRowFactory(param -> {
            TableRow<ObservableDrawingPen> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setContextMenu(new ObservablePenContextMenu(row));
            return row;
        });


        penTableView.setItems(DrawingBotV3.observableDrawingSet.pens);
        penTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(DrawingBotV3.display_mode.get() == EnumDisplayMode.SELECTED_PEN){
                DrawingBotV3.reRender();
            }
        });

        penNameColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        penNameColumn.setCellValueFactory(param -> param.getValue().name);

        penColourColumn.setCellFactory(ColorTableCell::new);
        penColourColumn.setCellValueFactory(param -> param.getValue().javaFXColour);

        penEnableColumn.setCellFactory(param -> new CheckBoxTableCell<>(index -> penEnableColumn.getCellObservableValue(index)));
        penEnableColumn.setCellValueFactory(param -> param.getValue().enable);

        penPercentageColumn.setCellValueFactory(param -> param.getValue().currentPercentage);

        penWeightColumn.setCellFactory(param -> new TextFieldTableCell<>(new IntegerStringConverter()));
        penWeightColumn.setCellValueFactory(param -> param.getValue().distributionWeight.asObject());

        penLinesColumn.setCellValueFactory(param -> param.getValue().currentLines.asObject());

        comboBoxDrawingPen.setItems(FXCollections.observableArrayList(DrawingRegistry.INSTANCE.registeredPens.values()));
        comboBoxDrawingPen.setValue(DrawingRegistry.INSTANCE.getDefaultPen());
        comboBoxDrawingPen.setCellFactory(param -> new ComboCellDrawingPen());
        comboBoxDrawingPen.setButtonCell(new ComboCellDrawingPen());
        buttonAddPen.setOnAction(e -> DrawingBotV3.observableDrawingSet.addNewPen(comboBoxDrawingPen.getValue()));

        renderOrderComboBox.setItems(FXCollections.observableArrayList(EnumDistributionOrder.values()));
        renderOrderComboBox.valueProperty().bindBidirectional(DrawingBotV3.observableDrawingSet.renderOrder);

        blendModeComboBox.setItems(FXCollections.observableArrayList(EnumBlendMode.values()));
        blendModeComboBox.valueProperty().bindBidirectional(DrawingBotV3.observableDrawingSet.blendMode);


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

    public void changePathFinderModule(GenericFactory<IPathFindingModule> pfm){
        DrawingBotV3.pfmFactory.set(pfm);
    }

    public void changeDrawingSet(DrawingSet set){
        DrawingBotV3.observableDrawingSet.loadDrawingSet(set);
    }

    public void importURL(){
        String url = getClipboardString();
        if (url != null && url.toLowerCase().matches("^https?:...*(jpg|png)")) {
            DrawingBotV3.logger.info("Image URL found on clipboard: " + url);
            DrawingBotV3.openImage(url, false);
        }
    }

    public void importFile(){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().add(FileUtils.IMPORT_IMAGES);
            d.setTitle("Select an image file to sketch");
            d.setInitialDirectory(new File(FileUtils.getUserHomeDirectory()));
            File file = d.showOpenDialog(null);
            if(file != null){
                DrawingBotV3.openImage(file.getAbsolutePath(), false);
            }
        });
    }

    public void exportFile(ExportFormats format, boolean seperatePens){
        if(DrawingBotV3.getActiveTask() == null){
            return;
        }
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(format.filters);
            d.setTitle(format.getDialogTitle());
            d.setInitialDirectory(new File(FileUtils.getUserHomeDirectory()));
            //TODO SET INITIAL FILENAME!!!
            File file = d.showSaveDialog(null);
            if(file != null){
                DrawingBotV3.createExportTask(format, DrawingBotV3.getActiveTask(), ExportFormats::defaultFilter, d.getSelectedExtensionFilter().getExtensions().get(0).substring(1), file, seperatePens);
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

    public void importPFMPreset(EnumPresetType presetType){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().add(FileUtils.FILTER_JSON);
            d.setTitle("Select a preset to import");
            d.setInitialDirectory(new File(FileUtils.getUserHomeDirectory()));
            File file = d.showOpenDialog(null);
            if(file != null){
                PresetManager.importPresetFile(file, presetType);
            }
        });
    }

    public void exportPFMPreset(GenericPreset preset){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(FileUtils.FILTER_JSON);
            d.setTitle("Save preset");
            d.setInitialDirectory(new File(FileUtils.getUserHomeDirectory()));
            d.setInitialFileName(preset.presetName + " - Preset");
            File file = d.showSaveDialog(null);
            if(file != null){
                PresetManager.exportPresetFile(file, preset);
            }
        });
    }

    //// EXTERNALLY TRIGGERED EVENTS / UI UPDATES

    public void onTaskStageFinished(PlottingTask task, EnumTaskStage stage){
        switch (stage){
            case QUEUED:
                break;
            case LOADING_IMAGE:
                break;
            case PRE_PROCESSING:
                break;
            case DO_PROCESS:
                sliderDisplayedLines.setValue(1.0F);
                textFieldDisplayedLines.setText(String.valueOf(task.plottedDrawing.getPlottedLineCount()));
                break;
            case POST_PROCESSING:
                break;
            case FINISHING:
                break;
            case FINISHED:
                break;
        }
    }

    public static class PresetRenameDialog extends Dialog<GenericPreset>{

        public Label labelPresetSubType;
        public Label labelPresetType;
        public TextField nameField;

        public PresetRenameDialog() {
            super();
            VBox vBox = new VBox();

            labelPresetSubType = new Label("Preset Subtype: "); //TODO REMOVE???
            //vBox.getChildren().add(labelTargetPFM);

            labelPresetType = new Label("Preset Type: ");
            //vBox.getChildren().add(labelTotalSettings);

            Label nameFieldLabel = new Label("Preset Name: ");
            nameField = new TextField();
            nameField.textProperty().addListener((observable, oldValue, newValue) -> FXController.editingPreset.presetName = newValue);
            nameFieldLabel.setGraphic(nameField);
            nameFieldLabel.setContentDisplay(ContentDisplay.RIGHT);
            vBox.getChildren().add(nameFieldLabel);

            setGraphic(vBox);
            setResultConverter(param -> param == ButtonType.APPLY ? editingPreset : null);
            getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        }

        public void updateDialog(){
            labelPresetSubType.setText("Preset Subtype: " + FXController.editingPreset.presetSubType);
            nameField.setText(FXController.editingPreset.presetName);
        }


    }

    public static class GenericSettingStringConverter<V> extends StringConverter<V>{

        public Supplier<GenericSetting<?, V>> supplier;

        public GenericSettingStringConverter(Supplier<GenericSetting<?, V>> supplier){
            this.supplier = supplier;
        }

        public String toString(V object){
            GenericSetting<?, V> setting = supplier.get();
            return setting.stringConverter.toString(object);
        }

        public V fromString(String string){
            GenericSetting<?, V> setting = supplier.get();
            try {
                V value = setting.stringConverter.fromString(string);
                return setting.validator.apply(value);
            } catch (Exception e) {
                DrawingBotV3.logger.info("Invalid input: " + string + " for setting " + setting.settingName.getName());
            }
            return setting.value.get();
        }
    }

    public static class ComboCellDrawingSet extends ComboBoxListCell<DrawingSet> {

        public ComboCellDrawingSet(){
            super();

        }

        @Override
        public void updateItem(DrawingSet item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText("  " + item.toString());
                HBox box = new HBox();
                for(IDrawingPen pen : item.getPens()){
                    box.getChildren().add(new Rectangle(10, 12, ImageTools.getColorFromARGB(pen.getARGB())));
                }
                setGraphic(box);
            }
        }
    }

    public static class ComboCellDrawingPen extends ComboBoxListCell<DrawingPen> {

        public final Rectangle colour;

        public ComboCellDrawingPen(){
            super();
            colour = new Rectangle(20, 12, Color.AQUA);
        }

        @Override
        public void updateItem(DrawingPen item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText("  " + item.toString());
                setGraphic(colour);
                colour.setFill(ImageTools.getColorFromARGB(item.getARGB()));
            }
        }
    }

    ///// CONTEXT MENUS \\\\\

    public static <O> void addDefaultTableViewContextMenuItems(ContextMenu menu, TableRow<O> row, ObservableList<O> list, Consumer<O> duplicate){

        MenuItem menuMoveUp = new MenuItem("Move Up");
        menuMoveUp.setOnAction(e -> {
            int index =list.indexOf(row.getItem());
            if(index != 0){
                list.remove(index);
                list.add(index-1, row.getItem());
            }
        });
        menu.getItems().add(menuMoveUp);

        MenuItem menuMoveDown = new MenuItem("Move Down");
        menuMoveDown.setOnAction(e -> {
            int index = list.indexOf(row.getItem());
            if(index != list.size()-1){
                list.remove(index);
                list.add(index+1, row.getItem());
            }
        });
        menu.getItems().add(menuMoveDown);

        menu.getItems().add(new SeparatorMenuItem());

        MenuItem menuDelete = new MenuItem("Delete");
        menuDelete.setOnAction(e -> list.remove(row.getItem()));
        menu.getItems().add(menuDelete);

        MenuItem menuDuplicate = new MenuItem("Duplicate");
        menuDuplicate.setOnAction(e -> duplicate.accept(row.getItem()));
        menu.getItems().add(menuDuplicate);
    }

    public static class ObservableFilterContextMenu extends ContextMenu{

        public ObservableFilterContextMenu(TableRow<ImageFilterRegistry.ObservableImageFilter> row){
            super();

            addDefaultTableViewContextMenuItems(this, row, ImageFilterRegistry.currentFilters, f -> ImageFilterRegistry.currentFilters.add(new ImageFilterRegistry.ObservableImageFilter(f)));
        }
    }

    public static class ObservablePenContextMenu extends ContextMenu{

        public ObservablePenContextMenu(TableRow<ObservableDrawingPen> row){
            super();

            MenuItem increaseWeight = new MenuItem("Increase Weight");
            increaseWeight.setOnAction(e -> row.getItem().distributionWeight.set(row.getItem().distributionWeight.get() + 10));
            getItems().add(increaseWeight);

            MenuItem decreaseWeight = new MenuItem("Decrease Weight");
            decreaseWeight.setOnAction(e -> row.getItem().distributionWeight.set(Math.max(0, row.getItem().distributionWeight.get() - 10)));
            getItems().add(decreaseWeight);

            MenuItem resetWeight = new MenuItem("Reset Weight");
            resetWeight.setOnAction(e -> row.getItem().distributionWeight.set(100));
            getItems().add(resetWeight);

            getItems().add(new SeparatorMenuItem());

            addDefaultTableViewContextMenuItems(this, row, DrawingBotV3.observableDrawingSet.pens, p -> DrawingBotV3.observableDrawingSet.addNewPen(p));
        }

    }

}
