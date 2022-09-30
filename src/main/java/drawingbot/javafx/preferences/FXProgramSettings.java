package drawingbot.javafx.preferences;

import com.dlsc.formsfx.model.validators.CustomValidator;
import com.dlsc.formsfx.model.validators.DoubleRangeValidator;
import com.dlsc.formsfx.model.validators.Validator;
import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import drawingbot.DrawingBotV3;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.settings.*;
import drawingbot.render.overlays.DrawingBorderOverlays;
import drawingbot.render.overlays.RulerOverlays;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class FXProgramSettings {

    public static PreferencesFx preferencesFx;

    public static void init(){


        ComboBox<String> comboBoxLayerNamingPattern = new ComboBox<>();
        comboBoxLayerNamingPattern.setEditable(true);
        comboBoxLayerNamingPattern.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.getProgramSettings().svgLayerNaming.valueProperty());
        comboBoxLayerNamingPattern.getItems().addAll("%NAME%", "%INDEX% - %NAME%", "Pen%INDEX%");

        ProgramSettings settings = DrawingBotV3.INSTANCE.settings.get();
        Category exportSettings = Category.of("Export Settings").subCategories(
            Category.of("Path Optimisation",
                Group.of(
                        convertSetting("Enable Path Optimisation", settings.pathOptimisationEnabled)
                ),
                Group.of("Line Simplifying",
                    Setting.of(new Label("Simplifies lines using the Douglas Peucker Algorithm")),
                        convertSetting("Enabled", settings.lineSimplifyEnabled),
                        convertSetting("Tolerance but this line is now too long", settings.lineSimplifyTolerance),
                        convertSetting("Units", settings.lineSimplifyUnits)
                ),
                Group.of("Line Merging",
                    Setting.of(new Label("Merges start/end points within the given tolerance")),
                        convertSetting("Enabled", settings.lineMergingEnabled),
                        convertSetting("Tolerance", settings.lineMergingTolerance),
                        convertSetting("Units", settings.lineMergingUnits)
                ),
                Group.of("Line Filtering",
                    Setting.of(new Label("Remove lines shorter than the tolerance")),
                        convertSetting("Enabled", settings.lineFilteringEnabled),
                        convertSetting("Tolerance", settings.lineFilteringTolerance),
                        convertSetting("Units", settings.lineFilteringUnits)
                ),
                Group.of("Line Sorting",
                    Setting.of(new Label("Sorts lines to minimise air time")),
                        convertSetting("Enabled", settings.lineSortingEnabled),
                        convertSetting("Tolerance", settings.lineSortingTolerance),
                        convertSetting("Units", settings.lineSortingUnits)
                ),
                Group.of("Line Multipass",
                    Setting.of(new Label("Draws over each geometry multiple times")),
                        convertSetting("Enabled", settings.multipassEnabled),
                        convertSetting("Count", settings.multipassCount)
                )
            ),
            Category.of("SVG",
                    Group.of("Inkscape SVG",
                        Setting.of(new Label("Layer Naming (with wildcards %INDEX% and %NAME%)")),
                        Setting.of(comboBoxLayerNamingPattern),
                            convertSetting("Export Background Layer", settings.exportSVGBackground)
                    )
            ),
            Category.of("HPGL"),
            Category.of("Image & Animation")
        );
        Category uiSettings = Category.of("User Interface",
            Group.of("Rulers",
                Setting.of("Enabled", RulerOverlays.INSTANCE.activeProperty())
            ),
            Group.of("Drawing Borders",
                Setting.of("Enabled", DrawingBorderOverlays.INSTANCE.activeProperty()),
                Setting.of("Colour", DrawingBorderOverlays.borderColour)
            ),
            Group.of("Notifications",
                convertSetting("Enabled", settings.notificationsEnabled),
                convertSetting("Screen Time", settings.notificationsScreenTime),
                Setting.of(new Label("Set this value to 0 if you don't want notifications to disappear"))
            )
        );
        Category plugins = Category.of("Plugins").subCategories(
            Category.of("Ruler Overlays")
        );
        Category advancedSettings = Category.of("Advanced").subCategories(
            Category.of("Viewport Texture Size Limit")
        );

        preferencesFx = PreferencesFx.of(FXProgramSettings.class, exportSettings, uiSettings, plugins, advancedSettings).buttonsVisibility(false).debugHistoryMode(false).saveSettings(false);

    }

    public static Setting<?, ?> convertSetting(GenericSetting<?, ?> setting){
        return convertSetting(setting.getDisplayName(), setting);
    }

    public static Setting<?, ?> convertSetting(String displayName, GenericSetting<?, ?> setting){
        if(setting instanceof BooleanSetting){
            BooleanSetting<?> booleanSetting = (BooleanSetting<?>) setting;
            return Setting.of(displayName, booleanSetting.asBooleanProperty());
        }
        if(setting instanceof ColourSetting){
            ColourSetting<?> colourSetting = (ColourSetting<?>) setting;
            return Setting.of(displayName, colourSetting.valueProperty());
        }
        if(setting instanceof DoubleSetting){
            DoubleSetting<?> doubleSetting = (DoubleSetting<?>) setting;
            if(doubleSetting.isRanged){
                return Setting.of(displayName, doubleSetting.asDoubleProperty());
            }else{
                return Setting.of(displayName, doubleSetting.asDoubleProperty());
            }
        }
        if(setting instanceof FloatSetting){
            FloatSetting<?> floatSetting = (FloatSetting<?>) setting;
            if(floatSetting.isRanged){
                return Setting.of(displayName, floatSetting.asDoubleProperty(), floatSetting.minValue, floatSetting.maxValue, floatSetting.precision, null);
            }else{
                return Setting.of(displayName, floatSetting.asDoubleProperty());
            }
        }
        if(setting instanceof IntegerSetting){
            IntegerSetting<?> integerSetting = (IntegerSetting<?>) setting;
            if(integerSetting.isRanged){
                return Setting.of(displayName, integerSetting.asIntegerProperty(), integerSetting.minValue, integerSetting.maxValue, null);
            }else{
                return Setting.of(displayName, integerSetting.asIntegerProperty());
            }
        }
        if(setting instanceof LongSetting){
            LongSetting<?> longSetting = (LongSetting<?>) setting;
            if(longSetting.isRanged){
                return Setting.of(displayName, longSetting.asDoubleProperty(), longSetting.minValue, longSetting.maxValue, 0, null);
            }else{
                return Setting.of(displayName, longSetting.asDoubleProperty());
            }
        }
        if(setting instanceof OptionSetting){
            OptionSetting<?, ?> optionSetting = (OptionSetting<?, ?>) setting;
            return convertOptionSetting(displayName, optionSetting);
        }
        if(setting instanceof StringSetting){
            StringSetting<?> stringSetting = (StringSetting<?> ) setting;
            return Setting.of(displayName, stringSetting.asStringProperty());
        }
        throw new UnsupportedOperationException("Invalid Setting Type: " + setting.getClass().getSimpleName());
    }

    public static <V> Setting<?, ?> convertOptionSetting(OptionSetting<?, V> setting){
        return convertOptionSetting(setting.getDisplayName(), setting);
    }

    public static <V> Setting<?, ?> convertOptionSetting(String displayName, OptionSetting<?, V> setting){
        return Setting.of(displayName, FXCollections.observableArrayList(setting.values), setting.valueProperty());
    }

}
