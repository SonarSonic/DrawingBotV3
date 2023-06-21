package drawingbot.javafx.editors;

import drawingbot.javafx.GenericSetting;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class SettingUnitsNode extends SettingNode {

    public GenericSetting<?, ?> units;

    public SettingUnitsNode(GenericSetting<?, ?> setting, GenericSetting<?, ?> units, TreeNode... children) {
        super(setting, children);
        this.units = units;
    }

    public SettingUnitsNode(String overrideName, GenericSetting<?, ?> setting, GenericSetting<?, ?> units, TreeNode... children) {
        super(overrideName, setting, children);
        this.units = units;
    }

    public Node createEditor() {
        HBox hBox = new HBox();

        Node settingEditor = Editors.createEditor(setting.valueProperty(), setting.type);
        HBox.setHgrow(settingEditor, Priority.ALWAYS);
        hBox.getChildren().add(settingEditor);

        Node unitsEditor = Editors.createEditor(units.valueProperty(), units.type);
        HBox.setHgrow(settingEditor, Priority.SOMETIMES);
        hBox.getChildren().add(unitsEditor);

        return hBox;
    }

}
