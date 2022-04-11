package drawingbot.files.exporters;

import drawingbot.DrawingBotV3;
import drawingbot.api.IProperties;
import drawingbot.javafx.util.PropertyUtil;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.utils.UnitsLength;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

public class GCodeSettings implements IProperties {

    public final SimpleFloatProperty gcodeOffsetX = new SimpleFloatProperty(0);
    public final SimpleFloatProperty gcodeOffsetY = new SimpleFloatProperty(0);
    public final SimpleObjectProperty<UnitsLength> gcodeUnits = new SimpleObjectProperty<>(UnitsLength.MILLIMETRES);
    public final SimpleStringProperty gcodeStartCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodeEndCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodePenDownCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodePenUpCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodeStartLayerCode = new SimpleStringProperty();
    public final SimpleStringProperty gcodeEndLayerCode = new SimpleStringProperty();
    public final SimpleFloatProperty gcodeCurveFlatness = new SimpleFloatProperty(0.1F);
    public final SimpleBooleanProperty gcodeEnableFlattening = new SimpleBooleanProperty(true);
    public final SimpleBooleanProperty gcodeCenterZeroPoint = new SimpleBooleanProperty(false);
    public final SimpleObjectProperty<GCodeBuilder.CommentType> gcodeCommentType = new SimpleObjectProperty<>(GCodeBuilder.CommentType.BRACKETS);

    public final ObservableList<Property<?>> observables = PropertyUtil.createPropertiesList(gcodeOffsetX, gcodeOffsetY, gcodeUnits, gcodeStartCode, gcodeEndCode, gcodePenDownCode, gcodePenUpCode, gcodeStartLayerCode, gcodeEndLayerCode, gcodeCurveFlatness, gcodeEnableFlattening, gcodeCenterZeroPoint, gcodeCommentType);


    public float getGCodeXOffset(){
        return gcodeUnits.get().toMM(gcodeOffsetX.get());
    }

    public float getGCodeYOffset(){
        return gcodeUnits.get().toMM(gcodeOffsetY.get());
    }

    @Override
    public ObservableList<Property<?>> getProperties() {
        return observables;
    }

    public GCodeSettings copy(){
        GCodeSettings copy = new GCodeSettings();
        copy.gcodeOffsetX.set(gcodeOffsetX.get());
        copy.gcodeOffsetY.set(gcodeOffsetY.get());
        copy.gcodeUnits.set(gcodeUnits.get());

        copy.gcodeStartCode.set(gcodeStartCode.get());
        copy.gcodeEndCode.set(gcodeEndCode.get());
        copy.gcodePenDownCode.set(gcodePenDownCode.get());
        copy.gcodePenUpCode.set(gcodePenUpCode.get());
        copy.gcodeStartLayerCode.set(gcodeStartLayerCode.get());
        copy.gcodeEndLayerCode.set(gcodeEndLayerCode.get());
        copy.gcodeCurveFlatness.set(gcodeCurveFlatness.get());

        copy.gcodeEnableFlattening.set(gcodeEnableFlattening.get());
        copy.gcodeCenterZeroPoint.set(gcodeCenterZeroPoint.get());
        copy.gcodeCommentType.set(gcodeCommentType.get());
        return copy;
    }
}
