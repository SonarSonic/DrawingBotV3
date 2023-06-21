package drawingbot.javafx.controls;

import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.Editors;
import drawingbot.javafx.editors.LabelNode;
import drawingbot.javafx.editors.PropertyNode;
import drawingbot.javafx.editors.SettingNode;
import drawingbot.javafx.observables.ObservableImageFilter;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;

public class DialogImageFilter extends DialogScrollPane {

    public DialogImageFilter(ObservableImageFilter imageFilter){
        super(imageFilter.name.get(), Editors.page("settings", builder -> {

            builder.add(new PropertyNode("Enabled", imageFilter.enable, Boolean.class).setTitleStyling());

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
