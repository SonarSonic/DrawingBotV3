package drawingbot.plugins;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.Hooks;
import drawingbot.api.IPFM;
import drawingbot.api.IPlottingTools;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.FileUtils;
import drawingbot.javafx.FXController;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.IDisplayMode;
import drawingbot.render.IRenderer;
import drawingbot.SoftwareDBV3Free;
import drawingbot.utils.flags.FlagStates;
import drawingbot.utils.flags.Flags;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class PremiumPluginDummy extends AbstractPlugin {

    public static final PremiumPluginDummy INSTANCE = new PremiumPluginDummy();

    private PremiumPluginDummy() {}

    @Override
    public String getVersion() {
        return SoftwareDBV3Free.rawVersion;
    }

    @Override
    public String getDisplayName() {
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
            public String toString() {
                return getName();
            }

        });
    }

    @Override
    public void registerPFMS() {
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Curves", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Quad Beziers", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Cubic Beziers", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Catmull-Roms", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Sweeping Curves", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Shapes", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Sobel Edges", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Waves", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Flow Field", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Superformula", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Sketch Sweeping Curves", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "ECS Drawing", Register.PFM_TYPE_SPECIAL, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Spiral Circular Scribbles", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Hatch Sawtooth", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Hatch Circular Scribbles", Register.PFM_TYPE_SKETCH, DummyPFM::new).setPremium(true);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Shapes", Register.PFM_TYPE_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Triangulation", Register.PFM_TYPE_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Tree", Register.PFM_TYPE_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Stippling", Register.PFM_TYPE_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Dashes", Register.PFM_TYPE_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Letters", Register.PFM_TYPE_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Diagram", Register.PFM_TYPE_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive Circular Scribbles", Register.PFM_TYPE_ADAPTIVE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Adaptive TSP", Register.PFM_TYPE_ADAPTIVE, DummyPFM::new).setPremium(true);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "LBG Shapes", Register.PFM_TYPE_LBG, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "LBG Triangulation", Register.PFM_TYPE_LBG, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "LBG Tree", Register.PFM_TYPE_LBG, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "LBG Stippling", Register.PFM_TYPE_LBG, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "LBG Dashes", Register.PFM_TYPE_LBG, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "LBG Letters", Register.PFM_TYPE_LBG, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "LBG Diagram", Register.PFM_TYPE_LBG, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "LBG TSP", Register.PFM_TYPE_LBG, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "LBG Quad Tiles", Register.PFM_TYPE_LBG, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "LBG Circular Scribbles", Register.PFM_TYPE_LBG, DummyPFM::new).setPremium(true);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Shapes", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Triangulation", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Tree", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Stippling", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Dashes", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Letters", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi Diagram", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Voronoi TSP", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Grid Shapes", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Grid Dashes", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Grid Letters", Register.PFM_TYPE_VORONOI, DummyPFM::new).setPremium(true);

        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Rectangles", Register.PFM_TYPE_COMPOSITE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Voronoi", Register.PFM_TYPE_COMPOSITE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Segments", Register.PFM_TYPE_COMPOSITE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Mosaic Custom", Register.PFM_TYPE_COMPOSITE, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Layers PFM", Register.PFM_TYPE_SPECIAL, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "SVG Converter", Register.PFM_TYPE_SPECIAL, DummyPFM::new).setPremium(true);
        MasterRegistry.INSTANCE.registerPFM(DummyPFM.class, "Pen Calibration", Register.PFM_TYPE_SPECIAL, DummyPFM::new).setPremium(true);
    }

    public Object[] disableBatchProcessingUI(Object... objects) {
        FXController controller = (FXController) objects[0];
        controller.batchProcessingController.vboxBatchProcessing.setDisable(true);
        controller.titledPaneBatchProcessing.setText(controller.titledPaneBatchProcessing.getText() + " (Premium)");
        controller.batchProcessingController.vboxBatchProcessing.setOnMouseClicked(e -> FXController.showPremiumFeatureDialog());
        controller.titledPaneBatchProcessing.setOnMouseClicked(e -> FXController.showPremiumFeatureDialog());
        return objects;
    }

    public Object[] disableColourSplitterUI(Object... objects) {
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

    public Object[] disableOpenGL(Object... values) {
        FXController controller = (FXController) values[0];
        controller.choiceBoxDisplayMode.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getRenderer() != null && !newValue.getRenderer().isDefaultRenderer() && !FXApplication.isPremiumEnabled) {
                FXController.showPremiumFeatureDialog();
                Platform.runLater(() -> DrawingBotV3.project().displayMode.set(Register.INSTANCE.DISPLAY_MODE_DRAWING));
            }
        });
        return values;
    }

    public Object[] initMenuOption(Object... values) {
        Menu menuFile = (Menu) values[0];
        MenuItem serialPortExport = new MenuItem("Connect to Plotter / Serial Port (Premium)");
        serialPortExport.setOnAction(e -> FXController.showPremiumFeatureDialog());
        menuFile.getItems().add(serialPortExport);
        return values;
    }


    @Override
    public void init() {
        MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.VECTOR, "hpgl_default", "Export HPGL File (.hpgl)", false, (exportTask, saveLocation) -> {}, FileUtils.FILTER_HPGL, FileUtils.FILTER_TXT)).setPremium();
        MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.ANIMATION, "animation_img_seq", "Export Animation - (Image Sequence, .png, .jpg)", false, (exportTask, saveLocation) -> { }, FileUtils.FILTER_PNG, FileUtils.FILTER_JPG, FileUtils.FILTER_TIF, FileUtils.FILTER_TGA, FileUtils.FILTER_WEBP)).setPremium();
        MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.ANIMATION, "animation_h264", "Export Animation - (H.264, .mp4)", false, (exportTask, saveLocation) -> { }, FileUtils.FILTER_MP4)).setPremium();
        MasterRegistry.INSTANCE.registerDrawingExportHandler(new DrawingExportHandler(DrawingExportHandler.Category.ANIMATION, "animation_prores422", "Export Animation - (ProRes 422, .mov)", false, (exportTask, saveLocation) -> { }, FileUtils.FILTER_MOV)).setPremium();

        MasterRegistry.INSTANCE.registerColourSplitter(new ColorSeparationHandler("CMYK"));
    }

    public static class DummyPFM implements IPFM {
        @Override
        public void setPlottingTools(IPlottingTools tools) {}

        @Override
        public void run() {}
    }

}
