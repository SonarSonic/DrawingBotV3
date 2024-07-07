package drawingbot.files.json;

import com.google.gson.Gson;
import drawingbot.DrawingBotV3;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.preferences.items.LabelNode;
import drawingbot.javafx.preferences.items.SettingNode;
import drawingbot.javafx.preferences.items.TreeNode;
import org.fxmisc.easybind.EasyBind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public final <C,V> GenericSetting<C, V> registerSetting(GenericSetting<C, V> setting){
        getSettings().add(setting);
        return setting;
    }

    public final void registerSettings(List<GenericSetting<?, ?>> setting){
        getSettings().addAll(setting);
    }

    public final void registerPresetDataLoader(PresetDataLoader<DATA> loader){
        presetDataLoaders.add(loader);
    }

    @Override
    public void updatePreset(DBTaskContext context, TARGET target, GenericPreset<DATA> preset, boolean changesOnly) {
        if(target == null){
            return;
        }
        GenericSetting.updateSettingsFromInstance(getSettings(), target);
        preset.data.settings = GenericSetting.toJsonMap(getSettings(), new HashMap<>(), changesOnly);

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
        GenericSetting.applySettings(preset.data.settings, getSettings());
        List<GenericSetting<?, ?>> toApply = getSettings();
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
    public IPresetEditor<TARGET, DATA> createPresetEditor() {
        return new DefaultPresetEditor<>(this){

            public List<GenericSetting<?, ?>> editingSettings;

            @Override
            public void init(TreeNode editorNode) {
                super.init(editorNode);
                editingSettings = new ArrayList<>();
                GenericSetting.copy(getSettings(), editingSettings);
                GenericSetting.addBindings(editingSettings);

                editorNode.getChildren().add(new LabelNode("Settings").setTitleStyling());
                EasyBind.subscribe(editingPresetProperty(), preset -> {
                    if(preset != null){
                        GenericSetting.applySettings(preset.data.settings, editingSettings);
                    }
                });

                for(GenericSetting<?, ?> setting : editingSettings){
                    editorNode.getChildren().add(new SettingNode<>(setting));
                }

            }

            @Override
            public void updatePreset() {
                super.updatePreset();
                getEditingPreset().data.settings = GenericSetting.toJsonMap(editingSettings, new HashMap<>(), false);
            }
        };
    }

}
