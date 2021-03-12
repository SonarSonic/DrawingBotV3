package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.files.ExportFormats;
import drawingbot.files.FileUtils;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.files.presets.IJsonData;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.image.blend.EnumBlendMode;
import drawingbot.image.filters.ObservableImageFilter;
import drawingbot.javafx.controls.DialogColourSeperationMode;
import drawingbot.plotting.PlottedPoint;
import drawingbot.registry.MasterRegistry;
import drawingbot.utils.EnumColourSplitter;
import drawingbot.utils.EnumDistributionOrder;
import drawingbot.utils.EnumJsonType;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A utility class for common actions for the FXController / JavaFX Classes
 */
public class FXHelper {

    public static void importFile(){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().add(FileUtils.IMPORT_IMAGES);
            d.setTitle("Select an image file to sketch");
            d.setInitialDirectory(FileUtils.getImportDirectory());
            File file = d.showOpenDialog(null);
            if(file != null){
                DrawingBotV3.INSTANCE.openImage(file, false);
                FileUtils.updateImportDirectory(file.getParentFile());
            }
        });
    }

    public static void exportFile(ExportFormats format, boolean seperatePens){
        if(DrawingBotV3.INSTANCE.getActiveTask() == null){
            return;
        }
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(format.filters);
            d.setTitle(format.getDialogTitle());
            d.setInitialDirectory(FileUtils.getExportDirectory());
            d.setInitialFileName(FileUtils.removeExtension(DrawingBotV3.INSTANCE.getActiveTask().originalFile.getName()) + "_plotted");
            File file = d.showSaveDialog(null);
            if(file != null){
                DrawingBotV3.INSTANCE.createExportTask(format, DrawingBotV3.INSTANCE.getActiveTask(), PlottedPoint.DEFAULT_FILTER, d.getSelectedExtensionFilter().getExtensions().get(0).substring(1), file, seperatePens);
                FileUtils.updateExportDirectory(file.getParentFile());
            }
        });
    }

    public static void importPreset(EnumJsonType presetType){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().add(FileUtils.FILTER_JSON);
            d.setTitle("Select a preset to import");
            d.setInitialDirectory(FileUtils.getImportDirectory());
            File file = d.showOpenDialog(null);
            if(file != null){
                JsonLoaderManager.importPresetFile(file, presetType);
                FileUtils.updateImportDirectory(file.getParentFile());
            }
        });
    }

    public static void exportPreset(GenericPreset<?> preset){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(FileUtils.FILTER_JSON);
            d.setTitle("Save preset");
            d.setInitialDirectory(FileUtils.getExportDirectory());
            d.setInitialFileName(preset.presetName + " - Preset");
            File file = d.showSaveDialog(null);
            if(file != null){
                JsonLoaderManager.exportPresetFile(file, preset);
                FileUtils.updateExportDirectory(file.getParentFile());
            }
        });
    }

    public static void openURL(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (IOException e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error opening webpage: " + url);
        }
    }

    public static void openFolder(File directory){
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(directory);
            }
        } catch (IOException e) {
            DrawingBotV3.logger.log(Level.WARNING, e, () -> "Error opening directory: " + directory);
        }
    }

    public String getClipboardString(){
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)){
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            //
        }
        return null;
    }

    public static <O extends IJsonData> void setupPresetMenuButton(AbstractPresetLoader<O> presetManager, MenuButton button, Supplier<GenericPreset<O>> getter, Consumer<GenericPreset<O>> setter){

        MenuItem newPreset = new MenuItem("New Preset");
        MenuItem updatePreset = new MenuItem("Update Preset");
        MenuItem renamePreset = new MenuItem("Rename Preset");
        MenuItem deletePreset = new MenuItem("Delete Preset");

        MenuItem importPreset = new MenuItem("Import Preset");
        MenuItem exportPreset = new MenuItem("Export Preset");

        newPreset.setOnAction(e -> {
            GenericPreset<O> editingPreset = presetManager.createNewPreset();
            if(editingPreset == null){
                return;
            }
            editingPreset = presetManager.tryUpdatePreset(editingPreset);
            if(editingPreset != null){
                DrawingBotV3.INSTANCE.controller.presetEditorDialog.setEditingPreset(editingPreset);
                DrawingBotV3.INSTANCE.controller.presetEditorDialog.setTitle("Save new preset");
                Optional<GenericPreset<?>> result = DrawingBotV3.INSTANCE.controller.presetEditorDialog.showAndWait();
                if(result.isPresent()){
                    presetManager.tryEditPreset(editingPreset);
                    presetManager.trySavePreset(editingPreset);
                    setter.accept(editingPreset);
                }
            }
        });

        updatePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            GenericPreset<O> preset = presetManager.tryUpdatePreset(current);
            if(preset != null){
                setter.accept(preset);
            }
        });

        renamePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null || !current.userCreated){
                return;
            }
            DrawingBotV3.INSTANCE.controller.presetEditorDialog.setEditingPreset(current);
            DrawingBotV3.INSTANCE.controller.presetEditorDialog.setTitle("Rename preset");
            Optional<GenericPreset<?>> result = DrawingBotV3.INSTANCE.controller.presetEditorDialog.showAndWait();
            if(result.isPresent()){
                presetManager.tryEditPreset(current);
                setter.accept(current);
            }
        });

        deletePreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            if(presetManager.tryDeletePreset(current)){
                setter.accept(presetManager.getDefaultPreset());
            }
        });

        importPreset.setOnAction(e -> {
            FXHelper.importPreset(presetManager.type);
        });
        exportPreset.setOnAction(e -> {
            GenericPreset<O> current = getter.get();
            if(current == null){
                return;
            }
            FXHelper.exportPreset(current);
        });

        button.getItems().addAll(newPreset, updatePreset, renamePreset, deletePreset, new SeparatorMenuItem(), importPreset, exportPreset);
    }

    public static <O> void addDefaultTableViewContextMenuItems(ContextMenu menu, TableRow<O> row, ObservableList<O> list, Consumer<O> duplicate){

        MenuItem menuMoveUp = new MenuItem("Move Up");
        menuMoveUp.setOnAction(e -> moveItemUp(row.getItem(), list));
        menu.getItems().add(menuMoveUp);

        MenuItem menuMoveDown = new MenuItem("Move Down");
        menuMoveDown.setOnAction(e -> moveItemDown(row.getItem(), list));
        menu.getItems().add(menuMoveDown);

        menu.getItems().add(new SeparatorMenuItem());

        MenuItem menuDelete = new MenuItem("Delete");
        menuDelete.setOnAction(e -> deleteItem(row.getItem(), list));
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

    public static void openColourSeperationDialog(EnumColourSplitter splitter){
        DialogColourSeperationMode dialog = new DialogColourSeperationMode(splitter);
        Optional<Boolean> result = dialog.showAndWait();
        if(result.isPresent() && result.get()){
            if(splitter != EnumColourSplitter.DEFAULT){
                DrawingBotV3.INSTANCE.observableDrawingSet.distributionOrder.set(EnumDistributionOrder.DISPLAYED);
                DrawingBotV3.INSTANCE.observableDrawingSet.blendMode.set(EnumBlendMode.DARKEN);
                DrawingBotV3.INSTANCE.observableDrawingSet.loadDrawingSet(splitter.drawingSet);
            }else{
                DrawingBotV3.INSTANCE.observableDrawingSet.distributionOrder.set(EnumDistributionOrder.DARKEST_FIRST);
                DrawingBotV3.INSTANCE.observableDrawingSet.blendMode.set(EnumBlendMode.NORMAL);
                DrawingBotV3.INSTANCE.observableDrawingSet.loadDrawingSet(MasterRegistry.INSTANCE.getDefaultSet(MasterRegistry.INSTANCE.getDefaultSetType()));
            }
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
                    DrawingBotV3.INSTANCE.onImageFiltersChanged();
                }
            }
        }
    }

}
