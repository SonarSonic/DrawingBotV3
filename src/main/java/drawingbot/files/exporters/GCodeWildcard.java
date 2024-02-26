package drawingbot.files.exporters;

public abstract class GCodeWildcard {

    public final String wildcard;

    public GCodeWildcard(String wildcard){
        this.wildcard = wildcard;
    }

    public abstract String formatWildcard(GCodeBuilder builder, String string);

    @Override
    public String toString() {
        return wildcard;
    }
}
