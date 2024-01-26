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
import drawingbot.javafx.util.JFXUtils;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PFMTaskBuilder;
import drawingbot.plotting.canvas.ObservableCanvas;
import drawingbot.plugins.PremiumPluginDummy;
import drawingbot.registry.MasterRegistry;
import drawingbot.javafx.GenericFactory;
import drawingbot.registry.Register;
import drawingbot.utils.*;
import javafx.application.Platform;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

@RunWith(JFXJUnit4ClassRunner.class)
public class DrawingBotV3Test {

    private static String testRunnerName = null;

    public static String getTestRunnerName() {
        if (testRunnerName == null) {
            String prop = System.getProperty("drawingbotv3_testrunner");
            testRunnerName = prop == null ? Utils.getOS().getShortName() : prop;
        }
        return testRunnerName;
    }

    public static String lazyFormat(String string) {
        return FileUtils.getSafeFileName(string.toLowerCase().replace(" ", "_").replace(".", "_"));
    }

    public static String getImageTestsDirectory() {
        return FileUtils.getTestDirectory() + "filter" + File.separator;
    }

    public static String getPFMTestsDirectory() {
        return FileUtils.getTestDirectory() + "pfm" + File.separator;
    }

    public static String getCanvasTestsDirectory() {
        return FileUtils.getTestDirectory() + "canvas" + File.separator;
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

        File canvasTestDir = new File(getCanvasTestsDirectory());
        assert canvasTestDir.exists() || canvasTestDir.mkdirs();

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

        for (List<GenericFactory<BufferedImageOp>> factories : MasterRegistry.INSTANCE.imgFilterFactories.values()) {
            for (GenericFactory<BufferedImageOp> factory : factories) {
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

        for (final PFMFactory<?> factory : MasterRegistry.INSTANCE.pfmFactories) {
            if (factory.getInstanceClass() == PremiumPluginDummy.DummyPFM.class || !filter.apply(factory)) {
                continue;
            }
            System.out.println("Started PFM Test: " + factory.getRegistryName());
            // Create an Export Task
            PFMTask pfmTask = PFMTestRunner.runPFMTest(PFMTaskBuilder.create(DrawingBotV3.context(), factory).createPFMTask());
            PFMTestRunner.exportPFMTest(pfmTask, new File(getPFMTestsDirectory(), "pfm_%s_%s.png".formatted(factory.getRegistryName().toLowerCase().replace(" ", "_"), getTestRunnerName())), true);

            System.out.println("Finished PFM Test: " + factory.getRegistryName());
        }
    }

    @Test
    public void testPreCropping() {

        DrawingBotV3.project().getOpenImage().cropStartX.set(305);
        DrawingBotV3.project().getOpenImage().cropStartY.set(25);
        DrawingBotV3.project().getOpenImage().cropWidth.set(250);
        DrawingBotV3.project().getOpenImage().cropHeight.set(250);

        PFMFactory<?> factory = MasterRegistry.INSTANCE.getPFMFactory("Sketch Lines PFM");
        PFMTask pfmTask = PFMTestRunner.runPFMTest(PFMTaskBuilder.create(DrawingBotV3.context(), factory).createPFMTask());
        PFMTestRunner.exportPFMTest(pfmTask, new File(getCanvasTestsDirectory(), "%s_%s.png".formatted("precrop", getTestRunnerName())), true);

        DrawingBotV3.project().getOpenImage().resetCrop();
    }

    @Test
    public void testDistributionType() {
        PFMFactory<?> factory = MasterRegistry.INSTANCE.getPFMFactory("Sketch Lines PFM");
        PFMTask pfmTask = PFMTestRunner.runPFMTest(PFMTaskBuilder.create(DrawingBotV3.context(), factory).createPFMTask());

        for (EnumDistributionType type : EnumDistributionType.values()) {
            for (EnumDistributionOrder order : EnumDistributionOrder.values()) {
                JFXUtils.runNow(() -> {
                    DrawingBotV3.project().getActiveDrawingSet().distributionType.set(type);
                    DrawingBotV3.project().getActiveDrawingSet().distributionOrder.set(order);
                });
                PFMTestRunner.exportPFMTest(pfmTask, new File(getCanvasTestsDirectory(), "distribute_%s_%s_%s.png".formatted(lazyFormat(type.displayName), lazyFormat(order.name()), getTestRunnerName())), false);
            }
        }
        JFXUtils.runNow(() -> {
            DrawingBotV3.project().getActiveDrawingSet().distributionType.set(EnumDistributionType.EVEN_WEIGHTED);
            DrawingBotV3.project().getActiveDrawingSet().distributionOrder.set(EnumDistributionOrder.DARKEST_FIRST);
        });
    }


    @Test
    public void testCanvasSetups() {

        PFMFactory<?> factory = MasterRegistry.INSTANCE.getPFMFactory("Sketch Lines PFM");

        for (EnumRescaleMode rescaleMode : EnumRescaleMode.values()) {
            for (float target : List.of(0.3F, 0.7F, 1F, 1.3F)) {
                testCanvas("rescale_%s_%s".formatted(lazyFormat(rescaleMode.displayName), lazyFormat(String.valueOf(target))), factory, List.of("A4 Paper"), canvas -> {
                    canvas.rescaleMode.set(rescaleMode);
                    canvas.orientation.set(EnumOrientation.LANDSCAPE);
                    canvas.targetPenWidth.set(target);
                });
            }
        }


        for (EnumCroppingMode croppingMode : EnumCroppingMode.values()) {
            for (EnumOrientation orientation : EnumOrientation.values()) {
                testCanvas("crop_%s_%s".formatted(lazyFormat(croppingMode.name()), lazyFormat(orientation.name())), factory, List.of("A5 Paper"), canvas -> {
                    canvas.croppingMode.set(croppingMode);
                    canvas.orientation.set(orientation);
                    canvas.targetPenWidth.set(0.3F);
                    canvas.drawingAreaPaddingLeft.set(10);
                });
            }
        }

        for (UnitsLength units : UnitsLength.values()) {
            for (EnumRescaleMode rescaleMode : EnumRescaleMode.values()) {
                testCanvas("units_%s_%s".formatted(lazyFormat(units.getSuffix()), lazyFormat(rescaleMode.name())), factory, List.of("A5 Paper"), canvas -> {
                    canvas.inputUnits.set(units);
                    canvas.rescaleMode.set(rescaleMode);
                    canvas.targetPenWidth.set(0.3F);
                    canvas.drawingAreaPaddingLeft.set(0);
                });
            }
        }

        testCanvas("offset_padding", factory, List.of("A4 Paper"), canvas -> {
            canvas.orientation.set(EnumOrientation.LANDSCAPE);
            canvas.drawingAreaGangPadding.set(false);
            canvas.drawingAreaPaddingLeft.set(10);
            canvas.drawingAreaPaddingRight.set(50);
            canvas.drawingAreaPaddingBottom.set(5);
            canvas.drawingAreaPaddingTop.set(15);
        });

        testCanvas("original", factory, List.of("Original Sizing"), canvas -> {
            canvas.useOriginalSizing.set(true);
        });
        JFXUtils.runNow(() -> {
            Register.PRESET_LOADER_DRAWING_AREA.getDefaultManager().tryApplyPreset(DrawingBotV3.context(), Register.PRESET_LOADER_DRAWING_AREA.getDefaultPreset());
        });
    }

    public static void testCanvas(String testID, PFMFactory<?> factory, List<String> testPresetNames, Consumer<ObservableCanvas> setup) {

        for (String presetName : testPresetNames) {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {

                //Apply the Drawing Area Preset
                GenericPreset<PresetData> drawingAreaPreset = Register.PRESET_LOADER_DRAWING_AREA.findPreset(presetName);
                Register.PRESET_LOADER_DRAWING_AREA.getDefaultManager().tryApplyPreset(DrawingBotV3.context(), drawingAreaPreset);

                setup.accept(DrawingBotV3.project().getDrawingArea());
                latch.countDown();
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                Assert.fail();
            }

            PFMTask pfmTask = PFMTestRunner.runPFMTest(PFMTaskBuilder.create(DrawingBotV3.context(), factory).createPFMTask());
            PFMTestRunner.exportPFMTest(pfmTask, new File(getCanvasTestsDirectory(), "%s_%s_%s.png".formatted(testID, lazyFormat(presetName), getTestRunnerName())), true);

        }
    }

    @Test
    public void testHeapSize() {
        String usedMemory = Utils.defaultNF.format(Runtime.getRuntime().totalMemory());
        String totalMemory = Utils.defaultNF.format(Runtime.getRuntime().maxMemory());
        System.out.println("Heap Usage: " + usedMemory + " / " + totalMemory);
    }

    @Test
    public void testPresets() {
        for (AbstractJsonLoader<?> loader : MasterRegistry.INSTANCE.presetLoaders) {
            for (GenericPreset<?> preset : loader.getAllPresets()) {
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
                if (newValue.intValue() == 0) { //when the value changes we add export tasks for every type
                    if (triggered.get()) {
                        latch.countDown();
                    } else {
                        for (DrawingExportHandler format : MasterRegistry.INSTANCE.drawingExportHandlers.values()) {
                            if (format.category == DrawingExportHandler.Category.ANIMATION || format.category == DrawingExportHandler.Category.SPECIAL) {
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