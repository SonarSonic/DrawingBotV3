package drawingbot.utils;

import java.text.NumberFormat;
import java.util.Random;

public class Utils {

    public static Random random = new Random();
    public static NumberFormat defaultNF = NumberFormat.getNumberInstance();
    public static String URL_GITHUB_REPO = "https://github.com/SonarSonic/Drawbot_image_to_gcode_v3";
    public static String URL_GITHUB_WIKI = URL_GITHUB_REPO + "/wiki";
    public static String URL_GITHUB_PFM_DOCS = URL_GITHUB_WIKI + "/Advanced-PFM-Settings";


    public static String capitalize(String name) {
        if (name != null && name.length() != 0) {
            char[] chars = name.toLowerCase().toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars).replace('_', ' ');
        } else {
            return name;
        }
    }

    public static double mapDouble(double value, double istart, double istop, double ostart, double ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }


}
