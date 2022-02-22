package drawingbot.utils;

public interface INamedSetting {

    default String getDisplayName(){
        return toString();
    }

    default EnumReleaseState getReleaseState(){
        return EnumReleaseState.RELEASE;
    }

    default boolean isPremiumFeature(){
        return false;
    }

}
