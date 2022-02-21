package drawingbot.javafx;

import drawingbot.DrawingBotV3;
import drawingbot.FXApplication;
import drawingbot.api.IGeometryFilter;
import drawingbot.files.DrawingExportHandler;
import drawingbot.files.FileUtils;
import drawingbot.files.presets.AbstractPresetLoader;
import drawingbot.files.presets.IJsonData;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.files.presets.PresetType;
import drawingbot.files.presets.types.PresetProjectSettings;
import drawingbot.javafx.controls.DialogExportPreset;
import drawingbot.javafx.controls.DialogImportPreset;
import drawingbot.javafx.observables.ObservableImageFilter;
import drawingbot.javafx.settings.AbstractNumberSetting;
import drawingbot.registry.MasterRegistry;
import drawingbot.registry.Register;
import drawingbot.utils.Utils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * A utility class for common actions for the FXController / JavaFX Classes
 */
public class FXHelper {

    public static final ButtonType buttonResetToDefault = new ButtonType("Reset to default", ButtonBar.ButtonData.OTHER);

    public static void importImageFile(){
        importFile(file -> DrawingBotV3.INSTANCE.openFile(file, false), FileUtils.IMPORT_IMAGES);
    }

    public static void importVideoFile(){
        importFile(file -> DrawingBotV3.INSTANCE.openFile(file, false), FileUtils.IMPORT_VIDEOS);
    }

    public static void importFile(Consumer<File> callback, FileChooser.ExtensionFilter filter){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().add(filter);
            d.setTitle("Select an image file to import");
            d.setInitialDirectory(FileUtils.getImportDirectory());
            File file = d.showOpenDialog(null);
            if(file != null){
                FileUtils.updateImportDirectory(file.getParentFile());
                callback.accept(file);
            }
        });
    }

    public static void exportFile(DrawingExportHandler exportHandler, boolean seperatePens){
        if(DrawingBotV3.INSTANCE.getActiveTask() == null){
            return;
        }
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(exportHandler.filters);
            d.setSelectedExtensionFilter(exportHandler.filters[0]);
            d.setTitle(exportHandler.getDialogTitle());
            d.setInitialDirectory(FileUtils.getExportDirectory());
            d.setInitialFileName(FileUtils.removeExtension(DrawingBotV3.INSTANCE.getActiveTask().originalFile.getName()) + "_plotted");
            File file = d.showSaveDialog(null);
            if(file != null){
                String extension = d.getSelectedExtensionFilter().getExtensions().get(0).substring(1);
                String fileName = file.toString();

                if(!FileUtils.hasExtension(fileName)){
                    //linux doesn't add file extensions so we add the default selected one
                    fileName += extension;
                }
                DrawingBotV3.INSTANCE.createExportTask(exportHandler, DrawingBotV3.INSTANCE.getActiveTask(), IGeometryFilter.DEFAULT_EXPORT_FILTER, extension, new File(fileName), seperatePens, false);
                FileUtils.updateExportDirectory(file.getParentFile());
            }
        });
    }

    public static void importPreset(PresetType presetType, boolean apply, boolean showDialog){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(presetType.filters);
            d.setTitle("Select a preset to import");
            d.setInitialDirectory(FileUtils.getImportDirectory());
            File file = d.showOpenDialog(null);
            if(file != null){
                GenericPreset<IJsonData> preset = loadPresetFile(presetType, file, apply);

                if(showDialog){
                    DialogImportPreset importPreset = new DialogImportPreset(preset);
                    importPreset.show();
                }
            }
        });
    }

    public static GenericPreset<IJsonData> loadPresetFile(PresetType presetType, File file, boolean apply){
        GenericPreset<IJsonData> preset = JsonLoaderManager.importPresetFile(file, presetType);
        FileUtils.updateImportDirectory(file.getParentFile());
        if(preset != null && apply){
            JsonLoaderManager.getJsonLoaderForPresetType(presetType).tryApplyPreset(preset);
        }
        return preset;
    }

    public static void exportProject(File initialDirectory, String initialName){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(FileUtils.FILTER_PROJECT);
            d.setTitle("Save project");
            d.setInitialDirectory(initialDirectory);
            d.setInitialFileName(initialName);
            File file = d.showSaveDialog(null);
            if(file != null){
                FileUtils.updateExportDirectory(file.getParentFile());
                DrawingBotV3.INSTANCE.backgroundService.submit(() -> {

                    GenericPreset<PresetProjectSettings> preset = Register.PRESET_LOADER_PROJECT.createNewPreset();
                    Register.PRESET_LOADER_PROJECT.updatePreset(preset);

                    JsonLoaderManager.exportPresetFile(file, preset);
                });
            }
        });
    }

    public static void exportPreset(GenericPreset<?> preset, File initialDirectory, String initialName, boolean showDialog){
        Platform.runLater(() -> {
            FileChooser d = new FileChooser();
            d.getExtensionFilters().addAll(preset.presetType.filters);
            d.setTitle("Save preset");
            d.setInitialDirectory(initialDirectory);
            d.setInitialFileName(initialName);
            File file = d.showSaveDialog(null);
            if(file != null){
                JsonLoaderManager.exportPresetFile(file, preset);
                FileUtils.updateExportDirectory(file.getParentFile());

                if(showDialog){
                    DialogExportPreset exportPreset = new DialogExportPreset(preset, file);
                    Optional<Boolean> openFolder = exportPreset.showAndWait();
                    if(openFolder.isPresent() && openFolder.get()){
                        FXHelper.openFolder(file.getParentFile());
                    }
                }
            }
        });
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

            Scene scene = new Scene(exportUILoader.load());
            stage.initModality(modality);
            stage.setScene(scene);
            stage.hide();
            stage.setTitle(stageTitle);
            stage.setResizable(false);
            FXApplication.applyDBIcon(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <O extends IJsonData> void setupPresetMenuButton(AbstractPresetLoader<O> presetManager, MenuButton button, boolean editableCategory, Supplier<GenericPreset<O>> getter, Consumer<GenericPreset<O>> setter){

        MenuItem newPreset = new MenuItem("Save Preset");
        MenuItem updatePreset = new MenuItem("Update Preset");
        MenuItem renamePreset = new MenuItem("Rename Preset");
        MenuItem deletePreset = new MenuItem("Delete Preset");

        MenuItem importPreset = new MenuItem("Import Preset");
        MenuItem exportPreset = new MenuItem("Export Preset");
        MenuItem setDefault = new MenuItem("Set As Default");

        newPreset.setOnAction(e -> {
            GenericPreset<O> editingPreset = presetManager.createNewPreset();
            if(editingPreset == null){
                return;
            }
            editingPreset = presetManager.tryUpdatePreset(editingPreset);
            if(editingPreset != null){
                DrawingBotV3.INSTANCE.controller.presetEditorDialog.setEditingPreset(editingPreset, editableCategory);
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
            String originalName = current.presetName;
            boolean isDefault = current == presetManager.getDefaultPreset();
            GenericPreset<O> preset = presetManager.tryUpdatePreset(current);
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
            boolean isDefault = current == presetManager.getDefaultPreset();

            DrawingBotV3.INSTANCE.controller.presetEditorDialog.setEditingPreset(current, editableCategory);
            DrawingBotV3.INSTANCE.controller.presetEditorDialog.setTitle("Rename preset");
            Optional<GenericPreset<?>> result = DrawingBotV3.INSTANCE.controller.presetEditorDialog.showAndWait();
            if(result.isPresent()){
                presetManager.tryEditPreset(current);
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
            if(presetManager.tryDeletePreset(current)){
                setter.accept(presetManager.getDefaultPreset());
            }
        });

        importPreset.setOnAction(e -> {
            FXHelper.importPreset(presetManager.type, false, true);
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


    public static void addImageFilter(GenericFactory<BufferedImageOp> filterFactory){
        ObservableImageFilter filter = new ObservableImageFilter(filterFactory);
        DrawingBotV3.INSTANCE.currentFilters.add(filter);
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
