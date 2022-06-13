package drawingbot.javafx.controllers;

import drawingbot.DrawingBotV3;
import drawingbot.api.ICanvas;
import drawingbot.geom.masking.MaskingSettings;
import drawingbot.geom.shapes.GEllipse;
import drawingbot.geom.shapes.GRectangle;
import drawingbot.geom.shapes.GShape;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.controls.ContextMenuMaskingShape;
import drawingbot.javafx.controls.ContextMenuObservableFilter;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.render.shapes.JFXShape;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.util.converter.DefaultStringConverter;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.locationtech.jts.awt.PointShapeFactory;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class FXMaskingSettings {

    public SimpleObjectProperty<MaskingSettings> maskingSettings = new SimpleObjectProperty<>();

    ////////////////////////////////////////////////////////

    public HBox hBoxShapeButtons = null;
    public ToggleSwitch toggleSwitchBypassMasking = null;
    public ToggleSwitch toggleSwitchShowMasks = null;
    public TableView<JFXShape> tableViewMasks;

    public TableColumn<JFXShape, Boolean> tableColumnEnable;
    public TableColumn<JFXShape, String> tableColumnName;
    public TableColumn<JFXShape, JFXShape.Type> tableColumnType;

    @FXML
    public void initialize(){

        maskingSettings.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                toggleSwitchBypassMasking.selectedProperty().unbindBidirectional(oldValue.bypassMasking);
                toggleSwitchShowMasks.selectedProperty().unbindBidirectional(oldValue.showMasks);
                tableViewMasks.setItems(FXCollections.observableArrayList());
            }
            if(newValue != null){
                toggleSwitchBypassMasking.selectedProperty().bindBidirectional(newValue.bypassMasking);
                toggleSwitchShowMasks.selectedProperty().bindBidirectional(newValue.showMasks);
                tableViewMasks.setItems(newValue.getMasks());
            }

        });


        tableColumnEnable.setCellFactory(param -> new CheckBoxTableCell<>(index -> tableColumnEnable.getCellObservableValue(index)));
        tableColumnEnable.setCellValueFactory(param -> param.getValue().enabledProperty());

        tableColumnName.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        tableColumnName.setCellValueFactory(param -> param.getValue().name);

        tableColumnType.setCellFactory(param -> new ComboBoxTableCell<>(JFXShape.Type.ADD, JFXShape.Type.SUBTRACT));
        tableColumnType.setCellValueFactory(param -> param.getValue().typeProperty());

        tableViewMasks.setRowFactory(param -> {
            TableRow<JFXShape> row = new TableRow<>();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                if(row.getItem() == null){
                    event.consume();
                }
            });
            row.setContextMenu(new ContextMenuMaskingShape(row, maskingSettings));
            row.setPrefHeight(30);
            return row;
        });

        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        Button square = new Button("Rectangle", fontAwesome.create(FontAwesome.Glyph.SQUARE));
        square.setOnAction(e -> {
            Rectangle2D.Float rectangle2F = getInitialShapeBounds();
            maskingSettings.get().addShape(new JFXShape(new GRectangle(rectangle2F.x, rectangle2F.y, rectangle2F.width, rectangle2F.height)), JFXShape.Type.SUBTRACT, "Rectangle");
        });

        Button circle = new Button("Circle", fontAwesome.create(FontAwesome.Glyph.CIRCLE));
        circle.setOnAction(e -> {
            Rectangle2D.Float rectangle2F = getInitialShapeBounds();
            maskingSettings.get().addShape(new JFXShape(new GEllipse(rectangle2F.x, rectangle2F.y, rectangle2F.width, rectangle2F.height)), JFXShape.Type.SUBTRACT, "Circle");
        });

        Button star = new Button("Star", fontAwesome.create(FontAwesome.Glyph.STAR));
        star.setOnAction(e -> {
            Rectangle2D.Float rectangle2F = getInitialShapeBounds();
            PointShapeFactory factory = new PointShapeFactory.Star(Math.min(rectangle2F.getWidth(), rectangle2F.getHeight()));
            GeneralPath path = new GeneralPath(factory.createPoint(new Point2D.Double(rectangle2F.getCenterX(), rectangle2F.getCenterY())));
            maskingSettings.get().addShape(new JFXShape(new GShape(path)), JFXShape.Type.SUBTRACT, "Star");
        });

        Button times = new Button("X", fontAwesome.create(FontAwesome.Glyph.TIMES));
        times.setOnAction(e -> {
            Rectangle2D.Float rectangle2F = getInitialShapeBounds();
            PointShapeFactory factory = new PointShapeFactory.X(Math.min(rectangle2F.getWidth(), rectangle2F.getHeight()));
            GeneralPath path = new GeneralPath(factory.createPoint(new Point2D.Double(rectangle2F.getCenterX(), rectangle2F.getCenterY())));
            maskingSettings.get().addShape(new JFXShape(new GShape(path)), JFXShape.Type.SUBTRACT, "X");
        });

        /*
        Button triangle = new Button("", fontAwesome.create(FontAwesome.Glyph.TIMES));
        triangle.setOnAction(e -> {
            Rectangle2D.Float rectangle2F = getInitialShapeBounds();
            PointShapeFactory factory = new PointShapeFactory.Triangle(Math.min(rectangle2F.getWidth(), rectangle2F.getHeight()));
            GeneralPath path = new GeneralPath(factory.createPoint(new Point2D.Double(rectangle2F.getCenterX(), rectangle2F.getCenterY())));
            maskingSettings.get().geometries.add(new JFXGeometry(new GShape(path)));
        });

         */

        Button plus = new Button("Plus", fontAwesome.create(FontAwesome.Glyph.PLUS));
        plus.setOnAction(e -> {
            Rectangle2D.Float rectangle2F = getInitialShapeBounds();
            PointShapeFactory factory = new PointShapeFactory.Cross(Math.min(rectangle2F.getWidth(), rectangle2F.getHeight()));
            GeneralPath path = new GeneralPath(factory.createPoint(new Point2D.Double(rectangle2F.getCenterX(), rectangle2F.getCenterY())));
            maskingSettings.get().addShape(new JFXShape(new GShape(path)), JFXShape.Type.SUBTRACT, "Plus");
        });

        hBoxShapeButtons.getChildren().addAll(square, circle, star, times, plus);



    }

    public Rectangle2D.Float getInitialShapeBounds(){
        ICanvas canvas = DrawingBotV3.INSTANCE.displayMode.get().getRenderer().getRefCanvas();

        float scaledWidth = canvas.getDrawingWidth();
        float scaledHeight = canvas.getDrawingHeight();

        float width = scaledWidth/4;
        float height = scaledHeight/4;
        return new Rectangle2D.Float(scaledWidth/2F - width/2F, scaledHeight/2F - height/2F, width, height);
    }

}
