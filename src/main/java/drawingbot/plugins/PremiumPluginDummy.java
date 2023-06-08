package drawingbot.plugins;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.Hooks;
import drawingbot.api.IPFM;
import drawingbot.api.IPlottingTools;
import drawingbot.api.IPlugin;
import drawingbot.drawing.ColourSeparationHandler;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.FileUtils;
import drawingbot.javafx.FXController;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.IDisplayMode;
import drawingbot.render.IRenderer;
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
        Hooks.addHook(Hooks.FX_CONTROLLER_POST_INIT, this::disableOpenGL);
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
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Curves", Register.CATEGORY_PFM_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Quad Beziers", Register.CATEGORY_PFM_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Cubic Beziers", Register.CATEGORY_PFM_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Catmull-Roms", Register.CATEGORY_PFM_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Shapes", Register.CATEGORY_PFM_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Sobel Edges", Register.CATEGORY_PFM_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Waves", Register.CATEGORY_PFM_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Spiral Circular Scribbles", Register.CATEGORY_PFM_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Hatch Sawtooth", Register.CATEGORY_PFM_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Hatch Circular Scribbles", Register.CATEGORY_PFM_SKETCH, DummyPFM::new).setPremium(true);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Shapes", Register.CATEGORY_PFM_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Triangulation", Register.CATEGORY_PFM_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Tree", Register.CATEGORY_PFM_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Stippling", Register.CATEGORY_PFM_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Dashes", Register.CATEGORY_PFM_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Letters", Register.CATEGORY_PFM_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Diagram", Register.CATEGORY_PFM_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Circular Scribbles", Register.CATEGORY_PFM_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive TSP", Register.CATEGORY_PFM_ADAPTIVE, DummyPFM::new).setPremium(true);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Shapes", Register.CATEGORY_PFM_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Triangulation", Register.CATEGORY_PFM_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Tree", Register.CATEGORY_PFM_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Stippling", Register.CATEGORY_PFM_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Dashes", Register.CATEGORY_PFM_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Letters", Register.CATEGORY_PFM_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Diagram", Register.CATEGORY_PFM_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi TSP", Register.CATEGORY_PFM_VORONOI, DummyPFM::new).setPremium(true);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Rectangles", Register.CATEGORY_PFM_MOSAIC, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Voronoi", Register.CATEGORY_PFM_MOSAIC, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Segments", Register.CATEGORY_PFM_MOSAIC, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Custom", Register.CATEGORY_PFM_MOSAIC, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Layers PFM", Register.CATEGORY_PFM_SPECIAL, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "SVG Converter", Register.CATEGORY_PFM_SPECIAL, DummyPFM::new).setPremium(true);
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

    public Object[] disableOpenGL(Object...values) {
        FXController controller = (FXController) values[0];
        controller.choiceBoxDisplayMode.valueProperty().addListener((observable, oldValue, newValue) -> {
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
        MasterRegistry.INSTANCE.registerColourSplitter(new ColourSeparationHandler("CMYK"));
    }

    public static class DummyPFM implements IPFM {
        @Override
        public void init(IPlottingTools tools) {}

        @Override
        public void run() {}
    }

}
