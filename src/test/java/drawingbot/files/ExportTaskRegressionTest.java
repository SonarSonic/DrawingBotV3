package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.JUnitDBV3ClassRunner;
import drawingbot.api.IGeometryFilter;
import drawingbot.registry.MasterRegistry;
import javafx.application.Platform;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static drawingbot.TestUtils.*;

@RunWith(JUnitDBV3ClassRunner.class)
public class ExportTaskRegressionTest{

    @Test
    public void testExport() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean triggered = new AtomicBoolean(false);
        Platform.runLater(() -> {
            DrawingBotV3.project().getPFMSettings().setPFMFactory(MasterRegistry.INSTANCE.getDefaultPFM());
            DrawingBotV3.INSTANCE.startPlotting(DrawingBotV3.context());

            DrawingBotV3.INSTANCE.taskMonitor.processingCount.addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() == 0) { //when the value changes we add export tasks for every type
                    if (triggered.get()) {
                        latch.countDown();
                    } else {
                        for (DrawingExportHandler format : MasterRegistry.INSTANCE.drawingExportHandlers.values()) {
                            if (format.category == DrawingExportHandler.Category.ANIMATION || format.category == DrawingExportHandler.Category.SPECIAL) {
                                continue;
                            }
                            String extension = format.getDefaultExtension();
                            String regressionDir = format.category == DrawingExportHandler.Category.IMAGE ? getRegressionImagesDirectory() : getRegressionFilesDirectory();
                            DrawingBotV3.INSTANCE.createExportTask(format, ExportTask.Mode.PER_DRAWING, DrawingBotV3.taskManager().getCurrentDrawing(), IGeometryFilter.DEFAULT_EXPORT_FILTER, extension, new File(regressionDir, format.getRegistryName() + " " + getTestRunnerName() + extension), false);
                            DrawingBotV3.INSTANCE.createExportTask(format, ExportTask.Mode.PER_PEN, DrawingBotV3.taskManager().getCurrentDrawing(), IGeometryFilter.DEFAULT_EXPORT_FILTER, extension, new File(regressionDir, format.getRegistryName() + " " + getTestRunnerName() + extension), false);
                        }
                        triggered.set(true);
                    }
                }
            });
        });
        latch.await();
    }
}