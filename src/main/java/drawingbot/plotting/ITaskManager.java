package drawingbot.plotting;

import drawingbot.api.ICanvas;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.image.format.FilteredImageData;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.pfm.PFMFactory;
import drawingbot.utils.EnumTaskStage;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public interface ITaskManager {

    PlottedDrawing createNewPlottedDrawing();

    PFMTask initPFMTask(DBTaskContext context, ICanvas canvas, PFMFactory<?> pfmFactory, @Nullable List<GenericSetting<?, ?>> pfmSettings, ObservableDrawingSet drawingPenSet, @Nullable FilteredImageData imageData, boolean isSubTask);

    PFMTask initPFMTask(DBTaskContext context, PlottedDrawing drawing, PFMFactory<?> pfmFactory, @Nullable List<GenericSetting<?, ?>> settings, ObservableDrawingSet drawingPenSet, @Nullable FilteredImageData imageData, boolean isSubTask);

    default void onPlottingTaskStageFinished(PFMTask task, EnumTaskStage stage){}

    ////////////////////////////////////////////////////////

    default void setActiveTask(PFMTask task){}

    default PFMTask getActiveTask(){
        return null;
    }

    ////////////////////////////////////////////////////////

    default void setRenderedTask(PFMTask task){}

    default PFMTask getRenderedTask(){
        return null;
    }

    ////////////////////////////////////////////////////////

    default void setCurrentDrawing(PlottedDrawing drawing){}

    default PlottedDrawing getCurrentDrawing(){
        return null;
    }

    default void clearDrawingRender(){}
}
