package drawingbot.javafx.controls;

import drawingbot.pfm.PFMFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class PFMSelectorControl extends Control {

    public SimpleObjectProperty<PFMFactory<?>> factory = new SimpleObjectProperty<>();

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PFMSelectorSkin(this);
    }
}
