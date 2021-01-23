package drawingbot.files;

import javafx.stage.FileChooser;

import java.io.File;

public class FileUtils {

    public static final FileChooser.ExtensionFilter FILTER_IMAGES = new FileChooser.ExtensionFilter("Image Files", "*.tif", "*.tga", "*.png", "*.jpg", "*.gif", "*.bmp", "*.jpeg"); //TODO CHECK GIF + BMP
    public static final FileChooser.ExtensionFilter EXPORT_PDF = new FileChooser.ExtensionFilter("PDF", "*.pdf");
    public static final FileChooser.ExtensionFilter EXPORT_SVG = new FileChooser.ExtensionFilter("SVG", "*.svg");
    public static final FileChooser.ExtensionFilter EXPORT_JPG = new FileChooser.ExtensionFilter("JPG", "*.jpg");
    public static final FileChooser.ExtensionFilter EXPORT_GCODE = new FileChooser.ExtensionFilter("GCODE", "*.gcode");

    public static File removeExtension(File file){
        String path = file.toString();
        path = path.substring(0, path.lastIndexOf('.'));
        return new File(path);
    }

}
