package drawingbot.image;

import drawingbot.utils.GenericFactory;
import drawingbot.utils.GenericPreset;
import drawingbot.utils.GenericSetting;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

//TODO
public class ImageFilterRegistry {

    public static ObservableList<GenericFactory<IImageFilter>> filterFactories = FXCollections.observableArrayList();
    public static ObservableList<ObservableImageFilter> currentFilters = FXCollections.observableArrayList();
    public static HashMap<Class<? extends IImageFilter>, List<GenericSetting<?, ?>>> filterSettings = new LinkedHashMap<>();
    public static ObservableList<GenericPreset> imagePresets = FXCollections.observableArrayList();

    static {
        registerImageFilter(LazyConvolutionFilter.class, "Gaussian Blur", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_GAUSSIAN_BLUR), false);
        registerImageFilter(UnsharpMaskFilter.class, "Unsharp Mask", UnsharpMaskFilter::new, false);
        registerImageFilter(LazyConvolutionFilter.class, "Motion Blur", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_MOTION_BLUR), false);
        registerImageFilter(LazyConvolutionFilter.class, "Outline", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_OUTLINE), false);
        registerImageFilter(LazyConvolutionFilter.class, "Edge Detect", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_EDGE_DETECT), false);
        registerImageFilter(LazyConvolutionFilter.class, "Emboss", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_EMBOSS), false);
        registerImageFilter(LazyConvolutionFilter.class, "Sharpen", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_SHARPEN), false);
        registerImageFilter(LazyConvolutionFilter.class, "Blur", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_BLUR), false);
        registerImageFilter(SobelFilter.class, "Sobel", SobelFilter::new, false);
        registerImageFilter(SimpleFilter.class, "Grayscale", () -> new SimpleFilter(ImageTools::grayscaleFilter), false);
        registerImageFilter(SimpleFilter.class, "Invert", () -> new SimpleFilter(ImageTools::invertFilter), false);
        registerImageFilter(BorderFilter.class, "Border", BorderFilter::new, false);

        registerSetting(GenericSetting.createRangedIntSetting(BorderFilter.class, "Type", 1, 1, 13, (filter, value) -> filter.borderNumber = value));

    }

    public static <I extends IImageFilter> void registerImageFilter(Class<I> filterClass, String name, Supplier<I> create, boolean isHidden){
        filterFactories.add(new GenericFactory(filterClass, name, create, isHidden));
    }

    public static GenericFactory<IImageFilter> getFilterFromName(String filter){
        return filterFactories.stream().filter(p -> p.getName().equals(filter)).findFirst().orElseGet(() -> null);
    }

    public static void registerSetting(GenericSetting<? extends IImageFilter, ?> setting){
        filterSettings.putIfAbsent(setting.clazz, new ArrayList<>());
        filterSettings.get(setting.clazz).add(setting);
    }

    public static GenericPreset getDefaultImageFilterPreset(){
        return imagePresets.stream().filter(p -> p.presetName.equals("Default")).findFirst().get();
    }

    public static void registerPreset(GenericPreset preset){
        imagePresets.add(preset);
    }

    public static <V> void onSettingChanged(GenericSetting<?,V> setting, ObservableValue<?> observable, V oldValue, V newValue){
        ///not used at the moment, called whenever a setting's value is changed
    }

    public static BufferedImage applyCurrentFilters(BufferedImage image){
        for(ImageFilterRegistry.IImageFilter filter : ImageFilterRegistry.createFilters()){
            image = filter.filter(image);
        }
        return image;
    }

    public static List<IImageFilter> createFilters(){
        List<IImageFilter> filters = new ArrayList<>();
        for(ObservableImageFilter filter : currentFilters){
            if(filter.enable.get()){
                IImageFilter instance = filter.filterFactory.instance();
                filter.filterSettings.forEach(setting -> setting.applySetting(instance));
                filters.add(instance);
            }
        }
        return filters;
    }

    public static ObservableList<GenericSetting<?,?>> getNewObservableSettingsList(GenericFactory<IImageFilter> filterFactory){
        ObservableList<GenericSetting<?, ?>> settings = FXCollections.observableArrayList();
        for(Map.Entry<Class<? extends IImageFilter>, List<GenericSetting<?, ?>>> entry : filterSettings.entrySet()){
            if(entry.getKey().isAssignableFrom(filterFactory.getInstanceClass())){
                for(GenericSetting<?, ?> setting : entry.getValue()){
                    settings.add(setting.copy());
                }
            }
        }
        return settings;
    }

    public static class ObservableImageFilter{

        public SimpleBooleanProperty enable;
        public SimpleStringProperty name;
        public GenericFactory<IImageFilter> filterFactory;
        public ObservableList<GenericSetting<?, ?>> filterSettings;

        public ObservableImageFilter(GenericFactory<IImageFilter> filterFactory) {
            this(true, filterFactory.getName(), filterFactory, getNewObservableSettingsList(filterFactory));
        }

        public ObservableImageFilter(ObservableImageFilter duplicate) {
            this(duplicate.enable.get(), duplicate.name.get(), duplicate.filterFactory, GenericSetting.copy(duplicate.filterSettings, FXCollections.observableArrayList()));
        }

        public ObservableImageFilter(boolean enable, String name, GenericFactory<IImageFilter> filterFactory, ObservableList<GenericSetting<?, ?>> filterSettings){
            this.enable = new SimpleBooleanProperty(enable);
            this.name = new SimpleStringProperty(name);
            this.filterFactory = filterFactory;
            this.filterSettings = filterSettings;
        }

    }

    public static class LazyConvolutionFilter implements IImageFilter{

        public final float[][] matrix;
        public int scale = 1;
        public boolean normalize = false;

        public LazyConvolutionFilter(float[][] matrix){
            this.matrix = matrix;
        }

        @Override
        public BufferedImage filter(BufferedImage img) {
            return ImageTools.lazyConvolutionFilter(img, matrix, scale, normalize);
        }
    }

    public static class SobelFilter implements IImageFilter{

        @Override
        public BufferedImage filter(BufferedImage img) {
            img = ImageTools.lazyConvolutionFilter(img, ConvolutionMatrices.MATRIX_SOBEL_X);
            img = ImageTools.lazyConvolutionFilter(img, ConvolutionMatrices.MATRIX_SOBEL_Y);
            return img;
        }
    }

    public static class UnsharpMaskFilter implements IImageFilter{

        @Override
        public BufferedImage filter(BufferedImage img) {
            img = ImageTools.lazyConvolutionFilter(img, ConvolutionMatrices.MATRIX_UNSHARP_MASK, 4, true);
            img = ImageTools.lazyConvolutionFilter(img, ConvolutionMatrices.MATRIX_UNSHARP_MASK, 3, true);
            return img;
        }
    }

    public static class SimpleFilter implements IImageFilter{

        public Function<Integer, Integer> filter;

        public SimpleFilter(Function<Integer, Integer> filter){
            this.filter = filter;
        }

        @Override
        public BufferedImage filter(BufferedImage img) {
            return ImageTools.lazyRGBFilter(img, filter);
        }
    }

    public static class BorderFilter implements IImageFilter{

        public final String prefix = "border/b", suffix = ".png";
        public int borderNumber;

        public BorderFilter(){
            this.borderNumber = 1;
        }

        public BorderFilter(int borderNumber){
            this.borderNumber = borderNumber;
        }

        @Override
        public BufferedImage filter(BufferedImage img) {
            return ImageTools.lazyImageBorder(img, prefix + borderNumber + suffix, 0, 0);
        }
    }

    public interface IImageFilter{
        BufferedImage filter(BufferedImage img);
    }

}
