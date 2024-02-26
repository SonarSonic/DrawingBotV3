package drawingbot.javafx.controls;

import drawingbot.DrawingBotV3;
import drawingbot.image.ImageFilterSettings;
import drawingbot.javafx.FXMLControl;
import drawingbot.files.json.PresetData;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.javafx.controllers.FXDocumentation;
import drawingbot.javafx.editors.EditorContext;
import drawingbot.javafx.editors.EditorFactoryCache;
import drawingbot.javafx.editors.EditorStyle;
import drawingbot.javafx.settings.CategorySetting;
import drawingbot.pfm.PFMSettings;
import drawingbot.registry.Register;
import drawingbot.utils.ISpecialListenable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.VBox;

import java.io.IOException;
/**
 * UI Control for the editing of {@link PFMSettings}
 * It can be instanced multiple times and bound to the required {@link PFMSettings}
 */
@FXMLControl
public class ControlPFMSettingsEditor extends VBox implements ISpecialListenable<ControlPFMSettingsEditor.Listener> {

    public TreeTableView<GenericSetting<?, ?>> treeTableViewPFMSettings = null;
    public TreeTableColumn<GenericSetting<?, ?>, Boolean> treeTableColumnLock = null;
    public TreeTableColumn<GenericSetting<?, ?>, String> treeTableColumnSetting = null;
    public TreeTableColumn<GenericSetting<?, ?>, Object> treeTableColumnValue = null;
    public TreeTableColumn<GenericSetting<?, ?>, Object> treeTableColumnControl = null;

    public Button buttonPFMSettingReset = null;
    public Button buttonPFMSettingRandom = null;
    public Button buttonPFMSettingHelp = null;

    public EditorContext editorContext = new EditorContext(this, EditorStyle.SIMPLE);
    public EditorFactoryCache editorFactoryCache = new EditorFactoryCache(editorContext);

    public ControlPFMSettingsEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("pfmsettingseditor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(c -> this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        parentProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null){
                editorFactoryCache.disposeCache();
            }
        });
    }



    ////////////////////////////////////////////////////////

    public final ObjectProperty<PFMSettings> pfmSettings = new SimpleObjectProperty<>();

    public PFMSettings getPFMSettings() {
        return pfmSettings.get();
    }

    public ObjectProperty<PFMSettings> pfmSettingsProperty() {
        return pfmSettings;
    }

    public void setPfmSettings(PFMSettings pfmSettings) {
        this.pfmSettings.set(pfmSettings);
    }

    ////////////////////////////////////////////////////////

    public final ObjectProperty<GenericPreset<PresetData>> activePreset = new SimpleObjectProperty<>();

    public GenericPreset<PresetData> getActivePreset() {
        return activePreset.get();
    }

    public ObjectProperty<GenericPreset<PresetData>> activePresetProperty() {
        return activePreset;
    }

    public void setActivePreset(GenericPreset<PresetData> activePreset) {
        this.activePreset.set(activePreset);
    }

    ////////////////////////////////////////////////////////

    @FXML
    public void initialize(){

        //VBox
        setSpacing(8);

        pfmSettings.addListener((observable, oldValue, newValue) -> {
            editorFactoryCache.disposeCache();
            if(oldValue != null){
                treeTableViewPFMSettings.rootProperty().unbind();
            }

            if(newValue != null){
                treeTableViewPFMSettings.rootProperty().bind(newValue.treeRoot);
            }
        });

        treeTableViewPFMSettings.setRowFactory(param -> {
            TreeTableRow<GenericSetting<?, ?>> row = new TreeTableRow<>();
            row.setContextMenu(new ContextMenuGenericSetting(row, pfmSettings.get().settings, true));
            row.treeItemProperty().addListener((observable, oldValue, newValue) -> {
                row.disableProperty().unbind();
                if(newValue != null){
                    row.disableProperty().bind(newValue.getValue().disabledProperty());
                }
            });
            row.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue && row.getItem() != null){
                    sendListenerEvent(listener -> listener.onPFMSettingHighlighted(row.getItem()));
                }
            });
            return row;
        });
        treeTableViewPFMSettings.setShowRoot(false);

        treeTableColumnLock.setCellFactory(param ->
                new CheckBoxTreeTableCell<>(index -> treeTableColumnLock.getCellObservableValue(index))
        );
        treeTableColumnLock.setCellValueFactory(param -> {
            GenericSetting<?, ? > setting = param.getValue().getValue();
            return setting instanceof CategorySetting ? null : setting.randomiseExcludeProperty();
        });

        treeTableColumnSetting.setCellValueFactory(param -> param.getValue().getValue().displayNameProperty());

        treeTableColumnValue.setCellFactory(param -> {
            TextFieldTreeTableCell<GenericSetting<?, ?>, Object> cell = new TextFieldTreeTableCell<>();

            cell.setConverter(new StringConverterGenericSetting(() -> {
                if(cell.getTableRow() != null && cell.getTableRow().getTreeItem() != null){
                    GenericSetting<?, ? > setting = cell.getTableRow().getTreeItem().getValue();
                    return setting instanceof CategorySetting ? null : setting;
                }
                return null;
            }));

            return cell;
        });
        treeTableColumnValue.setCellValueFactory(param -> {
            GenericSetting<?, ? > setting = param.getValue().getValue();
            return setting instanceof CategorySetting ? null : (ObservableValue<Object>) setting.valueProperty();
        });

        treeTableColumnControl.setCellFactory(param -> new TreeTableCellSettingControl<>(editorFactoryCache));
        treeTableColumnControl.setCellValueFactory(param -> {
            GenericSetting<?, ? > setting = param.getValue().getValue();
            return setting instanceof CategorySetting ? null : (ObservableValue<Object>) setting.valueProperty();
        });

        /*
        tableViewAdvancedPFMSettings.setRowFactory(param -> {
            TableRow<GenericSetting<?, ?>> row = new TableRow<>();
            row.setContextMenu(new ContextMenuPFMSetting(row));
            return row;
        });

        tableColumnLock.setCellFactory(param -> new CheckBoxTableCell<>(index -> tableColumnLock.getCellObservableValue(index)));
        tableColumnLock.setCellValueFactory(param -> param.getValue().randomiseExclude);

        tableColumnSetting.setCellValueFactory(param -> param.getValue().displayNameProperty());

        tableColumnValue.setCellFactory(param -> {
            TextFieldTableCell<GenericSetting<?, ?>, Object> cell = new TextFieldTableCell<>();
            cell.setConverter(new StringConverterGenericSetting(() -> cell.tableViewProperty().get().getItems().get(cell.getIndex())));
            return cell;
        });
        tableColumnValue.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

        tableColumnControl.setCellFactory(param -> new TableCellSettingControl());
        tableColumnControl.setCellValueFactory(param -> (ObservableValue<Object>)param.getValue().value);

         */
        buttonPFMSettingReset.setOnAction(e -> resetSettings());
        buttonPFMSettingRandom.setOnAction(e -> randomiseSettings());
        buttonPFMSettingHelp.setOnAction(e -> help());
    }

    public void randomiseSettings(){
        GenericSetting.randomiseSettings(pfmSettings.get().settings.get());
        sendListenerEvent(Listener::onPFMSettingsUserEdited);
    }

    public void resetSettings(){
        if(getActivePreset() == null){
            GenericSetting.resetSettings(pfmSettings.get().settings.get());
        }else{
            Register.PRESET_MANAGER_PFM.applyPreset(DrawingBotV3.context(), pfmSettings.get(), getActivePreset(), false);
        }
        sendListenerEvent(Listener::onPFMSettingsUserEdited);
    }

    public void help(){
        FXDocumentation.openPFMHelp(pfmSettings.get().getPFMFactory());
    }

    /////////////////////////////////

    private ObservableList<ControlPFMSettingsEditor.Listener> listeners = null;

    public ObservableList<ControlPFMSettingsEditor.Listener> listeners(){
        if(listeners == null){
            listeners = FXCollections.observableArrayList();
        }
        return listeners;
    }

    public interface Listener{

        default void onPFMSettingHighlighted(GenericSetting<?, ?> setting){}

        default void onPFMSettingsUserEdited(){}

    }

}
