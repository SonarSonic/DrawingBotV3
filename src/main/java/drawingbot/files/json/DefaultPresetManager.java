package drawingbot.files.json;

import com.google.gson.Gson;
import drawingbot.DrawingBotV3;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.editors.TreeNode;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public abstract class DefaultPresetManager<TARGET, DATA extends PresetData> extends AbstractPresetManager<TARGET, DATA> {

    private final List<GenericSetting<?, ?>> settings = new ArrayList<>();

    public List<PresetDataLoader<DATA>> presetDataLoaders = new ArrayList<>();

    public DefaultPresetManager(IPresetLoader<DATA> presetLoader, Class<TARGET> targetType) {
        super(presetLoader, targetType);
        registerDataLoaders();
    }

    public void registerDataLoaders(){}

    public List<GenericSetting<?, ?>> getSettings() {
        return settings;
    }

    public final void registerSetting(GenericSetting<?, ?>setting){
        settings.add(setting);
    }

    public final void registerSettings(List<GenericSetting<?, ?>> setting){
        settings.addAll(setting);
    }

    public final void registerPresetDataLoader(PresetDataLoader<DATA> loader){
        presetDataLoaders.add(loader);
    }

    @Override
    public void updatePreset(DBTaskContext context, TARGET target, GenericPreset<DATA> preset) {
        if(target == null){
            return;
        }
        GenericSetting.updateSettingsFromInstance(settings, target);
        preset.data.settings = GenericSetting.toJsonMap(settings, new HashMap<>(), false);

        Gson gson = JsonLoaderManager.createDefaultGson();
        for(PresetDataLoader<DATA> loader : presetDataLoaders){
            try {
                loader.save(context, gson, preset);
            } catch (Exception exception) {
                DrawingBotV3.logger.severe("Failed to save project data: " + loader.getKey());
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void applyPreset(DBTaskContext context, TARGET target, GenericPreset<DATA> preset, boolean changesOnly) {
        if(target == null){
            return;
        }
        GenericSetting.applySettings(preset.data.settings, settings);
        List<GenericSetting<?, ?>> toApply = settings;
        if (changesOnly) {
            toApply = GenericSetting.filterSettings(toApply, preset.data.settings.keySet());
        }
        GenericSetting.applySettingsToInstance(toApply, target);

        Gson gson = JsonLoaderManager.createDefaultGson();
        for(PresetDataLoader<DATA> loader : presetDataLoaders){
            try {
                loader.load(context, gson, preset);
            } catch (Exception exception) {
                DrawingBotV3.logger.severe("Failed to load preset data: " + loader.getKey());
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void addEditDialogElements(GenericPreset<DATA> preset, ObservableList<TreeNode> builder, List<Consumer<GenericPreset<DATA>>> callbacks) {
        super.addEditDialogElements(preset, builder, callbacks);
        /* TODO - Display specific settings in the preset dialog
        builder.add(new LabelNode("Settings").setTitleStyling());
        GenericSetting.applySettings(preset.data.settings, getSettings());
        for(GenericSetting<?, ?> setting : getSettings()){
            builder.add(new SettingNode(setting));
        }
        callbacks.add(save -> {
            save.data.settings = GenericSetting.toJsonMap(getSettings(), new HashMap<>(), false);
        });

         */
    }
}
