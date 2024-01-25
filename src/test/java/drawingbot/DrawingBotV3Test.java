package drawingbot;

import drawingbot.api.IGeometryFilter;
import drawingbot.api.IProgressCallback;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.json.AbstractJsonLoader;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.files.json.PresetData;
import drawingbot.files.json.presets.PresetImageFilters;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.ImageFilterSettings;
import drawingbot.image.ImageTools;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PFMTask;
import drawingbot.plugins.PremiumPluginDummy;
import drawingbot.registry.MasterRegistry;
import drawingbot.javafx.GenericFactory;
import drawingbot.registry.Register;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

//https://github.com/reg-viz/reg-actions
@RunWith(JFXJUnit4ClassRunner.class)
public class DrawingBotV3Test  {

    private static String testRunnerName = null;

    public static String getTestRunnerName(){
        if(testRunnerName == null){
            String prop = System.getProperty("drawingbotv3_testrunner");
            testRunnerName = prop == null ? Utils.getOS().getShortName() : prop;
        }
        return testRunnerName;
    }

    public static String getImageTestsDirectory() {
        return FileUtils.getTestDirectory() + "filter" + File.separator;
    }

    public static String getPFMTestsDirectory() {
        return FileUtils.getTestDirectory() + "pfm" + File.separator;
    }

    public static String getExportTestsDirectory() {
        return FileUtils.getTestDirectory() + "export" + File.separator;
    }


    @BeforeClass
    public static void setup() throws InterruptedException {
        DBPreferences.INSTANCE.autoRunPFM.set(false);
        JsonLoaderManager.loadDefaultPresetContainerJSON("pfm_unit_test.json");

        File testDir = new File(FileUtils.getTestDirectory());
        assert testDir.exists() || testDir.mkdirs();

        File testImageDir = new File(getImageTestsDirectory());
        assert testImageDir.exists() || testImageDir.mkdirs();

        File testPFMDir = new File(getPFMTestsDirectory());
        assert testPFMDir.exists() || testPFMDir.mkdirs();

        File testExportDir = new File(getExportTestsDirectory());
        assert testExportDir.exists() || testExportDir.mkdirs();

        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), new File("images/testimage.jpg"), true, false);
            DrawingBotV3.project().openImage.addListener((observable, oldValue, newValue) -> latch.countDown());
        });
        latch.await();
    }

    @Test
    public void testImageFilters() throws IOException {
        BufferedImage image = BufferedImageLoader.loadImage("images/testimage.jpg", true);
        assert image != null;
        InputStream stream = DrawingBotV3Test.class.getResourceAsStream("/presets/filter_unit_test.json");
        GenericPreset<PresetImageFilters> imageFilterPreset = JsonLoaderManager.importPresetFile(stream, null);
        assert imageFilterPreset != null;

        for(List<GenericFactory<BufferedImageOp>> factories : MasterRegistry.INSTANCE.imgFilterFactories.values()){
            for(GenericFactory<BufferedImageOp> factory : factories){
                System.out.println("Started Image Filter Test: " + factory.getRegistryName());

                //Create the ImageFilterSettings
                ImageFilterSettings imageFilterSettings = new ImageFilterSettings();
                ObservableImageFilter observableFilter = new ObservableImageFilter(factory);
                imageFilterSettings.currentFilters.get().add(observableFilter);

                //Apply the settings from the preset file if available
                imageFilterPreset.data.filters.stream().filter(presetFilter -> presetFilter.type.equals(factory.getRegistryName())).findFirst().ifPresent(presetFilter -> GenericSetting.applySettings(presetFilter.settings, observableFilter.filterSettings));
                observableFilter.enable.set(true); //make sure the filter is actually enabled

                //Filter the image
                BufferedImage filteredImage = ImageTools.applyCurrentImageFilters(image, imageFilterSettings, true, IProgressCallback.NULL);

                //Save the image
                ImageIO.write(filteredImage, "png", new File(getImageTestsDirectory(), "img_%s_%s.png".formatted(factory.getRegistryName().toLowerCase(), getTestRunnerName())));
                System.out.println("Finished Image Filter Test: " + factory.getRegistryName());

                //Further regression tests should be used on GitHub Actions
            }
        }
    }

    @Test
    public void testPathFindingModules() throws InterruptedException {
        testPFMS(pfm -> true);
    }

    public static void testPFMS(Function<PFMFactory<?>, Boolean> filter) throws InterruptedException {

        for(final PFMFactory<?> factory : MasterRegistry.INSTANCE.pfmFactories){
            if(factory.getInstanceClass() == PremiumPluginDummy.DummyPFM.class || !filter.apply(factory)){
                continue;
            }
            System.out.println("Started PFM Test: " + factory.getRegistryName());
            GenericPreset<PresetData> preset = Register.PRESET_LOADER_PFM.getPresetsForSubType(factory.getRegistryName()).stream().filter(f -> f.getPresetName().equals("pfm_unit_test")).findFirst().orElse(null);

            final CountDownLatch pfmTaskLatch = new CountDownLatch(1);
            AtomicReference<PFMTask> pfmTask = new AtomicReference<>();

            String usedMemory = Utils.defaultNF.format(Runtime.getRuntime().totalMemory());
            String totalMemory = Utils.defaultNF.format(Runtime.getRuntime().maxMemory());
            System.out.println("Heap Usage: " + usedMemory + " / " + totalMemory);

            // Create a PFM Task
            Platform.runLater(() -> {
                DrawingBotV3.project().getPFMSettings().setPFMFactory(factory);
                if(preset != null){
                    Register.PRESET_LOADER_PFM.getDefaultManager().applyPreset(DrawingBotV3.context(), preset, false);
                }
                DrawingBotV3.project().setRenderedTask(null);
                DrawingBotV3.project().setActiveTask(null);
                pfmTask.set(DrawingBotV3.taskManager().initPFMTask(DrawingBotV3.context(), DrawingBotV3.project().getDrawingArea().copy(), factory, null, DrawingBotV3.project().getDrawingSets().activeDrawingSet.get(), DrawingBotV3.project().openImage.get(), false));
                pfmTask.get().stateProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue == Worker.State.FAILED || newValue == Worker.State.SUCCEEDED){
                        pfmTaskLatch.countDown();
                    }
                });
                DrawingBotV3.INSTANCE.taskMonitor.queueTask(pfmTask.get());
            });
            pfmTaskLatch.await();

            Assert.assertNotNull(pfmTask.get());
            Assert.assertSame(Future.State.SUCCESS, pfmTask.get().state());

            // Create an Export Task

            final CountDownLatch exportTaskLatch = new CountDownLatch(1);
            AtomicReference<Task<?>> exportTask = new AtomicReference<>();

            Platform.runLater(() -> {
                exportTask.set(DrawingBotV3.INSTANCE.createExportTask(Register.EXPORT_IMAGE, ExportTask.Mode.PER_DRAWING, pfmTask.get().drawing, IGeometryFilter.DEFAULT_EXPORT_FILTER, ".png", new File(getPFMTestsDirectory(), "pfm_%s_%s.png".formatted(factory.getRegistryName().toLowerCase().replace(" ", "_"), getTestRunnerName())), false));
                exportTask.get().stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.FAILED || newValue == Worker.State.SUCCEEDED) {
                        exportTaskLatch.countDown();
                        DrawingBotV3.project().setActiveTask(null);
                        DrawingBotV3.project().setRenderedTask(null);
                        //Force: Destroy the task to prevent OutOfMemory errors
                        pfmTask.get().tryDestroy();
                    }
                });
            });
            exportTaskLatch.await();

            Assert.assertSame(Future.State.SUCCESS, exportTask.get().state());

            System.out.println("Finished PFM Test: " + factory.getRegistryName());
        }
    }

    @Test
    public void testPresets(){
        for(AbstractJsonLoader<?> loader : MasterRegistry.INSTANCE.presetLoaders){
            for(GenericPreset<?> preset : loader.getAllPresets()){
                String original = JsonLoaderManager.createDefaultGson().toJson(preset);
                String fromJSON = JsonLoaderManager.createDefaultGson().toJson(JsonLoaderManager.createDefaultGson().fromJson(original, GenericPreset.class));
                Assert.assertEquals(original, fromJSON);
            }
        }
    }

    @Test
    public void testExport() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean triggered = new AtomicBoolean(false);
        Platform.runLater(() -> {
            DrawingBotV3.project().getPFMSettings().setPFMFactory(MasterRegistry.INSTANCE.getDefaultPFM());
            DrawingBotV3.INSTANCE.startPlotting(DrawingBotV3.context());

            DrawingBotV3.INSTANCE.taskMonitor.processingCount.addListener((observable, oldValue, newValue) -> {
                if(newValue.intValue() == 0){ //when the value changes we add export tasks for every type
                    if(triggered.get()){
                        latch.countDown();
                    }else{
                        for(DrawingExportHandler format : MasterRegistry.INSTANCE.drawingExportHandlers.values()){
                            if(format.category == DrawingExportHandler.Category.ANIMATION || format.category == DrawingExportHandler.Category.SPECIAL){
                                continue;
                            }
                            String extension = format.getDefaultExtension();
                            DrawingBotV3.INSTANCE.createExportTask(format, ExportTask.Mode.PER_DRAWING, DrawingBotV3.taskManager().getCurrentDrawing(), IGeometryFilter.DEFAULT_EXPORT_FILTER, extension, new File(getExportTestsDirectory(), format.getRegistryName() + " " + getTestRunnerName() + " " + extension), false);
                            DrawingBotV3.INSTANCE.createExportTask(format, ExportTask.Mode.PER_PEN, DrawingBotV3.taskManager().getCurrentDrawing(), IGeometryFilter.DEFAULT_EXPORT_FILTER, extension, new File(getExportTestsDirectory(), format.getRegistryName() + " " + getTestRunnerName() + " " + extension), false);
                        }
                        triggered.set(true);
                    }
                }
            });
        });
        latch.await();
    }

}