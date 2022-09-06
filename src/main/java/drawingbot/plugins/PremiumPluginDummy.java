package drawingbot.plugins;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.*;
import drawingbot.drawing.ColourSeperationHandler;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.FileUtils;
import drawingbot.javafx.FXController;
import drawingbot.javafx.FXExportController;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.IDisplayMode;
import drawingbot.render.IRenderer;
import drawingbot.utils.EnumReleaseState;
import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class PremiumPluginDummy implements IPlugin {

    @Override
    public String getPluginName() {
        return "Free";
    }

    @Override
    public void preInit() {
        Hooks.addHook(Hooks.FX_CONTROLLER_POST_INIT, this::disableBatchProcessingUI);
        Hooks.addHook(Hooks.FX_CONTROLLER_POST_INIT, this::disableColourSplitterUI);
        Hooks.addHook(Hooks.FX_EXPORT_CONTROLLER_POST_INIT, this::disableHPGLUI);
        Hooks.addHook(Hooks.FX_EXPORT_CONTROLLER_POST_INIT, this::disableOpenGL);
        Hooks.addHook(Hooks.FILE_MENU, this::initMenuOption);

        MasterRegistry.INSTANCE.registerDisplayMode(new IDisplayMode() {

            final FlagStates renderFlags = new FlagStates(Flags.RENDER_CATEGORY);

            @Override
            public String getName() {
                return "Drawing (Hardware Accelerated) (Premium)";
            }

            @Override
            public IRenderer getRenderer() {
                return DrawingBotV3.OPENGL_RENDERER;
            }

            @Override
            public FlagStates getRenderFlags() {
                return renderFlags;
            }

            @Override
            public String toString(){
                return getName();
            }

        });
    }

    @Override
    public void registerPFMS() {
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Curves PFM", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Quad Beziers PFM", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Cubic Beziers PFM", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Catmull-Roms PFM", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Shapes PFM", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Sobel Edges PFM", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Waves PFM", DummyPFM::new, false, false).setPremium(true).setReleaseState(EnumReleaseState.BETA);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Circular Scribbles", DummyPFM::new, false, false).setPremium(true).setReleaseState(EnumReleaseState.ALPHA);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Shapes", DummyPFM::new, false, false).setPremium(true).setReleaseState(EnumReleaseState.BETA);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Triangulation", DummyPFM::new, false, false).setPremium(true).setReleaseState(EnumReleaseState.BETA);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Tree", DummyPFM::new, false, false).setPremium(true).setReleaseState(EnumReleaseState.BETA);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Stippling", DummyPFM::new, false, false).setPremium(true).setReleaseState(EnumReleaseState.BETA);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Diagram", DummyPFM::new, false, false).setPremium(true).setReleaseState(EnumReleaseState.BETA);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive TSP", DummyPFM::new, false, false).setPremium(true).setReleaseState(EnumReleaseState.BETA);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Circles", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Triangulation", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Tree", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Stippling", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Dashes", DummyPFM::new, false, false).setPremium(true).setReleaseState(EnumReleaseState.BETA);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Diagram", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi TSP", DummyPFM::new, false, false).setPremium(true);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Rectangles", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Voronoi", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Custom", DummyPFM::new, false, false).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Layers PFM", DummyPFM::new, false, false).setPremium(true).setReleaseState(EnumReleaseState.BETA);
    }

    public Object[] disableBatchProcessingUI(Object...objects){
        FXController controller = (FXController)objects[0];
        controller.batchProcessingController.vboxBatchProcessing.setDisable(true);
        controller.titledPaneBatchProcessing.setText(controller.titledPaneBatchProcessing.getText() + " (Premium)");
        controller.batchProcessingController.vboxBatchProcessing.setOnMouseClicked(e -> FXController.showPremiumFeatureDialog());
        controller.titledPaneBatchProcessing.setOnMouseClicked(e -> FXController.showPremiumFeatureDialog());
        return objects;
    }

    public Object[] disableColourSplitterUI(Object...objects) {
        FXController controller = (FXController) objects[0];
        controller.drawingSetsController.buttonConfigureSplitter.setOnMouseClicked(e -> FXController.showPremiumFeatureDialog());
        controller.drawingSetsController.comboBoxColourSeperation.setOnMouseClicked(e -> FXController.showPremiumFeatureDialog());
        /* TODO DO WE NEED THIS???
        DrawingBotV3.INSTANCE.colourSeperator.addListener((observable, oldValue, newValue) -> {
            if(!newValue.isDefault()){
                DrawingBotV3.INSTANCE.colourSeperator.set(Register.DEFAULT_COLOUR_SPLITTER);
            }
        });

         */
        return objects;
    }

    public Object[] disableHPGLUI(Object...objects) {
        FXExportController controller = (FXExportController) objects[0];
        controller.anchorPaneHPGLSettings.setDisable(true);
        controller.tabHPGLSettings.getTabPane().setOnMouseClicked(e -> {
            if(controller.tabHPGLSettings.isSelected()){
                FXController.showPremiumFeatureDialog();
            }
        });
        return objects;
    }


    public Object[] disableOpenGL(Object...values) {
        DrawingBotV3.INSTANCE.controller.choiceBoxDisplayMode.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null && newValue.getRenderer() != null && !newValue.getRenderer().isDefaultRenderer() && !FXApplication.isPremiumEnabled){
                FXController.showPremiumFeatureDialog();
                Platform.runLater(() -> DrawingBotV3.project().displayMode.set(Register.INSTANCE.DISPLAY_MODE_DRAWING));
            }
        });
        return values;
    }

    public Object[] initMenuOption(Object...values){
        Menu menuFile = (Menu) values[0];
        MenuItem serialPortExport = new MenuItem("Connect to Plotter / Serial Port (Premium)");
        serialPortExport.setOnAction(e -> FXController.showPremiumFeatureDialog());
        menuFile.getItems().add(serialPortExport);
        return values;
    }


    @Override
    public void registerDrawingExportHandlers() {
        MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.VECTOR, "hpgl_default", "Export HPGL File (.hpgl)", false, (exportTask, saveLocation) -> {}, FileUtils.FILTER_HPGL, FileUtils.FILTER_TXT)).setPremium();
        MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.ANIMATION, "animation_img_seq", "Export Animation - (Image Sequence, .png, .jpg)", false, (exportTask, saveLocation) -> {}, FileUtils.FILTER_PNG, FileUtils.FILTER_JPG, FileUtils.FILTER_TIF, FileUtils.FILTER_TGA, FileUtils.FILTER_WEBP)).setPremium();
        MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.ANIMATION, "animation_h264","Export Animation - (H.264, .mp4)", false, (exportTask, saveLocation) -> {}, FileUtils.FILTER_MP4)).setPremium();
        MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.ANIMATION, "animation_prores422","Export Animation - (ProRes 422, .mov)", false, (exportTask, saveLocation) -> {}, FileUtils.FILTER_MOV)).setPremium();
    }

    @Override
    public void registerColourSplitterHandlers() {
        MasterRegistry.INSTANCE.registerColourSplitter(new ColourSeperationHandler("CMYK"));
    }

    public static class DummyPFM implements IPFM {
        @Override
        public void init(IPlottingTools tools) {}

        @Override
        public void run() {}
    }

}
