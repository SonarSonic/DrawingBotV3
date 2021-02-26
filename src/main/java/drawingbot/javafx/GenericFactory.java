package drawingbot.javafx;

import java.util.function.Supplier;

/**used in menus to reference objects which the factory will create when selected*/
public class GenericFactory<C> {

    private final Class<C> clazz; //the class this factory will provide an instance of
    private final Supplier<C> create; //the method to create an instance of the class
    private final String name; //the name this factory will have in menus
    private final boolean isHidden; //true if this Factory should only be usable in developer mode.

    public GenericFactory(Class<C> clazz, String name, Supplier<C> create, boolean isHidden) {
        this.clazz = clazz;
        this.create = create;
        this.name = name;
        this.isHidden = isHidden;
    }

    public Class<C> getInstanceClass(){
        return clazz;
    }

    public C instance(){
        return create.get();
    }

    public String getName(){
        return name;
    }

    public boolean isHidden() {
        return isHidden;
    }

    @Override
    public String toString() {
        return getName();
    }

}