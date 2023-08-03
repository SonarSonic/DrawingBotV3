package drawingbot.render.overlays;

import drawingbot.DrawingBotV3;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.ExportedDrawingEntry;
import drawingbot.image.ImageTools;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.observables.ObservableDrawingStats;
import drawingbot.registry.Register;
import drawingbot.utils.UnitsLength;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Map;

public class ExportStatsOverlays extends AbstractOverlay {

    public static final ExportStatsOverlays INSTANCE = new ExportStatsOverlays();

    public final ObservableDrawingStats preStats = new ObservableDrawingStats();
    public final ObservableDrawingStats postStats = new ObservableDrawingStats();

    public SimpleObjectProperty<ExportedDrawingEntry> selectedEntry = new SimpleObjectProperty<>();
    public SimpleObjectProperty<ObservableList<ExportedDrawingEntry>> exportEntries = new SimpleObjectProperty<>();

    {
        selectedEntry.addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                preStats.updateFromStatic(newValue.before);
                postStats.updateFromStatic(newValue.after);
                DrawingBotV3.project().setExportDrawing(newValue.drawing);
            }else{
                preStats.reset();
                postStats.reset();
                DrawingBotV3.project().setExportDrawing(null);
            }
        });
    }

    @Override
    public void init() {
        exportEntries.bind(DrawingBotV3.INSTANCE.exportedDrawingsBinding);
        activeProperty().bind(DrawingBotV3.INSTANCE.displayMode.isEqualTo(Register.INSTANCE.DISPLAY_MODE_EXPORT_DRAWING));

        ScrollPane scrollPane = new ScrollPane();
        AnchorPane.setLeftAnchor(scrollPane, 8D);
        AnchorPane.setTopAnchor(scrollPane, 8D);
        scrollPane.visibleProperty().bind(activeProperty());
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setBorder(Border.EMPTY);
        scrollPane.setBackground(Background.EMPTY);
        scrollPane.setMaxHeight(600);
        scrollPane.getStyleClass().add("export-stats-scrollpane");

        StackPane stackPane = new StackPane();
        stackPane.setBackground(new Background(new BackgroundFill(Color.color(220/255F, 220/255F, 220/255F, 0.7), new CornerRadii(15),null)));

        VBox vBox = new VBox();
        vBox.setSpacing(2);
        vBox.setPadding(new Insets(15, 15, 15, 15));

        ////////

        ////////
        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("export-stats-grid");

        ComboBox<ExportedDrawingEntry> entryComboBox = new ComboBox<>();
        entryComboBox.itemsProperty().bind(exportEntries);
        entryComboBox.valueProperty().bindBidirectional(selectedEntry);
        entryComboBox.itemsProperty().addListener(observable -> {
            if(entryComboBox.getItems() == null || entryComboBox.getItems().isEmpty()){
                selectedEntry.set(null);
            }else{
                selectedEntry.set(entryComboBox.getItems().get(0));
            }
        });
        entryComboBox.setPrefWidth(300);
        entryComboBox.setPromptText("Exported File");

        gridPane.add(entryComboBox, 0, gridPane.getRowCount(), 3, 1);


        Label labelName = new Label("Exported File");
        labelName.getStyleClass().add("export-stats-name");

        Button openButton = new Button("Open File");
        openButton.setOnAction(e -> {
            if(selectedEntry.get() != null){
                FXHelper.openFolder(selectedEntry.get().file);
            }
        });

        Button openFolderButton = new Button("Open Folder");
        openFolderButton.setOnAction(e -> {
            if(selectedEntry.get() != null){
                FXHelper.openFolder(selectedEntry.get().file.getParentFile());
            }
        });
        gridPane.addRow(gridPane.getRowCount(), labelName, openButton, openFolderButton);

        ////////

        addGroupHeader(gridPane, "Dimensions");

        addDimensions(gridPane,"Page Size", postStats.pageWidth, postStats.pageHeight, postStats.drawingUnits);
        addDimensions(gridPane,"Drawing Size", postStats.drawingWid, postStats.drawingHeight, postStats.drawingUnits);

        ////////

        addGroupHeader(gridPane, "Path Optimisation");

        Label before = new Label("Before");
        before.getStyleClass().add("export-stats-column-label");
        GridPane.setHalignment(before, HPos.RIGHT);

        Label after = new Label("After");
        after.getStyleClass().add("export-stats-column-label");
        GridPane.setHalignment(after, HPos.RIGHT);

        gridPane.addRow(gridPane.getRowCount(), new Label(""), before, after);

        addStat(gridPane, "Shapes", preStats.geometryCount, postStats.geometryCount, "");
        addStat(gridPane,"Total Travel", preStats.totalTravelM, postStats.totalTravelM, " m");
        addStat(gridPane,"Distance Down", preStats.distanceDownM, postStats.distanceDownM, " m");
        addStat(gridPane,"Distance Up", preStats.distanceUpM, postStats.distanceUpM, " m");
        addStat(gridPane,"Pen Lifts", preStats.penLifts, postStats.penLifts, "");

        addGroupHeader(gridPane, "Pen Export Order");

        vBox.getChildren().add(gridPane);

        ////////

        TextFlow statPenFlow = new TextFlow();
        vBox.getChildren().add(statPenFlow);
        postStats.penStats.addListener((observable, oldValue, newValue) -> updatePenColours(statPenFlow, newValue));

        ////////

        stackPane.getChildren().add(vBox);
        scrollPane.setContent(stackPane);

        DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane.getChildren().add(scrollPane);
    }

    public void addGroupHeader(GridPane gridPane, String name){
        Label labelDimensions = new Label(name);
        labelDimensions.getStyleClass().add("export-stats-header");
        gridPane.add(labelDimensions, 0, gridPane.getRowCount(), 3, 1);
    }

    public void addStat(GridPane gridPane, String name, Property<?> before, Property<?> after, String suffix){
        int row = gridPane.getRowCount();

        Label labelName = new Label(name);
        labelName.getStyleClass().add("export-stats-name");

        Label labelBefore = new Label();
        labelBefore.getStyleClass().add("export-stats-before");
        GridPane.setHalignment(labelBefore, HPos.RIGHT);
        labelBefore.textProperty().bind(Bindings.createStringBinding(() -> before.getValue().toString() + "" + suffix, before));

        Label labelAfter = new Label();
        labelAfter.getStyleClass().add("export-stats-after");
        GridPane.setHalignment(labelAfter, HPos.RIGHT);
        labelAfter.textProperty().bind(Bindings.createStringBinding(() -> after.getValue().toString() + "" + suffix, after));

        gridPane.addRow(row, labelName, labelBefore, labelAfter);
    }

    public void addDimensions(GridPane gridPane, String name, Property<?> before, Property<?> after, Property<UnitsLength> units){
        int row = gridPane.getRowCount();

        Label labelName = new Label(name);
        labelName.getStyleClass().add("export-stats-name");
        gridPane.add(labelName, 0, row);

        Label labelDimensions = new Label();
        labelDimensions.setMinWidth(100);
        labelDimensions.textProperty().bind(Bindings.createStringBinding(() -> before.getValue().toString() + " " + units.getValue().getSuffix() + " x " + after.getValue().toString() + " " + units.getValue().getSuffix(), after, units));
        gridPane.add(labelDimensions, 1, row, 2, 1);
    }

    public void addSpecialStat(GridPane gridPane, String name, StringBinding binding){
        int row = gridPane.getRowCount();

        Label labelName = new Label(name);
        labelName.getStyleClass().add("export-stats-name");
        gridPane.add(labelName, 0, row);

        Label labelValue = new Label();
        labelValue.getStyleClass().add("export-stats-hyperlink");
        labelValue.textProperty().bind(binding);
        gridPane.add(labelValue, 1, row, 2, 1);
    }


    @Deprecated
    public void addStat(TextFlow flow, String name, Property<?> before, Property<?> after, String suffix){
        FXHelper.addText(flow, 12, "bold", name + ": ");

        Text text = new Text();
        text.getStyleClass().add("export-stats-name");
        text.textProperty().bind(Bindings.createStringBinding(() -> before.getValue().toString() + "" + suffix + " -> " + after.getValue().toString() + "" + suffix + " \n", before, after));
        flow.getChildren().add(text);
    }

    @Deprecated
    public void addDimensions(TextFlow flow, String name, Property<?> before, Property<?> after, Property<UnitsLength> units){
        FXHelper.addText(flow, 12, "bold", name + ": ");
        Text text = new Text();
        text.getStyleClass().add("export-stats-name");
        text.textProperty().bind(Bindings.createStringBinding(() -> before.getValue().toString() + "" + units.getValue().getSuffix() + " x " + after.getValue().toString() + "" + units.getValue().getSuffix() + " \n", before, after, units));
        flow.getChildren().add(text);
    }

    public void updatePenColours(TextFlow flow, Map<DrawingPen, Double> penStats){
        flow.getChildren().clear();
        int penIndex = 1;
        for(Map.Entry<DrawingPen, Double> drawingPenEntry : penStats.entrySet()){
            flow.getChildren().add(new Rectangle(12, 12, ImageTools.getColorFromARGB(drawingPenEntry.getKey().getARGB())));
            FXHelper.addText(flow, 12, "bold", " Pen " + penIndex + ": ");
            FXHelper.addText(flow, 12, "normal", drawingPenEntry.getKey().getName());
            FXHelper.addText(flow, 12, "normal", " - " + drawingPenEntry.getValue() + " m" + "\n");
            penIndex++;
        }
    }

    @Override
    public String getName() {
        return "Export Stats";
    }

}