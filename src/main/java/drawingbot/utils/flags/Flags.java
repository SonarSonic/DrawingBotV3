package drawingbot.utils.flags;

import java.util.*;

public class Flags {

    public final static Map<FlagCategory, List<Flag<?>>> ALL_FLAGS = new HashMap<>();

    public final static Flags.FlagCategory GLOBAL_CATEGORY = new Flags.FlagCategory("GLOBAL");
    public final static Flags.FlagCategory PROJECT_CATEGORY = new Flags.FlagCategory("PROJECT");
    public final static Flags.FlagCategory RENDER_CATEGORY = new Flags.FlagCategory("RENDER");
    public final static Flags.FlagCategory PFM_FACTORY_FLAGS = new Flags.FlagCategory("OPTIMISING");

    public final static Flags.BooleanFlag FORCE_REDRAW = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("FORCE_REDRAW", true, false));
    public final static Flags.BooleanFlag CURRENT_DRAWING_CHANGED = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("CURRENT_DRAWING_CHANGED", false, false));
    public final static Flags.BooleanFlag OPEN_IMAGE_UPDATED = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("OPEN_IMAGE_UPDATED", false, false));

    public final static Flags.BooleanFlag ACTIVE_TASK_CHANGED = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("ACTIVE_TASK_CHANGED",false, false));
    public final static Flags.BooleanFlag ACTIVE_TASK_CHANGED_STATE = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("ACTIVE_TASK_CHANGED_STATE", false, false));

    public final static Flags.BooleanFlag IMAGE_FILTERS_PARTIAL_UPDATE = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("IMAGE_FILTERS_PARTIAL_UPDATE", false, false));
    public final static Flags.BooleanFlag IMAGE_FILTERS_FULL_UPDATE = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("IMAGE_FILTERS_FULL_UPDATE", false, false));
    public final static Flags.BooleanFlag CANVAS_CHANGED = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("CANVAS_CHANGED", false, false));
    public final static Flags.BooleanFlag CLEAR_DRAWING_JFX = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("CLEAR_DRAWING_JFX", false, false));
    public final static Flags.BooleanFlag CLEAR_DRAWING_OPENGL = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("CLEAR_DRAWING_OPENGL", false, false));
    public final static Flags.BooleanFlag CANVAS_MOVED = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("CANVAS_SCROLLED", false, false));
    public final static Flags.BooleanFlag CHANGED_RENDERER = Flags.addBooleanFlag(RENDER_CATEGORY, new Flags.BooleanFlag("CHANGED_RENDERER", false, false));

    public final static Flags.BooleanFlag UPDATE_PEN_DISTRIBUTION = Flags.addBooleanFlag(GLOBAL_CATEGORY, new Flags.BooleanFlag("UPDATE_PEN_DISTRIBUTION", false, false));
    public final static Flags.BooleanFlag PFM_SETTINGS_UPDATE = Flags.addBooleanFlag(GLOBAL_CATEGORY, new Flags.BooleanFlag("PFM_SETTINGS_UPDATE", false, false));
    public final static Flags.BooleanFlag PFM_SETTINGS_USER_EDITED = Flags.addBooleanFlag(GLOBAL_CATEGORY, new Flags.BooleanFlag("PFM_SETTINGS_USER_EDITED", false, false));


    public final static Flags.BooleanFlag PFM_TRANSPARENT_CMYK = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_TRANSPARENT_CMYK", true, true));
    public final static Flags.BooleanFlag PFM_NEW_FEATURE = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_NEW_FEATURE", false, false));
    public final static Flags.BooleanFlag PFM_GENERATIVE = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_GENERATIVE", false, false));
    public final static Flags.BooleanFlag PFM_LAYERED = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_LAYERED", false, false));
    public final static Flags.BooleanFlag PFM_COMPOSITE = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_COMPOSITE", false, false));
    public final static Flags.BooleanFlag PFM_HAS_SAMPLED_ARGB = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_HAS_SAMPLED_ARGB", false, false));
    public final static Flags.BooleanFlag PFM_SUPPORTS_SOFT_CLIP = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_SUPPORTS_SOFT_CLIP", false, false));
    public final static Flags.BooleanFlag PFM_REQUIRES_PREMIUM = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_REQUIRES_PREMIUM", false, false));

    public final static Flags.BooleanFlag PFM_LINE_OPTIMISING = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_LINE_OPTIMISING", false, false));
    public final static Flags.BooleanFlag PFM_LINE_SIMPLIFY = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_LINE_SIMPLIFY", true, true));
    public final static Flags.BooleanFlag PFM_LINE_MERGING = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_LINE_MERGING", true, true));
    public final static Flags.BooleanFlag PFM_LINE_FILTERING = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_LINE_FILTERING", true, true));
    public final static Flags.BooleanFlag PFM_LINE_SORTING = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_LINE_SORTING", true, true));

    public final static Flags.BooleanFlag PFM_BYPASS_GEOMETRY_OPTIMISING = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_BYPASS_GEOMETRY_OPTIMISING", false, false));
    public final static Flags.BooleanFlag PFM_GEOMETRY_SIMPLIFY = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_GEOMETRY_MULTIPASS", true, true));
    public final static Flags.BooleanFlag PFM_GEOMETRY_MULTIPASS = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_GEOMETRY_MULTIPASS", true, true));
    public final static Flags.BooleanFlag PFM_GEOMETRY_SORTING = Flags.addBooleanFlag(PFM_FACTORY_FLAGS, new Flags.BooleanFlag("PFM_GEOMETRY_SORTING", true, true));

    public final static FlagStates DEFAULT_PFM_STATE = new FlagStates(PFM_FACTORY_FLAGS);

    public static <T> Flag<T> addFlag(FlagCategory category, Flag<T> flag){
        ALL_FLAGS.putIfAbsent(category, new ArrayList<>());
        ALL_FLAGS.get(category).add(flag);
        return flag;
    }

    public static BooleanFlag addBooleanFlag(FlagCategory category, BooleanFlag flag){
        return (BooleanFlag)addFlag(category, flag);
    }

    public static class FlagCategory {
        public String name;

        public FlagCategory(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof FlagCategory){
                return Objects.equals(((FlagCategory) obj).name, name);
            }
            return super.equals(obj);
        }
    }

    public static class Flag<T> {

        protected Class<T> type;
        protected String name;
        protected T resetValue;
        protected T clearValue;

        Flag(Class<T> type, String name, T resetValue, T clearValue){
            this.type = type;
            this.name = name;
            this.resetValue = resetValue;
            this.clearValue = clearValue;
        }
    }
    
    public static class BooleanFlag extends Flag<Boolean> {

        public BooleanFlag(String name, Boolean initValue, Boolean defaultValue) {
            super(Boolean.class, name, initValue, defaultValue);
        }
    }

}