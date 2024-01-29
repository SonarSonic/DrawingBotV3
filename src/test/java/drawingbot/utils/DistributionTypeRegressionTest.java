package drawingbot.utils;

import drawingbot.DrawingBotV3;
import drawingbot.JUnitDBV3ClassRunner;
import drawingbot.javafx.util.JFXUtils;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PFMTaskBuilder;
import drawingbot.registry.MasterRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static drawingbot.TestUtils.*;


@RunWith(JUnitDBV3ClassRunner.class)
public class DistributionTypeRegressionTest {

    @Test
    public void testDistributionType() {
        PFMFactory<?> factory = MasterRegistry.INSTANCE.getPFMFactory("Sketch Lines PFM");
        PFMTask pfmTask = runPFMTest(PFMTaskBuilder.create(DrawingBotV3.context(), factory).createPFMTask());

        for (EnumDistributionType type : EnumDistributionType.values()) {
            for (EnumDistributionOrder order : EnumDistributionOrder.values()) {
                JFXUtils.runNow(() -> {
                    DrawingBotV3.project().getActiveDrawingSet().distributionType.set(type);
                    DrawingBotV3.project().getActiveDrawingSet().distributionOrder.set(order);
                });
                exportPFMTestAsImage(pfmTask, new File(getRegressionImagesDirectory(), "distribute_%s_%s_%s.png".formatted(lazyFormat(type.displayName), lazyFormat(order.name()), getTestRunnerName())), false);
            }
        }
        JFXUtils.runNow(() -> {
            DrawingBotV3.project().getActiveDrawingSet().distributionType.set(EnumDistributionType.EVEN_WEIGHTED);
            DrawingBotV3.project().getActiveDrawingSet().distributionOrder.set(EnumDistributionOrder.DARKEST_FIRST);
        });
    }
}