package drawingbot.files.json.presets;

import drawingbot.JUnitDBV3ClassRunner;
import drawingbot.files.json.AbstractJsonLoader;
import drawingbot.files.json.JsonLoaderManager;
import drawingbot.javafx.GenericPreset;
import drawingbot.registry.MasterRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitDBV3ClassRunner.class)
public class GSONPresetTest {


    @Test
    public void testPresets() {
        for (AbstractJsonLoader<?> loader : MasterRegistry.INSTANCE.presetLoaders) {
            for (GenericPreset<?> preset : loader.getAllPresets()) {
                String original = JsonLoaderManager.createDefaultGson().toJson(preset);
                String fromJSON = JsonLoaderManager.createDefaultGson().toJson(JsonLoaderManager.createDefaultGson().fromJson(original, GenericPreset.class));
                Assert.assertEquals(original, fromJSON);
            }
        }
    }
}