package drawingbot.files;

import drawingbot.drawing.DrawingStats;
import drawingbot.plotting.PlottedDrawing;

import java.io.File;

public class ExportedDrawingEntry {

    public PlottedDrawing drawing;
    public File file;
    public DrawingStats before;
    public DrawingStats after;

    public ExportedDrawingEntry(PlottedDrawing drawing, File file, DrawingStats before, DrawingStats after) {
        this.drawing = drawing;
        this.file = file;
        this.before = before;
        this.after = after;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
