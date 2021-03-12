package drawingbot.files;

import drawingbot.DrawingBotV3;
import drawingbot.utils.DBConstants;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class FileUtils {

    public static final FileChooser.ExtensionFilter IMPORT_IMAGES = new FileChooser.ExtensionFilter("Image Files", "*.tif", "*.tga", "*.png", "*.jpg", "*.gif", "*.bmp", "*.jpeg");

    public static final FileChooser.ExtensionFilter FILTER_JSON = new FileChooser.ExtensionFilter("JSON - JavaScript Object Notation", "*.json");

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

    private static File importDirectory = null;

    public static File getImportDirectory(){
        if(importDirectory == null){
            importDirectory = new File(FileUtils.getUserHomeDirectory());
        }
        return importDirectory;
    }

    public static void updateImportDirectory(File directory){
        importDirectory = directory;
    }

    private static File exportDirectory = null;

    public static File getExportDirectory(){
        if(exportDirectory == null){
            exportDirectory = new File(FileUtils.getUserHomeDirectory());
        }
        return exportDirectory;
    }

    public static void updateExportDirectory(File directory){
        exportDirectory = directory;
    }

    public static String getUserHomeDirectory() {
        return System.getProperty("user.home");
    }

    public static String getUserDataDirectory() {
        return System.getProperty("user.home") + File.separator + "." + DBConstants.appName + File.separator;
    }

    public static PrintWriter createWriter(File file) {
        if (file == null) {
            throw new RuntimeException("File passed to createWriter() was null");
        }
        try {
            createPath(file);  // make sure in-between folders exist
            OutputStream output = new FileOutputStream(file);
            if (file.getName().toLowerCase().endsWith(".gz")) {
                output = new GZIPOutputStream(output);
            }
            return createWriter(output);

        } catch (Exception e) {
            throw new RuntimeException("Couldn't create a writer for " +
                    file.getAbsolutePath(), e);
        }
    }

    public static PrintWriter createWriter(OutputStream output) {
        BufferedOutputStream bos = new BufferedOutputStream(output, 8192);
        OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        return new PrintWriter(osw);
    }

    public static void createPath(File file) {
        try {
            String parent = file.getParent();
            if (parent != null) {
                File unit = new File(parent);
                if (!unit.exists()) unit.mkdirs();
            }
        } catch (SecurityException se) {
            System.err.println("You don't have permissions to create " +
                    file.getAbsolutePath());
        }
    }
}