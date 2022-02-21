package drawingbot.files.json.adapters;

import drawingbot.drawing.ColourSeperationHandler;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingPen;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumDistributionType;

import java.util.ArrayList;
import java.util.List;

public class JsonAdapterObservableDrawingSet extends JsonAdapterAbstract<ObservableDrawingSet>{

    public static List<GenericSetting<?, ?>> settings;

    static{
        settings = new ArrayList<>();
        settings.add(GenericSetting.createStringSetting(ObservableDrawingSet.class, "type", "", (I, V) -> I.type.set(V)).setGetter(I -> I.type.get()));
        settings.add(GenericSetting.createStringSetting(ObservableDrawingSet.class, "name", "", (I, V) -> I.name.set(V)).setGetter(I -> I.name.get()));
        settings.add(GenericSetting.createListSetting(ObservableDrawingSet.class, ObservableDrawingPen.class,"pens", new ArrayList<>(), (I, V) -> I.pens.setAll(V)).setGetter((I) -> new ArrayList<>(I.pens)));
        settings.add(GenericSetting.createOptionSetting(ObservableDrawingSet.class, "distributionOrder", List.of(EnumDistributionOrder.values()), EnumDistributionOrder.DARKEST_FIRST, (I, V) -> I.distributionOrder.set(V)).setGetter(I -> I.distributionOrder.get()));
        settings.add(GenericSetting.createOptionSetting(ObservableDrawingSet.class, "distributionType", List.of(EnumDistributionType.values()), EnumDistributionType.EVEN_WEIGHTED, (I, V) -> I.distributionType.set(V)).setGetter(I -> I.distributionType.get()));
        settings.add(GenericSetting.createObjectSetting(ObservableDrawingSet.class, ColourSeperationHandler.class, "colourSeperator", Register.DEFAULT_COLOUR_SPLITTER, (I, V) -> I.colourSeperator.set(V)).setGetter(I -> I.colourSeperator.get()));
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
