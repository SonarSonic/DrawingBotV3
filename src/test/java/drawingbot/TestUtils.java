package drawingbot;

import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PFMTaskBuilder;
import drawingbot.plugins.PremiumPluginDummy;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.EnumDistributionType;
import drawingbot.utils.EnumOrientation;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Utility classes for use when running Unit Tests, primarily for help setting up the test environments
 */
public class TestUtils {

    private static String testRunnerName = null;
    private static boolean setupDirs = false;

    /**
     * @return the runner's name, typically the {@link Utils.OS#getShortName()} but can be a custom name defined with the java property "drawingbotv3_testrunner"
     * <br>
     * e.g. -Pdrawingbotv3_testrunner=macos11
     */
    public static String getTestRunnerName() {
        if (testRunnerName == null) {
            String prop = System.getProperty("drawingbotv3_testrunner");
            testRunnerName = prop == null || prop.isEmpty() ? Utils.getOS().getShortName() : prop;
        }
        return testRunnerName;
    }

    public static String lazyFormat(String string) {
        return FileUtils.getSafeFileName(string.toLowerCase().replace(" ", "_").replace(".", "_"));
    }

    public static void lazySetupDirectories(){
        if(setupDirs){
            return;
        }
        setupDirs = true;

        File testDir = new File(FileUtils.getTestDirectory());
        assert testDir.exists() || testDir.mkdirs();

        File testRegressionImageDir = new File(getRegressionImagesDirectory());
        assert testRegressionImageDir.exists() || testRegressionImageDir.mkdirs();

        File testRegressionFilesDir = new File(getRegressionFilesDirectory());
        assert testRegressionFilesDir.exists() || testRegressionFilesDir.mkdirs();
    }

    public static String getRegressionImagesDirectory() {
        return FileUtils.getTestDirectory() + "images" + File.separator;
    }

    public static String getRegressionFilesDirectory() {
        return FileUtils.getTestDirectory() + "files" + File.separator;
    }

    public static GenericPreset<PresetData> getPFMRunnerPreset(PFMFactory<?> factory){
        return getPFMPreset(factory, "pfm_unit_test");
    }

    public static GenericPreset<PresetData> getPFMPreset(PFMFactory<?> factory, String presetName){
        return Register.PRESET_LOADER_PFM.getPresetsForSubType(factory.getRegistryName()).stream().filter(f -> f.getPresetName().equals(presetName)).findFirst().orElse(null);
    }

    public static void runPFMTests(Function<PFMFactory<?>, Boolean> filter){
        for (final PFMFactory<?> factory : MasterRegistry.INSTANCE.pfmFactories) {
            if (factory.getInstanceClass() != PremiumPluginDummy.DummyPFM.class && filter.apply(factory)) {
                runPFMTest(factory);
            }
        }
    }

    public static void runPFMTest(PFMFactory<?> factory){
        System.out.println("Started PFM Test: " + factory.getRegistryName());
        // Create an Export Task
        PFMTask pfmTask = runPFMTest(PFMTaskBuilder.create(DrawingBotV3.context(), factory).createPFMTask());
        exportPFMTestAsImage(pfmTask, new File(getRegressionImagesDirectory(), "pfm_%s_%s.png".formatted(factory.getRegistryName().toLowerCase().replace(" ", "_"), getTestRunnerName())), true);

        System.out.println("Finished PFM Test: " + factory.getRegistryName());
    }


    public static PFMTask runPFMTest(PFMTask task) {
        return runPFMTest(task, getPFMRunnerPreset(task.pfmFactory));
    }

    public static PFMTask runPFMTest(PFMTask task, @Nullable GenericPreset<PresetData> preset) {
        final CountDownLatch pfmTaskLatch = new CountDownLatch(1);

        String usedMemory = Utils.defaultNF.format(Runtime.getRuntime().totalMemory());
        String totalMemory = Utils.defaultNF.format(Runtime.getRuntime().maxMemory());
        System.out.println("Heap Usage: " + usedMemory + " / " + totalMemory);

        // Create a PFM Task
        Platform.runLater(() -> {
            DrawingBotV3.project().getPFMSettings().setPFMFactory(task.pfmFactory);
            if(preset != null){
                Register.PRESET_LOADER_PFM.getDefaultManager().applyPreset(DrawingBotV3.context(), preset, false, false);
            }
            EnumDistributionType distributionType = EnumDistributionType.getRecommendedType(DrawingBotV3.context());

            //Override the distribution type to be the recommended
            DrawingBotV3.project().getPFMSettings().setNextDistributionType(distributionType);
            DrawingBotV3.project().getActiveDrawingSet().distributionType.set(distributionType);

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
            task.get();
        } catch (InterruptedException | ExecutionException e) {
            Assert.fail();
        }

        Assert.assertNotNull(task);
        Assert.assertNotEquals(Future.State.FAILED, task.state());

        return task;
    }

    public static void exportPFMTestAsImage(PFMTask pfmTask, File saveLocation, boolean destroy) {

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
            exportTask.get().get();
        } catch (InterruptedException | ExecutionException e) {
            Assert.fail();
        }

        Assert.assertNotEquals(Future.State.FAILED, exportTask.get().state());
    }

    public static void resetProject(){
        JFXUtils.runNow(() -> {
            ObservableProject oldProject = DrawingBotV3.project();

            //Set the new project
            ObservableProject newProject = new ObservableProject();
            DrawingBotV3.INSTANCE.activeProjects.add(newProject);
            DrawingBotV3.INSTANCE.activeProject.set(newProject);

            //Remove the old project
            DrawingBotV3.INSTANCE.activeProjects.remove(oldProject);
        });
    }

    public static void loadDefaultTestImage(){
        loadTestImage("images/testimage.jpg", true);
    }

    public static void loadTestImage(String path, boolean internal){
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), new File(path), internal, false);
            DrawingBotV3.project().openImage.addListener((observable, oldValue, newValue) -> {
                latch.countDown();
            });
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail();
        }
    }

    public static void applyDrawingAreaPreset(String presetName){
        JFXUtils.runNow(() -> {
            GenericPreset<PresetData> drawingAreaPreset = Register.PRESET_LOADER_DRAWING_AREA.findPreset(presetName);
            Register.PRESET_LOADER_DRAWING_AREA.getDefaultManager().tryApplyPreset(DrawingBotV3.context(), drawingAreaPreset);
        });
    }

    public static void landscape(){
        JFXUtils.runNow(() -> {
            DrawingBotV3.project().getDrawingArea().orientation.set(EnumOrientation.LANDSCAPE);
        });
    }

    public static void portrait(){
        JFXUtils.runNow(() -> {
            DrawingBotV3.project().getDrawingArea().orientation.set(EnumOrientation.PORTRAIT);
        });
    }
}
