package drawingbot.files;

import java.io.File;

public interface IExportMethod {

    void export(ExportTask exportTask, File saveLocation);

}
