package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.*;
import drawingbot.files.json.*;
import drawingbot.files.json.presets.PresetProjectSettings;
import drawingbot.image.ImageFilterSettings;
import drawingbot.javafx.controls.DialogExportPreset;
import drawingbot.javafx.controls.DialogImportPreset;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.settings.AbstractNumberSetting;
import drawingbot.plotting.PlottedDrawing;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.Utils;
import javafx.application.Platform;
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

import java.awt.Desktop;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A utility class for common actions for the FXController / JavaFX Classes
 */
public class FXHelper {

    public static final ButtonType buttonResetToDefault = new ButtonType("Reset to default", ButtonBar.ButtonData.OTHER);

    public static void importImageFile(){
        importFile((file, chooser) -> DrawingBotV3.INSTANCE.openFile(file, false), FileUtils.IMPORT_IMAGES);
    }

    public static void importVideoFile(){
        importFile((file, chooser) -> DrawingBotV3.INSTANCE.openFile(file, false), FileUtils.IMPORT_VIDEOS);
    }

    public static void importFile(BiConsumer<File, FileChooser> callback, FileChooser.ExtensionFilter filter){
        importFile(callback, new FileChooser.ExtensionFilter[]{filter}, "Select a file to import");
    }

    public static void importFile(BiConsumer<File, FileChooser> callback, FileChooser.ExtensionFilter[] filters, String title){
        importFile((file, fileChooser) -> {
            FileUtils.updateImportDirectory(file.getParentFile());
            callback.accept(file, fileChooser);
        }, FileUtils.getExportDirectory(), filters, title);
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
        PlottedDrawing drawing = DrawingBotV3.INSTANCE.getCurrentDrawing();
        if(drawing == null){
            return;
        }
        String initialFileName = "";
        File originalFile = drawing.getOriginalFile();
        if(originalFile != null){
            initialFileName = FileUtils.removeExtension(originalFile.getName()) + "_plotted";
        }
        exportFile((file, chooser) -> {
            DrawingBotV3.INSTANCE.createExportTask(exportHandler, exportMode, drawing, IGeometryFilter.DEFAULT_EXPORT_FILTER, FileUtils.getExtension(file.toString()), file, false);
        }, exportHandler.filters, exportHandler.getDialogTitle(), initialFileName);
    }

    public static void importPreset(PresetType presetType, boolean apply, boolean showDialog){
        importFile((file, chooser) -> {
            GenericPreset<IJsonData> preset = loadPresetFile(presetType, file, apply);
            if(showDialog){
                DialogImportPreset importPreset = new DialogImportPreset(preset);
                importPreset.show();
            }
        }, presetType.filters, "Select a preset to import");
    }

    public static GenericPreset<IJsonData> loadPresetFile(PresetType presetType, File file, boolean apply){
        GenericPreset<IJsonData> preset = JsonLoaderManager.importPresetFile(file, presetType);
        FileUtils.updateImportDirectory(file.getParentFile());
        if(preset != null && apply){
            AbstractJsonLoader<IJsonData> jsonLoader =  JsonLoaderManager.getJsonLoaderForPresetType(presetType);
            if(jsonLoader != null){
                if(jsonLoader.getDefaultManager() != null){
                    jsonLoader.getDefaultManager().tryApplyPreset(preset);
                }
            }else{
                DrawingBotV3.logger.severe("Preset type is missing JsonLoader: " + presetType.id);
            }
        }
        return preset;
    }

    public static void exportProject(File initialDirectory, String initialName){
        exportFile((file, chooser) -> {
            DrawingBotV3.INSTANCE.backgroundService.submit(() -> {

                GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
                Register.PRESET_LOADER_PROJECT.getDefaultManager().updatePreset(preset);

                JsonLoaderManager.exportPresetFile(file, preset);
            });
        }, initialDirectory, new FileChooser.ExtensionFilter[]{FileUtils.FILTER_PROJECT}, "Save Project", initialName);
    }

    public static void exportPreset(GenericPreset<?> preset, File initialDirectory, String initialName, boolean showDialog){
        exportFile((file, chooser) -> {
            JsonLoaderManager.exportPresetFile(file, preset);

            if(showDialog){
                DialogExportPreset exportPreset = new DialogExportPreset(preset, file);
                Optional<Boolean> openFolder = exportPreset.showAndWait();
                if(openFolder.isPresent() && openFolder.get()){
                    FXHelper.openFolder(file.getParentFile());
                }
            }
        }, preset.presetType.filters, "Save preset", initialName);
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


    public static void initSeparateStage(String fmxlPath, Stage stage, Object controller, String stageTitle, Modality modality){
        try {
            FXMLLoader exportUILoader = new FXMLLoader(FXApplication.class.getResource(fmxlPath));
            exportUILoader.setController(controller);
            initSeparateStage(exportUILoader.load(), stage, stageTitle, modality);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initSeparateStage(Parent parent, Stage stage, String stageTitle, Modality modality){
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

    public static <O extends IJsonData> void setupPresetMenuButton(AbstractPresetLoader<O> loader, Supplier<AbstractPresetManager<O>> manager, MenuButton button, boolean editableCategory, Supplier<GenericPreset<O>> getter, Consumer<GenericPreset<O>> setter){

        MenuItem newPreset = new MenuItem("Save Preset");
        MenuItem updatePreset = new MenuItem("Update Preset");
        MenuItem renamePreset = new MenuItem("Rename Preset");
        MenuItem deletePreset = new MenuItem("Delete Preset");

        MenuItem importPreset = new MenuItem("Import Preset");
        MenuItem exportPreset = new MenuItem("Export Preset");
        MenuItem setDefault = new MenuItem("Set As Default");

        newPreset.setOnAction(e -> {
            GenericPreset<O> editingPreset = loader.createNewPreset();
            if(editingPreset == null){
                return;
            }
            editingPreset = manager.get().tryUpdatePreset(editingPreset);
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

        updatePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            String originalName = current.presetName;
            boolean isDefault = current == loader.getDefaultPreset();
            GenericPreset<O> preset = manager.get().tryUpdatePreset(current);
            if(preset != null){
                setter.accept(preset);

                if(isDefault && !originalName.equals(preset.presetName)){
                    MasterRegistry.INSTANCE.setDefaultPreset(preset);
                }
            }
        });

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

        deletePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            if(loader.tryDeletePreset(current)){
                setter.accept(loader.getDefaultPreset());
            }
        });

        importPreset.setOnAction(e -> {
            FXHelper.importPreset(loader.type, false, true);
        });
        exportPreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            FXHelper.exportPreset(current, FileUtils.getExportDirectory(), current.presetName, true);
        });

        setDefault.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current != null){
                MasterRegistry.INSTANCE.setDefaultPreset(current);
            }
        });

        button.getItems().addAll(newPreset, updatePreset, renamePreset, deletePreset, new SeparatorMenuItem(), setDefault, new SeparatorMenuItem(), importPreset, exportPreset);
    }

    public static <O> void addDefaultTableViewContextMenuItems(ContextMenu menu, TableRow<O> row, Supplier<ObservableList<O>> list, Consumer<O> duplicate){

        MenuItem menuMoveUp = new MenuItem("Move Up");
        menuMoveUp.setOnAction(e -> moveItemUp(row.getItem(), list.get()));
        menu.getItems().add(menuMoveUp);

        MenuItem menuMoveDown = new MenuItem("Move Down");
        menuMoveDown.setOnAction(e -> moveItemDown(row.getItem(), list.get()));
        menu.getItems().add(menuMoveDown);

        menu.getItems().add(new SeparatorMenuItem());

        MenuItem menuDelete = new MenuItem("Delete");
        menuDelete.setOnAction(e -> deleteItem(row.getItem(), list.get()));
        menu.getItems().add(menuDelete);

        MenuItem menuDuplicate = new MenuItem("Duplicate");
        menuDuplicate.setOnAction(e -> duplicate.accept(row.getItem()));
        menu.getItems().add(menuDuplicate);
    }

    public static <O> void moveItemUp(O item, ObservableList<O> list){
        if(item == null) return;
        int index = list.indexOf(item);
        if(index != 0){
            list.remove(index);
            list.add(index-1,item);
        }
    }

    public static <O> void moveItemDown(O item, ObservableList<O> list){
        if(item == null) return;
        int index = list.indexOf(item);
        if(index != list.size()-1){
            list.remove(index);
            list.add(index+1, item);
        }
    }

    public static <O> void deleteItem(O item, ObservableList<O> list){
        if(item == null) return;
        list.remove(item);
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

    public static GridPane createSettingsGridPane(Collection<GenericSetting<?, ?>> settings, Consumer<GenericSetting<?, ?>> onChanged){
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.TOP_LEFT);

        gridPane.setVgap(4);
        gridPane.setHgap(4);

        int i = 0;
        for(GenericSetting<?, ?> setting : settings){
            Label label = new Label(setting.key.getValue() + ": ");
            label.setAlignment(Pos.TOP_LEFT);
            Node node = setting.getJavaFXNode(true);
            node.minWidth(200);
            node.prefHeight(30);

            if(!(setting instanceof AbstractNumberSetting)){
                //check boxes don't need a value label.
                gridPane.addRow(i, label, node);
            }else{
                TextField field = setting.getEditableTextField();
                gridPane.addRow(i, label, node, setting.getEditableTextField());
                field.setOnAction(e -> {
                    setting.setValueFromString(field.getText());
                    if(onChanged != null) {
                        onChanged.accept(setting);
                    }
                });

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

}
