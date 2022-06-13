package drawingbot.geom.snapping;

import javafx.beans.property.SimpleBooleanProperty;

public abstract class AbstractSnappingGuide implements ISnappingGuide {

    public SimpleBooleanProperty enabled = new SimpleBooleanProperty(true);

    protected boolean shouldSnapValue(double value, double guide, double radius, double scale) {
        return value >= guide - radius*scale && value <= guide + radius*scale;
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public SimpleBooleanProperty enabledProperty() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
}
