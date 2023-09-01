package drawingbot.utils;

import drawingbot.image.ImageTools;
import drawingbot.javafx.observables.ObservableDrawingPen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public enum EnumDistributionOrder {

    DARKEST_FIRST(Comparator.comparingInt(pen -> ImageTools.getPerceivedLuminanceFromRGB(pen.getARGB()))),
    LIGHTEST_FIRST(Comparator.comparingInt(pen -> -ImageTools.getPerceivedLuminanceFromRGB(pen.getARGB()))),
    DISPLAYED(Comparator.comparingInt(pen -> pen.penNumber.get())),
    REVERSED(Comparator.comparingInt(pen -> -pen.penNumber.get()));

    public final Comparator<ObservableDrawingPen> comparator;

    EnumDistributionOrder(Comparator<ObservableDrawingPen> comparator){
        this.comparator = (o1, o2) -> o1.shouldForceOverlap() == o2.shouldForceOverlap() ? comparator.compare(o1, o2) :  o1.shouldForceOverlap() ? -1 : 1;
    }

    public List<ObservableDrawingPen> getSortedPens(List<ObservableDrawingPen> pens){
        List<ObservableDrawingPen> sortedList = new ArrayList<>(pens);
        sortedList.sort(comparator);
        return sortedList;
    }

    @Override
    public String toString() {
        if(this == DARKEST_FIRST){
            return Utils.capitalize(name()) + " " + "(Default)";
        }
        return Utils.capitalize(name());
    }

}