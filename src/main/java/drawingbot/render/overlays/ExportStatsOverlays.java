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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

        TextFlow statTextFlow = new TextFlow();
        FXHelper.addText(statTextFlow, 12, "bold", "File: ");

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
        entryComboBox.setPromptText("Export File");
        statTextFlow.getChildren().add(entryComboBox);

        Button openButton = new Button("Open");
        openButton.setOnAction(e -> {
            if(selectedEntry.get() != null){
                FXHelper.openFolder(selectedEntry.get().file);
            }
        });
        statTextFlow.getChildren().add(openButton);

        Button openFolderButton = new Button("Open Folder");
        openFolderButton.setOnAction(e -> {
            if(selectedEntry.get() != null){
                FXHelper.openFolder(selectedEntry.get().file.getParentFile());
            }
        });
        statTextFlow.getChildren().add(openFolderButton);
        FXHelper.addText(statTextFlow, 12, "bold", " \n\n");

        ////////

        addStat(statTextFlow, "Shapes", preStats.geometryCount, postStats.geometryCount, "");
        addStat(statTextFlow,"Total Travel", preStats.totalTravelMM, postStats.totalTravelMM, "m");
        addStat(statTextFlow,"Distance Down", preStats.distanceDownMM, postStats.distanceDownMM, "m");
        addStat(statTextFlow,"Distance Up", preStats.distanceUpMM, postStats.distanceUpMM, "m");
        addStat(statTextFlow,"Pen Lifts", preStats.penLifts, postStats.penLifts, "");
        addDimensions(statTextFlow,"Page Size", postStats.pageWidthMM, postStats.pageHeightMM, postStats.drawingUnitsMM);
        addDimensions(statTextFlow,"Drawing Size", postStats.drawingWidthMM, postStats.drawingHeightMM, postStats.drawingUnitsMM);
        vBox.getChildren().add(statTextFlow);

        ////////

        TextFlow statPenFlow = new TextFlow();
        vBox.getChildren().add(statPenFlow);
        postStats.penStats.addListener((observable, oldValue, newValue) -> updatePenColours(statPenFlow, newValue));

        ////////

        stackPane.getChildren().add(vBox);
        scrollPane.setContent(stackPane);

        DrawingBotV3.INSTANCE.controller.viewportOverlayAnchorPane.getChildren().add(scrollPane);
    }

    public void addStat(TextFlow flow, String name, Property<?> before, Property<?> after, String suffix){
        FXHelper.addText(flow, 12, "bold", name + ": ");

        Text text = new Text();
        text.setStyle("-fx-font-size: 12px;");
        text.textProperty().bind(Bindings.createStringBinding(() -> before.getValue().toString() + "" + suffix + " -> " + after.getValue().toString() + "" + suffix + " \n", before, after));
        flow.getChildren().add(text);
    }

    public void addDimensions(TextFlow flow, String name, Property<?> before, Property<?> after, Property<UnitsLength> units){
        FXHelper.addText(flow, 12, "bold", name + ": ");
        Text text = new Text();
        text.setStyle("-fx-font-size: 12px;");
        text.textProperty().bind(Bindings.createStringBinding(() -> before.getValue().toString() + "" + units.getValue().getSuffix() + " x " + after.getValue().toString() + "" + units.getValue().getSuffix() + " \n", before, after, units));
        flow.getChildren().add(text);
    }

    public void updatePenColours(TextFlow flow, Map<DrawingPen, Double> penStats){
        flow.getChildren().clear();
        FXHelper.addText(flow, 14, "bold", "Pens:\n");
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