package drawingbot.files;

import drawingbot.utils.DBConstants;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class FileUtils {

    public static final FileChooser.ExtensionFilter IMPORT_IMAGES = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.jif", "*.jfif", "*.tif", "*.tiff", "*.tga", "*.gif", "*.bmp", "*.wbmp");
    public static final FileChooser.ExtensionFilter IMPORT_VIDEOS = new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mov", "*.avi");
    public static final FileChooser.ExtensionFilter IMPORT_VECTORS = new FileChooser.ExtensionFilter("Vector Files", "*.svg");

    public static final FileChooser.ExtensionFilter FILTER_ALL_FILES = new FileChooser.ExtensionFilter("All Files", "*.*");
    public static final FileChooser.ExtensionFilter FILTER_JSON = new FileChooser.ExtensionFilter("JSON - JavaScript Object Notation", "*.json");
    public static final FileChooser.ExtensionFilter FILTER_PROJECT = new FileChooser.ExtensionFilter("DrawingBotV3 - Project File", "*.drawingbotv3");

    public static final FileChooser.ExtensionFilter FILTER_TIF = new FileChooser.ExtensionFilter("TIF - Tagged Image File", "*.tif");
    public static final FileChooser.ExtensionFilter FILTER_TGA = new FileChooser.ExtensionFilter("TGA - Truevision Advanced Raster Graphics Adapter", "*.tga");
    public static final FileChooser.ExtensionFilter FILTER_PNG = new FileChooser.ExtensionFilter("PNG - Portable Network Graphics", "*.png");
    public static final FileChooser.ExtensionFilter FILTER_JPG = new FileChooser.ExtensionFilter("JPG - Joint Photographic Experts Group", "*.jpg");
    public static final FileChooser.ExtensionFilter FILTER_WEBP = new FileChooser.ExtensionFilter("WEBP - Google WebP", "*.webp");
    public static final FileChooser.ExtensionFilter FILTER_MP4 = new FileChooser.ExtensionFilter("MP4 - MPEG-4", "*.mp4");
    public static final FileChooser.ExtensionFilter FILTER_MOV = new FileChooser.ExtensionFilter("MOV - QuickTime File Format", "*.mov");

    public static final FileChooser.ExtensionFilter FILTER_PDF = new FileChooser.ExtensionFilter("PDF - Portable Document Format", "*.pdf");
    public static final FileChooser.ExtensionFilter FILTER_SVG = new FileChooser.ExtensionFilter("SVG", "*.svg");
    public static final FileChooser.ExtensionFilter FILTER_GCODE = new FileChooser.ExtensionFilter("GCODE", "*.gcode");
    public static final FileChooser.ExtensionFilter FILTER_HPGL = new FileChooser.ExtensionFilter("HPGL", "*.hpgl");
    public static final FileChooser.ExtensionFilter FILTER_TXT = new FileChooser.ExtensionFilter("Text File", "*.txt");

    public static final FileChooser.ExtensionFilter IMPORT_ALL;

    static {
        List<String> filters = new ArrayList<>();
        filters.addAll(IMPORT_IMAGES.getExtensions());
        filters.addAll(IMPORT_VIDEOS.getExtensions());
        filters.addAll(IMPORT_VECTORS.getExtensions());
        IMPORT_ALL = new FileChooser.ExtensionFilter("Supported Files", filters);
    }

    public static File removeExtension(File file){
        String path = file.toString();
        return new File(removeExtension(path));
    }

    public static String removeExtension(String string){
        int end = string.lastIndexOf(".");
        if(end == -1){
            return string;
        }
        string = string.substring(0, end);
        return string;
    }

    public static String getExtension(String string){
        int begin = string.lastIndexOf(".");
        if(begin == -1){
            return "";
        }
        return string.substring(begin);
    }

    public static boolean hasExtension(String string){
        int begin = string.lastIndexOf(".");
        return begin != -1;
    }

    public static boolean matchesExtensionFilter(String extension, FileChooser.ExtensionFilter extensionFilter){
        return extensionFilter.getExtensions().contains("*" + extension.toLowerCase()) || extensionFilter.getExtensions().contains(extension.toLowerCase()) ;
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

    public static String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    public static String getUserHomeDirectory() {
        return System.getProperty("user.home");
    }

    public static String getUserDataDirectory() {
        return getUserHomeDirectory() + File.separator + "." + "DrawingBotV3" + File.separator;
    }

    public static String getUserThumbnailDirectory() {
        return getUserDataDirectory() + "thumbs" + File.separator;
    }

    public static String getUserFontsDirectory() {
        return getUserDataDirectory() + "fonts" + File.separator;
    }

    public static String getUserLogsDirectory() {
        return getUserDataDirectory() + "logs" + File.separator;
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

    public static String createPathMatcherGlob(FileChooser.ExtensionFilter ...filters){
        StringBuilder builder = new StringBuilder();
        for(FileChooser.ExtensionFilter filter : filters){
            for(String extension : filter.getExtensions()){
                extension = extension.replace("*.", "");
                builder.append(extension);
                builder.append(",");
            }
        }
        //Remove the trailing ","
        builder.deleteCharAt(builder.length() - 1);
        return "glob:*.{" + builder.toString() + "}";
    }

    public static void createPath(File file) {
        try {
            String parent = file.getParent();
            if (parent != null) {
                File unit = new File(parent);
                if (!unit.exists()){
                    unit.mkdirs();
                }
            }
        } catch (SecurityException se) {
            System.err.println("You don't have permissions to create " +
                    file.getAbsolutePath());
        }
    }
}