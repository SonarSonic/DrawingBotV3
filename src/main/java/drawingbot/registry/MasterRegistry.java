package drawingbot.registry;

import drawingbot.DrawingBotV3;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.api.IPathFindingModule;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.files.presets.types.PresetImageFilters;
import drawingbot.files.presets.types.PresetPFMSettings;
import drawingbot.image.filters.ObservableImageFilter;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controls.DialogImageFilter;
import drawingbot.pfm.PFMSketch;
import drawingbot.utils.EnumFilterTypes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Dialog;

import java.awt.image.BufferedImageOp;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class MasterRegistry {

    public static MasterRegistry INSTANCE;

    //// PATH FINDING MODULES \\\\
    public HashMap<Class<? extends IPathFindingModule>, GenericFactory<IPathFindingModule>> pfmFactories = new LinkedHashMap<>();
    public HashMap<Class<? extends IPathFindingModule>, ObservableList<GenericSetting<?, ?>>> pfmSettings = new LinkedHashMap<>();
    public HashMap<String, ObservableList<GenericPreset<PresetPFMSettings>>> pfmPresets = new LinkedHashMap<>();

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// IMAGE FILTERS \\\\
    public Map<EnumFilterTypes, ObservableList<GenericFactory<BufferedImageOp>>> imgFilterFactories = FXCollections.observableMap(new LinkedHashMap<>());
    public HashMap<Class<? extends BufferedImageOp>, List<GenericSetting<?, ?>>> imgFilterSettings = new LinkedHashMap<>();
    public ObservableList<GenericPreset<PresetImageFilters>> imgFilterPresets = FXCollections.observableArrayList();

    public HashMap<Class<? extends BufferedImageOp>, Function<ObservableImageFilter, Dialog<ObservableImageFilter>>> imgFilterDialogs = new LinkedHashMap<>();


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //// DRAWING PENS / SETS \\\\

    public ObservableMap<String, ObservableList<DrawingPen>> registeredPens = FXCollections.observableMap(new LinkedHashMap<>());
    public ObservableMap<String, ObservableList<IDrawingSet<IDrawingPen>>> registeredSets  = FXCollections.observableMap(new LinkedHashMap<>());

    public static void init(){
        INSTANCE = new MasterRegistry();
        Register.registerPFMs();
        Register.registerDrawingTools();
        Register.registerImageFilters();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PATH FINDING MODULES: DEFAULTS

    public GenericFactory<IPathFindingModule> getDefaultPFM(){
        return pfmFactories.get(PFMSketch.class);
    }

    public GenericPreset<PresetPFMSettings> getDefaultPFMPreset(){
        return getDefaultPFMPreset(DrawingBotV3.INSTANCE.pfmFactory.get());
    }

    public GenericPreset<PresetPFMSettings> getDefaultPFMPreset(GenericFactory<IPathFindingModule> loader){
        return pfmPresets.get(loader.getName()).stream().filter(p -> p.presetName.equals("Default")).findFirst().get();
    }

    //// IMAGE FILTER: DEFAULTS

    public EnumFilterTypes getDefaultImageFilterType(){
        return imgFilterFactories.keySet().stream().findFirst().orElseGet(null);
    }

    public GenericFactory<BufferedImageOp> getDefaultImageFilter(EnumFilterTypes type){
        return imgFilterFactories.get(type).stream().findFirst().orElseGet(null);
    }

    public GenericPreset<PresetImageFilters> getDefaultImageFilterPreset(){
        return imgFilterPresets.stream().filter(p -> p.presetName.equals("Default")).findFirst().get();
    }

    //// DRAWING PEN: DEFAULTS

    public String getDefaultPenType(){
        return "Copic Original";
    }

    public DrawingPen getDefaultPen(String type){
        ObservableList<DrawingPen> pens = registeredPens.get(type);
        return pens == null ? null : pens.stream().findFirst().orElse(null);
    }

    //// DRAWING SET: DEFAULTS

    public String getDefaultSetType(){
        return "Copic";
    }

    public IDrawingSet<IDrawingPen> getDefaultSet(String type){
        ObservableList<IDrawingSet<IDrawingPen>> sets = registeredSets.get(type);
        return sets == null ? null : sets.stream().findFirst().orElse(null);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PATH FINDING MODULES: REGISTERING

    public void registerPFM(Class<? extends IPathFindingModule> pfmClass, String name, Supplier<IPathFindingModule> create, boolean isHidden){
        pfmFactories.put(pfmClass, new GenericFactory(pfmClass, name, create, isHidden));
        registerPFMPreset(JsonLoaderManager.PFM.createNewPreset(name, "Default", false));
    }

    public <C, V> void registerPFMSetting(GenericSetting<C, V> setting){
        for(GenericFactory<IPathFindingModule> loader : pfmFactories.values()){
            if(setting.isAssignableFrom(loader.getInstanceClass())){
                GenericSetting<C,V> copy = setting.copy();
                pfmSettings.putIfAbsent(loader.getInstanceClass(), FXCollections.observableArrayList());
                pfmSettings.get(loader.getInstanceClass()).add(copy);
            }
        }
    }

    public void registerPFMPreset(GenericPreset<PresetPFMSettings> preset){
        pfmPresets.putIfAbsent(preset.presetSubType, FXCollections.observableArrayList());
        pfmPresets.get(preset.presetSubType).add(preset);
    }


    //// IMAGE FILTERS: REGISTERING

    public <I extends BufferedImageOp> void registerImageFilter(EnumFilterTypes filterType, Class<I> filterClass, String name, Supplier<I> create, boolean isHidden){
        imgFilterFactories.putIfAbsent(filterType, FXCollections.observableArrayList());
        imgFilterFactories.get(filterType).add(new GenericFactory(filterClass, name, create, isHidden));
    }

    public void registerImageFilterSetting(GenericSetting<? extends BufferedImageOp, ?> setting){
        imgFilterSettings.putIfAbsent(setting.clazz, new ArrayList<>());
        imgFilterSettings.get(setting.clazz).add(setting);
    }

    public void registerImageFilterDialog(Class<BufferedImageOp> filterClass, Function<ObservableImageFilter, Dialog<ObservableImageFilter>> dialog){
        imgFilterSettings.putIfAbsent(filterClass, new ArrayList<>());
        imgFilterDialogs.put(filterClass, dialog);
    }

    public void registerImageFilterPreset(GenericPreset<PresetImageFilters> preset){
        imgFilterPresets.add(preset);
    }


    //// DRAWING PEN: REGISTERING

    public void registerDrawingPen(DrawingPen pen){
        if(pen == null){
            return;
        }
        if(registeredPens.get(pen.getName()) != null){
            DrawingBotV3.logger.warning("DUPLICATE PEN UNIQUE ID: " + pen.getName());
            return;
        }
        registeredPens.putIfAbsent(pen.getType(), FXCollections.observableArrayList());
        registeredPens.get(pen.getType()).add(pen);
    }

    public void unregisterDrawingPen(DrawingPen pen){
        ObservableList<DrawingPen> pens = registeredPens.get(pen.getType());
        pens.remove(pen);
        if(pens.isEmpty()){
            registeredPens.remove(pen.getType());
        }
    }

    //// DRAWING SET: REGISTERING

    public void registerDrawingSet(IDrawingSet<IDrawingPen> penSet){
        if(penSet == null){
            return;
        }
        if(registeredSets.get(penSet.getName()) != null){
            DrawingBotV3.logger.warning("DUPLICATE DRAWING SET NAME: " + penSet.getName());
            return;
        }
        registeredSets.putIfAbsent(penSet.getType(), FXCollections.observableArrayList());
        registeredSets.get(penSet.getType()).add(penSet);
    }

    public void unregisterDrawingSet(IDrawingSet<IDrawingPen> penSet){
        ObservableList<IDrawingSet<IDrawingPen>> sets = registeredSets.get(penSet.getType());
        sets.remove(penSet);
        if(sets.isEmpty()){
            registeredSets.remove(penSet.getType());
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PATH FINDING MODULES: GETTERS

    public ObservableList<GenericFactory<IPathFindingModule>> getObservablePFMLoaderList(){
        ObservableList<GenericFactory<IPathFindingModule>> list = FXCollections.observableArrayList();
        for(GenericFactory<IPathFindingModule> loader : pfmFactories.values()){
            if(!loader.isHidden() || ConfigFileHandler.getApplicationSettings().isDeveloperMode){
                list.add(loader);
            }
        }
        return list;
    }

    public ObservableList<GenericSetting<?, ?>> getObservablePFMSettingsList(){
        return getObservablePFMSettingsList(DrawingBotV3.INSTANCE.pfmFactory.get());
    }

    public ObservableList<GenericSetting<?, ?>> getObservablePFMSettingsList(GenericFactory<IPathFindingModule> loader){
        return pfmSettings.get(loader.getInstanceClass());
    }

    public ObservableList<GenericPreset<PresetPFMSettings>> getObservablePFMPresetList(){
        return getObservablePFMPresetList(DrawingBotV3.INSTANCE.pfmFactory.get());
    }

    public ObservableList<GenericPreset<PresetPFMSettings>> getObservablePFMPresetList(GenericFactory<IPathFindingModule> loader){
        return pfmPresets.get(loader.getName());
    }


    //// IMAGE FILTERS: GETTERS

    /**
     * @param name the image filters name
     * @return the image filters factory
     */
    public GenericFactory<BufferedImageOp> getImageFilterFactory(String name){
        for(ObservableList<GenericFactory<BufferedImageOp>> factories : imgFilterFactories.values()){
            for(GenericFactory<BufferedImageOp> factory : factories){
                if(factory.getName().equals(name)){
                    return factory;
                }
            }
        }
        return null;
    }

    /**
     * Creates an editable copy of settings available to the given filter factory
     * @param filterFactory the filter factory
     * @return an observable array list with copies of the img filters default settings
     */
    public ObservableList<GenericSetting<?,?>> createObservableImageFilterSettings(GenericFactory<BufferedImageOp> filterFactory){
        ObservableList<GenericSetting<?, ?>> settings = FXCollections.observableArrayList();
        for(Map.Entry<Class<? extends BufferedImageOp>, List<GenericSetting<?, ?>>> entry : imgFilterSettings.entrySet()){
            if(entry.getKey().isAssignableFrom(filterFactory.getInstanceClass())){
                for(GenericSetting<?, ?> setting : entry.getValue()){
                    settings.add(setting.copy());
                }
            }
        }
        return settings;
    }

    /**
     * Creates a new dialog for editing the image filter, if the filter has a custom type the custom dialog will be returned
     * @param filter the observable image filter
     * @return the pop-up dialog for editing the given image filter
     */
    public Dialog<ObservableImageFilter> getDialogForFilter(ObservableImageFilter filter){
        Function<ObservableImageFilter, Dialog<ObservableImageFilter>> func = MasterRegistry.INSTANCE.imgFilterDialogs.get(filter.filterFactory.getInstanceClass());
        return func == null ? new DialogImageFilter(filter) : func.apply(filter);
    }

    //// DRAWING PEN: HELPERS

    public DrawingPen getDrawingPenFromCodeName(String codeName){
        String[] split = codeName.split(":");
        return registeredPens.get(split[0]).stream().filter(p -> p.getCodeName().equals(codeName)).findFirst().orElse(null);
    }

    public List<DrawingPen> getDrawingPensFromCodes(String[] codes){
        List<DrawingPen> pens = new ArrayList<>();
        for(String code : codes){
            DrawingPen pen = getDrawingPenFromCodeName(code);
            if(pen != null){
                pens.add(pen);
            }else{
                DrawingBotV3.logger.warning("Couldn't find a pen with the code: " + code);
            }
        }
        return pens;
    }

    //// DRAWING SET: HELPERS

    public IDrawingSet<IDrawingPen> getDrawingSetFromCodeName(String codeName){
        String[] split = codeName.split(":");
        return registeredSets.get(split[0]).stream().filter(s -> s.getCodeName().equals(codeName)).findFirst().orElse(null);
    }

}
