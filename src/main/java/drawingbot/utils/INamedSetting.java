package drawingbot.utils;

import javafx.scene.Node;

public interface INamedSetting {

    default String getDisplayName(){
        return toString();
    }

    default Node getDisplayNode(){
        return null;
    }

    default EnumReleaseState getReleaseState(){
        return EnumReleaseState.RELEASE;
    }

    default boolean isPremiumFeature(){
        return false;
    }

    default boolean isNewFeature(){
        return false;
    }

}
