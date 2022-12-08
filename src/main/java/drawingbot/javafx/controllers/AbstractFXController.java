package drawingbot.javafx.controllers;

import javafx.css.Styleable;

import java.util.List;

public abstract class AbstractFXController {

    /**
     * @return All JavaFX Nodes / Styleables of this controller which should be "persistent"
     * Meaning visual settings used by JavaFX should be maintained on reload, e.g. visible properties, collapsed properties.
     * This is not for nodes which contain project specific values that should be maintained, they should be registered as settings elsewhere
     */
    public List<Styleable> getPersistentNodes(){
        return List.of();
    }

}
