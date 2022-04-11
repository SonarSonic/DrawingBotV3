package drawingbot.files.json.presets;

import com.google.gson.JsonObject;
import drawingbot.DrawingBotV3;
import drawingbot.api.Hooks;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.ExportTask;
import drawingbot.files.FileUtils;
import drawingbot.files.json.AbstractPresetLoader;
import drawingbot.files.json.AbstractPresetManager;
import drawingbot.files.json.PresetType;
import drawingbot.image.BufferedImageLoader;
import drawingbot.javafx.FXHelper;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.observables.ObservableDrawingSet;
import drawingbot.javafx.observables.ObservableProjectSettings;
import drawingbot.plotting.PFMTask;
import drawingbot.registry.Register;
import drawingbot.utils.UnitsLength;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class PresetProjectSettingsLoader extends AbstractPresetLoader<PresetProjectSettings> {

    public PresetProjectSettingsLoader(PresetType presetType) {
        super(PresetProjectSettings.class, presetType, "projects.json");
        setDefaultManager(new PresetProjectSettingsManager(this));
    }

    @Override
    public PresetProjectSettings getPresetInstance(GenericPreset<PresetProjectSettings> preset) {
        return new PresetProjectSettings();
    }

    @Override
    public void registerPreset(GenericPreset<PresetProjectSettings> preset) {
        //MasterRegistry.INSTANCE.registerPFMPreset(preset);
    }

    @Override
    public void unregisterPreset(GenericPreset<PresetProjectSettings> preset) {
        //MasterRegistry.INSTANCE.pfmPresets.get(preset.presetSubType).remove(preset);
    }

    @Override
    public GenericPreset<PresetProjectSettings> getDefaultPreset() {
        return null;
    }

    @Override
    public List<GenericPreset<?>> getUserCreatedPresets() {
        /*

        List<GenericPreset<?>> userCreated = new ArrayList<>();
        for (Map.Entry<String, ObservableList<GenericPreset<PresetProjectSettings>>> entry : MasterRegistry.INSTANCE.pfmPresets.entrySet()) {
            for (GenericPreset<PresetProjectSettings> preset : entry.getValue()) {
                if (preset.userCreated) {
                    userCreated.add(preset);
                }
            }
        }
        return userCreated;
         */
        return new ArrayList<>();
    }

    @Override
    public Collection<GenericPreset<PresetProjectSettings>> getAllPresets() {
        return new ArrayList<>();
    }
}
