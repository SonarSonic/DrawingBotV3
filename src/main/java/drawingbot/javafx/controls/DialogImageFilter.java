package drawingbot.javafx.controls;

import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.preferences.items.EditorSheet;
import drawingbot.javafx.preferences.items.LabelNode;
import drawingbot.javafx.preferences.items.PropertyNode;
import drawingbot.javafx.preferences.items.SettingNode;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;

public class DialogImageFilter extends DialogScrollPane {

    public DialogImageFilter(ObservableImageFilter imageFilter){
        super(imageFilter.name.get(), EditorSheet.page("settings", builder -> {

            builder.add(new PropertyNode<>("Enabled", imageFilter.enable, imageFilter.enable.get(), Boolean.class).setTitleStyling());

            builder.add(new LabelNode("").setTitleStyling());

            BooleanBinding disabled = imageFilter.enable.not();
            for(GenericSetting<?, ?> filter : imageFilter.filterSettings){
                builder.add(new SettingNode(filter).setDisabledProperty(disabled));
            }

        }).getContent());
        getDialogPane().getButtonTypes().add(FXHelper.buttonResetToDefault);
        getDialogPane().lookupButton(FXHelper.buttonResetToDefault).addEventFilter(ActionEvent.ACTION, event -> {
            imageFilter.filterSettings.forEach(GenericSetting::resetSetting);
            event.consume();
        });
    }
}
