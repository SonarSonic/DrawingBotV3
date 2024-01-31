package drawingbot.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Utils {

    public final static Random random = new Random();
    public final static NumberFormat defaultNF = NumberFormat.getNumberInstance();
    public final static DecimalFormat defaultDF = new DecimalFormat("#.###");
    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    public final static SimpleDateFormat dateFormatSafe = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    public final static DecimalFormat oneDecimal = new DecimalFormat("#.#");

    public static NumberFormat gCodeNF;
    public static final int gcode_decimals = 3; // numbers of decimal places used on gcode exports
    public static final char gcode_decimal_seperator = '.';

    /**formats the value into GCODE Number Format*/
    public static String gcodeFloat(float f){
        if(gCodeNF == null){
            gCodeNF = NumberFormat.getNumberInstance();
            gCodeNF.setGroupingUsed(false);
            gCodeNF.setMinimumIntegerDigits(0);
            gCodeNF.setMinimumFractionDigits(gcode_decimals);
            gCodeNF.setMaximumFractionDigits(gcode_decimals);
        }
        String s = gCodeNF.format(f);
        s = s.replace('.', gcode_decimal_seperator);
        s = s.replace(',', gcode_decimal_seperator);

        return s;
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

    public static String getDateAndTime(){
        Date date = new Date(System.currentTimeMillis());
        return dateFormat.format(date);
    }

    public static String getDateAndTimeSafe(){
        Date date = new Date(System.currentTimeMillis());
        return dateFormatSafe.format(date);
    }

    public static String getSafeString(String input){
        return input.replaceAll("[<>:\"\\\\/|?*]", "_").replace('\\', '_');
    }

    public static String getSafeRegistryName(String input){
        return getSafeString(input).toLowerCase().replace(' ', '_');
    }

    /**
     * Compares two version strings, can work as a comparator
     * -1 = version1 < version2
     * 0 = version1 == version2
     * 1 = version1 > version2
     */
    public static int compareVersion(String v1, String v2, int depth){
        int[] version1 = normaliseVersion(v1, depth);
        int[] version2 = normaliseVersion(v2, depth);

        for(int i = 0; i < depth; i++){
            int subVersion1 = version1[i];
            int subVersion2 = version2[i];
            if(subVersion1 == subVersion2){
                continue;
            }
            if(subVersion1 < subVersion2){
                return -1; //version1 is older
            }
            return 1; //version1 is newer
        }
        return 0; //the versions are the same
    }

    public static int[] normaliseVersion(String version, int depth){
        String[] split = version.split("\\.");
        int[] versions = new int[depth];
        for(int i = 0; i < depth; i++){
            versions[i] = i < split.length ? Integer.parseInt(split[i]) : 0;
        }
        return versions;
    }

    public static String escape(String s){
        return s.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                .replace("\'", "\\'")
                .replace("\"", "\\\"");
    }

    public static <T> void addAllReverse(List<T> src, List<T> dst, boolean reverse){
        if(reverse){
            for(int i = src.size()-1; i >= 0; i--){
                dst.add(src.get(i));
            }
        }else{
            dst.addAll(src);
        }
    }

    public static long clamp(long value, long min, long max){
        if (value < min) return min;
        return Math.min(value, max);
    }

    public static float clamp(float value, float min, float max){
        if (value < min) return min;
        return Math.min(value, max);
    }

    public static double clamp(double value, double min, double max){
        if (value < min) return min;
        return Math.min(value, max);
    }

    public static int clamp(int value, int min, int max){
        if (value < min) return min;
        return Math.min(value, max);
    }

    public static boolean within(long value, long min, long max){
        return !(value < min) && !(value > max);
    }

    public static boolean within(float value, float min, float max){
        return !(value < min) && !(value > max);
    }

    public static boolean within(double value, double min, double max){
        return !(value < min) && !(value > max);
    }

    public static boolean within(int value, int min, int max){
        return !(value < min) && !(value > max);
    }


    public static int mapInt(int value, int istart, int istop, int ostart, int ostop) {
        return (int) mapFloat(value, istart, istop, ostart, ostop);
    }

    public static float mapFloat(float value, float istart, float istop, float ostart, float ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }

    public static double mapDouble(double value, double istart, double istop, double ostart, double ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }


    public static double decrease(double x, double mult) {
        if (mult == 0D) {
            return x;
        }
        double mod = x % mult;
        return x - mult + mod;
    }

    public static double increase(double x, double mult) {
        if (mult == 0D) {
            return x;
        }
        double mod = x % mult;
        return x + mult - mod;
    }

    public static double floorTo(double v, double mult) {
        return mult == 0D ? v : v - (v % mult);
    }

    public static double ceilTo(double v, double mult) {
        if (mult == 0D) return v;
        double mod = v % mult;
        return mod == 0D ? v : v + mult - mod;
    }

    public static double roundToMultiple(double v, double mult) {
        if (mult == 0D) {
            return v;
        }
        double mod = v % mult;
        return mod >= mult/2D ? v + mult - mod : v - mod;
    }

    public static float roundToMultiple(float v, float mult) {
        if (mult == 0F) {
            return v;
        }
        float mod = v % mult;
        return mod >= mult/2F ? v + mult - mod : v - mod;
    }

    public static double roundToPrecision(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static int mod(int a, int b) {
        return (a % b + b) % b;
    }

    public static int distmod(int a, int b) {
        return Math.min((a % b + b) % b, ((-a) % b + b) % b);
    }

    public static double distance(int x1, int y1, int x2, int y2){
        return Math.sqrt(((x2 - x1)*(x2 - x1)) + ((y2 - y1)*(y2 - y1)));
    }

    public static double distance(float x1, float y1, float x2, float y2){
        return Math.sqrt(((x2 - x1)*(x2 - x1)) + ((y2 - y1)*(y2 - y1)));
    }

    public static double distance(double x1, double y1, double x2, double y2){
        return Math.sqrt(((x2 - x1)*(x2 - x1)) + ((y2 - y1)*(y2 - y1)));
    }

    public static byte[][] convertArray1Dto2D(int width, int height, byte[] src, byte[][] dst){
        int index = 0;
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                dst[x][y] = src[index];
                index ++;
            }
        }
        return dst;

    }

    public static byte[] convertArray2Dto1D(int width, int height, byte[][] src, byte[] dst){
        int index = 0;
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                dst[index] = src[x][y];
                index++;
            }
        }
        return dst;
    }

    public static int[][] convertArray1Dto2D(int width, int height, int[] src, int[][] dst){
        int index = 0;
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                dst[x][y] = src[index];
                index ++;
            }
        }
        return dst;

    }

    public static int[] convertArray2Dto1D(int width, int height, int[][] src, int[] dst){
        int index = 0;
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                dst[index] = src[x][y];
                index++;
            }
        }
        return dst;
    }

    public static float[][] convertArray1Dto2D(int width, int height, float[] src, float[][] dst){
        int index = 0;
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                dst[x][y] = src[index];
                index ++;
            }
        }
        return dst;

    }

    public static float[] convertArray2Dto1D(int width, int height, float[][] src, float[] dst){
        int index = 0;
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                dst[index] = src[x][y];
                index++;
            }
        }
        return dst;
    }


    public enum OS {
        WINDOWS("win"), LINUX("linux"), MAC("mac"), SOLARIS("solaris");

        String shortName;

        OS(String shortName) {
            this.shortName = shortName;
        }

        public boolean isMac(){
            return this == MAC;
        }

        public boolean isWindows(){
            return this == WINDOWS;
        }

        public boolean isLinux(){
            return this == LINUX;
        }

        public boolean isSolaris(){
            return this == SOLARIS;
        }

        public String getShortName(){
            return shortName;
        }
    }

    private static OS os = null;

    public static OS getOS() {
        if (os == null) {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                os = OS.WINDOWS;
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                os = OS.LINUX;
            } else if (osName.contains("mac")) {
                os = OS.MAC;
            } else if (osName.contains("sunos")) {
                os = OS.SOLARIS;
            }
        }
        return os;
    }
}
