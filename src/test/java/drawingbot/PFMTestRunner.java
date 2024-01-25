package drawingbot;

import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.GenericPreset;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PFMTask;
import drawingbot.registry.Register;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import org.junit.Assert;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class PFMTestRunner {

    public static GenericPreset<PresetData> getPFMRunnerPreset(PFMFactory<?> factory){
        return Register.PRESET_LOADER_PFM.getPresetsForSubType(factory.getRegistryName()).stream().filter(f -> f.getPresetName().equals("pfm_unit_test")).findFirst().orElse(null);
    }

    public static PFMTask runPFMTest(PFMTask task) {
        return runPFMTest(task, getPFMRunnerPreset(task.pfmFactory));
    }

    public static PFMTask runPFMTest(PFMTask task, GenericPreset<PresetData> preset) {
        final CountDownLatch pfmTaskLatch = new CountDownLatch(1);

        String usedMemory = Utils.defaultNF.format(Runtime.getRuntime().totalMemory());
        String totalMemory = Utils.defaultNF.format(Runtime.getRuntime().maxMemory());
        System.out.println("Heap Usage: " + usedMemory + " / " + totalMemory);

        // Create a PFM Task
        Platform.runLater(() -> {
            DrawingBotV3.project().getPFMSettings().setPFMFactory(task.pfmFactory);
            if(preset != null){
                Register.PRESET_LOADER_PFM.getDefaultManager().applyPreset(DrawingBotV3.context(), preset, false);
            }
            DrawingBotV3.project().setRenderedTask(null);
            DrawingBotV3.project().setActiveTask(null);
            task.stateProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue == Worker.State.FAILED || newValue == Worker.State.SUCCEEDED){
                    pfmTaskLatch.countDown();
                }
            });
            DrawingBotV3.INSTANCE.taskMonitor.queueTask(task);
        });
        try {
            pfmTaskLatch.await();
        } catch (InterruptedException e) {
            Assert.fail();
        }

        Assert.assertNotNull(task);
        Assert.assertSame(Future.State.SUCCESS, task.state());

        return task;
    }

    public static void exportPFMTest(PFMTask pfmTask, File saveLocation, boolean destroy) {

        final CountDownLatch exportTaskLatch = new CountDownLatch(1);
        AtomicReference<Task<?>> exportTask = new AtomicReference<>();

        Platform.runLater(() -> {
            exportTask.set(DrawingBotV3.INSTANCE.createExportTask(Register.EXPORT_IMAGE, ExportTask.Mode.PER_DRAWING, pfmTask.drawing, IGeometryFilter.DEFAULT_EXPORT_FILTER, ".png", saveLocation, false));
            exportTask.get().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.FAILED || newValue == Worker.State.SUCCEEDED) {
                    exportTaskLatch.countDown();
                    if(destroy){
                        DrawingBotV3.project().setActiveTask(null);
                        DrawingBotV3.project().setRenderedTask(null);
                        //Force: Destroy the task to prevent OutOfMemory errors
                        pfmTask.tryDestroy();
                    }
                }
            });
        });
        try {
            exportTaskLatch.await();
        } catch (InterruptedException e) {
            Assert.fail();
        }

        Assert.assertSame(Future.State.SUCCESS, exportTask.get().state());
    }


}
