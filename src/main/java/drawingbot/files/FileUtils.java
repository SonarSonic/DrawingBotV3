package drawingbot.files;

import drawingbot.utils.Utils;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static final List<FileChooser.ExtensionFilter> SINGLE_FILTERS = new ArrayList<>();

    public static final FileChooser.ExtensionFilter IMPORT_IMAGES = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.jif", "*.jfif", "*.tif", "*.tiff", "*.tga", "*.gif", "*.bmp", "*.wbmp");
    public static final FileChooser.ExtensionFilter IMPORT_VIDEOS = new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mov", "*.avi");
    public static final FileChooser.ExtensionFilter IMPORT_VECTORS = new FileChooser.ExtensionFilter("Vector Files", "*.svg");

    public static final FileChooser.ExtensionFilter FILTER_ALL_FILES = new FileChooser.ExtensionFilter("All Files", "*");
    public static final FileChooser.ExtensionFilter FILTER_JSON = registerSingleExtensionFilter("JSON - JavaScript Object Notation", "*.json");
    public static final FileChooser.ExtensionFilter FILTER_PROJECT = registerSingleExtensionFilter("DrawingBotV3 - Project File", "*.drawingbotv3");

    public static final FileChooser.ExtensionFilter FILTER_TIF = registerSingleExtensionFilter("TIF - Tagged Image File", "*.tif");
    public static final FileChooser.ExtensionFilter FILTER_TGA = registerSingleExtensionFilter("TGA - Truevision Advanced Raster Graphics Adapter", "*.tga");
    public static final FileChooser.ExtensionFilter FILTER_PNG = registerSingleExtensionFilter("PNG - Portable Network Graphics", "*.png");
    public static final FileChooser.ExtensionFilter FILTER_JPG = registerSingleExtensionFilter("JPG - Joint Photographic Experts Group", "*.jpg");
    public static final FileChooser.ExtensionFilter FILTER_WEBP = registerSingleExtensionFilter("WEBP - Google WebP", "*.webp");
    public static final FileChooser.ExtensionFilter FILTER_MP4 = registerSingleExtensionFilter("MP4 - MPEG-4", "*.mp4");
    public static final FileChooser.ExtensionFilter FILTER_MOV = registerSingleExtensionFilter("MOV - QuickTime File Format", "*.mov");

    public static final FileChooser.ExtensionFilter FILTER_PDF = registerSingleExtensionFilter("PDF - Portable Document Format", "*.pdf");
    public static final FileChooser.ExtensionFilter FILTER_SVG = registerSingleExtensionFilter("SVG", "*.svg");
    public static final FileChooser.ExtensionFilter FILTER_GCODE = registerSingleExtensionFilter("GCODE", "*.gcode");
    public static final FileChooser.ExtensionFilter FILTER_HPGL = registerSingleExtensionFilter("HPGL", "*.hpgl");
    public static final FileChooser.ExtensionFilter FILTER_TXT = registerSingleExtensionFilter("Text File", "*.txt");
    public static final FileChooser.ExtensionFilter FILTER_ZIP = registerSingleExtensionFilter("ZIP Archive", "*.zip");

    public static final FileChooser.ExtensionFilter IMPORT_ALL;

    static {
        List<String> filters = new ArrayList<>();
        filters.addAll(IMPORT_IMAGES.getExtensions());
        filters.addAll(IMPORT_VIDEOS.getExtensions());
        filters.addAll(IMPORT_VECTORS.getExtensions());
        IMPORT_ALL = new FileChooser.ExtensionFilter("Supported Files", filters);
    }

    public static FileChooser.ExtensionFilter registerSingleExtensionFilter(String description, String... extensions){
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(description, extensions);
        SINGLE_FILTERS.add(filter);
        return filter;
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

    public static String getSafeFileName(String filename){
        return Utils.getSafeString(filename);
    }

    public static FileChooser.ExtensionFilter findMatchingExtensionFilter(String extension){
        return findMatchingExtensionFilter(extension, SINGLE_FILTERS);
    }

    public static FileChooser.ExtensionFilter findMatchingExtensionFilter(String extension, List<FileChooser.ExtensionFilter> filters){
        if(extension.isEmpty() || filters.isEmpty()){
            return null;
        }

        extension = extension.replaceAll("[*.]", "");
        for(FileChooser.ExtensionFilter filter : filters){
            for(String toMatch : filter.getExtensions()){
                if(extension.equals(toMatch.replaceAll("[*.]", ""))){
                    return filter;
                }
            }
        }
        return null;
    }

    public static boolean matchesExtensionFilter(String extension, FileChooser.ExtensionFilter extensionFilter){
        return extensionFilter.getExtensions().contains("*" + extension.toLowerCase()) || extensionFilter.getExtensions().contains(extension.toLowerCase()) ;
    }

    public static List<String> getRawExtensions(FileChooser.ExtensionFilter[] extensionFilters){
        List<String> extensions = new ArrayList<>();
        for(FileChooser.ExtensionFilter filter : extensionFilters){
            for(String extension : filter.getExtensions()){
                extensions.add(extension.replace("*", ""));
            }
        }
        return extensions;
    }

    public static String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
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

    public static String getTestDirectory() {
        return getWorkingDirectory() + File.separator + "tests" + File.separator;
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
}