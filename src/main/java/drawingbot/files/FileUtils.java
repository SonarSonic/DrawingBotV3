package drawingbot.files;

import drawingbot.DrawingBotV3;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;

public class FileUtils {

    public static final FileChooser.ExtensionFilter IMPORT_IMAGES = new FileChooser.ExtensionFilter("Image Files", "*.tif", "*.tga", "*.png", "*.jpg", "*.gif", "*.bmp", "*.jpeg");

    public static final FileChooser.ExtensionFilter FILTER_TIF = new FileChooser.ExtensionFilter("TIF - Tagged Image File", "*.tif");
    public static final FileChooser.ExtensionFilter FILTER_TGA = new FileChooser.ExtensionFilter("TGA - Truevision Advanced Raster Graphics Adapter", "*.tga");
    public static final FileChooser.ExtensionFilter FILTER_PNG = new FileChooser.ExtensionFilter("PNG - Portable Network Graphics", "*.png");
    public static final FileChooser.ExtensionFilter FILTER_JPG = new FileChooser.ExtensionFilter("JPG - Joint Photographic Experts Group", "*.jpg");

    public static final FileChooser.ExtensionFilter FILTER_PDF = new FileChooser.ExtensionFilter("PDF - Portable Document Format", "*.pdf");
    public static final FileChooser.ExtensionFilter FILTER_SVG = new FileChooser.ExtensionFilter("SVG", "*.svg");
    public static final FileChooser.ExtensionFilter FILTER_GCODE = new FileChooser.ExtensionFilter("GCODE", "*.gcode");
    public static final FileChooser.ExtensionFilter FILTER_TXT = new FileChooser.ExtensionFilter("Text File", "*.txt");

    public static File removeExtension(File file){
        String path = file.toString();
        path = path.substring(0, path.lastIndexOf('.'));
        return new File(path);
    }

    public static String removeExtension(String string){
        string = string.substring(0, string.lastIndexOf('.'));
        return string;
    }

    public static String getUserDataDirectory() {
        return System.getProperty("user.home") + File.separator + "." + DrawingBotV3.appName + File.separator;
    }
}