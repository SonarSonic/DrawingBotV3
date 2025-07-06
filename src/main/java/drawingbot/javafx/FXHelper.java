package drawingbot.javafx;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.files.*;
import drawingbot.files.json.*;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.ObservableProject;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.files.loaders.FileLoaderFlags;
import drawingbot.image.ImageFilterSettings;
import drawingbot.javafx.controls.*;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.util.PropertyAccessor;
import drawingbot.javafx.util.PropertyAccessorAbstract;
import drawingbot.javafx.util.PropertyAccessorProp;
import drawingbot.javafx.util.UINodeState;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.overlays.NotificationOverlays;
import drawingbot.render.viewport.Viewport;
import drawingbot.software.SoftwareManager;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.*;
import org.controlsfx.control.action.Action;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A utility class for common actions for the FXController / JavaFX Classes
 */
public class FXHelper {

    public static final List<Styleable> persistentStyleables = new ArrayList<>();

    public static final ButtonType buttonResetToDefault = new ButtonType("Reset to default", ButtonBar.ButtonData.OTHER);

    public static void importFile(DBTaskContext context){
        importFile(context, (file, chooser) -> DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, EnumSet.noneOf(FileLoaderFlags.class)), FileUtils.IMPORT_IMAGES, FileUtils.IMPORT_VIDEOS, FileUtils.FILTER_SVG);
    }

    public static void importImageFile(DBTaskContext context){
        importFile(context, (file, chooser) -> DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, EnumSet.noneOf(FileLoaderFlags.class)), FileUtils.IMPORT_IMAGES);
    }

    public static void importVideoFile(DBTaskContext context){
        importFile(context, (file, chooser) -> DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, EnumSet.noneOf(FileLoaderFlags.class)), FileUtils.IMPORT_VIDEOS);
    }

    public static void importSVGFile(DBTaskContext context){
        if(!FXApplication.isPremiumEnabled){
            FXController.showPremiumFeatureDialog();
            return;
        }
        importFile(context, (file, chooser) -> DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, EnumSet.noneOf(FileLoaderFlags.class)), FileUtils.FILTER_SVG);
    }

    public static void importProject(DBTaskContext context){
        importFile(context, (file, chooser) -> {
            DrawingBotV3.INSTANCE.openFile(context, file, EnumSet.noneOf(FileLoaderFlags.class));
        }, new FileChooser.ExtensionFilter[]{FileUtils.FILTER_PROJECT}, "Open DBV3 Project");
    }

    public static void importFile(DBTaskContext context, BiConsumer<File, FileChooser> callback, FileChooser.ExtensionFilter filter){
        importFile(context, callback, new FileChooser.ExtensionFilter[]{filter}, "Select a file to import");
    }

    public static void importFile(DBTaskContext context, BiConsumer<File, FileChooser> callback, FileChooser.ExtensionFilter... filters){
        importFile(context, callback, filters, "Select a file to import");
    }

    public static void importFile(DBTaskContext context, BiConsumer<File, FileChooser> callback, FileChooser.ExtensionFilter[] filters, String title){
        importFile(context, (file, fileChooser) -> {
            context.project().updateImportDirectoryFromFile(file);
            callback.accept(file, fileChooser);
        }, context.project().getImportDirectory(), filters, title);
    }

    public static void importFile(DBTaskContext context, BiConsumer<File, FileChooser> callback, File initialDirectory, FileChooser.ExtensionFilter[] filters, String title){
        Platform.runLater(() -> {
            FileChooser fileChooser = createFileChooser(title, initialDirectory, filters);
            File file = fileChooser.showOpenDialog(FXApplication.primaryStage);
            if(file != null){
                context.project().updateImportDirectoryFromFile(file);
                callback.accept(file, fileChooser);
            }
        });
    }

    public static FileChooser createFileChooser(String title, File initialDirectory, FileChooser.ExtensionFilter ...filters){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(initialDirectory);
        fileChooser.getExtensionFilters().addAll(filters);
        fileChooser.setSelectedExtensionFilter(filters[0]);
        return fileChooser;
    }

    public static void exportFile(DBTaskContext context, BiConsumer<File, FileChooser> callback, FileChooser.ExtensionFilter[] filters, FileChooser.ExtensionFilter selectedFilter, String title, String initialFileName) {
        exportFile((file, fileChooser) -> {
            if(file == null){
                return;
            }
            context.project().updateExportDirectoryFromFile(file);
            callback.accept(file, fileChooser);
        }, context.project().getExportDirectory(), filters, selectedFilter, title, initialFileName);
    }

    public static void exportFile(BiConsumer<File, FileChooser> callback, File initialDirectory, FileChooser.ExtensionFilter[] filters, FileChooser.ExtensionFilter selectedFilter, String title, String initialFileName){
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(filters);
            fileChooser.setSelectedExtensionFilter(selectedFilter);
            fileChooser.setTitle(title);
            if(initialDirectory.exists()){
                fileChooser.setInitialDirectory(initialDirectory);
            }
            fileChooser.setInitialFileName(initialFileName);

            File file = fileChooser.showSaveDialog(FXApplication.primaryStage);
            if(file != null){
                //LINUX FIX
                String extension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0).substring(1);
                String fileName = file.toString();

                if(!FileUtils.hasExtension(fileName)){
                    //linux doesn't add file extensions so we add the default selected one
                    fileName += extension;
                }

                callback.accept(new File(fileName), fileChooser);
            }else{
                callback.accept(null, fileChooser);
            }
        });
    }

    public static String getSaveLocation(DBTaskContext context, String baseName, String suffix, FileChooser.ExtensionFilter[] filters){
        return getSaveLocation(context.project().getExportDirectory(), baseName, suffix, FileUtils.getRawExtensions(filters));
    }

    public static String getSaveLocation(File saveDirectory, String baseName, String suffix, List<String> extensions){
        String saveLocation = null;
        int iteration = 0;
        while(saveLocation == null){
            iteration++;
            boolean matches = false;
            String location = FileUtils.removeExtension(baseName) + suffix + iteration;
            for(String extension : extensions){
                if(new File(saveDirectory + File.separator + location + extension).exists()){
                    matches = true;
                    break;
                }
            }
            if(!matches){
                saveLocation = location;
            }
        }
        return saveLocation;
    }

    public static void exportFile(DBTaskContext context, DrawingExportHandler exportHandler, ExportTask.Mode exportMode){
        exportFile(context, exportHandler, exportMode, "_plotted_", "Untitled");
    }

    public static void exportFile(DBTaskContext context, DrawingExportHandler exportHandler, ExportTask.Mode exportMode, String suffix, String fallbackFileName){
        PlottedDrawing drawing = DrawingBotV3.taskManager().getCurrentDrawing();
        if(drawing == null){
            return;
        }
        File originalFile = drawing.getOriginalFile();
        if(DrawingBotV3.project().file.get() != null){
            originalFile = DrawingBotV3.project().file.get();
        }
        if(originalFile == null){
            originalFile = new File(FileUtils.getUserDataDirectory() + File.separator + fallbackFileName);
        }
        String saveLocation = getSaveLocation(context, originalFile.getName(), suffix, exportHandler.filters);

        if(exportHandler.requiresSaveLocation(exportMode)) {
            exportFile(context, (file, chooser) -> {
                exportHandler.selectedFilter = chooser.getSelectedExtensionFilter();
                DrawingBotV3.INSTANCE.createExportTask(exportHandler, exportMode, drawing, context.project().getExportGeometryFilter(), FileUtils.getExtension(file.toString()), file, false);
            }, exportHandler.filters, exportHandler.selectedFilter, exportHandler.getDialogTitle(), saveLocation);
        }else{
            //Only used by vpype export, which doesn't always require an Export Destination, we pass a usable one anyway to avoid throwing other things off
            DrawingBotV3.INSTANCE.createExportTask(exportHandler, exportMode, drawing, context.project().getExportGeometryFilter(), "", new File(context.project().getExportDirectory(), saveLocation), false);
        }
    }

    public static void exportLogFiles(){
        FXHelper.exportFile((file, fileChooser) -> {
            if(file != null){
                boolean exported = LoggingHandler.createReportZip(file.getPath());
                if(exported){
                    NotificationOverlays.INSTANCE.showWithSubtitle("Logs Exported: " + file.getName(), file.toString(), new Action("Open Folder", event -> FXHelper.openFolder(file.getParentFile())));
                }else{
                    NotificationOverlays.INSTANCE.showWithSubtitle("WARNING", "Log Export Failed", "ZIP the logs folder manually instead", new Action("Open Logs Folder", event -> FXHelper.openFolder(new File(FileUtils.getUserLogsDirectory()))));
                }
            }
        }, new File(FileUtils.getUserHomeDirectory()), new FileChooser.ExtensionFilter[]{FileUtils.FILTER_ZIP}, FileUtils.FILTER_ZIP, "Export Log Archive", "%s_logs_%s_%s.zip".formatted(SoftwareManager.getSoftware().getDisplayName().toLowerCase(), Utils.getOS().getShortName(), Utils.getDateAndTimeSafe()));
    }

    public static void importPreset(DBTaskContext context, PresetType presetType, boolean apply, boolean showDialog){
        importFile(context, (file, chooser) -> {
            GenericPreset<Object> preset = loadPresetFile(context, presetType, file, apply);
            if(showDialog){
                if(preset.presetLoader != null){
                    NotificationOverlays.INSTANCE.showWithSubtitle("Preset Imported: " + preset.getPresetName(), file.toString(), new Action("Apply Preset", event -> preset.applyPreset(context)));
                }else{
                    NotificationOverlays.INSTANCE.showWithSubtitle("Preset Imported: " + preset.getPresetName(), file.toString());
                }

            }
        }, presetType.filters, "Select a preset to import");
    }

    public static <D> GenericPreset<D> loadPresetFile(DBTaskContext context, PresetType presetType, File file, boolean apply){
        GenericPreset<D> preset = JsonLoaderManager.importPresetFile(file, presetType);
        context.project().updateImportDirectoryFromFile(file);
        if(preset != null && apply){
            IPresetLoader<D> jsonLoader =  JsonLoaderManager.getJsonLoaderForPresetType(preset);
            if(jsonLoader != null){
                preset.applyPreset(context);
            }else{
                DrawingBotV3.logger.severe("Preset type is missing JsonLoader: " + presetType.registryName);
            }
        }
        return preset;
    }

    public static <TARGET, DATA> GenericPreset<DATA> loadPresetFile(DBTaskContext context, IPresetManager<TARGET, DATA> manager, File file, boolean apply){
        GenericPreset<DATA> preset = JsonLoaderManager.importPresetFile(file, manager.getPresetType());

        context.project().updateImportDirectoryFromFile(file);

        if(preset != null && apply){
            preset.applyPreset(context);
        }
        return preset;
    }

    /////////////////////////////////////////////////////

    /////////////////////////////////////////////////////

    private static boolean forceClose = false;

    public static boolean hasUnsavedProjects(){
        return DrawingBotV3.INSTANCE.activeProjects.stream().anyMatch(p -> p.hasChanged.get());
    }

    public static void exit(){
        if(hasUnsavedProjects()){
            saveAndCloseAllProjects(true);
            return;
        }
        Platform.exit();
    }

    public static void onCloseRequest(WindowEvent event){
        if(forceClose){
            return;
        }
        if(hasUnsavedProjects()){
            saveAndCloseAllProjects(true);
            event.consume();
        }
    }

    public static void saveAndCloseAllProjects(boolean shouldQuit){

        //Copy the list so we can close the projects as we go
        List<ObservableProject> projectList = new ArrayList<>(DrawingBotV3.INSTANCE.activeProjects);

        //Place the current project first
        if(DrawingBotV3.INSTANCE.activeProject.get() != null){
            projectList.remove(DrawingBotV3.INSTANCE.activeProject.get());
            projectList.add(0, DrawingBotV3.INSTANCE.activeProject.get());
        }

        for(ObservableProject project : projectList){
            if(!project.hasChanged.get()){
                continue;
            }
            DrawingBotV3.INSTANCE.activeProject.set(project);
            closeProject(project, response -> {
                if(!response.shouldCancel()){
                    saveAndCloseAllProjects(shouldQuit);
                }
            });
            return;
        }

        if(shouldQuit){
            forceClose = true;
            Platform.exit();
        }
    }


    public static boolean closeProject(ObservableProject project, Consumer<DialogSaveOnClose.ExitResponse> callback){
        if(project == null){
            return false;
        }
        DialogSaveOnClose dialogSaveOnClose = new DialogSaveOnClose(project.name.get());
        dialogSaveOnClose.initOwner(FXApplication.getPrimaryStage());
        Optional<DialogSaveOnClose.ExitResponse> response = dialogSaveOnClose.showAndWait();
        if(response.isPresent()){
            switch (response.get()){
                case SAVE -> {
                    saveProject(project, p -> {
                        doCloseProject(p);
                        callback.accept(response.get());
                    });
                    return true;
                }
                case DONT_SAVE -> {
                    doCloseProject(project);
                    callback.accept(response.get());
                    return true;
                }
                case CANCEL_CLOSE -> {
                    callback.accept(response.get());
                    return false;
                }
            }
        }
        return false;
    }

    public static void doCloseProject(ObservableProject project){
        DrawingBotV3.INSTANCE.activeProjects.remove(project);

        if(DrawingBotV3.INSTANCE.activeProject.get() == project){
            if(DrawingBotV3.INSTANCE.activeProjects.isEmpty()){
                DrawingBotV3.INSTANCE.activeProject.set(new ObservableProject());
                DrawingBotV3.INSTANCE.activeProjects.add(DrawingBotV3.INSTANCE.activeProject.get());
            }else{
                DrawingBotV3.INSTANCE.activeProject.set(DrawingBotV3.INSTANCE.activeProjects.get(0));
            }
        }
    }

    public static void saveProject(){
        saveProject(DrawingBotV3.project(), p -> {});
    }

    public static void saveProject(ObservableProject project, Consumer<ObservableProject> callback){
        if(project.file.get() != null){
            GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
            Register.PRESET_MANAGER_PROJECT.updatePreset(project.context, project, preset, false);

            JsonLoaderManager.exportPresetFile(project.file.get(), preset);
            project.hasChanged.set(false);
            RecentProjectHandler.addRecentProject(project);
            callback.accept(project);
            NotificationOverlays.INSTANCE.showWithSubtitle("Project Saved: " + project.name.get(), project.file.get().toString(), new Action("Open Folder", event -> openFolder(project.file.get().getParentFile())));
        }else{
            saveProjectAs(project, callback);
        }
    }

    public static void saveProjectAs(){
        saveProjectAs(DrawingBotV3.project(), p -> {});
    }

    public static void saveProjectAs(ObservableProject project, Consumer<ObservableProject> callback){
        File folder = project.getExportDirectory();
        String projectName = project.name.get();

        PlottedDrawing renderedDrawing = project.getCurrentDrawing();
        if(renderedDrawing != null){
            File originalFile = renderedDrawing.getOriginalFile();
            if(originalFile != null){
                folder = originalFile.getParentFile();
                projectName = FileUtils.removeExtension(originalFile.getName());
            }
        }

        exportFile((file, chooser) -> {
            if(file == null){
                return;
            }
            project.file.set(file);
            project.name.set(FileUtils.removeExtension(file.getName()));
            DrawingBotV3.INSTANCE.backgroundService.submit(() -> {
                //context.project().updateExportDirectory(file.getParentFile()); //saving our project is not "Exporting"

                GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
                Register.PRESET_MANAGER_PROJECT.updatePreset(project.context, project, preset, false);

                JsonLoaderManager.exportPresetFile(file, preset);
                project.hasChanged.set(false);
                RecentProjectHandler.addRecentProject(project);
                callback.accept(project);
                NotificationOverlays.INSTANCE.showWithSubtitle("Project Saved: " + project.name.get(), project.file.get().toString(), new Action("Open Folder", event -> openFolder(project.file.get().getParentFile())));
            });
        }, folder, new FileChooser.ExtensionFilter[]{FileUtils.FILTER_PROJECT}, FileUtils.FILTER_PROJECT, "Save Project", projectName);
    }

    public static void exportPreset(DBTaskContext context, GenericPreset<?> preset, File initialDirectory, String initialName, boolean showDialog){
        exportFile(context, (file, chooser) -> {
            context.project().updateExportDirectoryFromFile(file);
            JsonLoaderManager.exportPresetFile(file, preset);

            if(showDialog){
                NotificationOverlays.INSTANCE.showWithSubtitle("Preset Exported: " + preset.getPresetName(), file.toString(), new Action("Open Folder", event -> openFolder(file.getParentFile())));
                /*
                DialogExportPreset exportPreset = new DialogExportPreset(preset, file);
                Optional<Boolean> openFolder = exportPreset.showAndWait();
                if(openFolder.isPresent() && openFolder.get()){
                    FXHelper.openFolder(file.getParentFile());
                }
                 */
            }
        }, preset.presetType.filters, preset.presetType.filters[0], "Save preset", initialName);
    }

    public static <D> GenericPreset<D> copyPreset(GenericPreset<D> preset){
        IPresetLoader<D> loader = JsonLoaderManager.getJsonLoaderForPresetType(preset);
        assert loader != null;

        Gson gson = JsonLoaderManager.createDefaultGson();
        JsonElement element = loader.toJsonElement(gson, preset);

        GenericPreset<D> copy = loader.createNewPreset(preset.getPresetSubType(), preset.getPresetName(), preset.userCreated);
        copy.data = loader.fromJsonElement(gson, copy, element);
        return copy;
    }

    public static void selectFolder(String title, File initialDirectory, Consumer<File> callback){
        Platform.runLater(() -> {
            DirectoryChooser d = new DirectoryChooser();
            d.setTitle(title);
            if(initialDirectory.exists()) {
                d.setInitialDirectory(initialDirectory);
            }
            File file = d.showDialog(FXApplication.primaryStage);
            if(file != null){
                callback.accept(file);
            }
        });
    }

    public static void selectFile(String title, File initialDirectory, Consumer<File> callback){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.setTitle(title);
            if(initialDirectory.exists()) {
                d.setInitialDirectory(initialDirectory);
            }
            File file = d.showOpenDialog(FXApplication.primaryStage);
            if(file != null){
                callback.accept(file);
            }
        });
    }

    public static void openURL(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            EventQueue.invokeLater(() -> {
                try {
                    Desktop.getDesktop().browse(URI.create(url));
                } catch (IOException e) {
                    DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error opening webpage: " + url);
                }
            });
        }else if(Utils.getOS().isMac()){
            try {
                Runtime.getRuntime().exec("open " + url);
            } catch (IOException e) {
                DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error opening webpage: " + url);
            }
        }
    }

    public static void openFolder(File directory){
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            EventQueue.invokeLater(() -> {
                try {
                    Desktop.getDesktop().open(directory);
                } catch (IOException e) {
                    DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error opening directory: " + directory);
                }
            });
        }else if(Utils.getOS().isMac()){
            try {
                Runtime.getRuntime().exec("open " + directory.getPath());
            } catch (IOException e) {
                DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error opening directory: " + directory);
            }
        }
    }

    public static void initSeparateStageWithController(String fmxlPath, Stage stage, Object controller, String stageTitle, Modality modality){
        try {
            FXMLLoader exportUILoader = new FXMLLoader(FXController.class.getResource(fmxlPath));
            exportUILoader.setClassLoader(FXController.class.getClassLoader());
            exportUILoader.setController(controller);
            initSeparateStageProps(exportUILoader.load(), stage, stageTitle, modality);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T initSeparateStage(String fmxlPath, Stage stage, String stageTitle, Modality modality){
        try {
            FXMLLoader exportUILoader = new FXMLLoader(FXController.class.getResource(fmxlPath));
            exportUILoader.setClassLoader(FXController.class.getClassLoader());
            Parent root = exportUILoader.load();
            T controller = exportUILoader.getController();
            exportUILoader.setController(controller);
            initSeparateStageProps(root, stage, stageTitle, modality);
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void initSeparateStageProps(Parent parent, Stage stage, String stageTitle, Modality modality){
        Scene scene = new Scene(parent);
        stage.initModality(modality);
        stage.initOwner(FXApplication.primaryStage);
        stage.setScene(scene);
        stage.hide();
        stage.setTitle(stageTitle);
        stage.setResizable(false);
        FXApplication.applyTheme(stage);
        FXApplication.childStages.add(stage);
    }

    public static <DATA> GenericPreset<DATA> createEditablePreset(IPresetManager<?, DATA> manager, GenericPreset<DATA> preset){
        if(preset == null){
            return null;
        }
        if(preset.isSystemPreset()){
            switch (DialogSystemPresetDuplicate.openSystemPresetDialog(preset)){
                case OVERRIDE -> {
                    return manager.getPresetLoader().createOverridePreset(preset);
                }
                case DUPLICATE -> {
                    return manager.getPresetLoader().createEditablePreset(preset);
                }
                case CANCEL -> {
                    return null;
                }
            }
        }
        return manager.getPresetLoader().createEditablePreset(preset);
    }

    public static <TARGET, DATA> GenericPreset<DATA> actionNewPreset(IPresetManager<TARGET, DATA> manager, @Nullable GenericPreset<DATA> newInstance, TARGET target, boolean isInspector){
        GenericPreset<DATA> newPreset = newInstance == null ? manager.getPresetLoader().createNewPreset() : newInstance;
        if(newPreset == null){
            return null;
        }
        manager.updatePreset(DrawingBotV3.context(), target, newPreset, false);
        return DialogPresetEdit.openPresetNewDialog(manager, newPreset, isInspector);
    }


    public static <TARGET, DATA>  GenericPreset<DATA> actionUpdatePreset(IPresetManager<TARGET, DATA> manager, GenericPreset<DATA> preset, TARGET target){
        GenericPreset<DATA> oldPreset = preset;
        preset = createEditablePreset(manager, preset);
        if(preset == null){
            return null;
        }

        if(target != null){
            manager.updatePreset(DrawingBotV3.context(), target, preset, false);
        }

        GenericPreset<DATA> resultPreset = manager.getPresetLoader().editPreset(oldPreset, preset);
        logPresetAction(resultPreset, "Updated");

        return resultPreset;
    }

    public static <TARGET, DATA>  GenericPreset<DATA> actionEditPreset(IPresetManager<TARGET, DATA> manager, GenericPreset<DATA> preset, TARGET target, boolean isInspector){
        return DialogPresetEdit.openPresetEditDialog(manager, preset, isInspector);
    }

    public static <DATA>  GenericPreset<DATA> actionDuplicatePreset(GenericPreset<DATA> preset){
        if(preset == null){
            return null;
        }
        IPresetLoader<DATA> loader = MasterRegistry.INSTANCE.getPresetLoader(preset.getPresetType());
        GenericPreset<DATA> duplicatePreset = new GenericPreset<>(preset);
        duplicatePreset.userCreated = true;
        duplicatePreset.setPresetName(duplicatePreset.getPresetName() + " - Copy");
        if(duplicatePreset.getPresetType().getSubTypeBehaviour().isIgnored()){
            duplicatePreset.setPresetSubType("User");
        }
        loader.addPreset(duplicatePreset);

        logPresetAction(preset, "Duplicated");
        return duplicatePreset;
    }


    public static boolean actionDeletePresets(List<GenericPreset<?>> presets){
        if(presets.isEmpty()){
            return false;
        }
        if(presets.size() == 1){
            GenericPreset<?> result = actionDeletePreset(presets.get(0));
            return result == null;
        }
        boolean result = DialogPresetMultiDelete.openSystemPresetMultiDeleteDialog(presets);
        if(result){
            long count = presets.stream().filter(GenericPreset::isUserPreset).count();
            presets.forEach(preset -> {
                if(preset.isSystemPreset()){
                    return;
                }
                IPresetLoader loader = MasterRegistry.INSTANCE.getPresetLoader(preset.getPresetType());
                loader.removePreset(preset);
            });
            NotificationOverlays.INSTANCE.show("Deleted %s Presets".formatted(count));
        }
        return true;
    }

    public static <DATA>  GenericPreset<DATA> actionDeletePreset(GenericPreset<DATA> preset){
        IPresetLoader<DATA> loader = MasterRegistry.INSTANCE.getPresetLoader(preset.getPresetType());
        return actionDeletePreset(loader, preset);
    }

    public static <DATA>  GenericPreset<DATA> actionDeletePreset(IPresetLoader<DATA> loader, GenericPreset<DATA> preset){
        if(preset == null){
            return preset;
        }
        if(preset.isSystemPreset()){
            //System presets can't be deleted, allow hiding instead TODO
            if(DialogSystemPresetDelete.openSystemPresetDeleteDialog(preset)){
                preset.setEnabled(false);
            }
            return preset;
        }

        loader.removePreset(preset);
        logPresetAction(preset, "Deleted");
        return null;
    }

    public static <DATA> GenericPreset<DATA> actionSetDefaultPreset(IPresetLoader<DATA> loader, GenericPreset<DATA> preset){
        if(loader.getPresetType().defaultsPerSubType){
            loader.setDefaultPresetSubType(preset);
        }else{
            loader.setDefaultPreset(preset);
        }
        logPresetAction(preset, "Set default");
        return preset;
    }

    public static void logPresetAction(GenericPreset<?> preset, String action){
        if(preset.presetType.getSubTypeBehaviour().isIgnored()){
            NotificationOverlays.INSTANCE.showWithSubtitle("%s '%s'".formatted(action, preset.presetType.getDisplayName()), "Name: %s".formatted(preset.getPresetName()));
        }else{
            NotificationOverlays.INSTANCE.showWithSubtitle("%s '%s'".formatted(action, preset.presetType.getDisplayName()), "Name: %s, Category: %s".formatted(preset.getPresetName(), preset.getPresetSubType()));
        }
    }

    public static <O> void addDefaultTableViewContextMenuItems(ContextMenu menu, TableRow<O> row, Supplier<ObservableList<O>> list, Function<O, O> duplicate){

        MenuItem menuMoveUp = new MenuItem("Move Up");
        menuMoveUp.setOnAction(e -> moveItemUp(row.getTableView().getSelectionModel(), list.get()));
        menu.getItems().add(menuMoveUp);

        MenuItem menuMoveDown = new MenuItem("Move Down");
        menuMoveDown.setOnAction(e -> moveItemDown(row.getTableView().getSelectionModel(), list.get()));
        menu.getItems().add(menuMoveDown);

        menu.getItems().add(new SeparatorMenuItem());

        MenuItem menuDelete = new MenuItem("Delete");
        menuDelete.setOnAction(e -> deleteItem(row.getTableView().getSelectionModel(), list.get()));
        menu.getItems().add(menuDelete);

        MenuItem menuDuplicate = new MenuItem("Duplicate");
        menuDuplicate.setOnAction(e -> duplicateItem(row.getTableView().getSelectionModel(), list.get(), duplicate));
        menu.getItems().add(menuDuplicate);
    }

    public static <O> void moveItemUp(TableView.TableViewSelectionModel<O> selectionModel, ObservableList<O> list){
        O item = selectionModel.getSelectedItem();
        if(item != null){
            int index = list.indexOf(item);
            if(index != 0){
                list.remove(index);
                list.add(index-1,item);
            }
            selectionModel.clearSelection();
            selectionModel.select(item);
        }
    }

    public static <O> void moveItemDown(TableView.TableViewSelectionModel<O> selectionModel, ObservableList<O> list){
        O item = selectionModel.getSelectedItem();
        if(item != null){

            int index = list.indexOf(item);
            if(index != list.size()-1){
                list.remove(index);
                list.add(index+1, item);
            }

            selectionModel.clearSelection();
            selectionModel.select(item);
        }
    }

    public static <O> void addItem(TableView.TableViewSelectionModel<O> selectionModel, ObservableList<O> list, Supplier<O> add){
        O item = add.get();
        if(item != null){
            list.add(item);
            selectionModel.clearSelection();
            selectionModel.select(item);
        }
    }

    public static <O> void deleteItem(TableView.TableViewSelectionModel<O> selectionModel, ObservableList<O> list){
        O item = selectionModel.getSelectedItem();
        if(item != null){
            list.remove(item);
            selectionModel.clearSelection();
        }
    }

    public static <O> void duplicateItem(TableView.TableViewSelectionModel<O> selectionModel, ObservableList<O> list, Function<O, O> duplicate){
        O item = selectionModel.getSelectedItem();
        if(item != null){
            O newItem = duplicate.apply(item);
            list.add(newItem);
            selectionModel.clearSelection();
            selectionModel.select(newItem);
        }
    }

    public static void addImageFilter(GenericFactory<BufferedImageOp> filterFactory, ImageFilterSettings settings){
        ObservableImageFilter filter = new ObservableImageFilter(filterFactory);
        settings.currentFilters.get().add(filter);
        if(!filter.filterSettings.isEmpty()){
            FXHelper.openImageFilterDialog(filter);
        }
    }

    public static void openImageFilterDialog(ObservableImageFilter filter){
        if(filter != null){

            //Copy the original filter so we can restore it if needed
            ObservableImageFilter original = new ObservableImageFilter(filter);

            DialogImageFilter dialog = new DialogImageFilter(filter);
            dialog.initOwner(FXApplication.primaryStage);
            Optional<Boolean> result = dialog.showAndWait();
            if(result.isPresent() && !result.get()){ //if the dialog was cancelled copy the settings of the original
                GenericSetting.applySettings(original.filterSettings, filter.filterSettings);
            }
        }
    }

    public static void openRenameDialog(Supplier<StringProperty> propertySupplier){
        DialogGenericRename dialog = new DialogGenericRename(() -> propertySupplier.get().get());
        dialog.initOwner(FXApplication.primaryStage);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(s -> propertySupplier.get().set(s));
    }

    /*
    public static GridPane createSettingsGridPane(Collection<GenericSetting<?, ?>> settings, Consumer<GenericSetting<?, ?>> onChanged){
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_LEFT);

        gridPane.setVgap(4);
        gridPane.setHgap(4);

        int i = 0;
        for(GenericSetting<?, ?> setting : settings){
            Label label = new Label(setting.getKey() + ": ");
            label.setAlignment(Pos.TOP_LEFT);
            Node node = setting.getJavaFXEditor(true);
            node.minWidth(200);
            node.prefHeight(30);

            if(!(setting instanceof AbstractNumberSetting)){
                //check boxes don't need a value label.
                gridPane.addRow(i, label, node);
            }else{
                TextField field = setting.getEditableTextField();
                Runnable update = () -> {
                    setting.setValueFromString(field.getText());
                    if(onChanged != null) {
                        onChanged.accept(setting);
                    }
                };
                field.setOnAction(e -> update.run());
                field.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    //set the value when the text field is de-focused
                    if(oldValue && !newValue) {
                        update.run();
                    }
                });

                if(node != field){
                    gridPane.addRow(i, label, node, field);
                }else{
                    gridPane.addRow(i, label, field);
                }
            }
            if(onChanged != null){
                node.setOnMouseReleased(e -> onChanged.accept(setting)); //change on mouse release, not on value change
            }
            i++;
        }
        return gridPane;
    }

     */


    public static void addText(TextFlow flow, String style, String text){
        Text textNode = new Text(text);
        textNode.setStyle(style);
        flow.getChildren().add(textNode);
    }

    public static void addText(TextFlow flow, int size, String weight, String text){
        Text textNode = new Text(text);
        textNode.setStyle("-fx-font-size: "+ size +"px; -fx-font-weight: " + weight);
        flow.getChildren().add(textNode);
    }

    public static List<PropertyAccessorAbstract> nodePropertyAccessors = new ArrayList<>();

    @JsonData
    public static class SplitPaneDataFormat { //must be public static for GSON

        public double[] positions;

        public SplitPaneDataFormat() { //for gson
            super();
        }

        public SplitPaneDataFormat(double[] positions) {
            this.positions = positions;
        }
    }

    static {

        nodePropertyAccessors.add(new PropertyAccessorProp<>(TitledPane.class, "expanded", Boolean.class, TitledPane::expandedProperty));
        nodePropertyAccessors.add(new PropertyAccessorProp<>(TableColumnBase.class, "visible", Boolean.class, TableColumnBase::visibleProperty));
        nodePropertyAccessors.add(new PropertyAccessorProp<>(Viewport.class, "scale", Number.class, Viewport::zoomProperty));
        nodePropertyAccessors.add(new PropertyAccessor<>(SplitPane.class, SplitPaneDataFormat.class, "dividers"){

            @Override
            public SplitPaneDataFormat getData(SplitPane splitPane) {
                return new SplitPaneDataFormat(splitPane.getDividerPositions());
            }

            @Override
            public void setData(SplitPane splitPane, @Nullable SplitPaneDataFormat dataFormat) {
                if(dataFormat != null){
                    splitPane.setDividerPositions(dataFormat.positions);
                }
            }
        });
        nodePropertyAccessors.add(new PropertyAccessorProp<>(Node.class, "position", FXController.NodePosition.class){

            public boolean canAccess(Object obj){
                return obj instanceof Node && FXController.hasPosition((Node) obj);
            }

            @Override
            public FXController.NodePosition getData(Node node) {
                return FXController.getPosition(node);
            }

            @Override
            public void setData(Node node, FXController.NodePosition position) {
                Platform.runLater(() -> FXController.loadPosition(node, position));
            }
        });
    }


    public static void makePersistent(List<? extends Styleable> styleables){
        styleables.forEach(FXHelper::makePersistent);
    }

    public static void makePersistent(Styleable styleable){
        if(styleable != null && !persistentStyleables.contains(styleable)){

            if(styleable.getId() == null || styleable.getId().isEmpty()){
                DrawingBotV3.logger.warning("Non-Persistent Styleable: " + styleable);
            }else{
                persistentStyleables.add(styleable);
            }
        }
    }

    @Nullable
    public static Styleable findPersistentStyleable(String fxID){
        for(Styleable node : persistentStyleables){
            if(node.getId().equals(fxID)){
                return node;
            }
        }
        //return FXApplication.primaryScene.getRoot().lookup(fxID);
        return null;
    }

    public static List<UINodeState> defaultStates = null;

    public static void saveDefaultUIStates(){
        if(defaultStates == null){
            saveUIStates(defaultStates = new ArrayList<>());
        }
    }

    public static void loadDefaultUIStates(){
        if(defaultStates != null){
            loadUIStates(defaultStates);
        }
    }

    public static void loadUIStates(List<UINodeState> states){
        for(UINodeState state : states){
            Styleable styleable = FXHelper.findPersistentStyleable(state.getID());
            if(styleable != null && styleable.getId() != null && !styleable.getId().isEmpty() && styleable.getId().equals(state.getID())){
                state.loadState(styleable);
            }
        }
    }

    public static void saveUIStates(List<UINodeState> states){
        states.clear();

        for(Styleable styleable : FXHelper.persistentStyleables){
            if(styleable.getId() != null && !styleable.getId().isEmpty()){
                states.add(new UINodeState(styleable.getId(), styleable));
            }
        }
    }

    public static <T> void refreshComboBox(ComboBox<T> comboBox){
        ObservableList<T> items = comboBox.getItems();
        T value = comboBox.getValue();
        comboBox.setItems(FXCollections.observableArrayList());
        comboBox.setItems(items);
        comboBox.setValue(value);
    }
}
