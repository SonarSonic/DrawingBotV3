package drawingbot.files.exporters;

public class HPGLDictionary {

    //// VECTOR GROUP \\\\
    public static String PLOT_ABSOLUTE = "PA"; // x, y (int) - array of moves
    public static String PLOT_RELATIVE = "PR"; // x, y (int) - array of moves
    public static String PEN_DOWN = "PD"; // x, y (int) - array of moves
    public static String PEN_UP = "PU"; // x, y (int) - array of moves

    //// CHARACTER GROUP \\\\
    public static String DESIGNATE_ALT_CHARACTER_SET = "CA"; // n (int)
    public static String CHARACTER_PLOT = "CP"; // spaces, lines (double)
    public static String DESIGNATE_STANDARD_CHARACTER_SET = "CS"; // m (int)
    public static String ABSOLUTE_DIRECTION = "DI"; // run, rise (double)
    public static String RELATIVE_DIRECTION = "DR"; // run, rise (double)
    public static String LABEL = "LB"; // none
    public static String SELECT_ALT_CHARACTER_SET = "SA"; // none
    public static String ABSOLUTE_CHARACTER_SIZE = "SI"; // wide, high (double)
    public static String ABSOLUTE_CHARACTER_SLANT = "SL"; // tan theta (double)
    public static String RELATIVE_CHARACTER_SIZE = "SR"; // wide, high (double)
    public static String SELECT_STANDARD_CHARACTER_SET = "SS"; // none
    public static String USER_DEFINED_CHARACTER = "UC"; // x, y, pen (int)

    //// LINE TYPE GROUP \\\\
    public static String DESIGNATE_LINE_TYPE = "LT"; // type, length (double)
    public static String SYMBOL_MODE = "SM"; // none
    public static String SELECT_PEN = "SP"; // none
    public static String ADAPTIVE_VELOCITY = "VA"; // type, length (double)
    public static String NORMAL_VELOCITY = "VN"; // type, length (double)
    public static String SELECT_VELOCITY_FOR_PEN = "VS"; // velocity, n (double)

    //// DIGITIZE GROUP \\\\
    public static String DIGITIZE_CLEAR = "DC"; // none
    public static String DIGITIZE_POINT = "DP"; // none
    public static String OUTPUT_CURRENT_POSITION_AND_PEN_STATUS = "OC"; // none
    public static String OUTPUT_DIGITIZED_POINT_AND_PEN_STATUS = "OD"; // none

    //// AXES \\\\
    public static String TICK_LENGTH = "DC"; // tp, tn (double)
    public static String X_AXIS_TICK = "XT"; // none
    public static String Y_AXIS_TICK = "YT"; // none

    //// SET-UP GROUP \\\\
    public static String INPUT_POINTS = "IP"; // p1x, p1y, p2x, p2y (int)
    public static String INPUT_WINDOW = "IW"; // xlo, ylo, xhi, yhi (int)
    public static String OUTPUT_POINTS = "OP"; // none

    //// CONFIGURATION STATUS \\\\
    public static String AUTOMATIC_PEN_PICKUP = "AP"; //none
    public static String DEFAULT_VALUES = "DF"; // none
    public static String INPUT_MASKS = "IM"; // e{,s{,p}}
    public static String INITIALIZE = "IN"; // none
    public static String OUTPUT_ERROR = "OE"; // none
    public static String OUTPUT_STATUS = "OS"; // none



    ///// ERROR NUMBERS MEANING
    public static String ERROR_0 = "No error";
    public static String ERROR_1 = "Unrecognised instruction (or not recognized in current state)";
    public static String ERROR_2 = "Wrong number of parameters";
    public static String ERROR_3 = "Out-of-range or invalid parameter";
    public static String ERROR_4 = "None";
    public static String ERROR_5 = "None";
    public static String ERROR_6 = "Position overflow";
    public static String ERROR_7 = "Buffer overflow, or out of memory";

    public static String[] ERRORS = new String[]{ERROR_0, ERROR_1, ERROR_2, ERROR_3, ERROR_4, ERROR_5, ERROR_6, ERROR_7};


}
