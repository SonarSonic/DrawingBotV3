package drawingbot.utils;

public class Utils {

    public static String capitalize(String name) {
        if (name != null && name.length() != 0) {
            char[] chars = name.toLowerCase().toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        } else {
            return name;
        }
    }

}
