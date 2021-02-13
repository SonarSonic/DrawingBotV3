package drawingbot.utils;

import drawingbot.files.exporters.GCodeExporter;
import drawingbot.files.exporters.SVGExporter;

import java.text.NumberFormat;
import java.util.Random;

public class Utils {

    public static Random random = new Random();
    public static NumberFormat defaultNF = NumberFormat.getNumberInstance();
    public static String URL_GITHUB_REPO = "https://github.com/SonarSonic/Drawbot_image_to_gcode_v3";
    public static String URL_GITHUB_WIKI = URL_GITHUB_REPO + "/wiki";
    public static String URL_GITHUB_PFM_DOCS = URL_GITHUB_WIKI + "/Advanced-PFM-Settings";

    public static NumberFormat gCodeNF;

    public static String formatGCode(float f){
        if(gCodeNF == null){
            gCodeNF = NumberFormat.getNumberInstance();
            gCodeNF.setGroupingUsed(false);
            gCodeNF.setMinimumIntegerDigits(0);
            gCodeNF.setMinimumFractionDigits(GCodeExporter.gcode_decimals);
            gCodeNF.setMaximumFractionDigits(GCodeExporter.gcode_decimals);
        }
        return gCodeNF.format(f);
    }

    public static String formatSVG(float f){
        if(gCodeNF == null){
            gCodeNF = NumberFormat.getNumberInstance();
            gCodeNF.setGroupingUsed(false);
            gCodeNF.setMinimumIntegerDigits(0);
            gCodeNF.setMinimumFractionDigits(SVGExporter.svg_decimals);
            gCodeNF.setMaximumFractionDigits(SVGExporter.svg_decimals);
        }
        return gCodeNF.format(f);
    }

    public static String capitalize(String name) {
        if (name != null && name.length() != 0) {
            char[] chars = name.toLowerCase().toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars).replace('_', ' ');
        } else {
            return name;
        }
    }

    public static long clamp(long x, long min, long max){
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }

    public static float clamp(float x, float min, float max){
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }

    public static double clamp(double x, double min, double max){
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }

    public static int clamp(int x, int min, int max){
        if (x < min) return min;
        if (x > max) return max;
        return x;
    }

    public static int mapInt(int value, int istart, int istop, int ostart, int ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }

    public static float mapFloat(float value, float istart, float istop, float ostart, float ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }

    public static double mapDouble(double value, double istart, double istop, double ostart, double ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }


}
