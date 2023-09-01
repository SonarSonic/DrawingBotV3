package drawingbot.javafx.editors;

import drawingbot.javafx.GenericSetting;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.controlsfx.glyphfont.Glyph;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ElementNode} which wraps around a DrawingBotV3 {@link GenericSetting}
 * It will take the settings names and editors which edit the values live (e.g. sliders) will notify the setting when the value is changing with {@link GenericSetting#setValueChanging(boolean)}
 */
public class SettingsGroupNode extends ElementNode {

    public List<GenericSetting<?, ?>> settings = new ArrayList<>();

    public SettingsGroupNode(String overrideName, List<GenericSetting<?, ?>> settings, TreeNode... children) {
        super(overrideName, children);
        this.settings.addAll(settings);
    }

    public Node createEditor() {
        return Editors.createNodeEditorGroupSetting(settings);
    }

    public void addElement(PageBuilder builder) {
        Label label = new Label();
        label.textProperty().bind(nameProperty());
        label.getStyleClass().add(labelStyle);

        Node editor = createEditor();
        editor.getStyleClass().add(labelStyle);


        Button resetButton = new Button("", new Glyph("FontAwesome", "ROTATE_LEFT"));
        resetButton.getStyleClass().add("preference-reset-button");
        resetButton.setOnAction(e -> settings.forEach(GenericSetting::resetSetting));

        builder.addRow(label, editor, resetButton);

        if(settings.isEmpty()){
            return;
        }

        BooleanProperty tempDisabledProp;
        if(settings.size() == 1){
            tempDisabledProp = settings.get(0).disabledProperty();
        }else{
            //Handle if the setting is disabled if we are viewing multiple settings
            tempDisabledProp  = new SimpleBooleanProperty(false);
            List<BooleanProperty> dependencies = new ArrayList<>();
            settings.forEach(setting -> dependencies.add(setting.disabledProperty()));
            tempDisabledProp.bind(Bindings.createBooleanBinding(() -> {
                for(BooleanProperty observable : dependencies){
                    if(observable.get()){
                        return true;
                    }
                }
                return false;
            }, dependencies.toArray(new Observable[1])));
        }

        BooleanProperty finalDisabledProp = tempDisabledProp;

        if (disabled != null) {
            label.disableProperty().bind(Bindings.createBooleanBinding(() -> disabled.getValue() || finalDisabledProp.get(), disabled, finalDisabledProp));
            editor.disableProperty().bind(Bindings.createBooleanBinding(() -> disabled.getValue() || finalDisabledProp.get(), disabled, finalDisabledProp));
            resetButton.disableProperty().bind(Bindings.createBooleanBinding(() -> disabled.getValue() || finalDisabledProp.get(), disabled, finalDisabledProp));
        } else {
            label.disableProperty().bind(finalDisabledProp);
            editor.disableProperty().bind(finalDisabledProp);
            resetButton.disableProperty().bind(resetButton.disabledProperty());
        }
    }

}
