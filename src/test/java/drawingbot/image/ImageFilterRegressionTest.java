package drawingbot.image;

import drawingbot.DrawingBotV3;
import drawingbot.JUnitDBV3ClassRunner;
import drawingbot.TestUtils;
import drawingbot.api.IProgressCallback;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.files.json.presets.PresetImageFilters;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.pfm.PFMFactory;
import drawingbot.plotting.PFMTask;
import drawingbot.plotting.PFMTaskBuilder;
import drawingbot.registry.MasterRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static drawingbot.TestUtils.*;

@RunWith(JUnitDBV3ClassRunner.class)
public class ImageFilterRegressionTest {

    @Test
    public void testImageFilters() throws IOException {
        BufferedImage image = BufferedImageLoader.loadImage("images/testimage.jpg", true);
        assert image != null;
        InputStream stream = TestUtils.class.getResourceAsStream("/presets/filter_unit_test.json");
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
                ImageIO.write(filteredImage, "png", new File(getRegressionImagesDirectory(), "img_%s_%s.png".formatted(factory.getRegistryName().toLowerCase(), getTestRunnerName())));
                System.out.println("Finished Image Filter Test: " + factory.getRegistryName());

                //Further regression tests should be used on GitHub Actions
            }
        }
    }

    @Test
    public void testPreCropping() {

        DrawingBotV3.project().getOpenImage().cropStartX.set(305);
        DrawingBotV3.project().getOpenImage().cropStartY.set(25);
        DrawingBotV3.project().getOpenImage().cropWidth.set(250);
        DrawingBotV3.project().getOpenImage().cropHeight.set(250);

        PFMFactory<?> factory = MasterRegistry.INSTANCE.getPFMFactory("Sketch Lines PFM");
        PFMTask pfmTask = runPFMTest(PFMTaskBuilder.create(DrawingBotV3.context(), factory).createPFMTask());
        exportPFMTestAsImage(pfmTask, new File(getRegressionImagesDirectory(), "%s_%s.png".formatted("precrop", getTestRunnerName())), true);

        DrawingBotV3.project().getOpenImage().resetCrop();
    }

}
