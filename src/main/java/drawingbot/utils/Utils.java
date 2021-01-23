package drawingbot.utils;

public class Utils {

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
