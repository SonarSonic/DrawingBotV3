package drawingbot.javafx;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.*;
import drawingbot.files.json.*;
import drawingbot.files.json.projects.DBTaskContext;
import drawingbot.files.json.projects.PresetProjectSettings;
import drawingbot.image.ImageFilterSettings;
import drawingbot.javafx.controls.DialogGenericRename;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.settings.AbstractNumberSetting;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.render.overlays.NotificationOverlays;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.jetbrains.annotations.Nullable;

import java.awt.Desktop;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A utility class for common actions for the FXController / JavaFX Classes
 */
public class FXHelper {

    public static final List<Node> persistentNodes = new ArrayList<>();

    public static final ButtonType buttonResetToDefault = new ButtonType("Reset to default", ButtonBar.ButtonData.OTHER);

    public static void importImageFile(){
        importFile((file, chooser) -> DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, false, true), FileUtils.IMPORT_IMAGES);
    }

    public static void importVideoFile(){
        importFile((file, chooser) -> DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, false, true), FileUtils.IMPORT_VIDEOS);
    }

    public static void importSVGFile(){
        if(!FXApplication.isPremiumEnabled){
            FXController.showPremiumFeatureDialog();
            return;
        }
        importFile((file, chooser) -> DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, false, true), FileUtils.FILTER_SVG);
    }

    public static void importProject(){
        importFile((file, chooser) -> {
            DrawingBotV3.INSTANCE.openFile(DrawingBotV3.context(), file, false, true);
        }, FileUtils.FILTER_PROJECT);
    }

    public static void importFile(BiConsumer<File, FileChooser> callback, FileChooser.ExtensionFilter filter){
        importFile(callback, new FileChooser.ExtensionFilter[]{filter}, "Select a file to import");
    }

    public static void importFile(BiConsumer<File, FileChooser> callback, FileChooser.ExtensionFilter[] filters, String title){
        importFile((file, fileChooser) -> {
            FileUtils.updateImportDirectory(file.getParentFile());
            callback.accept(file, fileChooser);
        }, FileUtils.getImportDirectory(), filters, title);
    }

    public static void importFile(BiConsumer<File, FileChooser> callback, File initialDirectory, FileChooser.ExtensionFilter[] filters, String title){
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(filters);
            fileChooser.setSelectedExtensionFilter(filters[0]);
            fileChooser.setTitle(title);
            fileChooser.setInitialDirectory(initialDirectory);
            File file = fileChooser.showOpenDialog(null);
            if(file != null){
                FileUtils.updateImportDirectory(file.getParentFile());
                callback.accept(file, fileChooser);
            }
        });
    }

    public static void exportFile(BiConsumer<File, FileChooser> callback, FileChooser.ExtensionFilter[] filters, String title, String initialFileName) {
        exportFile((file, fileChooser) -> {
            FileUtils.updateExportDirectory(file.getParentFile());
            callback.accept(file, fileChooser);
        }, FileUtils.getExportDirectory(), filters, title, initialFileName);
    }

    public static void exportFile(BiConsumer<File, FileChooser> callback, File initialDirectory, FileChooser.ExtensionFilter[] filters, String title, String initialFileName){
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(filters);
            fileChooser.setSelectedExtensionFilter(filters[0]);
            fileChooser.setTitle(title);
            fileChooser.setInitialDirectory(initialDirectory);
            fileChooser.setInitialFileName(initialFileName);
            File file = fileChooser.showSaveDialog(null);
            if(file != null){
                //LINUX FIX
                String extension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0).substring(1);
                String fileName = file.toString();

                if(!FileUtils.hasExtension(fileName)){
                    //linux doesn't add file extensions so we add the default selected one
                    fileName += extension;
                }

                callback.accept(new File(fileName), fileChooser);
            }
        });
    }

    public static void exportFile(DrawingExportHandler exportHandler, ExportTask.Mode exportMode){
        PlottedDrawing drawing = DrawingBotV3.taskManager().getCurrentDrawing();
        if(drawing == null){
            return;
        }
        File originalFile = drawing.getOriginalFile();
        if(DrawingBotV3.project().file.get() != null){
            originalFile = DrawingBotV3.project().file.get();
        }
        if(originalFile == null){
            originalFile = new File(FileUtils.getUserDataDirectory() + File.separator + "Untitled");
        }

        String saveLocation = null;
        int iteration = 1;
        while(saveLocation == null || new File(FileUtils.getExportDirectory() + File.separator + saveLocation + exportHandler.getDefaultExtension()).exists()){
            saveLocation = FileUtils.removeExtension(originalFile.getName()) + "_plotted_" + iteration++;
        }
        exportFile((file, chooser) -> {
            DrawingBotV3.INSTANCE.createExportTask(exportHandler, exportMode, drawing, IGeometryFilter.DEFAULT_EXPORT_FILTER, FileUtils.getExtension(file.toString()), file, false);
        }, exportHandler.filters, exportHandler.getDialogTitle(), saveLocation);
    }

    public static void importPreset(DBTaskContext context, PresetType presetType, boolean apply, boolean showDialog){
        importFile((file, chooser) -> {
            GenericPreset<IJsonData> preset = loadPresetFile(context, presetType, file, apply);
            if(showDialog){
                AbstractJsonLoader<IJsonData> loader = JsonLoaderManager.getJsonLoaderForPresetType(preset);
                AbstractPresetManager<IJsonData> manager = loader == null ? null : loader.getDefaultManager();

                if(manager != null){
                    NotificationOverlays.INSTANCE.showWithSubtitle("Preset Imported: " + preset.presetName, file.toString(), new Action("Apply Preset", event -> manager.applyPreset(context, preset)));
                }else{
                    NotificationOverlays.INSTANCE.showWithSubtitle("Preset Imported: " + preset.presetName, file.toString());
                }

            }
        }, presetType.filters, "Select a preset to import");
    }

    public static GenericPreset<IJsonData> loadPresetFile(DBTaskContext context, PresetType presetType, File file, boolean apply){
        GenericPreset<IJsonData> preset = JsonLoaderManager.importPresetFile(file, presetType);
        FileUtils.updateImportDirectory(file.getParentFile());
        if(preset != null && apply){
            AbstractJsonLoader<IJsonData> jsonLoader =  JsonLoaderManager.getJsonLoaderForPresetType(preset);
            if(jsonLoader != null){
                if(jsonLoader.getDefaultManager() != null){
                    jsonLoader.getDefaultManager().tryApplyPreset(context, preset);
                }
            }else{
                DrawingBotV3.logger.severe("Preset type is missing JsonLoader: " + presetType.id);
            }
        }
        return preset;
    }

    public static <D extends IJsonData> GenericPreset<D> loadPresetFile(DBTaskContext context, AbstractJsonLoader<D> loader, File file, boolean apply){
        GenericPreset<D> preset = (GenericPreset<D>) JsonLoaderManager.importPresetFile(file, loader.type);
        FileUtils.updateImportDirectory(file.getParentFile());
        if(preset != null && apply){
            if(loader.getDefaultManager() != null){
                loader.getDefaultManager().tryApplyPreset(context, preset);
            }
        }
        return preset;
    }

    public static void saveProject(){
        final DBTaskContext context = DrawingBotV3.context();
        if(context.project().file.get() != null){
            GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
            Register.PRESET_LOADER_PROJECT.getDefaultManager().updatePreset(context, preset);

            JsonLoaderManager.exportPresetFile(context.project.file.get(), preset);
            NotificationOverlays.INSTANCE.showWithSubtitle("Project Saved: " + context.project.name.get(), context.project.file.get().toString(), new Action("Open Folder", event -> openFolder(context.project.file.get().getParentFile())));
        }else{
            saveProjectAs();
        }
    }

    public static void saveProjectAs(){
        final DBTaskContext context = DrawingBotV3.context();
        File folder = FileUtils.getExportDirectory();
        String projectName = context.project().name.get();

        PlottedDrawing renderedDrawing = DrawingBotV3.project().getCurrentDrawing();
        if(renderedDrawing != null){
            File originalFile = renderedDrawing.getOriginalFile();
            if(originalFile != null){
                folder = originalFile.getParentFile();
                projectName = FileUtils.removeExtension(originalFile.getName());
            }
        }

        exportFile((file, chooser) -> {
            context.project.file.set(file);
            context.project.name.set(FileUtils.removeExtension(file.getName()));
            DrawingBotV3.INSTANCE.backgroundService.submit(() -> {
                FileUtils.updateExportDirectory(file.getParentFile());

                GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
                Register.PRESET_LOADER_PROJECT.getDefaultManager().updatePreset(context, preset);

                JsonLoaderManager.exportPresetFile(file, preset);
                NotificationOverlays.INSTANCE.showWithSubtitle("Project Saved: " + context.project.name.get(), context.project.file.get().toString(), new Action("Open Folder", event -> openFolder(context.project.file.get().getParentFile())));
            });
        }, folder, new FileChooser.ExtensionFilter[]{FileUtils.FILTER_PROJECT}, "Save Project", projectName);
    }

    public static void exportPreset(GenericPreset<?> preset, File initialDirectory, String initialName, boolean showDialog){
        exportFile((file, chooser) -> {
            FileUtils.updateExportDirectory(file.getParentFile());
            JsonLoaderManager.exportPresetFile(file, preset);

            if(showDialog){
                NotificationOverlays.INSTANCE.showWithSubtitle("Preset Exported: " + preset.presetName, file.toString(), new Action("Open Folder", event -> openFolder(file.getParentFile())));
                /*
                DialogExportPreset exportPreset = new DialogExportPreset(preset, file);
                Optional<Boolean> openFolder = exportPreset.showAndWait();
                if(openFolder.isPresent() && openFolder.get()){
                    FXHelper.openFolder(file.getParentFile());
                }
                 */
            }
        }, preset.presetType.filters, "Save preset", initialName);
    }

    public static <D extends IJsonData> GenericPreset<D> copyPreset(GenericPreset<D> preset){
        AbstractJsonLoader<IJsonData> loader = JsonLoaderManager.getJsonLoaderForPresetType(preset);
        assert loader != null;

        Gson gson = JsonLoaderManager.createDefaultGson();
        JsonElement element = loader.toJsonElement(gson, preset);

        GenericPreset<IJsonData> copy = loader.createNewPreset(preset.presetSubType, preset.presetName, preset.userCreated);
        copy.data = loader.fromJsonElement(gson, copy, element);
        return (GenericPreset<D>) copy;
    }

    public static void openURL(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }else if(Utils.getOS().isMac()){
                Runtime.getRuntime().exec("open " + url);
            }
        } catch (IOException e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error opening webpage: " + url);
        }
    }

    public static void openFolder(File directory){
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(directory);
            }else if(Utils.getOS().isMac()){
                Runtime.getRuntime().exec("open " + directory.getPath());
            }
        } catch (IOException e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error opening directory: " + directory);
        }
    }

    public static void initSeparateStageWithController(String fmxlPath, Stage stage, Object controller, String stageTitle, Modality modality){
        try {
            FXMLLoader exportUILoader = new FXMLLoader(FXController.class.getResource(fmxlPath));
            exportUILoader.setController(controller);
            initSeparateStageProps(exportUILoader.load(), stage, stageTitle, modality);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T initSeparateStage(String fmxlPath, Stage stage, String stageTitle, Modality modality){
        try {
            FXMLLoader exportUILoader = new FXMLLoader(FXController.class.getResource(fmxlPath));
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
        FXApplication.applyDBStyle(stage);
        FXApplication.childStages.add(stage);
    }

    @Deprecated
    public static <O extends IJsonData> void setupPresetMenuButton(MenuButton button, AbstractPresetLoader<O> loader, Supplier<AbstractPresetManager<O>> manager, boolean editableCategory, Supplier<GenericPreset<O>> getter, Consumer<GenericPreset<O>> setter){

        MenuItem newPreset = new MenuItem("Save Preset");
        newPreset.setOnAction(e -> {
            GenericPreset<O> editingPreset = loader.createNewPreset();
            if(editingPreset == null){
                return;
            }
            editingPreset = manager.get().tryUpdatePreset(DrawingBotV3.context(), editingPreset);
            if(editingPreset != null){
                DrawingBotV3.INSTANCE.controller.presetEditorDialog.setEditingPreset(editingPreset, editableCategory);
                DrawingBotV3.INSTANCE.controller.presetEditorDialog.setTitle("Save new preset");
                Optional<GenericPreset<?>> result = DrawingBotV3.INSTANCE.controller.presetEditorDialog.showAndWait();
                if(result.isPresent()){
                    loader.tryEditPreset(editingPreset);
                    loader.trySavePreset(editingPreset);
                    setter.accept(editingPreset);
                }
            }
        });

        MenuItem updatePreset = new MenuItem("Update Preset");
        updatePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            String originalName = current.presetName;
            boolean isDefault = current == loader.getDefaultPreset();
            GenericPreset<O> preset = manager.get().tryUpdatePreset(DrawingBotV3.context(), current);
            if(preset != null){
                setter.accept(preset);

                if(isDefault && !originalName.equals(preset.presetName)){
                    MasterRegistry.INSTANCE.setDefaultPreset(preset);
                }
            }
        });

        MenuItem renamePreset = new MenuItem("Rename Preset");
        renamePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null || !current.userCreated){
                return;
            }
            String originalName = current.presetName;
            boolean isDefault = current == loader.getDefaultPreset();

            DrawingBotV3.INSTANCE.controller.presetEditorDialog.setEditingPreset(current, editableCategory);
            DrawingBotV3.INSTANCE.controller.presetEditorDialog.setTitle("Rename preset");
            Optional<GenericPreset<?>> result = DrawingBotV3.INSTANCE.controller.presetEditorDialog.showAndWait();
            if(result.isPresent()){
                loader.tryEditPreset(current);
                setter.accept(current);

                if(isDefault && !originalName.equals(current.presetName)){
                    MasterRegistry.INSTANCE.setDefaultPreset(current);
                }
            }
        });

        MenuItem deletePreset = new MenuItem("Delete Preset");
        deletePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            if(loader.tryDeletePreset(current)){
                setter.accept(loader.getDefaultPreset());
            }
        });

        MenuItem importPreset = new MenuItem("Import Preset");
        importPreset.setOnAction(e -> {
            FXHelper.importPreset(DrawingBotV3.context(), loader.type, false, true);
        });

        MenuItem exportPreset = new MenuItem("Export Preset");
        exportPreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            FXHelper.exportPreset(current, FileUtils.getExportDirectory(), current.presetName, true);
        });

        MenuItem setDefault = new MenuItem("Set As Default");
        setDefault.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current != null){
                MasterRegistry.INSTANCE.setDefaultPreset(current);
            }
        });

        button.getItems().addAll(newPreset, updatePreset, renamePreset, deletePreset, new SeparatorMenuItem(), setDefault, new SeparatorMenuItem(), importPreset, exportPreset);
    }

    public static <O extends IJsonData> MenuButton createPresetMenuButton(AbstractPresetLoader<O> loader, Supplier<AbstractPresetManager<O>> manager, boolean editableCategory, Property<GenericPreset<O>> property){
        MenuButton button = new MenuButton("Presets");
        setupPresetMenuButton(button, loader, manager, editableCategory, property);
        return button;
    }

    public static <O extends IJsonData> void setupPresetMenuButton(MenuButton button, AbstractPresetLoader<O> loader, Supplier<AbstractPresetManager<O>> manager, boolean editableCategory, Property<GenericPreset<O>> property){

        MenuItem newPreset = new MenuItem("Save Preset");
        newPreset.setOnAction(e -> {
            GenericPreset<O> editingPreset = loader.createNewPreset();
            if(editingPreset == null){
                return;
            }
            editingPreset = manager.get().tryUpdatePreset(DrawingBotV3.context(), editingPreset);
            if(editingPreset != null){
                DrawingBotV3.INSTANCE.controller.presetEditorDialog.setEditingPreset(editingPreset, editableCategory);
                DrawingBotV3.INSTANCE.controller.presetEditorDialog.setTitle("Save new preset");
                Optional<GenericPreset<?>> result = DrawingBotV3.INSTANCE.controller.presetEditorDialog.showAndWait();
                if(result.isPresent()){
                    loader.tryEditPreset(editingPreset);
                    loader.trySavePreset(editingPreset);
                    property.setValue(editingPreset);
                }
            }
        });

        MenuItem updatePreset = new MenuItem("Update Preset");
        updatePreset.setOnAction(e -> {
            GenericPreset<O> current = property.getValue();
            if(current == null){
                return;
            }
            String originalName = current.presetName;
            boolean isDefault = current == loader.getDefaultPreset();
            GenericPreset<O> preset = manager.get().tryUpdatePreset(DrawingBotV3.context(), current);
            if(preset != null){
                property.setValue(preset);

                if(isDefault && !originalName.equals(preset.presetName)){
                    MasterRegistry.INSTANCE.setDefaultPreset(preset);
                }
            }
        });

        MenuItem renamePreset = new MenuItem("Rename Preset");
        renamePreset.setOnAction(e -> {
            GenericPreset<O> current = property.getValue();
            if(current == null || !current.userCreated){
                return;
            }
            String originalName = current.presetName;
            boolean isDefault = current == loader.getDefaultPreset();

            DrawingBotV3.INSTANCE.controller.presetEditorDialog.setEditingPreset(current, editableCategory);
            DrawingBotV3.INSTANCE.controller.presetEditorDialog.setTitle("Rename preset");
            Optional<GenericPreset<?>> result = DrawingBotV3.INSTANCE.controller.presetEditorDialog.showAndWait();
            if(result.isPresent()){
                loader.tryEditPreset(current);
                property.setValue(current);

                if(isDefault && !originalName.equals(current.presetName)){
                    MasterRegistry.INSTANCE.setDefaultPreset(current);
                }
            }
        });

        MenuItem deletePreset = new MenuItem("Delete Preset");
        deletePreset.setOnAction(e -> {
            GenericPreset<O> current = property.getValue();
            if(current == null){
                return;
            }
            if(loader.tryDeletePreset(current)){
                property.setValue(loader.getDefaultPreset());
            }
        });

        MenuItem importPreset = new MenuItem("Import Preset");
        importPreset.setOnAction(e -> {
            FXHelper.importPreset(DrawingBotV3.context(), loader.type, false, true);
        });

        MenuItem exportPreset = new MenuItem("Export Preset");
        exportPreset.setOnAction(e -> {
            GenericPreset<O> current = property.getValue();
            if(current == null){
                return;
            }
            FXHelper.exportPreset(current, FileUtils.getExportDirectory(), current.presetName, true);
        });

        MenuItem setDefault = new MenuItem("Set As Default");
        setDefault.setOnAction(e -> {
            GenericPreset<O> current = property.getValue();
            if(current == null){
                return;
            }
            MasterRegistry.INSTANCE.setDefaultPreset(current);
        });

        button.getItems().addAll(newPreset, updatePreset, renamePreset, deletePreset, new SeparatorMenuItem(), setDefault, new SeparatorMenuItem(), importPreset, exportPreset);
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
            Dialog<ObservableImageFilter> dialog = MasterRegistry.INSTANCE.getDialogForFilter(filter);
            Optional<ObservableImageFilter> result = dialog.showAndWait();
            //TODO MAKE DIALOG APPEAR ON THE CORRECT DISPLAY
            if(result.isPresent()){
                if(result.get() != filter){ //if the dialog was cancelled copy the settings of the original
                    GenericSetting.applySettings(result.get().filterSettings, filter.filterSettings);
                    DrawingBotV3.INSTANCE.onImageFilterChanged(filter);
                }
            }
        }
    }

    public static void openRenameDialog(Supplier<StringProperty> propertySupplier){
        DialogGenericRename dialog = new DialogGenericRename(() -> propertySupplier.get().get());
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(s -> propertySupplier.get().set(s));
    }

    public static GridPane createSettingsGridPane(Collection<GenericSetting<?, ?>> settings, Consumer<GenericSetting<?, ?>> onChanged){
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_LEFT);

        gridPane.setVgap(4);
        gridPane.setHgap(4);

        int i = 0;
        for(GenericSetting<?, ?> setting : settings){
            Label label = new Label(setting.getKey() + ": ");
            label.setAlignment(Pos.TOP_LEFT);
            Node node = setting.getJavaFXNode(true);
            node.minWidth(200);
            node.prefHeight(30);

            if(!(setting instanceof AbstractNumberSetting)){
                //check boxes don't need a value label.
                gridPane.addRow(i, label, node);
            }else{
                TextField field = setting.getEditableTextField();
                field.setOnAction(e -> {
                    setting.setValueFromString(field.getText());
                    if(onChanged != null) {
                        onChanged.accept(setting);
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




    public static class UINodeState {

        public String id = "";
        public boolean expanded = false;

        public UINodeState(){} //for gson

        public UINodeState(Node node){
            saveState(node);
        }

        public void saveState(Node node){
            id = node.getId();
            if(node instanceof TitledPane){
                TitledPane pane = (TitledPane) node;
                expanded = pane.isExpanded();
            }
        }

        public void loadState(Node node){
            if(!node.getId().equals(id)){
                return;
            }
            if(node instanceof TitledPane){
                TitledPane pane = (TitledPane) node;
                pane.setExpanded(expanded);
            }
        }

        public String getID(){
            return id;
        }
    }

    public static void makePersistent(List<? extends Node> nodes){
        nodes.forEach(FXHelper::makePersistent);
    }

    public static void makePersistent(Node node){
        if(!persistentNodes.contains(node)){
            persistentNodes.add(node);
        }
    }

    @Nullable
    public static Node findPersistentNode(String fxID){
        for(Node node : persistentNodes){
            if(node.getId().equals(fxID)){
                return node;
            }
        }
        return FXApplication.primaryScene.getRoot().lookup(fxID);
    }

    public static void loadUIStates(List<FXHelper.UINodeState> nodes){
        for(FXHelper.UINodeState state : nodes){
            Node node = FXHelper.findPersistentNode(state.getID());
            if(node != null){
                state.loadState(node);
            }
        }
    }

    public static void saveUIStates(List<FXHelper.UINodeState> nodes){
        for(Node node : FXHelper.persistentNodes){
            if(node.getId() != null && !node.getId().isEmpty()){
                nodes.add(new FXHelper.UINodeState(node));
            }
        }
    }

}
