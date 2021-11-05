package drawingbot.utils;

import drawingbot.drawing.DrawingPen;
import drawingbot.drawing.DrawingSet;
import drawingbot.image.ImageTools;
import drawingbot.registry.MasterRegistry;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public enum EnumColourSplitter {
    DEFAULT(List::of, EnumColourSplitter::createDefaultDrawingSet, List.of("Original")),
    CMYK(EnumColourSplitter::splitCMYK, EnumColourSplitter::createDefaultDrawingSet, List.of("Cyan", "Magenta", "Yellow", "Key"));
    //RGB(EnumColourSplitter::splitRGB, EnumColourSplitter::createDefaultDrawingSet, List.of("Red", "Green", "Blue"));

    public final Function<BufferedImage, List<BufferedImage>> splitFunction;
    public final Function<EnumColourSplitter, DrawingSet> createDrawingSet;
    public final List<String> outputNames;
    public DrawingSet drawingSet;

    EnumColourSplitter(Function<BufferedImage, List<BufferedImage>> splitFunction, Function<EnumColourSplitter, DrawingSet> createDrawingSet, List<String> outputNames){
        this.splitFunction = splitFunction;
        this.createDrawingSet = createDrawingSet;
        this.outputNames = outputNames;
    }

    public DrawingSet getDrawingSet(){
        return drawingSet;
    }

    public int getSplitCount(){
        return outputNames.size();
    }

    public static DrawingSet createDefaultDrawingSet(EnumColourSplitter splitter){
        if(splitter != DEFAULT){
            List<DrawingPen> pens = new ArrayList<>();
            for(int i = 0; i < splitter.outputNames.size(); i++){
                DrawingPen pen = MasterRegistry.INSTANCE.getDrawingPenFromRegistryName(DBConstants.DRAWING_TYPE_SPECIAL + ":" + splitter.outputNames.get(i));
                pens.add(pen);
            }
            return new ColourSplitterDrawingSet(splitter, DBConstants.DRAWING_TYPE_SPECIAL,splitter.name() + " Seperation", pens);
        }
        return null;
    }

    public static List<BufferedImage> splitCMYK(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB( 0, 0, width, height, new int[width*height], 0, width);

        BufferedImage cyanImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage magentaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage yellowImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage keyImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        float[] rgb = new float[3];
        float[] cymk = new float[4];

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixels[index];
                ImageTools.getRGBFloatFromARGB(argb, rgb);
                ImageTools.RGBtoCMYK(rgb, cymk);
                cyanImage.setRGB(x, y, ImageTools.getARGBFromFloat(1F, 1-cymk[0], 1-cymk[0], 1-cymk[0]));
                magentaImage.setRGB(x, y, ImageTools.getARGBFromFloat(1F, 1-cymk[1], 1-cymk[1], 1-cymk[1]));
                yellowImage.setRGB(x, y, ImageTools.getARGBFromFloat(1F, 1-cymk[2], 1-cymk[2], 1-cymk[2]));
                keyImage.setRGB(x, y, ImageTools.getARGBFromFloat(1F, 1-cymk[3], 1-cymk[3], 1-cymk[3]));
                index++;
            }
        }
        return List.of(cyanImage, magentaImage, yellowImage, keyImage);
    }

    /*

    public static List<BufferedImage> splitRYB(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB( 0, 0, width, height, new int[width*height], 0, width);

        BufferedImage redImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage yellowImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage blueImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //BufferedImage keyImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        float[] rgb = new float[3];
        float[] ryb = new float[3];

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixels[index];
                ImageTools.getRGBFloatFromARGB(argb, rgb);
                ImageTools.RGBtoRYB(rgb, ryb);
                redImage.setRGB(x, y, ImageTools.getARGBFromFloat(1F, ryb[0], ryb[0], ryb[0]));
                yellowImage.setRGB(x, y, ImageTools.getARGBFromFloat(1F, ryb[1], ryb[1], ryb[1]));
                blueImage.setRGB(x, y, ImageTools.getARGBFromFloat(1F, ryb[2], ryb[2], ryb[2]));
                //keyImage.setRGB(x, y, ImageTools.getARGBFromFloat(1F, 1-cymk[3], 1-cymk[3], 1-cymk[3]));
                index++;
            }
        }
        return List.of(redImage, yellowImage, blueImage);
    }

     */

    public static List<BufferedImage> splitRGB(BufferedImage image){
        return splitRGBK(image, false);
    }

    public static List<BufferedImage> splitRGBK(BufferedImage image){
        return splitRGBK(image, true);
    }

    private static List<BufferedImage> splitRGBK(BufferedImage image, boolean includeKey){
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB( 0, 0, width, height, new int[width*height], 0, width);

        BufferedImage redImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage greenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage blueImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage keyImage = !includeKey ? null : new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[] argb = new int[4];

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ImageTools.getColourIntsFromARGB(pixels[index], argb);
                redImage.setRGB(x, y, ImageTools.getARGB(255, argb[1], 255, 255));
                greenImage.setRGB(x, y, ImageTools.getARGB(255, 255, argb[2], 255));
                blueImage.setRGB(x, y, ImageTools.getARGB(255, 255, 255, argb[3]));

                if(includeKey){
                    int lum = ImageTools.getPerceivedLuminanceFromRGB(argb[1], argb[2], argb[3]);
                    keyImage.setRGB(x, y, ImageTools.getARGB(255, lum, lum, lum));
                }
                index++;
            }
        }
        return includeKey ? List.of(redImage, greenImage, blueImage, keyImage) : List.of(redImage, greenImage, blueImage);
    }

    @Override
    public String toString() {
        return this == DEFAULT ? "Default" : name();
    }


    public static class ColourSplitterDrawingSet extends DrawingSet{

        public final transient EnumColourSplitter splitter;

        public ColourSplitterDrawingSet(EnumColourSplitter splitter) {
            super();
            this.splitter = splitter;
        }

        public ColourSplitterDrawingSet(EnumColourSplitter splitter, String type, String name, List<DrawingPen> pens) {
            super(type, name, pens);
            this.splitter = splitter;
        }
    }
}
