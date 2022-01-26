package drawingbot.javafx.observables;

import com.google.gson.JsonElement;
import drawingbot.api.IDrawingStyle;
import drawingbot.javafx.GenericSetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ObservableDrawingStyle implements IDrawingStyle {

    public SimpleBooleanProperty enable;
    public SimpleStringProperty name;
    public SimpleObjectProperty<PFMFactory<?>> pfmFactory;
    public SimpleIntegerProperty distributionWeight;
    public SimpleObjectProperty<Color> maskColor; //nullable

    public ObservableList<GenericSetting<?, ?>> pfmSettings;

    public ObservableDrawingStyle(PFMFactory<?> factory){
        init();
        update(true, factory.getName(), factory, 100, null, Register.PRESET_LOADER_PFM.getDefaultPresetForSubType(factory.getName()).data.settingList);
    }

    public ObservableDrawingStyle(IDrawingStyle style){
        init();
        update(style.isEnabled(), style.getName(), MasterRegistry.INSTANCE.getPFMFactory(style.getPFMName()), style.getDistributionWeight(), style.getMaskColor(), style.getSaveableSettings());
    }

    private void init(){
        this.enable = new SimpleBooleanProperty(true);
        this.name = new SimpleStringProperty();
        this.pfmFactory = new SimpleObjectProperty<>();
        this.distributionWeight = new SimpleIntegerProperty();
        this.maskColor = new SimpleObjectProperty<>();
        this.pfmSettings = FXCollections.observableArrayList();
    }

    public void update(boolean enabled, String name, PFMFactory<?> pfm, int weight, Color maskColor, HashMap<String, JsonElement> settings){
        this.enable.set(enabled);
        this.name.set(name);
        this.pfmFactory.set(pfm);
        this.distributionWeight.set(weight);
        this.maskColor.set(maskColor);
        this.pfmSettings.clear();
        this.pfmSettings.addAll(MasterRegistry.INSTANCE.getNewObservableSettingsList(pfm));
        GenericSetting.applySettings(settings, pfmSettings);
    }

    public void update(@Nullable IDrawingStyle style){
        if(style == null){
            PFMFactory<?> factory = MasterRegistry.INSTANCE.getDefaultPFM();
            update(true, factory.getName(), factory, 100, null, new HashMap<>());
            return;
        }
        update(style.isEnabled(), style.getName(), MasterRegistry.INSTANCE.getPFMFactory(style.getPFMName()), style.getDistributionWeight(), style.getMaskColor(), style.getSaveableSettings());
    }

    @Override
    public boolean isEnabled() {
        return enable.getValue();
    }

    @Override
    public String getName() {
        return name.getValue();
    }

    @Override
    public String getPFMName() {
        return pfmFactory.get().getName();
    }

    @Override
    public PFMFactory<?> getFactory() {
        return pfmFactory.get();
    }

    @Override
    public HashMap<String, JsonElement> getSaveableSettings() {
        return GenericSetting.toJsonMap(pfmSettings, new HashMap<>(), false);
    }

    @Override
    public int getDistributionWeight() {
        return distributionWeight.getValue();
    }

    @Override
    public Color getMaskColor() {
        return maskColor.get();
    }
}