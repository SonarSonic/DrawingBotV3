package drawingbot.plotting;

import drawingbot.utils.DBTask;
import drawingbot.utils.EnumRendererType;
import drawingbot.utils.EnumTaskStage;

public interface ITaskManager {

    PlottedDrawing createNewPlottedDrawing();

    PFMTaskBuilder createPFMTaskBuilder();

    default void onPlottingTaskStageFinished(PFMTask task, EnumTaskStage stage){}

    ////////////////////////////////////////////////////////

    default void setActiveTask(DBTask<?> task){}

    default DBTask<?> getActiveTask(){
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

    default void clearDrawingRender(EnumRendererType rendererType){}

}
