package drawingbot.javafx.editors;

import drawingbot.javafx.GenericSetting;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.controlsfx.glyphfont.Glyph;

/**
 * A {@link ElementNode} which wraps around a DrawingBotV3 {@link GenericSetting}
 * It will take the settings names and editors which edit the values live (e.g. sliders) will notify the setting when the value is changing with {@link GenericSetting#setValueChanging(boolean)}
 */
public class SettingNode extends ElementNode {

    public GenericSetting<?, ?> setting;

    public SettingNode(GenericSetting<?, ?> setting, TreeNode... children) {
        super(setting.getDisplayName(), children);
        this.setting = setting;
        this.nameProperty().bind(setting.displayNameProperty());
    }

    public SettingNode(String overrideName, GenericSetting<?, ?> setting, TreeNode... children) {
        super(overrideName, children);
        this.setting = setting;
    }

    public Node createEditor() {
        return Editors.createNodeEditor(setting);
    }

    public void addElement(PageBuilder builder) {
        Label label = new Label();
        label.textProperty().bind(nameProperty());
        label.getStyleClass().add(labelStyle);

        Node editor = createEditor();
        editor.getStyleClass().add(labelStyle);


        Button resetButton = new Button("", new Glyph("FontAwesome", "ROTATE_LEFT"));
        resetButton.getStyleClass().add("preference-reset-button");
        resetButton.setOnAction(e -> setting.resetSetting());

        builder.addRow(label, editor, resetButton);

        if (disabled != null) {
            label.disableProperty().bind(Bindings.createBooleanBinding(() -> disabled.getValue() || setting.isDisabled(), disabled, setting.disabledProperty()));
            editor.disableProperty().bind(Bindings.createBooleanBinding(() -> disabled.getValue() || setting.isDisabled(), disabled, setting.disabledProperty()));
            resetButton.disableProperty().bind(Bindings.createBooleanBinding(() -> disabled.getValue() || setting.isDisabled(), disabled, setting.disabledProperty()));
        } else {
            label.disableProperty().bind(setting.disabledProperty());
            editor.disableProperty().bind(setting.disabledProperty());
            resetButton.disableProperty().bind(resetButton.disabledProperty());
        }
    }

}
