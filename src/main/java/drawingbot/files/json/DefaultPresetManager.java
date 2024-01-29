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

public abstract class DefaultPresetManager<O extends PresetData, I> extends AbstractPresetManager<O> {

    private final List<GenericSetting<?, ?>> settings = new ArrayList<>();

    public List<PresetDataLoader<O>> presetDataLoaders = new ArrayList<>();

    public DefaultPresetManager(AbstractJsonLoader<O> presetLoader) {
        super(presetLoader);
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

    public final void registerPresetDataLoader(PresetDataLoader<O> loader){
        presetDataLoaders.add(loader);
    }

    public abstract I getInstance(DBTaskContext context);

    @Override
    public GenericPreset<O> updatePreset(DBTaskContext context, GenericPreset<O> preset, boolean loadingProject) {
        I instance = getInstance(context);
        if(instance != null){
            GenericSetting.updateSettingsFromInstance(settings, instance);
            preset.data.settings = GenericSetting.toJsonMap(settings, new HashMap<>(), false);

            Gson gson = JsonLoaderManager.createDefaultGson();
            for(PresetDataLoader<O> loader : presetDataLoaders){
                try {
                    loader.save(context, gson, preset);
                } catch (Exception exception) {
                    DrawingBotV3.logger.severe("Failed to save project data: " + loader.getKey());
                    exception.printStackTrace();
                }
            }
        }
        return preset;
    }

    @Override
    public void applyPreset(DBTaskContext context, GenericPreset<O> preset, boolean changesOnly, boolean loadingProject) {
        I instance = getInstance(context);
        if(instance != null) {
            GenericSetting.applySettings(preset.data.settings, settings);
            List<GenericSetting<?, ?>> toApply = settings;
            if (changesOnly) {
                toApply = GenericSetting.filterSettings(toApply, preset.data.settings.keySet());
            }
            GenericSetting.applySettingsToInstance(toApply, instance);

            Gson gson = JsonLoaderManager.createDefaultGson();
            for(PresetDataLoader<O> loader : presetDataLoaders){
                try {
                    loader.load(context, gson, preset);
                } catch (Exception exception) {
                    DrawingBotV3.logger.severe("Failed to load preset data: " + loader.getKey());
                    exception.printStackTrace();
                }
            }
        }
    }

    @Override
    public void addEditDialogElements(GenericPreset<O> preset, ObservableList<TreeNode> builder, List<Consumer<GenericPreset<O>>> callbacks) {
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
