package drawingbot.image;

import drawingbot.pfm.IPFM;
import drawingbot.pfm.PFMMasterRegistry;
import drawingbot.utils.GenericFactory;
import drawingbot.utils.GenericSetting;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

//TODO
public class ImageFilters {

    public static List<GenericFactory<IImageFilter>> filterFactories = new LinkedList<>();
    public static ObservableList<GenericFactory<IImageFilter>> currentFilters = FXCollections.observableArrayList();

    static {
        registerImageFilter(LazyConvolutionFilter.class, "Gaussian Blur", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_GAUSSIAN_BLUR), false);
        registerImageFilter(UnsharpMaskFilter.class, "Unsharp Mask", UnsharpMaskFilter::new, false);
        registerImageFilter(LazyConvolutionFilter.class, "Motion Blur", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_MOTION_BLUR), false);
        registerImageFilter(LazyConvolutionFilter.class, "Outline Image", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_OUTLINE), false);
        registerImageFilter(LazyConvolutionFilter.class, "Edge Detect", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_EDGE_DETECT), false);
        registerImageFilter(LazyConvolutionFilter.class, "Emboss", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_EMBOSS), false);
        registerImageFilter(LazyConvolutionFilter.class, "Sharpen", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_SHARPEN), false);
        registerImageFilter(LazyConvolutionFilter.class, "Blur", () -> new LazyConvolutionFilter(ConvolutionMatrices.MATRIX_BLUR), false);
        registerImageFilter(SobelFilter.class, "Sobel", SobelFilter::new, false);
        registerImageFilter(SimpleFilter.class, "Grayscale", () -> new SimpleFilter(ImageTools::grayscaleFilter), false);
        registerImageFilter(SimpleFilter.class, "Invert", () -> new SimpleFilter(ImageTools::invertFilter), false);
        registerImageFilter(BorderFilter.class, "Border", BorderFilter::new, false);

    }

    public static <I extends IImageFilter> void registerImageFilter(Class<I> filterClass, String name, Supplier<I> create, boolean isHidden){
        filterFactories.add(new GenericFactory(filterClass, name, create, isHidden));
    }

    public static GenericFactory<IImageFilter> getFilterFromName(String filter){
        return filterFactories.stream().filter(p -> p.getName().equals(filter)).findFirst().orElseGet(() -> null);
    }

    public static <C, V> void registerSetting(GenericSetting<C, V> setting){
        for(GenericFactory<IPFM> loader : PFMMasterRegistry.pfmFactories.values()){
            if(setting.isAssignableFrom(loader.getInstanceClass())){
                GenericSetting<C,V> copy = setting.copy();
                PFMMasterRegistry.pfmSettings.putIfAbsent(loader.getInstanceClass(), FXCollections.observableArrayList());
                PFMMasterRegistry.pfmSettings.get(loader.getInstanceClass()).add(copy);
                setting.value.addListener((observable, oldValue, newValue) -> ImageFilters.onSettingChanged(copy, observable, oldValue, newValue));
            }
        }
    }

    public static <V> void onSettingChanged(GenericSetting<?,V> setting, ObservableValue<?> observable, V oldValue, V newValue){
        ///not used at the moment, called whenever a setting's value is changed
    }

    public static class LazyConvolutionFilter implements IImageFilter{

        public final float[][] matrix;
        public int scale;
        public boolean normalize;

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

        public String image = "border/b1.png";

        @Override
        public BufferedImage filter(BufferedImage img) {
            return ImageTools.lazyImageBorder(img, image, 0, 0);
        }
    }

    public interface IImageFilter{
        BufferedImage filter(BufferedImage img);
    }

}
