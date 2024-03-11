package drawingbot.registry;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.IDrawingPen;
import drawingbot.api.IDrawingSet;
import drawingbot.api.IPFM;
import drawingbot.drawing.ColorSeparationHandler;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.json.*;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.files.loaders.AbstractFileLoader;
import drawingbot.files.loaders.IFileLoaderFactory;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.shapes.JFXGeometryConverter;
import drawingbot.image.kernels.IKernelFactory;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.preferences.items.EditorSheet;
import drawingbot.javafx.preferences.items.TreeNode;
import drawingbot.javafx.settings.CategorySetting;
import drawingbot.pfm.PFMFactory;
import drawingbot.render.modes.DisplayModeBase;
import drawingbot.utils.EnumFilterTypes;
import drawingbot.utils.Metadata;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.fxmisc.easybind.EasyBind;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImageOp;
import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MasterRegistry {

    public static MasterRegistry INSTANCE = new MasterRegistry();

    //// PRESETS \\\\

    @Nullable
    public String getDefaultPresetName(PresetType presetType){
        return DBPreferences.INSTANCE.getDefaultPreset(presetType);
    }

    @Nullable
    public <DATA> GenericPreset<DATA> getDefaultPreset(IPresetLoader<DATA> loader){
        String name = getDefaultPresetName(loader.getPresetType());
        if(name != null && !name.isEmpty()){

            //Attempt 1: Try to find the preset from code name, which is more accurate and likely to match the correct prest
            if(name.contains(":")){
                GenericPreset<DATA> preset = loader.findPresetFromID(name);
                if(preset != null){
                    return preset;
                }
            }

            //Attempt 2: Try the find the preset from just it's name, for legacy support
            return loader.findPreset(name);
        }
        return null;
    }

    @Nullable
    public <O> GenericPreset<O> getDefaultPreset(IPresetLoader<O> jsonLoader, String fallbackName){
        return getDefaultPresetWithFallback(jsonLoader, "", fallbackName, true);
    }

    @Nullable
    public <DATA> GenericPreset<DATA> getDefaultPresetWithFallback(IPresetLoader<DATA> loader, String fallbackSubType, String fallbackName, boolean orFirst){
        //Attempt 1: Find the registered default preset, or global default
        GenericPreset<DATA> defaultPreset = getDefaultPreset(loader);
        if(defaultPreset != null){
            return defaultPreset;
        }
        //Attempt 2: Find the registered default preset by the fallback sub type, different from the global default
        GenericPreset<DATA> defaultPresetFromSubType = getDefaultPresetForSubType(loader, fallbackSubType);
        if(defaultPresetFromSubType != null){
            return defaultPresetFromSubType;
        }
        //Attempt 3: Switch to finding the fallback from sub type and name
        if(!fallbackSubType.isEmpty() && !fallbackName.isEmpty()){
            GenericPreset<DATA> fallbackPreset = loader.findPreset(fallbackSubType, fallbackName);
            if(fallbackPreset != null){
                return fallbackPreset;
            }
        }
        //Attempt 4: Switch to finding the fallback from name only
        if(!fallbackName.isEmpty()){
            GenericPreset<DATA> fallbackPreset = loader.findPreset(fallbackName);
            if(fallbackPreset != null){
                return fallbackPreset;
            }
        }
        //Attempt 5: Use the first preset found
        if(orFirst && !loader.getPresets().isEmpty()){
            return loader.getPresets().get(0);
        }
        //Fail: No suitable preset found
        return null;
    }

    @Nullable
    public String getDefaultPresetNameForSubType(PresetType presetType, String presetSubType){
        return DBPreferences.INSTANCE.getDefaultPreset(presetType, presetSubType);
    }

    @Nullable
    public <DATA> GenericPreset<DATA> getDefaultPresetForSubType(IPresetLoader<DATA> loader, String presetSubType){
        String name = getDefaultPresetNameForSubType(loader.getPresetType(), presetSubType);
        if(name != null && !name.isEmpty()){
            return loader.findPreset(presetSubType, name);
        }
        return null;
    }

    @Nullable
    public <DATA> GenericPreset<DATA> getDefaultPresetForSubTypeWithFallback(IPresetLoader<DATA> loader, String presetSubType, String fallbackName, boolean orFirst){
        //Attempt 1: Find the registered default preset
        GenericPreset<DATA> defaultPreset = getDefaultPresetForSubType(loader, presetSubType);
        if(defaultPreset != null){
            return defaultPreset;
        }
        //Attempt 2: Switch to the fallback default
        GenericPreset<DATA> fallbackPreset = loader.findPreset(presetSubType, fallbackName);
        if(fallbackPreset != null){
            return fallbackPreset;
        }
        //Attempt 3: Use the first preset found
        if(orFirst && !loader.getPresetsForSubType(presetSubType).isEmpty()){
            return loader.getPresetsForSubType(presetSubType).get(0);
        }
        //Fail: No suitable preset found
        return null;
    }


    /*

    @Nullable
    public <O> GenericPreset<O> getDefaultPreset(AbstractJsonLoader<O> jsonLoader, String presetSubType, String fallbackName, String fallbackSubType, boolean orFirst){
        Collection<GenericPreset<O>> presets = jsonLoader.presetType.defaultsPerSubType ? jsonLoader.getPresetsForSubType(presetSubType) : jsonLoader.getPresets();
        String defaultName = getDefaultPresetNameForSubType(jsonLoader.presetType, presetSubType);
        if(defaultName != null){
            GenericPreset<O> preset = presets.stream().filter(p -> p.getPresetName().equals(defaultName) && (presetSubType.isEmpty() || p.getPresetSubType().equals(presetSubType))).findFirst().orElse(null);
            if(preset != null){
                return preset;
            }
        }
        if(fallbackName != null && !fallbackName.isEmpty()){
            GenericPreset<O> preset = presets.stream().filter(p -> p.getPresetName().equals(fallbackName) && (fallbackSubType.isEmpty() || p.getPresetSubType().equals(fallbackSubType))).findFirst().orElse(null);
            if(preset != null){
                return preset;
            }
        }

        if(orFirst){
            return presets.stream().filter(p -> (presetSubType.isEmpty() || p.getPresetSubType().equals(presetSubType))).findFirst().orElse(null);
        }

        return null;
    }

     */

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// SETTINGS CATEGORIES \\\\
    public HashMap<String, CategorySetting<?>> settingCategories = new HashMap<>();

    public CategorySetting<?> getCategorySettingInstance(String categoryRegistryName){
        CategorySetting<?> categorySetting = settingCategories.get(categoryRegistryName);
        if(categorySetting != null){
            return categorySetting.copy();
        }
        return new CategorySetting<>(Object.class, categoryRegistryName, categoryRegistryName, true);
    }

    public Map<String, List<GenericSetting<?, ?>>> getSettingsByCategory(Collection<GenericSetting<?, ?>> settings){
        return settings.stream().collect(Collectors.groupingBy(GenericSetting::getCategory));
    }

    public List<CategorySetting<?>> getSortedCategorySettings(Collection<GenericSetting<?, ?>> settings){
        return settings.stream().map(GenericSetting::getCategory).distinct().map(category -> MasterRegistry.INSTANCE.getCategorySettingInstance(category)).sorted(Comparator.comparingInt(value -> -value.getPriority())).collect(Collectors.toList());
    }

    public List<String> getSortedCategoryNamesFromSettings(Collection<GenericSetting<?, ?>> settings){
        return settings.stream().map(GenericSetting::getCategory).distinct().sorted(Comparator.comparingInt(value -> -getCategoryPriority(value))).collect(Collectors.toList());
    }

    public List<String> getSortedCategoryNames(Collection<String> categoryNames){
        return categoryNames.stream().distinct().sorted(Comparator.comparingInt(value -> -getCategoryPriority(value))).collect(Collectors.toList());
    }

    public List<GenericSetting<?, ?>> sortSettingsByCategory(List<GenericSetting<?, ?>> settings){
        List<GenericSetting<?, ?>> sortedList = new ArrayList<>();

        Map<String, List<GenericSetting<?, ?>>> groupedSettings = MasterRegistry.INSTANCE.getSettingsByCategory(settings);
        getSortedCategoryNames(groupedSettings.keySet()).forEach(category -> sortedList.addAll(groupedSettings.get(category)));
        
        return sortedList;
    }

    public int getCategoryPriority(String category){
        CategorySetting<?> categorySetting = settingCategories.get(category);
        return categorySetting == null ? CategorySetting.DEFAULT_PRIORITY : categorySetting.getPriority();
    }

    /**
     * It's not actually necessary to register the setting category, if it's not registered one will be created at runtime.
     * However, if we are configuring them we do enforce the registry name is unique so ensure any special attributes are used correctly.
     * 
     * This is also added to allow custom Documentation links for category using {@link GenericSetting#setDocURLSuffix(String)}
     */
    public CategorySetting<?> registerSettingCategory(String categoryRegistryName, int priority){
        if(settingCategories.containsKey(categoryRegistryName)){
            throw new UnsupportedOperationException("DUPLICATE CATEGORY REGISTRY NAME: " + categoryRegistryName);
        }
        CategorySetting<?> categorySetting = new CategorySetting<>(Object.class, categoryRegistryName, categoryRegistryName, true);
        categorySetting.setPriority(priority);
        settingCategories.put(categoryRegistryName, categorySetting);

        return categorySetting;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PATH FINDING MODULES \\\\
    public ObservableList<PFMFactory<?>> pfmFactories = FXCollections.observableArrayList();
    public ObservableList<String> pfmCategories = FXCollections.observableArrayList();
    public HashMap<PFMFactory<?>, ObservableList<GenericSetting<?, ?>>> pfmSettings = new LinkedHashMap<>();
    {
        pfmCategories.add("All");
    }

    public PFMFactory<?> getDefaultPFM(){
        return pfmFactories.stream().filter(factory -> factory.getRegistryName().equals(DBPreferences.INSTANCE.defaultPFM.get())).findFirst().orElseGet(() -> pfmFactories.stream().filter(factory -> factory.getRegistryName().equals("Sketch Lines PFM")).findFirst().orElse(null));
    }

    public ObservableList<GenericSetting<?, ?>> getNewObservableSettingsList(PFMFactory<?> factory){
        ObservableList<GenericSetting<?, ?>> newList = FXCollections.observableArrayList();
        createSettingsList(factory, newList);
        return newList;
    }

    public List<GenericSetting<?, ?>> getNewPFMSettingsList(PFMFactory<?> factory){
        ArrayList<GenericSetting<?, ?>> newList = new ArrayList<>();
        createSettingsList(factory, newList);
        return newList;
    }

    public void createSettingsList(PFMFactory<?> factory, List<GenericSetting<?, ?>> dst){
        ObservableList<GenericSetting<?, ?>> settings = MasterRegistry.INSTANCE.pfmSettings.getOrDefault(factory, FXCollections.observableArrayList());
        List<GenericSetting<?, ?>> list = GenericSetting.copy(settings, dst);
        GenericSetting.resetSettings(list);
        GenericPreset<PresetData> preset = Register.PRESET_LOADER_PFM.getDefaultPresetForSubType(factory.getRegistryName());
        if(preset != null){
            GenericSetting.applySettings(preset.data.settings, list);
        }
    }

    public void sortPFMSettings(){
        for(ObservableList<GenericSetting<?, ?>> settingsList : MasterRegistry.INSTANCE.pfmSettings.values()){
            settingsList.sort(Comparator.comparingInt(value -> -getCategoryPriority(value.getCategory())));
        }
    }

    public <C extends IPFM> PFMFactory<C> registerPFM(Class<C> pfmClass, String name, String category, Supplier<C> create){
        DrawingBotV3.logger.config("Registering PFM: " + name);
        PFMFactory<C> factory = new PFMFactory<C>(pfmClass, name, category, create);
        pfmFactories.add(factory);
        if(!pfmCategories.contains(category)){
            pfmCategories.add(category);
        }
        return factory;
    }

    public <C, V> void registerPFMSetting(GenericSetting<C, V> setting){
        DrawingBotV3.logger.config("Registering PFM Setting: " + setting.getKey());
        for(PFMFactory<?> factory : pfmFactories){
            if(setting.isAssignableFrom(factory.getInstanceClass())){
                GenericSetting<C,V> copy = setting.copy();
                pfmSettings.putIfAbsent(factory, FXCollections.observableArrayList());
                pfmSettings.get(factory).add(copy);
            }
        }
    }

    public PFMFactory<?> getPFMFactory(String name){
        for(PFMFactory<?> factory : pfmFactories){
            if(factory.getRegistryName().equals(name)){
                return factory;
            }
        }
        return null;
    }

    public PFMFactory<?> getPFMFactory(Class<?> pfmClass){
        for(PFMFactory<?> factory : pfmFactories){
            if(factory.getInstanceClass().equals(pfmClass)){
                return factory;
            }
        }
        return null;
    }

    public GenericSetting<?, ?> getPFMSettingByName(Class<?> pfmClass, String name){
        PFMFactory<?> factory = getPFMFactory(pfmClass);
        if(factory != null){
            ObservableList<GenericSetting<?, ?>> settings = pfmSettings.get(factory);
            for(GenericSetting<?, ?> setting : settings){
                if(setting.getKey().equals(name)){
                    return setting;
                }
            }
        }
        return null;
    }

    public void removePFMSettingByName(Class<?> pfmClass, String name){
        PFMFactory<?> factory = getPFMFactory(pfmClass);
        if(factory != null){
            removePFMSettingByName(factory, name);
        }
    }

    public void removePFMSettingByName(PFMFactory<?> factory, String name){
        ObservableList<GenericSetting<?, ?>> settings = pfmSettings.get(factory);
        for(GenericSetting<?, ?> setting : settings){
            if(setting.getKey().equals(name)){
                settings.remove(setting);
                return;
            }
        }
    }

    public ObservableList<PFMFactory<?>> getFilteredObservablePFMLoaderList(ObjectProperty<String> category){
        FilteredList<PFMFactory<?>> filteredList = new FilteredList<>(pfmFactories);
        EasyBind.subscribe(category, s -> {
            filteredList.setPredicate(f -> {
                if(!f.isHidden() || FXApplication.isDeveloperMode){
                    return category.get().equals("All") || f.category.equals(category.get());
                }
                return false;
            });
        });
        return filteredList;
    }

    public ObservableList<PFMFactory<?>> getObservablePFMLoaderList(){
        ObservableList<PFMFactory<?>> list = FXCollections.observableArrayList();
        for(PFMFactory<?> loader : pfmFactories){
            if(!loader.isHidden() || FXApplication.isDeveloperMode){
                list.add(loader);
            }
        }
        return list;
    }

    public ObservableList<GenericSetting<?, ?>> getObservablePFMSettingsList(){
        return getObservablePFMSettingsList(DrawingBotV3.project().getPFMFactory());
    }

    public ObservableList<GenericSetting<?, ?>> getObservablePFMSettingsList(PFMFactory<?> factory){
        return pfmSettings.get(factory);
    }

    public ObservableList<GenericPreset<PresetData>> getObservablePFMPresetList(){
        return getObservablePFMPresetList(DrawingBotV3.project().getPFMFactory());
    }

    public ObservableList<GenericPreset<PresetData>> getObservablePFMPresetList(PFMFactory<?> loader){
        return Register.PRESET_LOADER_PFM.presetsByType.get(loader.getRegistryName());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// DRAWING PENS \\\\

    public void registerDrawingPen(IDrawingPen pen){
        if(pen == null){
            return;
        }
        Register.PRESET_LOADER_DRAWING_PENS.addDrawingPen(pen);
    }

    public void unregisterDrawingPen(IDrawingPen pen){
        if (pen == null) {
            return;
        }
        Register.PRESET_LOADER_DRAWING_PENS.removeDrawingPen(pen);
    }

    @Nullable
    public IDrawingPen getDrawingPenFromRegistryName(String codeName){
        if(!codeName.contains(":")){
            return null;
        }
        String[] split = codeName.split(":");
        return Register.PRESET_LOADER_DRAWING_PENS.findDrawingPen(split[0], split[1]);
    }

    public List<IDrawingPen> getDrawingPensFromRegistryNames(String[] codes){
        List<IDrawingPen> pens = new ArrayList<>();
        for(String code : codes){
            IDrawingPen pen = getDrawingPenFromRegistryName(code);
            if(pen != null){
                pens.add(pen);
            }else{
                DrawingBotV3.logger.warning("Couldn't find a pen with the code: " + code);
            }
        }
        return pens;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// DRAWING SETS \\\\

    public IDrawingSet getDefaultDrawingSet(){
        return Register.PRESET_LOADER_DRAWING_SET.unwrapPreset(Register.PRESET_LOADER_DRAWING_SET.getDefaultPreset());
    }

    public IDrawingSet getDefaultSet(String type){
        return Register.PRESET_LOADER_DRAWING_SET.unwrapPreset(Register.PRESET_LOADER_DRAWING_SET.getDefaultPresetForSubType(type));
    }

    public void registerDrawingSet(IDrawingSet set){
        if(set == null){
            return;
        }
        Register.PRESET_LOADER_DRAWING_SET.addDrawingSet(set);
    }

    public void unregisterDrawingSet(IDrawingSet set){
        if(set == null){
            return;
        }
        Register.PRESET_LOADER_DRAWING_SET.removeDrawingSet(set);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// IMAGE FILTERS \\\\
    public Map<EnumFilterTypes, ObservableList<GenericFactory<BufferedImageOp>>> imgFilterFactories = FXCollections.observableMap(new LinkedHashMap<>());
    public List<IKernelFactory> imgFilterKernelFactories = new ArrayList<>();
    public HashMap<Class<? extends BufferedImageOp>, List<GenericSetting<?, ?>>> imgFilterSettings = new LinkedHashMap<>();

    //public HashMap<Class<? extends BufferedImageOp>, Function<ObservableImageFilter, Dialog<ObservableImageFilter>>> imgFilterDialogs = new LinkedHashMap<>();

    public EnumFilterTypes getDefaultImageFilterType(){
        return EnumFilterTypes.COLOURS;
    }

    public GenericFactory<BufferedImageOp> getDefaultImageFilter(EnumFilterTypes type){
        return imgFilterFactories.get(type).stream().findFirst().orElse(null);
    }

    public <I extends BufferedImageOp> void registerImageFilter(EnumFilterTypes filterType, Class<I> filterClass, String name, Supplier<I> create, boolean isHidden){
        DrawingBotV3.logger.config("Registering Image Filter: " + name);
        imgFilterFactories.putIfAbsent(filterType, FXCollections.observableArrayList());
        imgFilterFactories.get(filterType).add(new GenericFactory(filterClass, name, create, isHidden));
    }

    public void registerImageFilterKernelFactory(IKernelFactory factory){
        DrawingBotV3.logger.finest("Registering Image Filter Kernel Factory: for " + factory.getFactoryName());
        imgFilterKernelFactories.add(factory);
    }

    public void registerImageFilterSetting(GenericSetting<? extends BufferedImageOp, ?> setting){
        DrawingBotV3.logger.finest("Registering Image Filter Setting: " + setting.getKey());
        imgFilterSettings.putIfAbsent(setting.clazz, new ArrayList<>());
        imgFilterSettings.get(setting.clazz).add(setting);
    }

    /*
    public void registerImageFilterDialog(Class<BufferedImageOp> filterClass, Function<ObservableImageFilter, Dialog<ObservableImageFilter>> dialog){
        DrawingBotV3.logger.finest("Registering Image Filter Dialog: " + filterClass);
        imgFilterSettings.putIfAbsent(filterClass, new ArrayList<>());
        imgFilterDialogs.put(filterClass, dialog);
    }
     */

    /**
     * @param name the image filters name
     * @return the image filters factory
     */
    public GenericFactory<BufferedImageOp> getImageFilterFactory(String name){
        for(ObservableList<GenericFactory<BufferedImageOp>> factories : imgFilterFactories.values()){
            for(GenericFactory<BufferedImageOp> factory : factories){
                if(factory.getRegistryName().equals(name)){
                    return factory;
                }
            }
        }
        return null;
    }

    @Nullable
    public IKernelFactory getImageFilterKernel(BufferedImageOp op){
        for(IKernelFactory factory : imgFilterKernelFactories){
            if(factory.canProcess(op)){
                return factory;
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
    /*
    public Dialog<ObservableImageFilter> getDialogForFilter(ObservableImageFilter filter){
        Function<ObservableImageFilter, Dialog<ObservableImageFilter>> func = MasterRegistry.INSTANCE.imgFilterDialogs.get(filter.filterFactory.getInstanceClass());
        return func == null ? new DialogImageFilterOld(filter) : func.apply(filter);
    }
     */

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// DISPLAY MODES \\\\
    public ObservableList<DisplayModeBase> displayModes = FXCollections.observableArrayList();

    public DisplayModeBase registerDisplayMode(DisplayModeBase displayMode){
        DrawingBotV3.logger.config("Registering Display Mode: " + displayMode.getName());
        this.displayModes.add(displayMode);
        return displayMode;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// LOADERS \\\\
    public List<IFileLoaderFactory> fileLoaderFactories = new ArrayList<>();
    public IFileLoaderFactory fallbackFileLoaderFactory;

    public void setFallbackFileLoaderFactory(IFileLoaderFactory fallbackFileLoaderFactory) {
        this.fallbackFileLoaderFactory = fallbackFileLoaderFactory;
    }

    public IFileLoaderFactory getFallbackFileLoaderFactory(){
        return fallbackFileLoaderFactory;
    }

    public IFileLoaderFactory registerFileLoaderFactory(IFileLoaderFactory exportHandler){
        DrawingBotV3.logger.config("Registering File Loader: " + exportHandler.getName());
        this.fileLoaderFactories.add(exportHandler);
        return exportHandler;
    }

    public AbstractFileLoader getFileLoader(DBTaskContext context, File file, boolean internal, boolean isSubTask){
        for(IFileLoaderFactory factory : fileLoaderFactories){
            AbstractFileLoader loader = factory.createLoader(context, file, internal, isSubTask);
            if(loader != null){
                return loader;
            }
        }
        return fallbackFileLoaderFactory.createLoader(context, file, internal, isSubTask);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// EXPORTERS \\\\
    public Map<String, DrawingExportHandler> drawingExportHandlers = new LinkedHashMap<>();

    public DrawingExportHandler registerDrawingExportHandler(DrawingExportHandler exportHandler){
        DrawingBotV3.logger.config("Registering Export Handler: " + exportHandler.getRegistryName() + " " + exportHandler.description);
        if(drawingExportHandlers.containsKey(exportHandler.getRegistryName())){
            DrawingBotV3.logger.severe("Duplicate Export Handler: " + exportHandler.getRegistryName());
            return exportHandler;
        }
        this.drawingExportHandlers.put(exportHandler.getRegistryName(), exportHandler);
        return exportHandler;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// COLOUR SPLITTERS \\\\
    public ObservableList<ColorSeparationHandler> colourSplitterHandlers = FXCollections.observableArrayList();

    public ColorSeparationHandler registerColourSplitter(ColorSeparationHandler colourSplitter){
        DrawingBotV3.logger.config("Registering Colour Splitter: " + colourSplitter.name);
        this.colourSplitterHandlers.add(colourSplitter);
        return colourSplitter;
    }

    public ColorSeparationHandler getColourSplitter(String name){
        for(ColorSeparationHandler colourSplitter : colourSplitterHandlers){
            if(colourSplitter.name.equals(name)){
                return colourSplitter;
            }
        }
        return Register.DEFAULT_COLOUR_SPLITTER;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PRESET LOADERS \\\\
    public List<PresetType> presetTypes = new ArrayList<>();
    public List<IPresetLoader<?>> presetLoaders = new ArrayList<>();
    public List<IPresetManager<?, ?>> presetManagers = new ArrayList<>();

    public IPresetLoader<?> registerPresetLoaders(IPresetLoader<?> presetLoader){
        DrawingBotV3.logger.config("Registering Preset Loader: " + presetLoader.getPresetType().registryName);
        this.presetLoaders.add(presetLoader);
        return presetLoader;
    }

    public IPresetManager<?, ?> registerPresetManager(IPresetManager<?, ?> presetManager){
        DrawingBotV3.logger.config("Registering Preset Manager: " + presetManager.getPresetType().registryName + " " + presetManager.getTargetType().getSimpleName());
        this.presetManagers.add(presetManager);
        return presetManager;
    }

    public PresetType registerPresetType(PresetType presetType){
        DrawingBotV3.logger.config("Registering Json Type: " + presetType.registryName);
        this.presetTypes.add(presetType);
        return presetType;
    }

    public PresetType getPresetType(String name){
        for(PresetType presetType : presetTypes){
            if(presetType.registryName.equals(name)){
                return presetType;
            }
        }
        return null;
    }

    public <DATA> IPresetLoader<DATA> getPresetLoader(PresetType presetType){
        return (IPresetLoader<DATA>) presetLoaders.stream().filter(loader -> loader.getPresetType() == presetType).findFirst().orElse(null);
    }

    public <TARGET, DATA> IPresetManager<TARGET, DATA> getPresetManager(PresetType presetType, TARGET target){
        return (IPresetManager<TARGET, DATA>) presetManagers.stream().filter(manager -> manager.getPresetType() == presetType && manager.getTargetType().isInstance(target)).findFirst().orElse(null);
    }

    public IPresetManager<?, ?> getDefaultPresetManager(PresetType presetType){
        return presetManagers.stream().filter(manager -> manager.getPresetType().equals(presetType)).findFirst().orElse(null);
    }

    public <TARGET, DATA> IPresetManager<TARGET, DATA> getDefaultPresetManager(GenericPreset<DATA> presetType){
        return (IPresetManager<TARGET, DATA>) presetManagers.stream().filter(manager -> manager.getPresetType().equals(presetType.presetType)).findFirst().orElse(null);
    }


    public <DATA> GenericPreset<?> createNewPreset(PresetType type, String presetSubType, String presetName, boolean userCreated){
        IPresetLoader<DATA> loader = getPresetLoader(type);
        assert loader != null;
        return loader.createNewPreset(presetSubType, presetName, userCreated);
    }

    public <TARGET, DATA> void updatePreset(DBTaskContext context, GenericPreset<DATA> preset, TARGET target){
        IPresetManager<TARGET, DATA> manager = getPresetManager(preset.presetType, target);
        assert manager != null;
        manager.updatePreset(context, target, preset, false);
    }

    public <TARGET, DATA> void applyPresetToProject(DBTaskContext context, GenericPreset<DATA> preset, boolean changesOnly, boolean isLoading){
        IPresetManager<TARGET, DATA> manager = getDefaultPresetManager(preset);
        assert manager != null;
        TARGET target = manager.getTargetFromContext(context);
        manager.applyPreset(context, target, preset, changesOnly);
    }

    public <TARGET, DATA> void updatePresetFromProject(DBTaskContext context, GenericPreset<DATA> preset, boolean changesOnly, boolean isLoading){
        IPresetManager<TARGET, DATA> manager = getDefaultPresetManager(preset);
        assert manager != null;
        TARGET target = manager.getTargetFromContext(context);
        manager.updatePreset(context, target, preset, false);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PROJECT DATA LOADER \\\\
    private final Map<String, PresetDataLoader<PresetProjectSettings>> projectDataLoadersMap = new HashMap<>();
    public List<PresetDataLoader<PresetProjectSettings>> projectDataLoaders = new ArrayList<>();

    public void registerProjectDataLoader(PresetDataLoader<PresetProjectSettings> loader){
        if(projectDataLoadersMap.containsKey(loader.getKey())){
            DrawingBotV3.logger.severe("DUPLICATE PROJECT DATA LOADER KEY: " + loader.getKey());
        }else{
            DrawingBotV3.logger.config("Registering Project Data Loader: " + loader.getKey());
            projectDataLoadersMap.put(loader.getKey(), loader);
        }
    }

    public void sortDataLoaders(){
        projectDataLoaders.addAll(projectDataLoadersMap.values());
        projectDataLoaders.sort(Comparator.comparingInt(l -> l.order));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public final List<GenericSetting<?, ?>> applicationSettings = new ArrayList<>();

    public <T extends GenericSetting<?, ?>> T  registerApplicationSetting(T add){
        applicationSettings.add(add);
        add.createDefaultGetterAndSetter();
        return add;
    }   

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// GEOMETRIES \\\\
    public Map<Class<? extends IGeometry>, String> geometryNames = new HashMap<>();
    public Map<String, Class<? extends IGeometry>> geometryTypes = new HashMap<>();
    public Map<String, Supplier<IGeometry>> geometryFactories = new HashMap<>();

    public void registerGeometryType(String name, Class<? extends IGeometry> geometryType, Supplier<IGeometry> factory){
        DrawingBotV3.logger.config("Registering Geometry Type: " + name);
        this.geometryNames.put(geometryType, name);
        this.geometryTypes.put(name, geometryType);
        this.geometryFactories.put(name, factory);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// GEOMETRY TO JFX CONVERTERS \\\\

    public List<JFXGeometryConverter> jfxGeometryConverters = new ArrayList<>();
    public JFXGeometryConverter fallbackConverter = null;

    public void registerJFXGeometryConverter(JFXGeometryConverter converter){
        jfxGeometryConverters.add(converter);
    }

    public void setFallbackJFXGeometryConverter(JFXGeometryConverter fallbackConverter) {
        this.fallbackConverter = fallbackConverter;
    }

    public JFXGeometryConverter getFallbackJFXGeometryConverter(){
        return fallbackConverter;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// METADATA \\\\
    public Map<String, Metadata<?>> metadataTypes = new LinkedHashMap<>();

    public void registerMetadataType(Metadata<?> metadata){
        if(metadata == null){
            DrawingBotV3.logger.warning("Passed NULL Metadata");
            return;
        }
        if(metadataTypes.get(metadata.key) != null){
            throw new IllegalArgumentException("Metadata must have Unique Key");
        }
        this.metadataTypes.put(metadata.key, metadata);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PREFERENCES \\\\
    public TreeNode root = EditorSheet.root();

    public void registerPreferencesPage(TreeNode treeNode){
        registerPreferencesPage("", treeNode);
    }

    public void registerPreferencesPage(String breadcrumb, TreeNode treeNode){
        if(!breadcrumb.isEmpty()){
            String[] pages = breadcrumb.split("#");
            TreeNode parentPage = root;
            pages: for(String pageName : pages){
                for(TreeNode child : parentPage.getChildren()){
                    if(child.getName().equals(pageName)){
                        parentPage = child;
                        continue pages;
                    }
                }
                parentPage.getChildren().add(parentPage = EditorSheet.node(pageName));
            }
            parentPage.getChildren().add(treeNode);
        }else{
            root.getChildren().add(treeNode);
        }

    }

}