package drawingbot.files.json.adapters;

import drawingbot.drawing.ColourSeparationHandler;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.List;

public class JsonAdapterObservableDrawingSet extends JsonAdapterAbstract<ObservableDrawingSet>{

    public static List<GenericSetting<?, ?>> settings;

    static{
        settings = new ArrayList<>();
        settings.add(GenericSetting.createStringSetting(ObservableDrawingSet.class, "type", "", i -> i.type));
        settings.add(GenericSetting.createStringSetting(ObservableDrawingSet.class, "name", "", i -> i.name));
        settings.add(GenericSetting.createListSetting(ObservableDrawingSet.class, ObservableDrawingPen.class,"pens", new ArrayList<>(), i -> i.pens));
        settings.add(GenericSetting.createOptionSetting(ObservableDrawingSet.class, EnumDistributionOrder.class, "distributionOrder", FXCollections.observableArrayList(EnumDistributionOrder.values()), EnumDistributionOrder.DARKEST_FIRST, i -> i.distributionOrder));
        settings.add(GenericSetting.createOptionSetting(ObservableDrawingSet.class, EnumDistributionType.class, "distributionType", FXCollections.observableArrayList(EnumDistributionType.values()), EnumDistributionType.EVEN_WEIGHTED, i -> i.distributionType));
        settings.add(GenericSetting.createObjectSetting(ObservableDrawingSet.class, ColourSeparationHandler.class, "colourSeperator", Register.DEFAULT_COLOUR_SPLITTER, i -> i.colourSeperator).setValidator(i -> i == null ? Register.DEFAULT_COLOUR_SPLITTER : i));
    }

    @Override
    public List<GenericSetting<?, ?>> getSettings() {
        return settings;
    }

    @Override
    public ObservableDrawingSet getInstance() {
        return new ObservableDrawingSet();
    }
}
