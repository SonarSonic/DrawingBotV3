package drawingbot;

import drawingbot.api.IPathFindingModule;
import drawingbot.files.ExportFormats;
import drawingbot.files.FileUtils;
import drawingbot.geom.basic.IGeometry;
import drawingbot.image.BufferedImageLoader;
import drawingbot.image.FilteredBufferedImage;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import drawingbot.javafx.GenericFactory;
import javafx.application.Platform;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RunWith(JavaFxJUnit4ClassRunner.class)
public class DrawingBotV3Test {

    @Test
    public void testImageFilters() throws IOException {
        BufferedImage image = BufferedImageLoader.loadImage("images/testimage.jpg", true);
        assert image != null;
        for(List<GenericFactory<BufferedImageOp>> factories : MasterRegistry.INSTANCE.imgFilterFactories.values()){
            for(GenericFactory<BufferedImageOp> factory : factories){
                System.out.println("Started Image Filter Test: " + factory.getName());
                image = factory.instance().filter(image, null);
                System.out.println("Finished Image Filter Test: " + factory.getName());
            }
        }
    }

    @Test
    public void testPathFindingModules() throws InterruptedException, IOException {

        for(final PFMFactory factory : MasterRegistry.INSTANCE.pfmFactories){
            System.out.println("Started PFM Test: " + factory.getName());
            final CountDownLatch latch = new CountDownLatch(1);

            FilteredBufferedImage filteredImage = BufferedImageLoader.loadFilteredImage("images/testimage.jpg", true);
            assert filteredImage != null;

            Platform.runLater(() -> {
                DrawingBotV3.INSTANCE.pfmFactory.setValue(factory);
                DrawingBotV3.INSTANCE.openImage.set(filteredImage);
                DrawingBotV3.INSTANCE.startPlotting();
                DrawingBotV3.INSTANCE.taskMonitor.processingCount.addListener((observable, oldValue, newValue) -> {
                    if(newValue.intValue() == 0){
                        latch.countDown();
                    }
                });
            });
            latch.await();
            System.out.println("Finished PFM Test: " + factory.getName());
        }
    }
    @Test
    public void textExport() throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);

        FilteredBufferedImage filteredImage = BufferedImageLoader.loadFilteredImage("images/testimage.jpg", true);
        assert filteredImage != null;

        Platform.runLater(() -> {
            DrawingBotV3.INSTANCE.pfmFactory.setValue(MasterRegistry.INSTANCE.getDefaultPFM());
            DrawingBotV3.INSTANCE.openImage.set(filteredImage);
            DrawingBotV3.INSTANCE.startPlotting();
            DrawingBotV3.INSTANCE.taskMonitor.processingCount.addListener((observable, oldValue, newValue) -> {
                if(newValue.intValue() == 0){ //when the value changes we add export tasks for every type
                    for(ExportFormats format : ExportFormats.values()){
                        String extension = format.filters[0].getExtensions().get(0).substring(1);
                        DrawingBotV3.INSTANCE.createExportTask(format, DrawingBotV3.INSTANCE.getActiveTask(), IGeometry.DEFAULT_FILTER, extension, new File(FileUtils.getUserDataDirectory(), "testimage" + extension), true, false);
                        DrawingBotV3.INSTANCE.createExportTask(format, DrawingBotV3.INSTANCE.getActiveTask(), IGeometry.DEFAULT_FILTER, extension, new File(FileUtils.getUserDataDirectory(), "testimage" + extension), false, false);
                    }
                    DrawingBotV3.INSTANCE.taskService.submit(latch::countDown); //we add a final task to the exporter service, when this is reached we know the other export tasks are down also. note we don't add it via the TaskMonitor so it is ignored by the progress bars
                }
            });
        });
        latch.await();
    }

}