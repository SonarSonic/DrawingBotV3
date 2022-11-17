package drawingbot.registry;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.*;
import drawingbot.drawing.ColourSeperationHandler;
import drawingbot.drawing.DrawingPen;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.json.AbstractJsonLoader;
import drawingbot.files.json.IJsonData;
import drawingbot.files.json.PresetType;
import drawingbot.files.json.presets.PresetPFMSettings;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.PresetDataLoader;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.files.loaders.AbstractFileLoader;
import drawingbot.files.loaders.IFileLoaderFactory;
import drawingbot.geom.shapes.IGeometry;
import drawingbot.geom.shapes.JFXGeometryConverter;
import drawingbot.image.kernels.IKernelFactory;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.GenericFactory;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controls.DialogImageFilter;
import drawingbot.javafx.preferences.DBPreferences;
import drawingbot.javafx.preferences.FXPreferences;
import drawingbot.pfm.PFMFactory;
import drawingbot.pfm.PFMSketchLines;
import drawingbot.render.IDisplayMode;
import drawingbot.render.overlays.AbstractOverlay;
import drawingbot.utils.EnumFilterTypes;
import drawingbot.utils.Metadata;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Dialog;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImageOp;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class MasterRegistry {

    public static MasterRegistry INSTANCE = new MasterRegistry();

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PLUGINS \\\\
    public static List<IPlugin> PLUGINS = new ArrayList<>(List.of(Register.INSTANCE));

    public static void findPlugins(){
        findPlugins(PLUGINS);
    }

    private static void findPlugins(List<IPlugin> plugins){
        List<IPlugin> newPlugins = new ArrayList<>();
        for(IPlugin plugin : plugins){
            plugin.registerPlugins(newPlugins);
        }
        if(!newPlugins.isEmpty()){
            PLUGINS.addAll(newPlugins);
            findPlugins(newPlugins);
        }
    }

    private static boolean init = false;

    public static void init(){
        if(init){
            return;
        }

        PLUGINS.forEach(IPlugin::init);
        PLUGINS.forEach(IPlugin::registerPFMS);
        PLUGINS.forEach(IPlugin::registerPFMSettings);

        INSTANCE.sortPFMSettings();
        INSTANCE.sortDataLoaders();

        PLUGINS.forEach(IPlugin::registerDrawingTools);
        PLUGINS.forEach(IPlugin::registerImageFilters);
        PLUGINS.forEach(IPlugin::registerDrawingExportHandlers);
        PLUGINS.forEach(IPlugin::registerColourSplitterHandlers);

        init = true;
    }

    public static void postInit(){
        PLUGINS.forEach(IPlugin::registerPreferencePages);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PRESETS \\\\

    @Nullable
    public String getDefaultPresetName(PresetType presetType, String presetSubType){
        return DBPreferences.INSTANCE.getDefaultPreset(presetType.defaultsPerSubType ? presetType.id + ":" + presetSubType : presetType.id);
    }

    @Nullable
    public <O extends IJsonData> GenericPreset<O> getDefaultPreset(AbstractJsonLoader<O> jsonLoader, String fallbackName){
        return getDefaultPreset(jsonLoader, "", fallbackName, "", true);
    }

    @Nullable
    public <O extends IJsonData> GenericPreset<O> getDefaultPreset(AbstractJsonLoader<O> jsonLoader, String presetSubType, String fallbackName){
        return getDefaultPreset(jsonLoader, presetSubType, fallbackName, presetSubType, true);
    }

    @Nullable
    public <O extends IJsonData> GenericPreset<O> getDefaultPreset(AbstractJsonLoader<O> jsonLoader, String presetSubType, String fallbackName, boolean orFirst){
        return getDefaultPreset(jsonLoader, presetSubType, fallbackName, presetSubType, orFirst);
    }

    @Nullable
    public <O extends IJsonData> GenericPreset<O> getDefaultPreset(AbstractJsonLoader<O> jsonLoader, String presetSubType, String fallbackName, String fallbackSubType, boolean orFirst){
        Collection<GenericPreset<O>> presets = jsonLoader.type.defaultsPerSubType ? jsonLoader.getPresetsForSubType(presetSubType) : jsonLoader.getAllPresets();
        String defaultName = getDefaultPresetName(jsonLoader.type, presetSubType);
        if(defaultName != null){
            GenericPreset<O> preset = presets.stream().filter(p -> p.presetName.equals(defaultName) && (presetSubType.isEmpty() || p.presetSubType.equals(presetSubType))).findFirst().orElse(null);
            if(preset != null){
                return preset;
            }
        }
        if(fallbackName != null && !fallbackName.isEmpty()){
            GenericPreset<O> preset = presets.stream().filter(p -> p.presetName.equals(fallbackName) && (fallbackSubType.isEmpty() || p.presetSubType.equals(fallbackSubType))).findFirst().orElse(null);
            if(preset != null){
                return preset;
            }
        }

        if(orFirst){
            return presets.stream().filter(p -> (presetSubType.isEmpty() || p.presetSubType.equals(presetSubType))).findFirst().orElse(null);
        }

        return null;
    }

    public void setDefaultPreset(GenericPreset<?> preset){
        if(preset.presetType.defaultsPerSubType){
            DBPreferences.INSTANCE.setDefaultPreset(preset.presetType.id + ":" + preset.presetSubType, preset.presetName);
        }else{
            DBPreferences.INSTANCE.setDefaultPreset(preset.presetType.id, preset.presetName);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// SETTINGS CATEGORIES \\\\
    public HashMap<String, Integer> settingCategories = new HashMap<>();

    public int getCategoryPriority(String category){
        Integer priority = settingCategories.get(category);
        return priority == null ? 5 : priority;
    }

    public void registerSettingCategory(String category, int priority){
        settingCategories.put(category, priority);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PATH FINDING MODULES \\\\
    public List<PFMFactory> pfmFactories = new ArrayList<>();
    public HashMap<PFMFactory, ObservableList<GenericSetting<?, ?>>> pfmSettings = new LinkedHashMap<>();

    public PFMFactory<?> getDefaultPFM(){
        return pfmFactories.stream().filter(factory -> factory.getName().equals(DBPreferences.INSTANCE.defaultPFM.get())).findFirst().orElseGet(() -> pfmFactories.stream().filter(factory -> factory.getInstanceClass().equals(PFMSketchLines.class)).findFirst().orElse(null));
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
        List<GenericSetting<?, ?>> list = GenericSetting.copy(MasterRegistry.INSTANCE.pfmSettings.get(factory), dst);
        GenericSetting.resetSettings(list);
        GenericPreset<PresetPFMSettings> preset = Register.PRESET_LOADER_PFM.getDefaultPresetForSubType(factory.getName());
        if(preset != null){
            GenericSetting.applySettings(preset.data.settingList, list);
        }
    }

    public void sortPFMSettings(){
        for(ObservableList<GenericSetting<?, ?>> settingsList : MasterRegistry.INSTANCE.pfmSettings.values()){
            settingsList.sort(Comparator.comparingInt(value -> -getCategoryPriority(value.getCategory())));
        }
    }

    public <C extends IPFM> PFMFactory<C> registerPFM(Class<C> pfmClass, String name, String category, Supplier<C> create, boolean isHidden, boolean registerDefaultPreset){
        DrawingBotV3.logger.fine("Registering PFM: " + name);
        PFMFactory<C> factory = new PFMFactory<C>(pfmClass, name, category, create, isHidden);
        pfmFactories.add(factory);
        if(registerDefaultPreset){
            Register.PRESET_LOADER_PFM.registerPreset(Register.PRESET_LOADER_PFM.createNewPreset(name, "Default", false));
        }
        return factory;
    }

    public <C, V> void registerPFMSetting(GenericSetting<C, V> setting){
        DrawingBotV3.logger.fine("Registering PFM Setting: " + setting.getKey());
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
            if(factory.getName().equals(name)){
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
            ObservableList<GenericSetting<?, ?>> settings = pfmSettings.get(factory);
            for(GenericSetting<?, ?> setting : settings){
                if(setting.getKey().equals(name)){
                    settings.remove(setting);
                    return;
                }
            }
        }
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
        return getObservablePFMSettingsList(DrawingBotV3.project().getPFMSettings().getPFMFactory());
    }

    public ObservableList<GenericSetting<?, ?>> getObservablePFMSettingsList(PFMFactory<?> factory){
        return pfmSettings.get(factory);
    }

    public ObservableList<GenericPreset<PresetPFMSettings>> getObservablePFMPresetList(){
        return getObservablePFMPresetList(DrawingBotV3.project().getPFMSettings().getPFMFactory());
    }

    public ObservableList<GenericPreset<PresetPFMSettings>> getObservablePFMPresetList(PFMFactory<?> loader){
        return Register.PRESET_LOADER_PFM.presetsByType.get(loader.getName());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// DRAWING PENS \\\\
    public ObservableMap<String, ObservableList<DrawingPen>> registeredPens = FXCollections.observableMap(new LinkedHashMap<>());

    public String getDefaultPenCode(){
        return "Copic Original:100 Black";
    }

    public DrawingPen getDefaultDrawingPen(){
        String defaultPen = getDefaultPresetName(Register.PRESET_TYPE_DRAWING_PENS, "");
        if(defaultPen != null){
            DrawingPen pen = getDrawingPenFromRegistryName(defaultPen);
            if(pen != null){
                return pen;
            }
        }
        return getDrawingPenFromRegistryName(getDefaultPenCode());
    }

    public DrawingPen getDefaultPen(String type){
        ObservableList<DrawingPen> pens = registeredPens.get(type);
        return pens == null ? null : pens.stream().findFirst().orElse(null);
    }

    public void registerDrawingPen(DrawingPen pen){
        if(pen == null){
            return;
        }
        DrawingBotV3.logger.finest("Registering Drawing Pen: " + pen.getCodeName());
        if(registeredPens.get(pen.getName()) != null){
            DrawingBotV3.logger.warning("DUPLICATE PEN UNIQUE ID: " + pen.getName());
            return;
        }
        registeredPens.putIfAbsent(pen.getType(), FXCollections.observableArrayList());
        registeredPens.get(pen.getType()).add(pen);
    }

    public void unregisterDrawingPen(DrawingPen pen){
        DrawingBotV3.logger.finest("Unregistering Drawing Pen: " + pen.getCodeName());
        ObservableList<DrawingPen> pens = registeredPens.get(pen.getType());
        pens.remove(pen);
        if(pens.isEmpty()){
            registeredPens.remove(pen.getType());
        }
    }

    @Nullable
    public DrawingPen getDrawingPenFromRegistryName(String codeName){
        if(!codeName.contains(":")){
            return null;
        }
        String[] split = codeName.split(":");
        ObservableList<DrawingPen> pens = registeredPens.get(split[0]);
        if(pens == null){
            return null;
        }
        return pens.stream().filter(p -> p.getCodeName().equals(codeName)).findFirst().orElse(null);
    }

    public List<DrawingPen> getDrawingPensFromRegistryNames(String[] codes){
        List<DrawingPen> pens = new ArrayList<>();
        for(String code : codes){
            DrawingPen pen = getDrawingPenFromRegistryName(code);
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
    public ObservableMap<String, ObservableList<IDrawingSet<IDrawingPen>>> registeredSets  = FXCollections.observableMap(new LinkedHashMap<>());

    public String getDefaultSetCode(){
        return "Copic:Dark Greys";
    }

    public IDrawingSet<IDrawingPen> getDefaultDrawingSet(){
        String defaultSet = getDefaultPresetName(Register.PRESET_TYPE_DRAWING_SET, "");
        if(defaultSet != null){
            IDrawingSet<IDrawingPen> set = getDrawingSetFromRegistryName(defaultSet);
            if(set != null){
                return set;
            }
        }
        return getDrawingSetFromRegistryName(getDefaultSetCode());
    }

    public IDrawingSet<IDrawingPen> getDefaultSet(String type){
        ObservableList<IDrawingSet<IDrawingPen>> sets = registeredSets.get(type);
        return sets == null ? null : sets.stream().findFirst().orElse(null);
    }

    public void registerDrawingSet(IDrawingSet<IDrawingPen> penSet){
        if(penSet == null){
            return;
        }
        DrawingBotV3.logger.finest("Registering Drawing Set: " + penSet.getCodeName());
        if(registeredSets.get(penSet.getName()) != null){
            DrawingBotV3.logger.warning("DUPLICATE DRAWING SET NAME: " + penSet.getName());
            return;
        }
        registeredSets.putIfAbsent(penSet.getType(), FXCollections.observableArrayList());
        registeredSets.get(penSet.getType()).add(penSet);
    }

    public void unregisterDrawingSet(IDrawingSet<IDrawingPen> penSet){
        DrawingBotV3.logger.finest("Unregistering Drawing Set: " + penSet.getCodeName());
        ObservableList<IDrawingSet<IDrawingPen>> sets = registeredSets.get(penSet.getType());
        sets.remove(penSet);
        if(sets.isEmpty()){
            registeredSets.remove(penSet.getType());
        }
    }

    public IDrawingSet<IDrawingPen> getDrawingSetFromRegistryName(String codeName){
        if(!codeName.contains(":")){
            return null;
        }
        String[] split = codeName.split(":");
        ObservableList<IDrawingSet<IDrawingPen>> sets = registeredSets.get(split[0]);
        if(sets == null){
            return null;
        }
        return sets.stream().filter(s -> s.getCodeName().equals(codeName)).findFirst().orElse(null);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// IMAGE FILTERS \\\\
    public Map<EnumFilterTypes, ObservableList<GenericFactory<BufferedImageOp>>> imgFilterFactories = FXCollections.observableMap(new LinkedHashMap<>());
    public List<IKernelFactory> imgFilterKernelFactories = new ArrayList<>();
    public HashMap<Class<? extends BufferedImageOp>, List<GenericSetting<?, ?>>> imgFilterSettings = new LinkedHashMap<>();

    public HashMap<Class<? extends BufferedImageOp>, Function<ObservableImageFilter, Dialog<ObservableImageFilter>>> imgFilterDialogs = new LinkedHashMap<>();

    public EnumFilterTypes getDefaultImageFilterType(){
        return EnumFilterTypes.COLOURS;
    }

    public GenericFactory<BufferedImageOp> getDefaultImageFilter(EnumFilterTypes type){
        return imgFilterFactories.get(type).stream().filter(i -> i.getName().equals("Contrast")).findFirst().orElse(null);
    }

    public <I extends BufferedImageOp> void registerImageFilter(EnumFilterTypes filterType, Class<I> filterClass, String name, Supplier<I> create, boolean isHidden){
        DrawingBotV3.logger.fine("Registering Image Filter: " + name);
        imgFilterFactories.putIfAbsent(filterType, FXCollections.observableArrayList());
        imgFilterFactories.get(filterType).add(new GenericFactory(filterClass, name, create, isHidden));
    }

    public void registerImageFilterKernelFactory(IKernelFactory factory){
        DrawingBotV3.logger.finest("Registering Image Filter Kernel Factory: for " + factory.getFactoryName());
        imgFilterKernelFactories.add(factory);
    }

    public void registerImageFilterSetting(GenericSetting<? extends BufferedImageOp, ?> setting){
        DrawingBotV3.logger.finest("Registering Image Filter: " + setting.getKey());
        imgFilterSettings.putIfAbsent(setting.clazz, new ArrayList<>());
        imgFilterSettings.get(setting.clazz).add(setting);
    }

    public void registerImageFilterDialog(Class<BufferedImageOp> filterClass, Function<ObservableImageFilter, Dialog<ObservableImageFilter>> dialog){
        DrawingBotV3.logger.finest("Registering Image Filter Dialog: " + filterClass);
        imgFilterSettings.putIfAbsent(filterClass, new ArrayList<>());
        imgFilterDialogs.put(filterClass, dialog);
    }

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
    public Dialog<ObservableImageFilter> getDialogForFilter(ObservableImageFilter filter){
        Function<ObservableImageFilter, Dialog<ObservableImageFilter>> func = MasterRegistry.INSTANCE.imgFilterDialogs.get(filter.filterFactory.getInstanceClass());
        return func == null ? new DialogImageFilter(filter) : func.apply(filter);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// DISPLAY MODES \\\\
    public ObservableList<IDisplayMode> displayModes = FXCollections.observableArrayList();

    public IDisplayMode registerDisplayMode(IDisplayMode displayMode){
        DrawingBotV3.logger.fine("Registering Display Mode: " + displayMode.getName());
        this.displayModes.add(displayMode);
        return displayMode;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// OVERLAYS \\\\
    public ObservableList<AbstractOverlay> overlays = FXCollections.observableArrayList();

    public AbstractOverlay registerOverlay(AbstractOverlay displayMode){
        DrawingBotV3.logger.fine("Registering Overlay: " + displayMode.getName());
        this.overlays.add(displayMode);
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
        DrawingBotV3.logger.fine("Registering File Loader: " + exportHandler.getName());
        this.fileLoaderFactories.add(exportHandler);
        return exportHandler;
    }

    public AbstractFileLoader getFileLoader(DBTaskContext context, File file, boolean internal){
        for(IFileLoaderFactory factory : fileLoaderFactories){
            AbstractFileLoader loader = factory.createLoader(context, file, internal);
            if(loader != null){
                return loader;
            }
        }
        return fallbackFileLoaderFactory.createLoader(context, file, internal);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// EXPORTERS \\\\
    public Map<String, DrawingExportHandler> drawingExportHandlers = new LinkedHashMap<>();

    public DrawingExportHandler registerDrawingExportHandler(DrawingExportHandler exportHandler){
        DrawingBotV3.logger.fine("Registering Export Handler: " + exportHandler.getRegistryName() + " " + exportHandler.description);
        if(drawingExportHandlers.containsKey(exportHandler.getRegistryName())){
            DrawingBotV3.logger.severe("Duplicate Export Handler: " + exportHandler.getRegistryName());
            return exportHandler;
        }
        this.drawingExportHandlers.put(exportHandler.getRegistryName(), exportHandler);
        return exportHandler;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// COLOUR SPLITTERS \\\\
    public ObservableList<ColourSeperationHandler> colourSplitterHandlers = FXCollections.observableArrayList();

    public ColourSeperationHandler registerColourSplitter(ColourSeperationHandler colourSplitter){
        DrawingBotV3.logger.fine("Registering Colour Splitter: " + colourSplitter.name);
        this.colourSplitterHandlers.add(colourSplitter);
        return colourSplitter;
    }

    public ColourSeperationHandler getColourSplitter(String name){
        for(ColourSeperationHandler colourSplitter : colourSplitterHandlers){
            if(colourSplitter.name.equals(name)){
                return colourSplitter;
            }
        }
        return Register.DEFAULT_COLOUR_SPLITTER;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PRESET LOADERS \\\\
    public List<PresetType> presetTypes = new ArrayList<>();
    public List<AbstractJsonLoader<IJsonData>> presetLoaders = new ArrayList<>();

    public AbstractJsonLoader<IJsonData> registerPresetLoaders(AbstractJsonLoader presetLoader){
        DrawingBotV3.logger.fine("Registering Preset Loader: " + presetLoader.type.id);
        this.presetLoaders.add(presetLoader);
        return presetLoader;
    }

    public PresetType registerPresetType(PresetType presetType){
        DrawingBotV3.logger.fine("Registering Json Type: " + presetType.id);
        this.presetTypes.add(presetType);
        return presetType;
    }

    public PresetType getPresetType(String name){
        for(PresetType presetType : presetTypes){
            if(presetType.id.equals(name)){
                return presetType;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PROJECT DATA LOADER \\\\
    private Map<String, PresetDataLoader<PresetProjectSettings>> projectDataLoadersMap = new HashMap<>();
    public List<PresetDataLoader<PresetProjectSettings>> projectDataLoaders = new ArrayList<>();

    public void registerProjectDataLoader(PresetDataLoader<PresetProjectSettings> loader){
        if(projectDataLoadersMap.containsKey(loader.getKey())){
            DrawingBotV3.logger.severe("DUPLICATE PROJECT DATA LOADER KEY: " + loader.getKey());
        }else{
            DrawingBotV3.logger.fine("Registering Project Data Loader: " + loader.getKey());
            projectDataLoadersMap.put(loader.getKey(), loader);
        }
    }

    public void sortDataLoaders(){
        projectDataLoaders.addAll(projectDataLoadersMap.values());
        projectDataLoaders.sort(Comparator.comparingInt(l -> l.order));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// GEOMETRIES \\\\
    public Map<Class<? extends IGeometry>, String> geometryNames = new HashMap<>();
    public Map<String, Class<? extends IGeometry>> geometryTypes = new HashMap<>();
    public Map<String, Supplier<IGeometry>> geometryFactories = new HashMap<>();

    public void registerGeometryType(String name, Class<? extends IGeometry> geometryType, Supplier<IGeometry> factory){
        DrawingBotV3.logger.fine("Registering Geometry Type: " + name);
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
    public List<Metadata<?>> metadataTypes = new ArrayList<>();

    public void registerMetadataType(Metadata<?> metadata){
        this.metadataTypes.add(metadata);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //// PREFERENCES \\\\
    public FXPreferences.TreeNode root = FXPreferences.root();

    public void registerPreferencesPage(FXPreferences.TreeNode treeNode){
        registerPreferencesPage("", treeNode);
    }

    public void registerPreferencesPage(String breadcrumb, FXPreferences.TreeNode treeNode){
        if(!breadcrumb.isEmpty()){
            String[] pages = breadcrumb.split("#");
            FXPreferences.TreeNode parentPage = root;
            pages: for(String pageName : pages){
                for(FXPreferences.TreeNode child : parentPage.getChildren()){
                    if(child.getName().equals(pageName)){
                        parentPage = child;
                        continue pages;
                    }
                }
                parentPage.getChildren().add(parentPage = FXPreferences.node(pageName));
            }
            parentPage.getChildren().add(treeNode);
        }else{
            root.getChildren().add(treeNode);
        }

    }

}
