package drawingbot.plotting.canvas;

import drawingbot.DrawingBotV3;
import drawingbot.JUnitDBV3ClassRunner;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PFMTaskBuilder;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.EnumCroppingMode;
import drawingbot.utils.EnumOrientation;
import drawingbot.utils.EnumRescaleMode;
import drawingbot.utils.UnitsLength;
import javafx.application.Platform;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import static drawingbot.TestUtils.*;

@RunWith(JUnitDBV3ClassRunner.class)
public class ObservableCanvasRegressionTest {

    private static PFMFactory<?> factory;

    @BeforeClass
    public static void setup(){
        factory = MasterRegistry.INSTANCE.getPFMFactory("Sketch Lines PFM");
    }


    @Test
    public void testRescaleModesPenWidth() {
        for (EnumRescaleMode rescaleMode : EnumRescaleMode.values()) {
            for (float target : List.of(0.3F, 0.7F, 1F, 1.3F)) {
                testCanvas("rescale_%s_%s".formatted(lazyFormat(rescaleMode.displayName), lazyFormat(String.valueOf(target))), factory, List.of("A4 Paper"), canvas -> {
                    canvas.rescaleMode.set(rescaleMode);
                    canvas.orientation.set(EnumOrientation.LANDSCAPE);
                    canvas.targetPenWidth.set(target);
                });
            }
        }
    }


    @Test
    public void testCroppingModesOrientation() {
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
    }


    @Test
    public void testInputUnitsRescale() {
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
    }


    @Test
    public void testOffsetPadding() {
        testCanvas("offset_padding", factory, List.of("A4 Paper"), canvas -> {
            canvas.orientation.set(EnumOrientation.LANDSCAPE);
            canvas.drawingAreaGangPadding.set(false);
            canvas.drawingAreaPaddingLeft.set(10);
            canvas.drawingAreaPaddingRight.set(50);
            canvas.drawingAreaPaddingBottom.set(5);
            canvas.drawingAreaPaddingTop.set(15);
        });
    }


    @Test
    public void testOriginalSizing() {
        testCanvas("original", factory, List.of("Original Sizing"), canvas -> {
            canvas.useOriginalSizing.set(true);
        });
    }

    public static void testCanvas(String testID, PFMFactory<?> factory, List<String> testPresetNames, Consumer<ObservableCanvas> setup) {
;
        for (String presetName : testPresetNames) {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {

                //Apply the Drawing Area Preset
                applyDrawingAreaPreset(presetName);

                setup.accept(DrawingBotV3.project().getDrawingArea());
                latch.countDown();
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                Assert.fail();
            }

            PFMTask pfmTask = runPFMTest(PFMTaskBuilder.create(DrawingBotV3.context(), factory).createPFMTask());
            exportPFMTestAsImage(pfmTask, new File(getRegressionImagesDirectory(), "%s_%s_%s.png".formatted(testID, lazyFormat(presetName), getTestRunnerName())), true);

            //Reset the canvas to default settings
            JFXUtils.runNow(() -> {
                Register.PRESET_LOADER_DRAWING_AREA.getDefaultManager().tryApplyPreset(DrawingBotV3.context(), Register.PRESET_LOADER_DRAWING_AREA.getDefaultPreset());
            });
        }
    }
}
